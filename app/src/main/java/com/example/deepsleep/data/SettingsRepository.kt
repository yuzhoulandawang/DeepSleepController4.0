package com.example.deepsleep.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.example.deepsleep.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 设置仓库 - 单例对象
 * 注意：所有键必须与 AppSettings 中的字段一一对应
 */
object SettingsRepository {
    private lateinit var dataStore: DataStore<Preferences>

    // ========== 根权限状态 ==========
    private val ROOT_GRANTED = stringPreferencesKey("root_granted")
    private val SERVICE_RUNNING = booleanPreferencesKey("service_running")

    // ========== 深度睡眠控制 ==========
    private val DEEP_SLEEP_ENABLED = booleanPreferencesKey("deep_sleep_enabled")
    private val WAKEUP_SUPPRESS_ENABLED = booleanPreferencesKey("wakeup_suppress_enabled")
    private val ALARM_SUPPRESS_ENABLED = booleanPreferencesKey("alarm_suppress_enabled")

    // ========== 深度 Doze 配置 ==========
    private val DEEP_DOZE_ENABLED = booleanPreferencesKey("deep_doze_enabled")
    private val DEEP_DOZE_DELAY_SECONDS = intPreferencesKey("deep_doze_delay_seconds")
    private val DEEP_DOZE_FORCE_MODE = booleanPreferencesKey("deep_doze_force_mode")

    // ========== 深度睡眠 Hook 配置 ==========
    private val DEEP_SLEEP_HOOK_ENABLED = booleanPreferencesKey("deep_sleep_hook_enabled")
    private val DEEP_SLEEP_DELAY_SECONDS = intPreferencesKey("deep_sleep_delay_seconds")
    private val DEEP_SLEEP_BLOCK_EXIT = booleanPreferencesKey("deep_sleep_block_exit")
    private val DEEP_SLEEP_CHECK_INTERVAL = intPreferencesKey("deep_sleep_check_interval")

    // ========== 后台优化 ==========
    private val BACKGROUND_OPTIMIZATION_ENABLED = booleanPreferencesKey("background_optimization_enabled")
    private val APP_SUSPEND_ENABLED = booleanPreferencesKey("app_suspend_enabled")
    private val BACKGROUND_RESTRICT_ENABLED = booleanPreferencesKey("background_restrict_enabled")

    // ========== GPU 优化 ==========
    private val GPU_OPTIMIZATION_ENABLED = booleanPreferencesKey("gpu_optimization_enabled")
    private val GPU_MODE = stringPreferencesKey("gpu_mode")
    private val GPU_THROTTLING_ENABLED = booleanPreferencesKey("gpu_throttling_enabled")
    private val GPU_BUS_SPLIT_ENABLED = booleanPreferencesKey("gpu_bus_split_enabled")
    private val GPU_IDLE_TIMER = intPreferencesKey("gpu_idle_timer")
    private val GPU_MAX_FREQ = longPreferencesKey("gpu_max_freq")
    private val GPU_MIN_FREQ = longPreferencesKey("gpu_min_freq")
    private val GPU_THERMAL_PWR_LEVEL = intPreferencesKey("gpu_thermal_pwr_level")
    private val GPU_TRIP_POINT_TEMP = intPreferencesKey("gpu_trip_point_temp")
    private val GPU_TRIP_POINT_HYST = intPreferencesKey("gpu_trip_point_hyst")

    // ========== 电池优化 ==========
    private val BATTERY_OPTIMIZATION_ENABLED = booleanPreferencesKey("battery_optimization_enabled")
    private val POWER_SAVING_ENABLED = booleanPreferencesKey("power_saving_enabled")

    // ========== 系统省电模式联动 ==========
    private val ENABLE_POWER_SAVER_ON_SLEEP = booleanPreferencesKey("enable_power_saver_on_sleep")
    private val DISABLE_POWER_SAVER_ON_WAKE = booleanPreferencesKey("disable_power_saver_on_wake")

    // ========== CPU 绑定 ==========
    private val CPU_BIND_ENABLED = booleanPreferencesKey("cpu_bind_enabled")
    private val CPU_MODE = stringPreferencesKey("cpu_mode")

