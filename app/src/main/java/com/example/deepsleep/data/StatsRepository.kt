package com.example.deepsleep.data

import android.util.Log
import com.example.deepsleep.model.Statistics
import com.example.deepsleep.root.RootCommander
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 统计数据仓库 - 单例对象
 * 管理应用运行统计数据的持久化和查询
 */
object StatsRepository {
    
    private const val TAG = "StatsRepository"
    
    private val statsPath = "/data/local/tmp/deep_sleep_logs/stats.txt"
    private val statsMutex = Mutex()
    
    // 内存缓存
    private val _statistics = MutableStateFlow(Statistics())
    val statistics: StateFlow<Statistics> = _statistics.asStateFlow()
    
    // 内部计数器
    private var _totalRuntime = 0L
    private var _totalOptimizations = 0
    private var _powerSaved = 0L
    private var _memoryReleased = 0L
    private var _gpuOptimizations = 0
    private var _avgGpuFreq = 770000000L
    private var _gpuThrottlingCount = 0
    private var _currentGpuMode = "daily"
    private var _cpuBindingCount = 0
    private var _currentCpuMode = "daily"
    private var _cpuUsageOptimized = 0
    private var _suppressedApps = 0
    private var _killedProcesses = 0
    private var _oomAdjustments = 0
    private var _avgOomScore = 500
    private var _frozenApps = 0
    private var _thawedApps = 0
    private var _avgFreezeTime = 0L
    private var _preventedFreezes = 0
    private var _gameSceneCount = 0
    private var _navigationSceneCount = 0
    private var _chargingSceneCount = 0
    private var _callSceneCount = 0
    private var _castSceneCount = 0
    private val _recentActivities = mutableListOf<String>()
    
    private var serviceStartTime = System.currentTimeMillis()
    
    /**
     * 从文件加载统计数据
     */
    suspend fun loadStats(): Statistics = withContext(Dispatchers.IO) {
        val content = RootCommander.readFile(statsPath)
        
        if (content == null) {
            return@withContext Statistics()
        }
        
        val map = content.lineSequence()
            .filter { it.contains("=") }
            .associate { 
                val parts = it.split("=", limit = 2)
                parts[0].trim() to parts[1].trim()
            }
        
        Statistics(
            totalRuntime = map["TOTAL_RUNTIME"]?.toLongOrNull() ?: 0L,
            totalOptimizations = map["TOTAL_OPTIMIZATIONS"]?.toIntOrNull() ?: 0,
            powerSaved = map["POWER_SAVED"]?.toLongOrNull() ?: 0L,
            memoryReleased = map["MEMORY_RELEASED"]?.toLongOrNull() ?: 0L,
            gpuOptimizations = map["GPU_OPTIMIZATIONS"]?.toIntOrNull() ?: 0,
            avgGpuFreq = map["AVG_GPU_FREQ"]?.toLongOrNull() ?: 770000000L,
            gpuThrottlingCount = map["GPU_THROTTLING_COUNT"]?.toIntOrNull() ?: 0,
            currentGpuMode = map["CURRENT_GPU_MODE"] ?: "daily",
            cpuBindingCount = map["CPU_BINDING_COUNT"]?.toIntOrNull() ?: 0,
            currentCpuMode = map["CURRENT_CPU_MODE"] ?: "daily",
            cpuUsageOptimized = map["CPU_USAGE_OPTIMIZED"]?.toIntOrNull() ?: 0,
            suppressedApps = map["SUPPRESSED_APPS"]?.toIntOrNull() ?: 0,
            killedProcesses = map["KILLED_PROCESSES"]?.toIntOrNull() ?: 0,
            oomAdjustments = map["OOM_ADJUSTMENTS"]?.toIntOrNull() ?: 0,
            avgOomScore = map["AVG_OOM_SCORE"]?.toIntOrNull() ?: 500,
            frozenApps = map["FROZEN_APPS"]?.toIntOrNull() ?: 0,
            thawedApps = map["THAWED_APPS"]?.toIntOrNull() ?: 0,
            avgFreezeTime = map["AVG_FREEZE_TIME"]?.toLongOrNull() ?: 0L,
            preventedFreezes = map["PREVENTED_FREEZES"]?.toIntOrNull() ?: 0,
            gameSceneCount = map["GAME_SCENE_COUNT"]?.toIntOrNull() ?: 0,
            navigationSceneCount = map["NAVIGATION_SCENE_COUNT"]?.toIntOrNull() ?: 0,
            chargingSceneCount = map["CHARGING_SCENE_COUNT"]?.toIntOrNull() ?: 0,
            callSceneCount = map["CALL_SCENE_COUNT"]?.toIntOrNull() ?: 0,
            castSceneCount = map["CAST_SCENE_COUNT"]?.toIntOrNull() ?: 0,
            recentActivities = map["RECENT_ACTIVITIES"]?.split("|") ?: emptyList()
        )
    }
    
