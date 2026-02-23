package com.example.deepsleep.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deepsleep.data.SettingsRepository
import com.example.deepsleep.model.AppSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 设置页面 ViewModel
 * 管理设置界面的状态和业务逻辑
 */
class SettingsViewModel : ViewModel() {
    
    companion object {
        private const val TAG = "SettingsViewModel"
    }
    
    // 直接使用单例 SettingsRepository
    val settings: StateFlow<AppSettings> = SettingsRepository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )
    
    // ========== 深度 Doze 配置 ==========
    fun setDeepDozeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            SettingsRepository.setDeepDozeEnabled(enabled)
        }
    }
    
    fun setDeepDozeDelaySeconds(seconds: Int) {
        viewModelScope.launch {
            SettingsRepository.setDeepDozeDelaySeconds(seconds)
        }
    }
    
    fun setDeepDozeForceMode(enabled: Boolean) {
        viewModelScope.launch {
            SettingsRepository.setDeepDozeForceMode(enabled)
        }
    }
    
    // ========== 深度睡眠配置（Hook 版本） ==========
    fun setDeepSleepHookEnabled(enabled: Boolean) {
        viewModelScope.launch {
            SettingsRepository.setDeepSleepHookEnabled(enabled)
        }
    }
    
    fun setDeepSleepDelaySeconds(seconds: Int) {
        viewModelScope.launch {
            SettingsRepository.setDeepSleepDelaySeconds(seconds)
        }
    }
    
    fun setDeepSleepBlockExit(enabled: Boolean) {
        viewModelScope.launch {
            SettingsRepository.setDeepSleepBlockExit(enabled)
        }
    }
    
    fun setDeepSleepCheckInterval(seconds: Int) {
        viewModelScope.launch {
            SettingsRepository.setDeepSleepCheckInterval(seconds)
        }
    }
    
    // ========== 系统省电模式联动 ==========
    fun setEnablePowerSaverOnSleep(enabled: Boolean) {
        viewModelScope.launch {
            SettingsRepository.setEnablePowerSaverOnSleep(enabled)
        }
    }
    
    fun setDisablePowerSaverOnWake(enabled: Boolean) {
        viewModelScope.launch {
            SettingsRepository.setDisablePowerSaverOnWake(enabled)
        }
    }
    
    // ========== CPU 调度优化配置 ==========
    fun setCpuOptimizationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            SettingsRepository.setCpuOptimizationEnabled(enabled)
        }
    }
    
    fun setCpuModeOnScreen(mode: String) {
        viewModelScope.launch {
            SettingsRepository.setCpuModeOnScreen(mode)
        }
    }
    
    fun setCpuModeOnScreenOff(mode: String) {
        viewModelScope.launch {
            SettingsRepository.setCpuModeOnScreenOff(mode)
        }
    }
    
    fun setAutoSwitchCpuMode(enabled: Boolean) {
        viewModelScope.launch {
            SettingsRepository.setAutoSwitchCpuMode(enabled)
        }
    }
    
    fun setAllowManualCpuMode(enabled: Boolean) {
        viewModelScope.launch {
            SettingsRepository.setAllowManualCpuMode(enabled)
        }
    }
    
    // ========== CPU 参数 - 日常模式 ==========
    fun setDailyUpRateLimit(value: Int) {
        viewModelScope.launch {
            SettingsRepository.setDailyUpRateLimit(value)
        }
    }
    
    fun setDailyDownRateLimit(value: Int) {
        viewModelScope.launch {
            SettingsRepository.setDailyDownRateLimit(value)
        }
    }
    
    fun setDailyHiSpeedLoad(value: Int) {
        viewModelScope.launch {
            SettingsRepository.setDailyHiSpeedLoad(value)
        }
    }
    
    fun setDailyTargetLoads(value: Int) {
        viewModelScope.launch {
            SettingsRepository.setDailyTargetLoads(value)
        }
    }
    
    // ========== CPU 参数 - 待机模式 ==========
    fun setStandbyUpRateLimit(value: Int) {
        viewModelScope.launch {
            SettingsRepository.setStandbyUpRateLimit(value)
        }
    }
    
    fun setStandbyDownRateLimit(value: Int) {
        viewModelScope.launch {
            SettingsRepository.setStandbyDownRateLimit(value)
        }
    }
    
    fun setStandbyHiSpeedLoad(value: Int) {
        viewModelScope.launch {
            SettingsRepository.setStandbyHiSpeedLoad(value)
        }
    }
    
    fun setStandbyTargetLoads(value: Int) {
        viewModelScope.launch {
            SettingsRepository.setStandbyTargetLoads(value)
        }
    }
    
    // ========== CPU 参数 - 默认模式 ==========
    fun setDefaultUpRateLimit(value: Int) {
        viewModelScope.launch {
            SettingsRepository.setDefaultUpRateLimit(value)
        }
    }
    
    fun setDefaultDownRateLimit(value: Int) {
        viewModelScope.launch {
            SettingsRepository.setDefaultDownRateLimit(value)
        }
    }
    
    fun setDefaultHiSpeedLoad(value: Int) {
        viewModelScope.launch {
            SettingsRepository.setDefaultHiSpeedLoad(value)
        }
    }
    
    fun setDefaultTargetLoads(value: Int) {
        viewModelScope.launch {
            SettingsRepository.setDefaultTargetLoads(value)
        }
    }
    
    // ========== CPU 参数 - 性能模式 ==========
    fun setPerfUpRateLimit(value: Int) {
        viewModelScope.launch {
            SettingsRepository.setPerfUpRateLimit(value)
        }
    }
    
    fun setPerfDownRateLimit(value: Int) {
        viewModelScope.launch {
            SettingsRepository.setPerfDownRateLimit(value)
        }
    }
    
    fun setPerfHiSpeedLoad(value: Int) {
        viewModelScope.launch {
            SettingsRepository.setPerfHiSpeedLoad(value)
        }
    }
    
    fun setPerfTargetLoads(value: Int) {
        viewModelScope.launch {
            SettingsRepository.setPerfTargetLoads(value)
        }
    }
    
    // ========== 场景检测配置 ==========
    fun setCheckNetworkTraffic(enabled: Boolean) {
        viewModelScope.launch {
            SettingsRepository.setCheckNetworkTraffic(enabled)
        }
    }
    
    fun setCheckAudioPlayback(enabled: Boolean) {
        viewModelScope.launch {
            SettingsRepository.setCheckAudioPlayback(enabled)
        }
    }
    
    fun setCheckNavigation(enabled: Boolean) {
        viewModelScope.launch {
            SettingsRepository.setCheckNavigation(enabled)
        }
    }
    
    fun setCheckPhoneCall(enabled: Boolean) {
        viewModelScope.launch {
            SettingsRepository.setCheckPhoneCall(enabled)
        }
    }
    
    fun setCheckNfcP2p(enabled: Boolean) {
        viewModelScope.launch {
            SettingsRepository.setCheckNfcP2p(enabled)
        }
    }
    
    fun setCheckWifiHotspot(enabled: Boolean) {
        viewModelScope.launch {
            SettingsRepository.setCheckWifiHotspot(enabled)
        }
    }
    
    fun setCheckUsbTethering(enabled: Boolean) {
        viewModelScope.launch {
            SettingsRepository.setCheckUsbTethering(enabled)
        }
    }
    
    fun setCheckScreenCasting(enabled: Boolean) {
        viewModelScope.launch {
            SettingsRepository.setCheckScreenCasting(enabled)
        }
    }
    
    fun setCheckCharging(enabled: Boolean) {
        viewModelScope.launch {
            SettingsRepository.setCheckCharging(enabled)
        }
    }
}