    // ========== CPU 调度优化 ==========
    private val CPU_OPTIMIZATION_ENABLED = booleanPreferencesKey("cpu_optimization_enabled")
    private val AUTO_SWITCH_CPU_MODE = booleanPreferencesKey("auto_switch_cpu_mode")
    private val ALLOW_MANUAL_CPU_MODE = booleanPreferencesKey("allow_manual_cpu_mode")
    private val CPU_MODE_ON_SCREEN = stringPreferencesKey("cpu_mode_on_screen")
    private val CPU_MODE_ON_SCREEN_OFF = stringPreferencesKey("cpu_mode_on_screen_off")

    // ========== CPU 参数 - 日常模式 ==========
    private val DAILY_UP_RATE_LIMIT = intPreferencesKey("daily_up_rate_limit")
    private val DAILY_DOWN_RATE_LIMIT = intPreferencesKey("daily_down_rate_limit")
    private val DAILY_HI_SPEED_LOAD = intPreferencesKey("daily_hi_speed_load")
    private val DAILY_TARGET_LOADS = intPreferencesKey("daily_target_loads")

    // ========== CPU 参数 - 待机模式 ==========
    private val STANDBY_UP_RATE_LIMIT = intPreferencesKey("standby_up_rate_limit")
    private val STANDBY_DOWN_RATE_LIMIT = intPreferencesKey("standby_down_rate_limit")
    private val STANDBY_HI_SPEED_LOAD = intPreferencesKey("standby_hi_speed_load")
    private val STANDBY_TARGET_LOADS = intPreferencesKey("standby_target_loads")

    // ========== CPU 参数 - 默认模式 ==========
    private val DEFAULT_UP_RATE_LIMIT = intPreferencesKey("default_up_rate_limit")
    private val DEFAULT_DOWN_RATE_LIMIT = intPreferencesKey("default_down_rate_limit")
    private val DEFAULT_HI_SPEED_LOAD = intPreferencesKey("default_hi_speed_load")
    private val DEFAULT_TARGET_LOADS = intPreferencesKey("default_target_loads")

    // ========== CPU 参数 - 性能模式 ==========
    private val PERF_UP_RATE_LIMIT = intPreferencesKey("perf_up_rate_limit")
    private val PERF_DOWN_RATE_LIMIT = intPreferencesKey("perf_down_rate_limit")
    private val PERF_HI_SPEED_LOAD = intPreferencesKey("perf_hi_speed_load")
    private val PERF_TARGET_LOADS = intPreferencesKey("perf_target_loads")

    // ========== Freezer 服务 ==========
    private val FREEZER_ENABLED = booleanPreferencesKey("freezer_enabled")
    private val FREEZE_DELAY = intPreferencesKey("freeze_delay")

    // ========== 进程压制 ==========
    private val PROCESS_SUPPRESS_ENABLED = booleanPreferencesKey("process_suppress_enabled")
    private val SUPPRESS_SCORE = intPreferencesKey("suppress_score")

    // ========== 场景检测 ==========
    private val SCENE_CHECK_ENABLED = booleanPreferencesKey("scene_check_enabled")
    private val CHECK_NETWORK_TRAFFIC = booleanPreferencesKey("check_network_traffic")
    private val CHECK_AUDIO_PLAYBACK = booleanPreferencesKey("check_audio_playback")
    private val CHECK_NAVIGATION = booleanPreferencesKey("check_navigation")
    private val CHECK_PHONE_CALL = booleanPreferencesKey("check_phone_call")
    private val CHECK_NFC_P2P = booleanPreferencesKey("check_nfc_p2p")
    private val CHECK_WIFI_HOTSPOT = booleanPreferencesKey("check_wifi_hotspot")
    private val CHECK_USB_TETHERING = booleanPreferencesKey("check_usb_tethering")
    private val CHECK_SCREEN_CASTING = booleanPreferencesKey("check_screen_casting")
    private val CHECK_CHARGING = booleanPreferencesKey("check_charging")

    // ========== 白名单 ==========
    private val WHITELIST = stringPreferencesKey("whitelist")

    // DataStore 扩展属性
    private val Context.dataStore: DataStore<Preferences> by androidx.datastore.preferences.preferencesDataStore("settings")

    fun initialize(context: Context) {
        dataStore = context.dataStore
    }

