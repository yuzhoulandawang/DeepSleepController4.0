package com.example.deepsleep.root

import com.example.deepsleep.data.LogRepository
import com.example.deepsleep.model.LogLevel

/**
 * GPU 优化控制器 - 专门针对 Adreno GPU
 */
object GpuOptimizer {

    private const val KGSL_BASE = "/sys/class/kgsl/kgsl-3d0"

    // GPU 节流控制
    suspend fun setThrottling(enabled: Boolean): Boolean {
        return try {
            val value = if (enabled) "1" else "0"
            RootCommander.exec("echo $value > $KGSL_BASE/throttling")
            LogRepository.appendLog(LogLevel.INFO, "GpuOptimizer", "GPU 节流已${if (enabled) "启用" else "禁用"}")
            true
        } catch (e: Exception) {
            LogRepository.appendLog(LogLevel.ERROR, "GpuOptimizer", "设置 GPU 节流失败: ${e.message}")
            false
        }
    }

    // 总线分割
    suspend fun setBusSplit(enabled: Boolean): Boolean {
        return try {
            val value = if (enabled) "1" else "0"
            RootCommander.exec("echo $value > $KGSL_BASE/bus_split")
            LogRepository.appendLog(LogLevel.INFO, "GpuOptimizer", "总线分割已${if (enabled) "启用" else "禁用"}")
            true
        } catch (e: Exception) {
            LogRepository.appendLog(LogLevel.ERROR, "GpuOptimizer", "设置总线分割失败: ${e.message}")
            false
        }
    }

    // 空闲定时器（毫秒）
    suspend fun setIdleTimer(ms: Int): Boolean {
        return try {
            RootCommander.exec("echo $ms > $KGSL_BASE/idle_timer")
            LogRepository.appendLog(LogLevel.INFO, "GpuOptimizer", "空闲定时器已设置为 ${ms}ms")
            true
        } catch (e: Exception) {
            LogRepository.appendLog(LogLevel.ERROR, "GpuOptimizer", "设置空闲定时器失败: ${e.message}")
            false
        }
    }

    // GPU 最大频率（Hz）
    suspend fun setMaxGpuClk(hz: Long): Boolean {
        return try {
            RootCommander.exec("echo $hz > $KGSL_BASE/max_gpuclk")
            LogRepository.appendLog(LogLevel.INFO, "GpuOptimizer", "GPU 最大频率已设置为 ${hz}Hz")
            true
        } catch (e: Exception) {
            LogRepository.appendLog(LogLevel.ERROR, "GpuOptimizer", "设置 GPU 最大频率失败: ${e.message}")
            false
        }
    }

    // 热功率等级
    suspend fun setThermalPwrLevel(level: Int): Boolean {
        return try {
            RootCommander.exec("echo $level > $KGSL_BASE/thermal_pwrlevel")
            LogRepository.appendLog(LogLevel.INFO, "GpuOptimizer", "热功率等级已设置为 $level")
            true
        } catch (e: Exception) {
            LogRepository.appendLog(LogLevel.ERROR, "GpuOptimizer", "设置热功率等级失败: ${e.message}")
            false
        }
    }

    // 最小频率（Hz）- 通过 devfreq
    suspend fun setMinFreq(hz: Long): Boolean {
        return try {
            RootCommander.exec("echo $hz > /sys/class/devfreq/kgsl-3d0/min_freq")
            LogRepository.appendLog(LogLevel.INFO, "GpuOptimizer", "GPU 最小频率已设置为 ${hz}Hz")
            true
        } catch (e: Exception) {
            LogRepository.appendLog(LogLevel.ERROR, "GpuOptimizer", "设置 GPU 最小频率失败: ${e.message}")
            false
        }
    }

    // 最大频率（Hz）- 通过 devfreq
    suspend fun setMaxFreq(hz: Long): Boolean {
        return try {
            RootCommander.exec("echo $hz > /sys/class/devfreq/kgsl-3d0/max_freq")
            LogRepository.appendLog(LogLevel.INFO, "GpuOptimizer", "GPU 最大频率已设置为 ${hz}Hz")
            true
        } catch (e: Exception) {
            LogRepository.appendLog(LogLevel.ERROR, "GpuOptimizer", "设置 GPU 最大频率失败: ${e.message}")
            false
        }
    }

    // 设置温度触发点（毫度）
    suspend fun setTripPointTemp(zoneId: Int, temp: Int): Boolean {
        return try {
            RootCommander.exec("echo $temp > /sys/class/thermal/thermal_zone$zoneId/trip_point_0_temp")
            LogRepository.appendLog(LogLevel.INFO, "GpuOptimizer", "温度触发点已设置为 ${temp}mC")
            true
        } catch (e: Exception) {
            LogRepository.appendLog(LogLevel.ERROR, "GpuOptimizer", "设置温度触发点失败: ${e.message}")
            false
        }
    }

    // 设置温度滞后（毫度）
    suspend fun setTripPointHyst(zoneId: Int, hyst: Int): Boolean {
        return try {
            RootCommander.exec("echo $hyst > /sys/class/thermal/thermal_zone$zoneId/trip_point_0_hyst")
            LogRepository.appendLog(LogLevel.INFO, "GpuOptimizer", "温度滞后已设置为 ${hyst}mC")
            true
        } catch (e: Exception) {
            LogRepository.appendLog(LogLevel.ERROR, "GpuOptimizer", "设置温度滞后失败: ${e.message}")
            false
        }
    }

