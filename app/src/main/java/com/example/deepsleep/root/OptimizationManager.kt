package com.example.deepsleep.root

import android.util.Log
import com.example.deepsleep.data.LogRepository
import com.example.deepsleep.model.LogLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 优化管理器 - 单例对象
 * 统一管理 CPU 和 GPU 优化策略
 */
object OptimizationManager {

    private const val TAG = "OptimizationManager"

    enum class PerformanceMode {
        PERFORMANCE, DAILY, STANDBY
    }

    private suspend fun executeShellCommand(command: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            val success = process.waitFor() == 0
            if (!success) {
                val error = process.errorStream.bufferedReader().readText()
                Log.e(TAG, "Command failed: $command, error: $error")
            }
            return@withContext success
        } catch (e: Exception) {
            Log.e(TAG, "Exception: $command", e)
            return@withContext false
        }
    }

    private suspend fun writeToFile(path: String, value: Any): Boolean {
        return executeShellCommand("echo $value > $path")
    }

    private suspend fun fileExists(path: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext File(path).exists()
    }

    private suspend fun applyCpuSet(mode: PerformanceMode): Boolean {
        val cpusetBase = "/dev/cpuset"
        if (!fileExists(cpusetBase)) {
            Log.w(TAG, "cpuset directory not found")
            return false
        }

        val (topApp, foreground, background, systemBackground) = when (mode) {
            PerformanceMode.PERFORMANCE -> arrayOf("0-7", "2-7", "0-1", "0-1")
            PerformanceMode.DAILY -> arrayOf("0-7", "0-6", "0-1", "0-3")
            PerformanceMode.STANDBY -> arrayOf("0-3", "0-1", "0-1", "0-1")
        }

        var allSuccess = true
        allSuccess = allSuccess && writeToFile("$cpusetBase/top-app/cpus", topApp)
        allSuccess = allSuccess && writeToFile("$cpusetBase/foreground/cpus", foreground)
        allSuccess = allSuccess && writeToFile("$cpusetBase/background/cpus", background)
        allSuccess = allSuccess && writeToFile("$cpusetBase/system-background/cpus", systemBackground)

        val cpuExclusive = if (mode == PerformanceMode.PERFORMANCE || mode == PerformanceMode.DAILY) "1" else "0"
        writeToFile("$cpusetBase/background/cpu_exclusive", cpuExclusive)
        writeToFile("$cpusetBase/background/sched_load_balance", "0")
        writeToFile("$cpusetBase/system-background/sched_load_balance", "0")
        writeToFile("$cpusetBase/top-app/sched_load_balance", "1")

        return allSuccess
    }

    private suspend fun getAvailableGpuFreqs(): List<Long> {
        val freqFile = "/sys/class/kgsl/kgsl-3d0/gpu_available_frequencies"
        return try {
            File(freqFile).readText().trim().split(" ").mapNotNull { it.toLongOrNull() }
        } catch (e: Exception) {
            listOf(903000000, 834000000, 770000000, 720000000, 680000000,
                629000000, 578000000, 500000000, 422000000, 310000000, 231000000)
        }
    }

    private fun selectClosestFreq(target: Long, available: List<Long>): Long {
        return available.minByOrNull { kotlin.math.abs(it - target) } ?: target
    }

    private suspend fun applyGpuOptimization(mode: PerformanceMode): Boolean {
        val gpuBase = "/sys/class/kgsl/kgsl-3d0"
        if (!fileExists(gpuBase)) {
            Log.w(TAG, "GPU directory not found")
            return false
        }

        val availableFreqs = getAvailableGpuFreqs()
        if (availableFreqs.isEmpty()) {
            Log.e(TAG, "No available GPU frequencies")
            return false
        }

        val (targetMax, targetMin) = when (mode) {
            PerformanceMode.PERFORMANCE -> Pair(903000000L, 578000000L)
            PerformanceMode.DAILY -> Pair(770000000L, 310000000L)
            PerformanceMode.STANDBY -> Pair(500000000L, 231000000L)
        }

        val maxFreq = selectClosestFreq(targetMax, availableFreqs)
        val minFreq = selectClosestFreq(targetMin, availableFreqs)

        var allSuccess = true

        // 修复：分别赋值，避免解构超长
        val params = when (mode) {
            PerformanceMode.PERFORMANCE -> arrayOf(0, 0, 1, 1, 1, 1, 10, 0)
            PerformanceMode.DAILY -> arrayOf(1, 1, 0, 0, 0, 0, 50, 5)
            PerformanceMode.STANDBY -> arrayOf(1, 1, 0, 0, 0, 0, 100, 8)
        }
        val throttling = params[0]
        val busSplit = params[1]
        val forceClkOn = params[2]
        val forceRailOn = params[3]
        val forceNoNap = params[4]
        val forceBusOn = params[5]
        val idleTimer = params[6]
        val thermalPwrlevel = params[7]

        allSuccess = allSuccess and writeToFile("$gpuBase/throttling", throttling)
        allSuccess = allSuccess and writeToFile("$gpuBase/bus_split", busSplit)
        allSuccess = allSuccess and writeToFile("$gpuBase/force_clk_on", forceClkOn)
        allSuccess = allSuccess and writeToFile("$gpuBase/force_rail_on", forceRailOn)
        allSuccess = allSuccess and writeToFile("$gpuBase/force_no_nap", forceNoNap)
        allSuccess = allSuccess and writeToFile("$gpuBase/force_bus_on", forceBusOn)
        allSuccess = allSuccess and writeToFile("$gpuBase/idle_timer", idleTimer)
        allSuccess = allSuccess and writeToFile("$gpuBase/max_gpuclk", maxFreq)
        allSuccess = allSuccess and writeToFile("$gpuBase/thermal_pwrlevel", thermalPwrlevel)

        val devfreqBase = "/sys/class/devfreq"
        File(devfreqBase).listFiles { file -> file.isDirectory && file.name.contains("kgsl-3d0") }?.forEach { dir ->
            allSuccess = allSuccess and writeToFile("${dir.absolutePath}/min_freq", minFreq)
            allSuccess = allSuccess and writeToFile("${dir.absolutePath}/max_freq", maxFreq)
        }

        return allSuccess
    }

    /**
     * 应用所有优化策略
     */
    suspend fun applyAllOptimizations(mode: PerformanceMode): Boolean {
        Log.i(TAG, "Applying optimizations for mode: $mode")

        val cpuSuccess = applyCpuSet(mode)
        val gpuSuccess = applyGpuOptimization(mode)

        val success = cpuSuccess && gpuSuccess

        Log.i(TAG, "Optimizations applied: cpu=$cpuSuccess, gpu=$gpuSuccess, overall=$success")

        LogRepository.appendLog(
            if (success) LogLevel.INFO else LogLevel.WARNING,
            TAG,
            "Optimizations applied for mode: $mode (cpu=$cpuSuccess, gpu=$gpuSuccess)"
        )

        return success
    }
}