    // 修复：改为 get()，避免类加载时初始化
    val settings: Flow<AppSettings>
        get() = dataStore.data.map { preferences ->
            AppSettings(
                rootGranted = preferences[ROOT_GRANTED]?.toBoolean() ?: false,
                serviceRunning = preferences[SERVICE_RUNNING] ?: false,
                deepSleepEnabled = preferences[DEEP_SLEEP_ENABLED] ?: false,
                wakeupSuppressEnabled = preferences[WAKEUP_SUPPRESS_ENABLED] ?: false,
                alarmSuppressEnabled = preferences[ALARM_SUPPRESS_ENABLED] ?: false,
                deepDozeEnabled = preferences[DEEP_DOZE_ENABLED] ?: false,
                deepDozeDelaySeconds = preferences[DEEP_DOZE_DELAY_SECONDS] ?: 30,
                deepDozeForceMode = preferences[DEEP_DOZE_FORCE_MODE] ?: false,
                deepSleepHookEnabled = preferences[DEEP_SLEEP_HOOK_ENABLED] ?: false,
                deepSleepDelaySeconds = preferences[DEEP_SLEEP_DELAY_SECONDS] ?: 60,
                deepSleepBlockExit = preferences[DEEP_SLEEP_BLOCK_EXIT] ?: false,
                deepSleepCheckInterval = preferences[DEEP_SLEEP_CHECK_INTERVAL] ?: 30,
                backgroundOptimizationEnabled = preferences[BACKGROUND_OPTIMIZATION_ENABLED] ?: false,
                appSuspendEnabled = preferences[APP_SUSPEND_ENABLED] ?: false,
                backgroundRestrictEnabled = preferences[BACKGROUND_RESTRICT_ENABLED] ?: false,
                gpuOptimizationEnabled = preferences[GPU_OPTIMIZATION_ENABLED] ?: false,
                gpuMode = preferences[GPU_MODE] ?: "default",
                gpuThrottlingEnabled = preferences[GPU_THROTTLING_ENABLED] ?: false,
                gpuBusSplitEnabled = preferences[GPU_BUS_SPLIT_ENABLED] ?: false,
                gpuIdleTimer = preferences[GPU_IDLE_TIMER] ?: 50,
                gpuMaxFreq = preferences[GPU_MAX_FREQ] ?: 770000000L,
                gpuMinFreq = preferences[GPU_MIN_FREQ] ?: 310000000L,
                gpuThermalPwrLevel = preferences[GPU_THERMAL_PWR_LEVEL] ?: 5,
                gpuTripPointTemp = preferences[GPU_TRIP_POINT_TEMP] ?: 55000,
                gpuTripPointHyst = preferences[GPU_TRIP_POINT_HYST] ?: 5000,
                batteryOptimizationEnabled = preferences[BATTERY_OPTIMIZATION_ENABLED] ?: false,
                powerSavingEnabled = preferences[POWER_SAVING_ENABLED] ?: false,
                enablePowerSaverOnSleep = preferences[ENABLE_POWER_SAVER_ON_SLEEP] ?: false,
                disablePowerSaverOnWake = preferences[DISABLE_POWER_SAVER_ON_WAKE] ?: false,
                cpuBindEnabled = preferences[CPU_BIND_ENABLED] ?: false,
                cpuMode = preferences[CPU_MODE] ?: "daily",
                cpuOptimizationEnabled = preferences[CPU_OPTIMIZATION_ENABLED] ?: false,
                autoSwitchCpuMode = preferences[AUTO_SWITCH_CPU_MODE] ?: false,
                allowManualCpuMode = preferences[ALLOW_MANUAL_CPU_MODE] ?: true,
                cpuModeOnScreen = preferences[CPU_MODE_ON_SCREEN] ?: "daily",
                cpuModeOnScreenOff = preferences[CPU_MODE_ON_SCREEN_OFF] ?: "standby",
                dailyUpRateLimit = preferences[DAILY_UP_RATE_LIMIT] ?: 1000,
                dailyDownRateLimit = preferences[DAILY_DOWN_RATE_LIMIT] ?: 500,
                dailyHiSpeedLoad = preferences[DAILY_HI_SPEED_LOAD] ?: 85,
                dailyTargetLoads = preferences[DAILY_TARGET_LOADS] ?: 80,
                standbyUpRateLimit = preferences[STANDBY_UP_RATE_LIMIT] ?: 5000,
                standbyDownRateLimit = preferences[STANDBY_DOWN_RATE_LIMIT] ?: 0,
                standbyHiSpeedLoad = preferences[STANDBY_HI_SPEED_LOAD] ?: 95,
                standbyTargetLoads = preferences[STANDBY_TARGET_LOADS] ?: 90,
                defaultUpRateLimit = preferences[DEFAULT_UP_RATE_LIMIT] ?: 0,
                defaultDownRateLimit = preferences[DEFAULT_DOWN_RATE_LIMIT] ?: 0,
                defaultHiSpeedLoad = preferences[DEFAULT_HI_SPEED_LOAD] ?: 90,
                defaultTargetLoads = preferences[DEFAULT_TARGET_LOADS] ?: 90,
                perfUpRateLimit = preferences[PERF_UP_RATE_LIMIT] ?: 0,
                perfDownRateLimit = preferences[PERF_DOWN_RATE_LIMIT] ?: 0,
                perfHiSpeedLoad = preferences[PERF_HI_SPEED_LOAD] ?: 75,
                perfTargetLoads = preferences[PERF_TARGET_LOADS] ?: 70,
                freezerEnabled = preferences[FREEZER_ENABLED] ?: false,
                freezeDelay = preferences[FREEZE_DELAY] ?: 30,
                processSuppressEnabled = preferences[PROCESS_SUPPRESS_ENABLED] ?: false,
                suppressScore = preferences[SUPPRESS_SCORE] ?: 500,
                sceneCheckEnabled = preferences[SCENE_CHECK_ENABLED] ?: false,
                checkNetworkTraffic = preferences[CHECK_NETWORK_TRAFFIC] ?: true,
                checkAudioPlayback = preferences[CHECK_AUDIO_PLAYBACK] ?: true,
                checkNavigation = preferences[CHECK_NAVIGATION] ?: true,
                checkPhoneCall = preferences[CHECK_PHONE_CALL] ?: true,
                checkNfcP2p = preferences[CHECK_NFC_P2P] ?: true,
                checkWifiHotspot = preferences[CHECK_WIFI_HOTSPOT] ?: true,
                checkUsbTethering = preferences[CHECK_USB_TETHERING] ?: true,
                checkScreenCasting = preferences[CHECK_SCREEN_CASTING] ?: true,
                checkCharging = preferences[CHECK_CHARGING] ?: false,
                whitelist = preferences[WHITELIST]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
            )
        }

