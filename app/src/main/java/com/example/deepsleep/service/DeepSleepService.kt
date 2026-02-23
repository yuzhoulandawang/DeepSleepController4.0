package com.example.deepsleep.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.deepsleep.MainActivity
import com.example.deepsleep.R
import com.example.deepsleep.data.LogRepository
import com.example.deepsleep.data.SettingsRepository
import com.example.deepsleep.data.StatsRepository
import com.example.deepsleep.model.AppSettings
import com.example.deepsleep.model.LogLevel
import com.example.deepsleep.root.OptimizationManager
import com.example.deepsleep.root.ProcessSuppressor
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class DeepSleepService : Service() {

    companion object {
        private const val TAG = "DeepSleepService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "deepsleep_service"
        private const val CHANNEL_NAME = "DeepSleep Controller"

        const val ACTION_START = "com.example.deepsleep.ACTION_START"
        const val ACTION_STOP = "com.example.deepsleep.ACTION_STOP"
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isRunning = false
    private var optimizationJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.i(TAG, "Service created")
        LogRepository.appendLog(LogLevel.INFO, TAG, "DeepSleep service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: action=${intent?.action}")

        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
        }

        if (!isRunning) {
            startAsForeground()
            isRunning = true
            startOptimizationLoop()
            LogRepository.appendLog(LogLevel.INFO, TAG, "DeepSleep service started")
        }

        return START_STICKY
    }

    private fun startAsForeground() {
        val notification = createNotification("DeepSleep 控制器运行中")
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "DeepSleep 后台优化服务"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("DeepSleep 控制器")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun startOptimizationLoop() {
        optimizationJob = serviceScope.launch {
            Log.i(TAG, "Starting optimization loop")

            while (isActive) {
                try {
                    val settings = SettingsRepository.settings.first()

                    if (settings.backgroundOptimizationEnabled) {
                        applyBackgroundOptimization(settings)
                    }

                    if (settings.gpuOptimizationEnabled) {
                        applyGpuOptimization(settings)
                    }

                    if (settings.processSuppressEnabled) {
                        applyProcessSuppression(settings)
                    }

                    if (settings.cpuBindEnabled) {
                        applyCpuBinding(settings)
                    }

                    delay(60000)
                } catch (e: CancellationException) {
                    Log.i(TAG, "Optimization loop cancelled")
                    break
                } catch (e: Exception) {
                    Log.e(TAG, "Optimization loop error", e)
                    LogRepository.appendLog(LogLevel.ERROR, TAG, "Optimization loop error: ${e.message}")
                    delay(10000)
                }
            }
        }
    }

    private suspend fun applyBackgroundOptimization(settings: AppSettings) {
        if (settings.appSuspendEnabled) {
            Log.d(TAG, "App suspend enabled")
        }
        if (settings.backgroundRestrictEnabled) {
            val count = ProcessSuppressor.suppressBackgroundApps(settings.suppressScore)
            Log.d(TAG, "Background restrict: suppressed $count apps")
        }
    }

    private suspend fun applyGpuOptimization(settings: AppSettings) {
        val mode = when (settings.gpuMode) {
            "performance" -> OptimizationManager.PerformanceMode.PERFORMANCE
            "power_saving" -> OptimizationManager.PerformanceMode.STANDBY
            else -> OptimizationManager.PerformanceMode.DAILY
        }
        val success = OptimizationManager.applyAllOptimizations(mode)
        Log.d(TAG, "GPU optimization applied: ${settings.gpuMode}")

        if (success) {
            StatsRepository.recordGpuOptimization()
        }
    }

    private suspend fun applyProcessSuppression(settings: AppSettings) {
        val count = ProcessSuppressor.suppressBackgroundApps(settings.suppressScore)
        Log.d(TAG, "Process suppression: $count apps")

        if (count > 0) {
            StatsRepository.recordAppSuppressed(count)
        }
    }

    private suspend fun applyCpuBinding(settings: AppSettings) {
        val mode = when (settings.cpuMode) {
            "performance" -> OptimizationManager.PerformanceMode.PERFORMANCE
            "standby" -> OptimizationManager.PerformanceMode.STANDBY
            else -> OptimizationManager.PerformanceMode.DAILY
        }
        val success = OptimizationManager.applyAllOptimizations(mode)
        Log.d(TAG, "CPU binding applied: ${settings.cpuMode}")

        if (success) {
            StatsRepository.recordCpuBinding()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.i(TAG, "Service destroying")

        optimizationJob?.cancel()
        serviceScope.cancel()

        isRunning = false

        LogRepository.appendLog(LogLevel.INFO, TAG, "DeepSleep service destroyed")
        super.onDestroy()
    }
}