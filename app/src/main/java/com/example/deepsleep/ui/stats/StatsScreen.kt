package com.example.deepsleep.ui.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.deepsleep.model.Statistics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatsViewModel = viewModel()
) {
    val statistics by viewModel.statistics.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("统计数据") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshStatistics() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StatsCard(
                    title = "📊 优化概览",
                    icon = Icons.Default.Analytics
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatRow(
                            label = "总运行时长",
                            value = formatDuration(statistics.totalRuntime),
                            icon = Icons.Default.AccessTime
                        )
                        StatRow(
                            label = "优化次数",
                            value = "${statistics.totalOptimizations}",
                            icon = Icons.Default.Bolt
                        )
                        StatRow(
                            label = "节省电量",
                            value = "${statistics.powerSaved} mAh",
                            icon = Icons.Default.BatteryChargingFull
                        )
                        StatRow(
                            label = "释放内存",
                            value = "${statistics.memoryReleased} MB",
                            icon = Icons.Default.Memory
                        )
                    }
                }
            }

            item {
                StatsCard(
                    title = "🎮 GPU 优化",
                    icon = Icons.Default.Games
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatRow(
                            label = "GPU 优化次数",
                            value = "${statistics.gpuOptimizations}",
                            icon = Icons.Default.Speed
                        )
                        StatRow(
                            label = "平均 GPU 频率",
                            value = "${statistics.avgGpuFreq / 1000000} MHz",
                            icon = Icons.Default.TrendingUp
                        )
                        StatRow(
                            label = "GPU 节流次数",
                            value = "${statistics.gpuThrottlingCount}",
                            icon = Icons.Default.Thermostat
                        )
                        StatRow(
                            label = "当前 GPU 模式",
                            value = getGpuModeName(statistics.currentGpuMode),
                            icon = Icons.Default.Tune
                        )
                    }
                }
            }

            item {
                StatsCard(
                    title = "🖥️ CPU 优化",
                    icon = Icons.Default.Memory
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatRow(
                            label = "CPU 绑定次数",
                            value = "${statistics.cpuBindingCount}",
                            icon = Icons.Default.Memory   // 修复：将 Cpu 改为 Memory
                        )
                        StatRow(
                            label = "当前 CPU 模式",
                            value = getCpuModeName(statistics.currentCpuMode),
                            icon = Icons.Default.Tune
                        )
                        StatRow(
                            label = "CPU 使用率优化",
                            value = "${statistics.cpuUsageOptimized}%",
                            icon = Icons.Default.TrendingDown
                        )
                    }
                }
            }

            item {
                StatsCard(
                    title = "🔧 进程压制",
                    icon = Icons.Default.Settings
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatRow(
                            label = "压制应用总数",
                            value = "${statistics.suppressedApps}",
                            icon = Icons.Default.Block
                        )
                        StatRow(
                            label = "释放进程数",
                            value = "${statistics.killedProcesses}",
                            icon = Icons.Default.DeleteForever
                        )
                        StatRow(
                            label = "OOM 调整次数",
                            value = "${statistics.oomAdjustments}",
                            icon = Icons.Default.SwapVert
                        )
                        StatRow(
                            label = "平均 OOM 评分",
                            value = "${statistics.avgOomScore}",
                            icon = Icons.Default.ShowChart
                        )
                    }
                }
            }

            item {
                StatsCard(
                    title = "❄️ 应用冻结",
                    icon = Icons.Default.AcUnit
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatRow(
                            label = "冻结应用总数",
                            value = "${statistics.frozenApps}",
                            icon = Icons.Default.AcUnit
                        )
                        StatRow(
                            label = "解冻应用总数",
                            value = "${statistics.thawedApps}",
                            icon = Icons.Default.Restore
                        )
                        StatRow(
                            label = "平均冻结时长",
                            value = formatDuration(statistics.avgFreezeTime),
                            icon = Icons.Default.Timer
                        )
                        StatRow(
                            label = "阻止冻结次数",
                            value = "${statistics.preventedFreezes}",
                            icon = Icons.Default.Shield
                        )
                    }
                }
            }

            item {
                StatsCard(
                    title = "🎯 场景检测",
                    icon = Icons.Default.Radar
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatRow(
                            label = "游戏场景",
                            value = "${statistics.gameSceneCount}",
                            icon = Icons.Default.SportsEsports
                        )
                        StatRow(
                            label = "导航场景",
                            value = "${statistics.navigationSceneCount}",
                            icon = Icons.Default.Navigation
                        )
                        StatRow(
                            label = "充电场景",
                            value = "${statistics.chargingSceneCount}",
                            icon = Icons.Default.BatteryChargingFull
                        )
                        StatRow(
                            label = "通话场景",
                            value = "${statistics.callSceneCount}",
                            icon = Icons.Default.Phone
                        )
                        StatRow(
                            label = "投屏场景",
                            value = "${statistics.castSceneCount}",
                            icon = Icons.Default.Cast
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun StatRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

// 辅助函数
fun formatDuration(millis: Long): String {
    val seconds = millis / 1000
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m ${secs}s"
        else -> "${secs}s"
    }
}

fun getGpuModeName(mode: String): String {
    return when (mode) {
        "performance" -> "性能模式"
        "power_saving" -> "节能模式"
        "daily" -> "日常模式"
        else -> "默认"
    }
}

fun getCpuModeName(mode: String): String {
    return when (mode) {
        "performance" -> "性能模式"
        "standby" -> "待机模式"
        "daily" -> "日常模式"
        else -> "默认"
    }
}