    suspend fun setRootGranted(granted: Boolean) {
        dataStore.edit { it[ROOT_GRANTED] = granted.toString() }
    }

    suspend fun setServiceRunning(running: Boolean) {
        dataStore.edit { it[SERVICE_RUNNING] = running }
    }

    suspend fun setDeepSleepEnabled(enabled: Boolean) {
        dataStore.edit { it[DEEP_SLEEP_ENABLED] = enabled }
    }

    suspend fun setWakeupSuppressEnabled(enabled: Boolean) {
        dataStore.edit { it[WAKEUP_SUPPRESS_ENABLED] = enabled }
    }

    suspend fun setAlarmSuppressEnabled(enabled: Boolean) {
        dataStore.edit { it[ALARM_SUPPRESS_ENABLED] = enabled }
    }

    suspend fun setDeepDozeEnabled(enabled: Boolean) {
        dataStore.edit { it[DEEP_DOZE_ENABLED] = enabled }
    }

    suspend fun setDeepDozeDelaySeconds(seconds: Int) {
        dataStore.edit { it[DEEP_DOZE_DELAY_SECONDS] = seconds }
    }

    suspend fun setDeepDozeForceMode(enabled: Boolean) {
        dataStore.edit { it[DEEP_DOZE_FORCE_MODE] = enabled }
    }

    suspend fun setDeepSleepHookEnabled(enabled: Boolean) {
        dataStore.edit { it[DEEP_SLEEP_HOOK_ENABLED] = enabled }
    }

    suspend fun setDeepSleepDelaySeconds(seconds: Int) {
        dataStore.edit { it[DEEP_SLEEP_DELAY_SECONDS] = seconds }
    }

    suspend fun setDeepSleepBlockExit(enabled: Boolean) {
        dataStore.edit { it[DEEP_SLEEP_BLOCK_EXIT] = enabled }
    }

    suspend fun setDeepSleepCheckInterval(seconds: Int) {
        dataStore.edit { it[DEEP_SLEEP_CHECK_INTERVAL] = seconds }
    }