    /**
     * 保存统计数据到文件
     */
    suspend fun saveStats(stats: Statistics) = statsMutex.withLock {
        withContext(Dispatchers.IO) {
            val content = buildString {
                appendLine("TOTAL_RUNTIME=${stats.totalRuntime}")
                appendLine("TOTAL_OPTIMIZATIONS=${stats.totalOptimizations}")
                appendLine("POWER_SAVED=${stats.powerSaved}")
                appendLine("MEMORY_RELEASED=${stats.memoryReleased}")
                appendLine("GPU_OPTIMIZATIONS=${stats.gpuOptimizations}")
                appendLine("AVG_GPU_FREQ=${stats.avgGpuFreq}")
                appendLine("GPU_THROTTLING_COUNT=${stats.gpuThrottlingCount}")
                appendLine("CURRENT_GPU_MODE=${stats.currentGpuMode}")
                appendLine("CPU_BINDING_COUNT=${stats.cpuBindingCount}")
                appendLine("CURRENT_CPU_MODE=${stats.currentCpuMode}")
                appendLine("CPU_USAGE_OPTIMIZED=${stats.cpuUsageOptimized}")
                appendLine("SUPPRESSED_APPS=${stats.suppressedApps}")
                appendLine("KILLED_PROCESSES=${stats.killedProcesses}")
                appendLine("OOM_ADJUSTMENTS=${stats.oomAdjustments}")
                appendLine("AVG_OOM_SCORE=${stats.avgOomScore}")
                appendLine("FROZEN_APPS=${stats.frozenApps}")
                appendLine("THAWED_APPS=${stats.thawedApps}")
                appendLine("AVG_FREEZE_TIME=${stats.avgFreezeTime}")
                appendLine("PREVENTED_FREEZES=${stats.preventedFreezes}")
                appendLine("GAME_SCENE_COUNT=${stats.gameSceneCount}")
                appendLine("NAVIGATION_SCENE_COUNT=${stats.navigationSceneCount}")
                appendLine("CHARGING_SCENE_COUNT=${stats.chargingSceneCount}")
                appendLine("CALL_SCENE_COUNT=${stats.callSceneCount}")
                appendLine("CAST_SCENE_COUNT=${stats.castSceneCount}")
                appendLine("RECENT_ACTIVITIES=${stats.recentActivities.joinToString("|")}")
            }
            
            RootCommander.exec("mkdir -p /data/local/tmp/deep_sleep_logs")
            RootCommander.exec("printf '%s\\n' \"$content\" > $statsPath")
        }
    }
    
    // ========== 统计数据获取方法 ==========
    
    suspend fun getTotalRuntime(): Long = withContext(Dispatchers.IO) {
        return@withContext System.currentTimeMillis() - serviceStartTime
    }
    
    suspend fun getTotalOptimizations(): Int = withContext(Dispatchers.IO) {
        return@withContext _totalOptimizations
    }
    
    suspend fun getPowerSaved(): Long = withContext(Dispatchers.IO) {
        return@withContext _powerSaved
    }
    
    suspend fun getMemoryReleased(): Long = withContext(Dispatchers.IO) {
        return@withContext _memoryReleased
    }
    
    suspend fun getGpuOptimizations(): Int = withContext(Dispatchers.IO) {
        return@withContext _gpuOptimizations
    }
    
    suspend fun getAvgGpuFreq(): Long = withContext(Dispatchers.IO) {
        val freqFile = "/sys/class/kgsl/kgsl-3d0/devfreq/kgsl-3d0/cur_freq"
        try {
            val freq = File(freqFile).readText().trim().toLongOrNull() ?: 770000000L
            _avgGpuFreq = (_avgGpuFreq * 0.9 + freq * 0.1).toLong()
        } catch (e: Exception) {
            LogRepository.error(TAG, "Failed to read GPU frequency")
        }
        return@withContext _avgGpuFreq
    }
    
    suspend fun getGpuThrottlingCount(): Int = withContext(Dispatchers.IO) {
        return@withContext _gpuThrottlingCount
    }
    
    suspend fun getCurrentGpuMode(): String = withContext(Dispatchers.IO) {
        return@withContext _currentGpuMode
    }
    
    suspend fun getCpuBindingCount(): Int = withContext(Dispatchers.IO) {
        return@withContext _cpuBindingCount
    }
    
    suspend fun getCurrentCpuMode(): String = withContext(Dispatchers.IO) {
        return@withContext _currentCpuMode
    }
    