    // 查找 GPU 温度传感器的 thermal_zone
    suspend fun findGpuThermalZone(): Int? {
        return try {
            for (i in 0..50) {
                val type = RootCommander.exec("cat /sys/class/thermal/thermal_zone$i/type 2>/dev/null").trim()
                if (type.contains("gpu", ignoreCase = true)) {
                    LogRepository.appendLog(LogLevel.INFO, "GpuOptimizer", "找到 GPU 温度传感器: thermal_zone$i")
                    return i
                }
            }
            LogRepository.appendLog(LogLevel.WARN, "GpuOptimizer", "未找到 GPU 温度传感器")
            null
        } catch (e: Exception) {
            LogRepository.appendLog(LogLevel.ERROR, "GpuOptimizer", "查找 GPU 温度传感器失败: ${e.message}")
            null
        }
    }

    // 应用性能优化模式
    suspend fun applyPerformanceMode(): Boolean {
        var success = true

        // 启用 GPU 节流
        success = success && setThrottling(true)

        // 启用总线分割
        success = success && setBusSplit(true)

        // 设置空闲定时器为 50ms
        success = success && setIdleTimer(50)

        // 设置最大 GPU 频率为 310MHz
        success = success && setMaxGpuClk(310000000)

        // 设置热功率等级为 5
        success = success && setThermalPwrLevel(5)

        // 设置最小频率为 155MHz
        success = success && setMinFreq(155000000)

        // 设置最大频率为 310MHz
        success = success && setMaxFreq(310000000)

        // 设置温度触发点和滞后
        val gpuZone = findGpuThermalZone()
        if (gpuZone != null) {
            success = success && setTripPointTemp(gpuZone, 55000)  // 55°C
            success = success && setTripPointHyst(gpuZone, 5000)   // 5°C
        }

        LogRepository.appendLog(
            if (success) LogLevel.INFO else LogLevel.ERROR,
            "GpuOptimizer",
            "GPU 性能优化模式${if (success) "应用成功" else "应用失败"}"
        )

        return success
    }

    // 应用节能优化模式
    suspend fun applyPowerSavingMode(): Boolean {
        var success = true

        // 禁用 GPU 节流
        success = success && setThrottling(false)

        // 禁用总线分割
        success = success && setBusSplit(false)

        // 设置空闲定时器为 100ms
        success = success && setIdleTimer(100)

        // 设置最大 GPU 频率为 200MHz
        success = success && setMaxGpuClk(200000000)

        // 设置热功率等级为 8
        success = success && setThermalPwrLevel(8)

        // 设置最小频率为 100MHz
        success = success && setMinFreq(100000000)

        // 设置最大频率为 200MHz
        success = success && setMaxFreq(200000000)

        // 设置温度触发点和滞后
        val gpuZone = findGpuThermalZone()
        if (gpuZone != null) {
            success = success && setTripPointTemp(gpuZone, 45000)  // 45°C
            success = success && setTripPointHyst(gpuZone, 3000)   // 3°C
        }

        LogRepository.appendLog(
            if (success) LogLevel.INFO else LogLevel.ERROR,
            "GpuOptimizer",
            "GPU 节能优化模式${if (success) "应用成功" else "应用失败"}"
        )

        return success
    }

    // 恢复默认设置
    suspend fun applyDefaultMode(): Boolean {
        var success = true

        // 启用 GPU 节流
        success = success && setThrottling(true)

        // 启用总线分割
        success = success && setBusSplit(true)

        // 设置空闲定时器为 50ms
        success = success && setIdleTimer(50)

        // 设置最大 GPU 频率为 310MHz
        success = success && setMaxGpuClk(310000000)

        // 设置热功率等级为 5
        success = success && setThermalPwrLevel(5)

        // 设置最小频率为 155MHz
        success = success && setMinFreq(155000000)

        // 设置最大频率为 310MHz
        success = success && setMaxFreq(310000000)

        // 设置温度触发点和滞后
        val gpuZone = findGpuThermalZone()
        if (gpuZone != null) {
            success = success && setTripPointTemp(gpuZone, 55000)  // 55°C
            success = success && setTripPointHyst(gpuZone, 5000)   // 5°C
        }

        LogRepository.appendLog(
            if (success) LogLevel.INFO else LogLevel.ERROR,
            "GpuOptimizer",
            "GPU 默认设置${if (success) "应用成功" else "应用失败"}"
        )

        return success
    }

    // 获取当前 GPU 频率
    suspend fun getCurrentFreq(): Long {
        return try {
            val result = RootCommander.exec("cat /sys/class/devfreq/kgsl-3d0/cur_freq").trim()
            result.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            LogRepository.appendLog(LogLevel.ERROR, "GpuOptimizer", "获取当前 GPU 频率失败: ${e.message}")
            0L
        }
    }

    // 获取 GPU 温度
    suspend fun getGpuTemperature(): Int {
        return try {
            val gpuZone = findGpuThermalZone()
            if (gpuZone != null) {
                val result = RootCommander.exec("cat /sys/class/thermal/thermal_zone$gpuZone/temp").trim()
                result.toIntOrNull() ?: 0
            } else {
                0
            }
        } catch (e: Exception) {
            LogRepository.appendLog(LogLevel.ERROR, "GpuOptimizer", "获取 GPU 温度失败: ${e.message}")
            0
        }
    }
}