    suspend fun setBackgroundOptimizationEnabled(enabled: Boolean) {
        dataStore.edit { it[BACKGROUND_OPTIMIZATION_ENABLED] = enabled }
    }

    suspend fun setAppSuspendEnabled(enabled: Boolean) {
        dataStore.edit { it[APP_SUSPEND_ENABLED] = enabled }
    }

    suspend fun setBackgroundRestrictEnabled(enabled: Boolean) {
        dataStore.edit { it[BACKGROUND_RESTRICT_ENABLED] = enabled }
    }

    suspend fun setGpuOptimizationEnabled(enabled: Boolean) {
        dataStore.edit { it[GPU_OPTIMIZATION_ENABLED] = enabled }
    }

    suspend fun setGpuMode(mode: String) {
        dataStore.edit { it[GPU_MODE] = mode }
    }

    suspend fun setGpuThrottling(enabled: Boolean) {
        dataStore.edit { it[GPU_THROTTLING_ENABLED] = enabled }
    }

    suspend fun setGpuBusSplit(enabled: Boolean) {
        dataStore.edit { it[GPU_BUS_SPLIT_ENABLED] = enabled }
    }

    suspend fun setGpuIdleTimer(timer: Int) {
        dataStore.edit { it[GPU_IDLE_TIMER] = timer }
    }

    suspend fun setGpuMaxFreq(freq: Long) {
        dataStore.edit { it[GPU_MAX_FREQ] = freq }
    }

    suspend fun setGpuMinFreq(freq: Long) {
        dataStore.edit { it[GPU_MIN_FREQ] = freq }
    }

    suspend fun setGpuThermalPwrLevel(level: Int) {
        dataStore.edit { it[GPU_THERMAL_PWR_LEVEL] = level }
    }

    suspend fun setGpuTripPointTemp(temp: Int) {
        dataStore.edit { it[GPU_TRIP_POINT_TEMP] = temp }
    }

    suspend fun setGpuTripPointHyst(hyst: Int) {
        dataStore.edit { it[GPU_TRIP_POINT_HYST] = hyst }
    }

    suspend fun setBatteryOptimizationEnabled(enabled: Boolean) {
        dataStore.edit { it[BATTERY_OPTIMIZATION_ENABLED] = enabled }
    }

    suspend fun setPowerSavingEnabled(enabled: Boolean) {
        dataStore.edit { it[POWER_SAVING_ENABLED] = enabled }
    }

    suspend fun setEnablePowerSaverOnSleep(enabled: Boolean) {
        dataStore.edit { it[ENABLE_POWER_SAVER_ON_SLEEP] = enabled }
    }

    suspend fun setDisablePowerSaverOnWake(enabled: Boolean) {
        dataStore.edit { it[DISABLE_POWER_SAVER_ON_WAKE] = enabled }
    }

    suspend fun setCpuBindEnabled(enabled: Boolean) {
        dataStore.edit { it[CPU_BIND_ENABLED] = enabled }
    }

    suspend fun setCpuMode(mode: String) {
        dataStore.edit { it[CPU_MODE] = mode }
    }

    suspend fun setCpuOptimizationEnabled(enabled: Boolean) {
        dataStore.edit { it[CPU_OPTIMIZATION_ENABLED] = enabled }
    }

    suspend fun setAutoSwitchCpuMode(enabled: Boolean) {
        dataStore.edit { it[AUTO_SWITCH_CPU_MODE] = enabled }
    }

    suspend fun setAllowManualCpuMode(enabled: Boolean) {
        dataStore.edit { it[ALLOW_MANUAL_CPU_MODE] = enabled }
    }

    suspend fun setCpuModeOnScreen(mode: String) {
        dataStore.edit { it[CPU_MODE_ON_SCREEN] = mode }
    }

    suspend fun setCpuModeOnScreenOff(mode: String) {
        dataStore.edit { it[CPU_MODE_ON_SCREEN_OFF] = mode }
    }

    suspend fun setDailyUpRateLimit(value: Int) {
        dataStore.edit { it[DAILY_UP_RATE_LIMIT] = value }
    }

    suspend fun setDailyDownRateLimit(value: Int) {
        dataStore.edit { it[DAILY_DOWN_RATE_LIMIT] = value }
    }

    suspend fun setDailyHiSpeedLoad(value: Int) {
        dataStore.edit { it[DAILY_HI_SPEED_LOAD] = value }
    }

