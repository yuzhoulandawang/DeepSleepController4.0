package com.example.deepsleep.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deepsleep.data.SettingsRepository
import com.example.deepsleep.data.LogRepository
import com.example.deepsleep.model.AppSettings
import com.example.deepsleep.root.OptimizationManager
import com.example.deepsleep.root.ProcessSuppressor
import com.example.deepsleep.root.RootCommander   // 添加导入
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 主页面 ViewModel
 * 管理主界面的状态和业务逻辑
 */
class MainViewModel : ViewModel() {

    companion object {
        private const val TAG = "MainViewModel"
    }

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    init {
        loadSettings()
        refreshRootStatus()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            SettingsRepository.settings.collect { appSettings ->
                _settings.value = appSettings
                LogRepository.debug(TAG, "Settings loaded: deepSleep=${appSettings.deepSleepEnabled}")
            }
        }
    }

    // 修改点：使用 RootCommander.checkRoot() 替代 Runtime.exec
    fun refreshRootStatus() {
        viewModelScope.launch {
            val hasRoot = try {
                RootCommander.checkRoot()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check root status via RootCommander", e)
                false
            }
            _settings.value = _settings.value.copy(rootGranted = hasRoot)
            LogRepository.info(TAG, "Root status refreshed: $hasRoot")
        }
    }

    // ========== 深度睡眠控制 ==========
    fun setDeepSleepEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(deepSleepEnabled = enabled)
            SettingsRepository.setDeepSleepEnabled(enabled)
            LogRepository.info(TAG, "Deep sleep enabled: $enabled")
        }
    }

    fun setWakeupSuppressEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(wakeupSuppressEnabled = enabled)
            SettingsRepository.setWakeupSuppressEnabled(enabled)
        }
    }

    fun setAlarmSuppressEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(alarmSuppressEnabled = enabled)
            SettingsRepository.setAlarmSuppressEnabled(enabled)
        }
    }

    // ========== 后台优化 ==========
    fun setBackgroundOptimizationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(backgroundOptimizationEnabled = enabled)
            SettingsRepository.setBackgroundOptimizationEnabled(enabled)
            LogRepository.info(TAG, "Background optimization: $enabled")
        }
    }

    fun setAppSuspendEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(appSuspendEnabled = enabled)
            SettingsRepository.setAppSuspendEnabled(enabled)
        }
    }

    fun setBackgroundRestrictEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(backgroundRestrictEnabled = enabled)
            SettingsRepository.setBackgroundRestrictEnabled(enabled)
            // 真实实现：调用 ProcessSuppressor
            if (enabled) {
                val count = ProcessSuppressor.suppressBackgroundApps(_settings.value.suppressScore)
                LogRepository.info(TAG, "Background restrict enabled, suppressed $count apps")
            }
        }
    }

    // ========== GPU 优化 ==========
    fun setGpuOptimizationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(gpuOptimizationEnabled = enabled)
            SettingsRepository.setGpuOptimizationEnabled(enabled)
        }
    }

    fun setGpuMode(mode: String) {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(gpuMode = mode)
            SettingsRepository.setGpuMode(mode)
            // 应用 GPU 模式
            val optMode = when (mode) {
                "performance" -> OptimizationManager.PerformanceMode.PERFORMANCE
                "power_saving" -> OptimizationManager.PerformanceMode.STANDBY
                else -> OptimizationManager.PerformanceMode.DAILY
            }
            OptimizationManager.applyAllOptimizations(optMode)
            LogRepository.info(TAG, "GPU mode changed to: $mode")
        }
    }

    fun setGpuThrottling(enabled: Boolean) {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(gpuThrottlingEnabled = enabled)
            SettingsRepository.setGpuThrottling(enabled)
        }
    }

    fun setGpuBusSplit(enabled: Boolean) {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(gpuBusSplitEnabled = enabled)
            SettingsRepository.setGpuBusSplit(enabled)
        }
    }

    fun setGpuIdleTimer(timer: Int) {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(gpuIdleTimer = timer)
            SettingsRepository.setGpuIdleTimer(timer)
        }
    }

    fun setGpuMaxFreq(freq: Long) {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(gpuMaxFreq = freq)
            SettingsRepository.setGpuMaxFreq(freq)
        }
    }

    fun setGpuMinFreq(freq: Long) {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(gpuMinFreq = freq)
            SettingsRepository.setGpuMinFreq(freq)
        }
    }

    fun setGpuThermalPwrLevel(level: Int) {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(gpuThermalPwrLevel = level)
            SettingsRepository.setGpuThermalPwrLevel(level)
        }
    }

    fun setGpuTripPointTemp(temp: Int) {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(gpuTripPointTemp = temp)
            SettingsRepository.setGpuTripPointTemp(temp)
        }
    }

    fun setGpuTripPointHyst(hyst: Int) {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(gpuTripPointHyst = hyst)
            SettingsRepository.setGpuTripPointHyst(hyst)
        }
    }

    // GPU 模式快捷按钮
    fun applyGpuPerformanceMode() {
        setGpuMode("performance")
    }

    fun applyGpuPowerSavingMode() {
        setGpuMode("power_saving")
    }

    fun applyGpuDefaultMode() {
        setGpuMode("default")
    }

    // ========== 电池优化 ==========
    fun setBatteryOptimizationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(batteryOptimizationEnabled = enabled)
            SettingsRepository.setBatteryOptimizationEnabled(enabled)
        }
    }

    fun setPowerSavingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(powerSavingEnabled = enabled)
            SettingsRepository.setPowerSavingEnabled(enabled)
        }
    }

    // ========== CPU 绑定 ==========
    fun setCpuBindEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(cpuBindEnabled = enabled)
            SettingsRepository.setCpuBindEnabled(enabled)
        }
    }

    fun setCpuMode(mode: String) {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(cpuMode = mode)
            SettingsRepository.setCpuMode(mode)
        }
    }

    // ========== Freezer 服务 ==========
    fun setFreezerEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(freezerEnabled = enabled)
            SettingsRepository.setFreezerEnabled(enabled)
        }
    }

    fun setFreezeDelay(delay: Int) {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(freezeDelay = delay)
            SettingsRepository.setFreezeDelay(delay)
        }
    }

    // ========== 场景检测 ==========
    fun setSceneCheckEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(sceneCheckEnabled = enabled)
            SettingsRepository.setSceneCheckEnabled(enabled)
        }
    }

    // ========== 进程压制 ==========
    fun setProcessSuppressEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(processSuppressEnabled = enabled)
            SettingsRepository.setProcessSuppressEnabled(enabled)
        }
    }

    fun setSuppressScore(score: Int) {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(suppressScore = score)
            SettingsRepository.setSuppressScore(score)
        }
    }
}