    suspend fun getCpuUsageOptimized(): Int = withContext(Dispatchers.IO) {
        return@withContext _cpuUsageOptimized
    }
    
    suspend fun getSuppressedApps(): Int = withContext(Dispatchers.IO) {
        return@withContext _suppressedApps
    }
    
    suspend fun getKilledProcesses(): Int = withContext(Dispatchers.IO) {
        return@withContext _killedProcesses
    }
    
    suspend fun getOomAdjustments(): Int = withContext(Dispatchers.IO) {
        return@withContext _oomAdjustments
    }
    
    suspend fun getAvgOomScore(): Int = withContext(Dispatchers.IO) {
        return@withContext _avgOomScore
    }
    
    suspend fun getFrozenApps(): Int = withContext(Dispatchers.IO) {
        return@withContext _frozenApps
    }
    
    suspend fun getThawedApps(): Int = withContext(Dispatchers.IO) {
        return@withContext _thawedApps
    }
    
    suspend fun getAvgFreezeTime(): Long = withContext(Dispatchers.IO) {
        return@withContext _avgFreezeTime
    }
    
    suspend fun getPreventedFreezes(): Int = withContext(Dispatchers.IO) {
        return@withContext _preventedFreezes
    }
    
    suspend fun getGameSceneCount(): Int = withContext(Dispatchers.IO) {
        return@withContext _gameSceneCount
    }
    
    suspend fun getNavigationSceneCount(): Int = withContext(Dispatchers.IO) {
        return@withContext _navigationSceneCount
    }
    
    suspend fun getChargingSceneCount(): Int = withContext(Dispatchers.IO) {
        return@withContext _chargingSceneCount
    }
    
    suspend fun getCallSceneCount(): Int = withContext(Dispatchers.IO) {
        return@withContext _callSceneCount
    }
    
    suspend fun getCastSceneCount(): Int = withContext(Dispatchers.IO) {
        return@withContext _castSceneCount
    }
    
    suspend fun getRecentActivities(): List<String> = withContext(Dispatchers.IO) {
        return@withContext _recentActivities.toList()
    }
    
    // ========== 统计数据记录方法 ==========
    
    suspend fun recordGpuOptimization() = withContext(Dispatchers.IO) {
        _gpuOptimizations++
        _totalOptimizations++
        Log.d(TAG, "Recorded GPU optimization: total=$_totalOptimizations")
    }
    
    suspend fun recordAppSuppressed(count: Int) = withContext(Dispatchers.IO) {
        _suppressedApps += count
        _oomAdjustments++
        _totalOptimizations++
        Log.d(TAG, "Recorded app suppression: count=$count, total=$_suppressedApps")
    }
    
    suspend fun recordCpuBinding() = withContext(Dispatchers.IO) {
        _cpuBindingCount++
        _totalOptimizations++
        Log.d(TAG, "Recorded CPU binding: total=$_cpuBindingCount")
    }
    
    suspend fun recordFrozenApp() = withContext(Dispatchers.IO) {
        _frozenApps++
        Log.d(TAG, "Recorded frozen app: total=$_frozenApps")
    }
    
    suspend fun recordThawedApp() = withContext(Dispatchers.IO) {
        _thawedApps++
        Log.d(TAG, "Recorded thawed app: total=$_thawedApps")
    }
    
    suspend fun recordSceneDetected(sceneType: String) = withContext(Dispatchers.IO) {
        when (sceneType) {
            "game" -> _gameSceneCount++
            "navigation" -> _navigationSceneCount++
            "charging" -> _chargingSceneCount++
            "call" -> _callSceneCount++
            "cast" -> _castSceneCount++
        }
        Log.d(TAG, "Recorded scene: $sceneType")
    }
    
    /**
     * 重置所有统计数据
     */
    suspend fun resetStats() = statsMutex.withLock {
        withContext(Dispatchers.IO) {
            _totalRuntime = 0L
            _totalOptimizations = 0
            _powerSaved = 0L
            _memoryReleased = 0L
            _gpuOptimizations = 0
            _gpuThrottlingCount = 0
            _cpuBindingCount = 0
            _suppressedApps = 0
            _killedProcesses = 0
            _oomAdjustments = 0
            _frozenApps = 0
            _thawedApps = 0
            _preventedFreezes = 0
            _gameSceneCount = 0
            _navigationSceneCount = 0
            _chargingSceneCount = 0
            _callSceneCount = 0
            _castSceneCount = 0
            _recentActivities.clear()
            serviceStartTime = System.currentTimeMillis()
            
            _statistics.value = Statistics()
            
            LogRepository.info(TAG, "Statistics reset")
        }
    }
}