    suspend fun setDailyTargetLoads(value: Int) {
        dataStore.edit { it[DAILY_TARGET_LOADS] = value }
    }

    suspend fun setStandbyUpRateLimit(value: Int) {
        dataStore.edit { it[STANDBY_UP_RATE_LIMIT] = value }
    }

    suspend fun setStandbyDownRateLimit(value: Int) {
        dataStore.edit { it[STANDBY_DOWN_RATE_LIMIT] = value }
    }

    suspend fun setStandbyHiSpeedLoad(value: Int) {
        dataStore.edit { it[STANDBY_HI_SPEED_LOAD] = value }
    }

    suspend fun setStandbyTargetLoads(value: Int) {
        dataStore.edit { it[STANDBY_TARGET_LOADS] = value }
    }

    suspend fun setDefaultUpRateLimit(value: Int) {
        dataStore.edit { it[DEFAULT_UP_RATE_LIMIT] = value }
    }

    suspend fun setDefaultDownRateLimit(value: Int) {
        dataStore.edit { it[DEFAULT_DOWN_RATE_LIMIT] = value }
    }

    suspend fun setDefaultHiSpeedLoad(value: Int) {
        dataStore.edit { it[DEFAULT_HI_SPEED_LOAD] = value }
    }

    suspend fun setDefaultTargetLoads(value: Int) {
        dataStore.edit { it[DEFAULT_TARGET_LOADS] = value }
    }

    suspend fun setPerfUpRateLimit(value: Int) {
        dataStore.edit { it[PERF_UP_RATE_LIMIT] = value }
    }

    suspend fun setPerfDownRateLimit(value: Int) {
        dataStore.edit { it[PERF_DOWN_RATE_LIMIT] = value }
    }

    suspend fun setPerfHiSpeedLoad(value: Int) {
        dataStore.edit { it[PERF_HI_SPEED_LOAD] = value }
    }

    suspend fun setPerfTargetLoads(value: Int) {
        dataStore.edit { it[PERF_TARGET_LOADS] = value }
    }

    suspend fun setFreezerEnabled(enabled: Boolean) {
        dataStore.edit { it[FREEZER_ENABLED] = enabled }
    }

    suspend fun setFreezeDelay(delay: Int) {
        dataStore.edit { it[FREEZE_DELAY] = delay }
    }

    suspend fun setProcessSuppressEnabled(enabled: Boolean) {
        dataStore.edit { it[PROCESS_SUPPRESS_ENABLED] = enabled }
    }

    suspend fun setSuppressScore(score: Int) {
        dataStore.edit { it[SUPPRESS_SCORE] = score }
    }

    suspend fun setSceneCheckEnabled(enabled: Boolean) {
        dataStore.edit { it[SCENE_CHECK_ENABLED] = enabled }
    }

    suspend fun setCheckNetworkTraffic(enabled: Boolean) {
        dataStore.edit { it[CHECK_NETWORK_TRAFFIC] = enabled }
    }

    suspend fun setCheckAudioPlayback(enabled: Boolean) {
        dataStore.edit { it[CHECK_AUDIO_PLAYBACK] = enabled }
    }

    suspend fun setCheckNavigation(enabled: Boolean) {
        dataStore.edit { it[CHECK_NAVIGATION] = enabled }
    }

    suspend fun setCheckPhoneCall(enabled: Boolean) {
        dataStore.edit { it[CHECK_PHONE_CALL] = enabled }
    }

    suspend fun setCheckNfcP2p(enabled: Boolean) {
        dataStore.edit { it[CHECK_NFC_P2P] = enabled }
    }

    suspend fun setCheckWifiHotspot(enabled: Boolean) {
        dataStore.edit { it[CHECK_WIFI_HOTSPOT] = enabled }
    }

    suspend fun setCheckUsbTethering(enabled: Boolean) {
        dataStore.edit { it[CHECK_USB_TETHERING] = enabled }
    }

    suspend fun setCheckScreenCasting(enabled: Boolean) {
        dataStore.edit { it[CHECK_SCREEN_CASTING] = enabled }
    }

    suspend fun setCheckCharging(enabled: Boolean) {
        dataStore.edit { it[CHECK_CHARGING] = enabled }
    }

    suspend fun setWhitelist(whitelist: List<String>) {
        dataStore.edit { it[WHITELIST] = whitelist.joinToString(",") }
    }
}