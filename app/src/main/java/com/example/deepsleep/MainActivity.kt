package com.example.deepsleep

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.deepsleep.data.SettingsRepository
import com.example.deepsleep.service.FreezerService
import com.example.deepsleep.ui.main.MainScreen
import com.example.deepsleep.ui.main.MainViewModel
import com.example.deepsleep.ui.settings.SettingsScreen
import com.example.deepsleep.ui.logs.LogsScreen
import com.example.deepsleep.ui.stats.StatsScreen
import com.example.deepsleep.ui.theme.DeepSleepTheme
import com.example.deepsleep.root.RootCommander
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化 SettingsRepository
        SettingsRepository.initialize(this)

        // 请求通知权限（Android 13+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        // 主动请求 root 授权
        lifecycleScope.launch {
            RootCommander.requestRootAccess()
            viewModel.refreshRootStatus()
        }

        setContent {
            DeepSleepTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "main"
                    ) {
                        composable("main") {
                            MainScreen(
                                onNavigateToLogs = { navController.navigate("logs") },
                                onNavigateToWhitelist = { navController.navigate("whitelist") },
                                onNavigateToSettings = { navController.navigate("settings") },
                                onNavigateToStats = { navController.navigate("stats") },
                                viewModel = viewModel
                            )
                        }
                        composable("settings") {
                            SettingsScreen(onNavigateBack = { navController.popBackStack() })
                        }
                        composable("logs") {
                            LogsScreen(onNavigateBack = { navController.popBackStack() })
                        }
                        composable("whitelist") {
                            WhitelistScreen(onNavigateBack = { navController.popBackStack() })
                        }
                        composable("stats") {
                            StatsScreen(onNavigateBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            viewModel.refreshRootStatus()
        }
    }
}
