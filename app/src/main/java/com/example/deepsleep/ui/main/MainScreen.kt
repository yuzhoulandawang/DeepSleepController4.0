package com.example.deepsleep.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.deepsleep.model.AppSettings

/**
 * 主页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToLogs: () -> Unit,
    onNavigateToWhitelist: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToStats: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DeepSleep 控制器") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 总体状态卡片
            StatusCard(settings, viewModel)
            
            // 深度睡眠控制
            DeepSleepControlSection(settings, viewModel)
            
            // 后台优化
            BackgroundOptimizationSection(settings, viewModel)
            
            // 白名单管理
            WhitelistSection(settings, viewModel, onNavigateToWhitelist)
            
            // GPU 优化
            GpuOptimizationSection(settings, viewModel)
            
            // 电池优化
            BatteryOptimizationSection(settings, viewModel)
            
            // CPU 优化
            CpuOptimizationSection(settings, viewModel)
            
            // Freezer 配置
            FreezerSection(settings, viewModel)
            
            // 场景检测配置
            SceneCheckSection(settings, viewModel)
            
            // 统计数据入口
            ClickableItem(
                title = "统计数据",
                subtitle = "查看优化效果统计",
                icon = Icons.Default.BarChart,
                onClick = onNavigateToStats
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 日志入口
            ClickableItem(
                title = "日志",
                subtitle = "查看应用运行日志",
                icon = Icons.Default.EventNote,
                onClick = onNavigateToLogs
            )
        }
    }
}

@Composable
fun StatusCard(
    settings: AppSettings,
    viewModel: MainViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (settings.rootGranted) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (settings.rootGranted) "Root 权限已获取" else "未获取 Root 权限",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if (settings.serviceRunning) "服务运行中" else "服务未运行",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (settings.rootGranted) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (settings.rootGranted) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun DeepSleepControlSection(
    settings: AppSettings,
    viewModel: MainViewModel
) {
    SectionCard(
        title = "💤 深度睡眠控制",
        icon = Icons.Default.PowerSettingsNew
    ) {
        SwitchItem(
            title = "启用深度睡眠控制",
            subtitle = "控制系统进入深度睡眠模式",
            checked = settings.deepSleepEnabled,
            onCheckedChange = { viewModel.setDeepSleepEnabled(it) }
        )
        
        if (settings.deepSleepEnabled) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            
            SwitchItem(
                title = "抑制唤醒",
                subtitle = "阻止应用唤醒设备",
                checked = settings.wakeupSuppressEnabled,
                onCheckedChange = { viewModel.setWakeupSuppressEnabled(it) }
            )
            
            SwitchItem(
                title = "抑制闹钟",
                subtitle = "阻止非重要闹钟唤醒",
                checked = settings.alarmSuppressEnabled,
                onCheckedChange = { viewModel.setAlarmSuppressEnabled(it) }
            )
        }
    }
}

@Composable
fun BackgroundOptimizationSection(
    settings: AppSettings,
    viewModel: MainViewModel
) {
    SectionCard(
        title = "⚡ 后台优化",
        icon = Icons.Default.FlashOn
    ) {
        SwitchItem(
            title = "启用后台优化",
            subtitle = "优化后台应用行为",
            checked = settings.backgroundOptimizationEnabled,
            onCheckedChange = { viewModel.setBackgroundOptimizationEnabled(it) }
        )
        
        if (settings.backgroundOptimizationEnabled) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            
            SwitchItem(
                title = "应用挂起",
                subtitle = "挂起不活跃的后台应用",
                checked = settings.appSuspendEnabled,
                onCheckedChange = { viewModel.setAppSuspendEnabled(it) }
            )
            
            SwitchItem(
                title = "后台限制",
                subtitle = "限制后台应用资源使用",
                checked = settings.backgroundRestrictEnabled,
                onCheckedChange = { viewModel.setBackgroundRestrictEnabled(it) }
            )
        }
    }
}

@Composable
fun WhitelistSection(
    settings: AppSettings,
    viewModel: MainViewModel,
    onNavigateToWhitelist: () -> Unit
) {
    SectionCard(
        title = "📋 白名单管理",
        icon = Icons.Default.FormatListBulleted
    ) {
        ClickableItem(
            title = "管理白名单",
            subtitle = "选择不受深度睡眠影响的应用",
            icon = Icons.Default.Edit,
            onClick = onNavigateToWhitelist
        )
        
        if (settings.whitelist.isNotEmpty()) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            
            Text(
                text = "已添加 ${settings.whitelist.size} 个应用",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BatteryOptimizationSection(
    settings: AppSettings,
    viewModel: MainViewModel
) {
    SectionCard(
        title = "🔋 电池优化",
        icon = Icons.Default.BatteryChargingFull
    ) {
        SwitchItem(
            title = "启用电池优化",
            subtitle = "优化电池使用效率",
            checked = settings.batteryOptimizationEnabled,
            onCheckedChange = { viewModel.setBatteryOptimizationEnabled(it) }
        )
        
        if (settings.batteryOptimizationEnabled) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            
            SwitchItem(
                title = "省电模式",
                subtitle = "降低功耗以延长续航",
                checked = settings.powerSavingEnabled,
                onCheckedChange = { viewModel.setPowerSavingEnabled(it) }
            )
        }
    }
}

@Composable
fun CpuOptimizationSection(
    settings: AppSettings,
    viewModel: MainViewModel
) {
    var showModeDialog by remember { mutableStateOf(false) }

    SectionCard(
        title = "🖥️ CPU 优化",
        icon = Icons.Default.Memory
    ) {
        SwitchItem(
            title = "启用 CPU 绑定",
            subtitle = "通过 cpuset 控制不同进程组的 CPU 核心分配",
            checked = settings.cpuBindEnabled,
            onCheckedChange = { viewModel.setCpuBindEnabled(it) }
        )
        
        if (settings.cpuBindEnabled) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            
            // CPU 模式选择
            ClickableItem(
                title = "CPU 模式",
                subtitle = "当前: ${getCpuModeDisplayName(settings.cpuMode)}",
                icon = Icons.Default.Tune,
                onClick = { showModeDialog = true }
            )
        }
    }
    
    if (showModeDialog) {
        CpuModeDialog(
            currentMode = settings.cpuMode,
            onDismiss = { showModeDialog = false },
            onModeSelected = { 
                viewModel.setCpuMode(it)
                showModeDialog = false
            }
        )
    }
}

@Composable
fun GpuOptimizationSection(
    settings: AppSettings,
    viewModel: MainViewModel
) {
    var showModeDialog by remember { mutableStateOf(false) }
    
    SectionCard(
        title = "🎮 GPU 优化",
        icon = Icons.Default.VideogameAsset
    ) {
        SwitchItem(
            title = "启用 GPU 优化",
            subtitle = "优化 GPU 性能和功耗",
            checked = settings.gpuOptimizationEnabled,
            onCheckedChange = { viewModel.setGpuOptimizationEnabled(it) }
        )
        
        if (settings.gpuOptimizationEnabled) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            
            // GPU 模式选择
            ClickableItem(
                title = "GPU 模式",
                subtitle = "当前: ${getGpuModeDisplayName(settings.gpuMode)}",
                icon = Icons.Default.Tune,
                onClick = { showModeDialog = true }
            )
        }
    }
    
    if (showModeDialog) {
        GpuModeDialog(
            currentMode = settings.gpuMode,
            onDismiss = { showModeDialog = false },
            onModeSelected = { 
                viewModel.setGpuMode(it)
                showModeDialog = false
            }
        )
    }
}

@Composable
fun FreezerSection(
    settings: AppSettings,
    viewModel: MainViewModel
) {
    SectionCard(
        title = "❄️ Freezer 服务",
        icon = Icons.Default.AcUnit
    ) {
        SwitchItem(
            title = "启用 Freezer",
            subtitle = "冻结不活跃的后台进程",
            checked = settings.freezerEnabled,
            onCheckedChange = { viewModel.setFreezerEnabled(it) }
        )
        
        if (settings.freezerEnabled) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            
            var delayText by remember { mutableStateOf(settings.freezeDelay.toString()) }
            
            OutlinedTextField(
                value = delayText,
                onValueChange = { 
                    delayText = it
                    it.toIntOrNull()?.let { delay ->
                        viewModel.setFreezeDelay(delay)
                    }
                },
                label = { Text("冻结延迟（秒）") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SceneCheckSection(
    settings: AppSettings,
    viewModel: MainViewModel
) {
    SectionCard(
        title = "🎯 场景检测",
        icon = Icons.Default.Radar
    ) {
        SwitchItem(
            title = "启用场景检测",
            subtitle = "检测特定场景并调整优化策略",
            checked = settings.sceneCheckEnabled,
            onCheckedChange = { viewModel.setSceneCheckEnabled(it) }
        )
    }
}

// ========== 辅助组件 ==========

@Composable
fun SectionCard(
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
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun SwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun ClickableItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(text = title, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CpuModeDialog(
    currentMode: String,
    onDismiss: () -> Unit,
    onModeSelected: (String) -> Unit
) {
    val modes = listOf(
        "daily" to "日常模式",
        "performance" to "性能模式",
        "standby" to "待机模式"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择 CPU 模式") },
        text = {
            Column {
                modes.forEach { (mode, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentMode == mode,
                            onClick = { onModeSelected(mode) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun GpuModeDialog(
    currentMode: String,
    onDismiss: () -> Unit,
    onModeSelected: (String) -> Unit
) {
    val modes = listOf(
        "default" to "默认模式",
        "performance" to "性能模式",
        "power_saving" to "节能模式"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择 GPU 模式") },
        text = {
            Column {
                modes.forEach { (mode, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentMode == mode,
                            onClick = { onModeSelected(mode) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

// 辅助函数
fun getCpuModeDisplayName(mode: String): String {
    return when (mode) {
        "performance" -> "性能模式"
        "standby" -> "待机模式"
        "daily" -> "日常模式"
        else -> "默认"
    }
}

fun getGpuModeDisplayName(mode: String): String {
    return when (mode) {
        "performance" -> "性能模式"
        "power_saving" -> "节能模式"
        "default" -> "默认模式"
        else -> "默认"
    }
}
