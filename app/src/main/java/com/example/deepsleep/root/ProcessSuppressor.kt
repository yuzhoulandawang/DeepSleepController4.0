package com.example.deepsleep.root

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 进程压制器 - 单例对象
 * 通过调整 OOM 评分来管理后台进程
 */
object ProcessSuppressor {
    
    private const val TAG = "ProcessSuppressor"

    /**
     * 设置进程的 OOM 评分
     * @param packageName 包名
     * @param score OOM 评分 (-1000 到 1000)
     *  -1000: 最容易被杀死
     *  0: 正常优先级
     *  1000: 最难被杀死
     */
    suspend fun setOomScore(packageName: String, score: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val pids = getPackagePids(packageName)
            if (pids.isEmpty()) {
                Log.w(TAG, "No PIDs found for $packageName")
                return@withContext false
            }

            var successCount = 0
            pids.forEach { pid ->
                val oomFile = File("/proc/$pid/oom_score_adj")
                if (oomFile.exists()) {
                    oomFile.writeText(score.toString())
                    successCount++
                    Log.d(TAG, "Set OOM score $score for PID $pid ($packageName)")
                }
            }

            val success = successCount > 0
            if (success) {
                LogRepository.info(TAG, "Set OOM score $score for $packageName ($successCount/$pids.size PIDs)")
            }
            return@withContext success
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set OOM score for $packageName", e)
            LogRepository.error(TAG, "Failed to set OOM score for $packageName: ${e.message}")
            return@withContext false
        }
    }

    /**
     * 压制后台应用 - 设置较低的 OOM 评分
     */
    suspend fun suppressBackgroundApps(score: Int = 500): Int = withContext(Dispatchers.IO) {
        try {
            val suppressedCount = mutableListOf<String>()

            // 获取所有用户应用进程
            File("/proc").listFiles { file ->
                file.isDirectory && file.name.matches(Regex("\\d+"))
            }?.forEach { procDir ->
                val pid = procDir.name.toIntOrNull() ?: return@forEach
                val cmdlineFile = File(procDir, "cmdline")
                
                if (cmdlineFile.exists()) {
                    val cmdline = cmdlineFile.readText().trim().substringBefore(" ")
                    // 跳过系统应用和关键服务
                    if (cmdline.isNotBlank() && 
                        !cmdline.startsWith("com.android") &&
                        !cmdline.contains("deepsleep") &&
                        !cmdline.contains("systemui")) {
                        
                        val oomFile = File(procDir, "oom_score_adj")
                        if (oomFile.exists()) {
                            val currentScore = oomFile.readText().toIntOrNull() ?: 0
                            // 只压制评分高于目标值的进程
                            if (currentScore > score) {
                                oomFile.writeText(score.toString())
                                suppressedCount.add(cmdline)
                            }
                        }
                    }
                }
            }

            LogRepository.info(TAG, "Suppressed ${suppressedCount.size} background apps with OOM score $score")
            return@withContext suppressedCount.size
        } catch (e: Exception) {
            Log.e(TAG, "Failed to suppress background apps", e)
            LogRepository.error(TAG, "Failed to suppress background apps: ${e.message}")
            return@withContext 0
        }
    }

    /**
     * 获取包名的所有 PID
     */
    private fun getPackagePids(packageName: String): List<Int> {
        val pids = mutableListOf<Int>()
        
        try {
            File("/proc").listFiles { file ->
                file.isDirectory && file.name.matches(Regex("\\d+"))
            }?.forEach { procDir ->
                val pid = procDir.name.toIntOrNull() ?: return@forEach
                val cmdlineFile = File(procDir, "cmdline")
                
                if (cmdlineFile.exists()) {
                    val cmdline = cmdlineFile.readText().trim().substringBefore(" ")
                    if (cmdline == packageName) {
                        pids.add(pid)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get PIDs for $packageName", e)
        }
        
        return pids
    }

    /**
     * 重置 OOM 评分到默认值
     */
    suspend fun resetOomScore(packageName: String): Boolean {
        return setOomScore(packageName, 0)
    }
}
