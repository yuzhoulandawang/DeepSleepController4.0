package com.example.deepsleep.root

import android.content.Context
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.PowerManager
import android.telephony.TelephonyManager
import com.example.deepsleep.data.LogRepository
import com.example.deepsleep.data.WhitelistRepository
import com.example.deepsleep.model.AppSettings
import com.example.deepsleep.model.LogLevel
import com.example.deepsleep.model.WhitelistType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 场景检查器 - 检查各种可能阻止深度睡眠的场景
 */
class SceneChecker(
    private val context: Context,
    private val logRepository: LogRepository,
    private val whitelistRepository: WhitelistRepository
) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    // 网络白名单缓存
    private var networkWhitelist: List<String> = emptyList()
    private var lastWhitelistUpdate = 0L
    private val whitelistCacheDuration = 30_000L // 30秒缓存

    /**
     * 更新网络白名单缓存
     */
    private suspend fun updateNetworkWhitelist() {
        val now = System.currentTimeMillis()
        if (now - lastWhitelistUpdate < whitelistCacheDuration) return

        try {
            networkWhitelist = whitelistRepository.loadItems(
                context,
                WhitelistType.NETWORK
            ).map { it.name }
            lastWhitelistUpdate = now
            logRepository.appendLog(LogLevel.DEBUG, "SceneChecker", "网络白名单已更新，共 ${networkWhitelist.size} 个应用")
        } catch (e: Exception) {
            logRepository.appendLog(LogLevel.ERROR, "SceneChecker", "更新网络白名单失败: ${e.message}")
        }
    }

    /**
     * 检查包名是否在网络白名单中
     */
    private fun isInNetworkWhitelist(packageName: String): Boolean {
        return networkWhitelist.contains(packageName)
    }

    /**
     * 检查所有启用的场景，返回是否应该阻止深度睡眠
     */
    suspend fun shouldBlockDeepSleep(settings: AppSettings): Boolean = withContext(Dispatchers.IO) {
        // 更新网络白名单缓存
        updateNetworkWhitelist()

        val blockedScenes = mutableListOf<String>()

        // 检查流量活跃（排除网络白名单中的应用）
        if (settings.checkNetworkTraffic && isNetworkTrafficActive()) {
            blockedScenes.add("流量活跃")
            logRepository.appendLog(LogLevel.DEBUG, "SceneChecker", "检测到流量活跃")
        }

        // 检查音频播放
        if (settings.checkAudioPlayback && isAudioPlaying()) {
            blockedScenes.add("音频播放")
            logRepository.appendLog(LogLevel.DEBUG, "SceneChecker", "检测到音频播放")
        }

        // 检查导航应用
        if (settings.checkNavigation && isNavigationActive()) {
            blockedScenes.add("导航应用")
            logRepository.appendLog(LogLevel.DEBUG, "SceneChecker", "检测到导航应用")
        }

        // 检查通话状态
        if (settings.checkPhoneCall && isInPhoneCall()) {
            blockedScenes.add("通话状态")
            logRepository.appendLog(LogLevel.DEBUG, "SceneChecker", "检测到通话状态")
        }

        // 检查 NFC/P2P
        if (settings.checkNfcP2p && isNfcP2pActive()) {
            blockedScenes.add("NFC/P2P")
            logRepository.appendLog(LogLevel.DEBUG, "SceneChecker", "检测到 NFC/P2P 传输")
        }

        // 检查 WiFi 热点
        if (settings.checkWifiHotspot && isWifiHotspotEnabled()) {
            blockedScenes.add("WiFi热点")
            logRepository.appendLog(LogLevel.DEBUG, "SceneChecker", "检测到 WiFi 热点")
        }

        // 检查 USB 网络共享
        if (settings.checkUsbTethering && isUsbTetheringEnabled()) {
            blockedScenes.add("USB网络共享")
            logRepository.appendLog(LogLevel.DEBUG, "SceneChecker", "检测到 USB 网络共享")
        }

        // 检查投屏
        if (settings.checkScreenCasting && isScreenCasting()) {
            blockedScenes.add("投屏")
            logRepository.appendLog(LogLevel.DEBUG, "SceneChecker", "检测到投屏")
        }

        // 检查充电状态
        if (settings.checkCharging && isCharging()) {
            blockedScenes.add("充电状态")
            logRepository.appendLog(LogLevel.DEBUG, "SceneChecker", "检测到充电状态")
        }

        if (blockedScenes.isNotEmpty()) {
            logRepository.appendLog(LogLevel.INFO, "SceneChecker", "深度睡眠被阻止: ${blockedScenes.joinToString(", ")}")
        }

        blockedScenes.isNotEmpty()
    }

    /**
     * 检查是否有活跃的流量（下行）
     */
    private fun isNetworkTrafficActive(): Boolean {
        return try {
            val activeNetwork = connectivityManager.activeNetworkInfo ?: return false
            if (!activeNetwork.isConnected) return false

            // 获取所有运行中的应用
            val runningAppsResult = RootCommander.exec("dumpsys activity processes | grep -E 'Record=\\{' | grep -v 'STOPPED'")
            val runningPackages = runningAppsResult.lines()
                .mapNotNull { line ->
                    val match = Regex("([^/]+)/(\\w+)").find(line)
                    match?.groupValues?.getOrNull(1)
                }
                .distinct()

            // 检查网络流量
            val rxBytes = activeNetwork.rxBytes
            val txBytes = activeNetwork.txBytes
            val totalBytes = rxBytes + txBytes

            // 如果有流量但所有活跃应用都在白名单中，则不阻止深度睡眠
            if (totalBytes > 1000 && runningPackages.isNotEmpty()) {
                val nonWhitelistedApps = runningPackages.filter { !isInNetworkWhitelist(it) }
                if (nonWhitelistedApps.isEmpty()) {
                    logRepository.appendLog(LogLevel.DEBUG, "SceneChecker", "检测到流量，但所有活跃应用都在网络白名单中")
                    return false
                }
            }

            // 如果流量超过阈值（1000字节），认为有活跃流量
            totalBytes > 1000
        } catch (e: Exception) {
            logRepository.appendLog(LogLevel.ERROR, "SceneChecker", "检查流量活跃失败: ${e.message}")
            false
        }
    }

    /**
     * 检查是否有音频正在播放
     */
    private fun isAudioPlaying(): Boolean {
        return try {
            // 检查音乐流
            val isMusicActive = audioManager.isMusicActive

            // 检查是否有音频焦点
            val hasAudioFocus = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) > 0

            // 检查音量大小
            val musicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

            isMusicActive && hasAudioFocus && musicVolume > 0
        } catch (e: Exception) {
            logRepository.appendLog(LogLevel.ERROR, "SceneChecker", "检查音频播放失败: ${e.message}")
            false
        }
    }

    /**
     * 检查是否有导航应用在运行
     */
    private fun isNavigationActive(): Boolean {
        return try {
            val result = RootCommander.exec("dumpsys activity top | grep -E '(google|amap|mapnav|com.autonavi)' | grep -v 'STOPPED'")
            val isActive = result.isNotEmpty()
            isActive
        } catch (e: Exception) {
            logRepository.appendLog(LogLevel.ERROR, "SceneChecker", "检查导航应用失败: ${e.message}")
            false
        }
    }

    /**
     * 检查是否处于通话中
     */
    private fun isInPhoneCall(): Boolean {
        return try {
            val callState = telephonyManager.callState
            callState == TelephonyManager.CALL_STATE_OFFHOOK || callState == TelephonyManager.CALL_STATE_RINGING
        } catch (e: Exception) {
            logRepository.appendLog(LogLevel.ERROR, "SceneChecker", "检查通话状态失败: ${e.message}")
            false
        }
    }

    /**
     * 检查 NFC P2P 是否处于活跃状态
     */
    private fun isNfcP2pActive(): Boolean {
        return try {
            val result = RootCommander.exec("dumpsys nfc | grep 'P2P' | grep -v 'disabled' | grep -v 'false'")
            result.contains("P2P") && result.contains("true")
        } catch (e: Exception) {
            logRepository.appendLog(LogLevel.ERROR, "SceneChecker", "检查 NFC P2P 失败: ${e.message}")
            false
        }
    }

    /**
     * 检查 WiFi 热点是否开启
     */
    private fun isWifiHotspotEnabled(): Boolean {
        return try {
            val result = RootCommander.exec("dumpsys connectivity | grep -i 'tethering'")
            result.contains("tethering") || wifiManager.isWifiApEnabled
        } catch (e: Exception) {
            logRepository.appendLog(LogLevel.ERROR, "SceneChecker", "检查 WiFi 热点失败: ${e.message}")
            false
        }
    }

    /**
     * 检查 USB 网络共享是否开启
     */
    private fun isUsbTetheringEnabled(): Boolean {
        return try {
            val result = RootCommander.exec("dumpsys connectivity | grep -i 'usb' | grep -i 'rndis'")
            result.contains("rndis") || result.contains("tethering")
        } catch (e: Exception) {
            logRepository.appendLog(LogLevel.ERROR, "SceneChecker", "检查 USB 网络共享失败: ${e.message}")
            false
        }
    }

    /**
     * 检查是否正在投屏
     */
    private fun isScreenCasting(): Boolean {
        return try {
            // 检查是否有投屏相关的进程
            val result1 = RootCommander.exec("dumpsys media.audio_flinger | grep -i 'hdmi'")
            val result2 = RootCommander.exec("dumpsys display | grep -i 'cast'")
            val result3 = RootCommander.exec("dumpsys connectivity | grep -i 'miracast'")

            result1.contains("hdmi") || result2.contains("cast") || result3.contains("miracast")
        } catch (e: Exception) {
            logRepository.appendLog(LogLevel.ERROR, "SceneChecker", "检查投屏失败: ${e.message}")
            false
        }
    }

    /**
     * 检查是否正在充电
     */
    private fun isCharging(): Boolean {
        return try {
            val chargingStatus = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
            chargingStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
            chargingStatus == BatteryManager.BATTERY_STATUS_FULL
        } catch (e: Exception) {
            logRepository.appendLog(LogLevel.ERROR, "SceneChecker", "检查充电状态失败: ${e.message}")
            false
        }
    }

    /**
     * 获取当前所有阻止深度睡眠的场景
     */
    suspend fun getBlockingScenes(settings: AppSettings): List<String> = withContext(Dispatchers.IO) {
        // 更新网络白名单缓存
        updateNetworkWhitelist()

        val scenes = mutableListOf<String>()

        // 检查流量活跃（排除网络白名单中的应用）
        if (settings.checkNetworkTraffic && isNetworkTrafficActive()) scenes.add("流量活跃")
        if (settings.checkAudioPlayback && isAudioPlaying()) scenes.add("音频播放")
        if (settings.checkNavigation && isNavigationActive()) scenes.add("导航应用")
        if (settings.checkPhoneCall && isInPhoneCall()) scenes.add("通话状态")
        if (settings.checkNfcP2p && isNfcP2pActive()) scenes.add("NFC/P2P")
        if (settings.checkWifiHotspot && isWifiHotspotEnabled()) scenes.add("WiFi热点")
        if (settings.checkUsbTethering && isUsbTetheringEnabled()) scenes.add("USB网络共享")
        if (settings.checkScreenCasting && isScreenCasting()) scenes.add("投屏")
        if (settings.checkCharging && isCharging()) scenes.add("充电状态")

        scenes
    }
}
