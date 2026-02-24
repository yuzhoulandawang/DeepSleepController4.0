package com.example.deepsleep.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.deepsleep.model.AppSettings
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.FocusManager

/**
 * 主页面（整合所有设置项，数值输入统一为圆角文本框，仅保留卡片标题图标）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToLogs: () -> Unit,
    onNavigateToWhitelist: () -> Unit,
    onNavigateToStats: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DeepSleep 控制器") }
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
            // 状态卡片
            StatusCard(settings, viewModel)

            // 深度睡眠控制
            DeepSleepControlSection(settings, viewModel)

            // 深度 Doze 配置
            SettingsSection(title = "深度 Doze 配置") {
                SwitchItem(
                    title = "启用深度 Doze",
                    subtitle = "息屏后自动进入 Device Idle 模式",
                    checked = settings.deepDozeEnabled,
                    onCheckedChange = { viewModel.setDeepDozeEnabled(it) }
                )

                if (settings.deepDozeEnabled) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    NumberInputField(
                        label = "延迟进入时间（秒）",
                        value = settings.deepDozeDelaySeconds.toString(),
                        onValueChange = { newValue ->
                            newValue.toIntOrNull()?.let {
                                scope.launch { viewModel.setDeepDozeDelaySeconds(it) }
                            }
                        },
                        focusManager = focusManager
                    )

                    SwitchItem(
                        title = "强制 Doze 模式",
                        subtitle = "禁用 motion 检测，强制进入 Doze",
                        checked = settings.deepDozeForceMode,
                        onCheckedChange = { viewModel.setDeepDozeForceMode(it) }
                    )
                }
            }

            // 深度睡眠 Hook 版本
            SettingsSection(title = "深度睡眠（Hook 版本）") {
                SwitchItem(
                    title = "启用深度睡眠 Hook",
                    subtitle = "息屏后强制进入深度休眠，屏蔽自动退出",
                    checked = settings.deepSleepHookEnabled,
                    onCheckedChange = { viewModel.setDeepSleepHookEnabled(it) }
                )

                if (settings.deepSleepHookEnabled) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    NumberInputField(
                        label = "延迟进入时间（秒）",
                        value = settings.deepSleepDelaySeconds.toString(),
                        onValueChange = { newValue ->
                            newValue.toIntOrNull()?.let {
                                scope.launch { viewModel.setDeepSleepDelaySeconds(it) }
                            }
                        },
                        focusManager = focusManager
                    )

                    SwitchItem(
                        title = "阻止自动退出",
                        subtitle = "屏蔽移动、广播等自动退出条件",
                        checked = settings.deepSleepBlockExit,
                        onCheckedChange = { viewModel.setDeepSleepBlockExit(it) }
                    )

                    NumberInputField(
                        label = "状态检查间隔（秒）",
                        value = settings.deepSleepCheckInterval.toString(),
                        onValueChange = { newValue ->
                            newValue.toIntOrNull()?.let {
                                scope.launch { viewModel.setDeepSleepCheckInterval(it) }
                            }
                        },
                        focusManager = focusManager
                    )
                }
            }

            // 系统省电模式联动
            SettingsSection(title = "系统省电模式") {
                SwitchItem(
                    title = "睡眠时开启省电模式",
                    subtitle = "进入深度睡眠时自动开启系统省电",
                    checked = settings.enablePowerSaverOnSleep,
                    onCheckedChange = { viewModel.setEnablePowerSaverOnSleep(it) }
                )
                SwitchItem(
                    title = "唤醒时关闭省电模式",
                    subtitle = "退出深度睡眠时自动关闭系统省电",
                    checked = settings.disablePowerSaverOnWake,
                    onCheckedChange = { viewModel.setDisablePowerSaverOnWake(it) }
                )
            }

            // 后台优化
            BackgroundOptimizationSection(settings, viewModel)

            // 白名单管理
            WhitelistSection(settings, viewModel, onNavigateToWhitelist)

            // GPU 优化
            GpuOptimizationSection(settings, viewModel)

            // 电池优化
            BatteryOptimizationSection(settings, viewModel)

            // CPU 绑定
            CpuOptimizationSection(settings, viewModel)

            // CPU 调度优化
            SettingsSection(title = "CPU 调度优化") {
                SwitchItem(
                    title = "启用 CPU 调度优化",
                    subtitle = "优化 WALT 调度器参数",
                    checked = settings.cpuOptimizationEnabled,
                    onCheckedChange = { viewModel.setCpuOptimizationEnabled(it) }
                )

                if (settings.cpuOptimizationEnabled) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    SwitchItem(
                        title = "自动切换 CPU 模式",
                        subtitle = "亮屏/息屏时自动切换模式",
                        checked = settings.autoSwitchCpuMode,
                        onCheckedChange = { viewModel.setAutoSwitchCpuMode(it) }
                    )

                    if (settings.autoSwitchCpuMode) {
                        Text(
                            text = "亮屏模式",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CpuModeChip(
                                mode = "daily",
                                currentMode = settings.cpuModeOnScreen,
                                onClick = { viewModel.setCpuModeOnScreen("daily") }
                            )
                            CpuModeChip(
                                mode = "standby",
                                currentMode = settings.cpuModeOnScreen,
                                onClick = { viewModel.setCpuModeOnScreen("standby") }
                            )
                            CpuModeChip(
                                mode = "default",
                                currentMode = settings.cpuModeOnScreen,
                                onClick = { viewModel.setCpuModeOnScreen("default") }
                            )
                            CpuModeChip(
                                mode = "performance",
                                currentMode = settings.cpuModeOnScreen,
                                onClick = { viewModel.setCpuModeOnScreen("performance") }
                            )
                        }

                        Text(
                            text = "息屏模式",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CpuModeChip(
                                mode = "daily",
                                currentMode = settings.cpuModeOnScreenOff,
                                onClick = { viewModel.setCpuModeOnScreenOff("daily") }
                            )
                            CpuModeChip(
                                mode = "standby",
                                currentMode = settings.cpuModeOnScreenOff,
                                onClick = { viewModel.setCpuModeOnScreenOff("standby") }
                            )
                            CpuModeChip(
                                mode = "default",
                                currentMode = settings.cpuModeOnScreenOff,
                                onClick = { viewModel.setCpuModeOnScreenOff("default") }
                            )
                            CpuModeChip(
                                mode = "performance",
                                currentMode = settings.cpuModeOnScreenOff,
                                onClick = { viewModel.setCpuModeOnScreenOff("performance") }
                            )
                        }
                    }
                }
            }

            // Freezer 服务
            FreezerSection(settings, viewModel, focusManager)

            // 场景检测配置
            SettingsSection(title = "场景检测") {
                SwitchItem(
                    title = "检测流量活跃",
                    subtitle = "有活跃流量时阻止深度睡眠",
                    checked = settings.checkNetworkTraffic,
                    onCheckedChange = { viewModel.setCheckNetworkTraffic(it) }
                )
                SwitchItem(
                    title = "检测音频播放",
                    subtitle = "有音频播放时阻止深度睡眠",
                    checked = settings.checkAudioPlayback,
                    onCheckedChange = { viewModel.setCheckAudioPlayback(it) }
                )
                SwitchItem(
                    title = "检测导航应用",
                    subtitle = "导航应用运行时阻止深度睡眠",
                    checked = settings.checkNavigation,
                    onCheckedChange = { viewModel.setCheckNavigation(it) }
                )
                SwitchItem(
                    title = "检测通话状态",
                    subtitle = "通话中阻止深度睡眠",
                    checked = settings.checkPhoneCall,
                    onCheckedChange = { viewModel.setCheckPhoneCall(it) }
                )
                SwitchItem(
                    title = "检测 NFC/P2P",
                    subtitle = "NFC 传输中阻止深度睡眠",
                    checked = settings.checkNfcP2p,
                    onCheckedChange = { viewModel.setCheckNfcP2p(it) }
                )
                SwitchItem(
                    title = "检测 WiFi 热点",
                    subtitle = "热点开启时阻止深度睡眠",
                    checked = settings.checkWifiHotspot,
                    onCheckedChange = { viewModel.setCheckWifiHotspot(it) }
                )
                SwitchItem(
                    title = "检测 USB 网络共享",
                    subtitle = "USB 共享时阻止深度睡眠",
                    checked = settings.checkUsbTethering,
                    onCheckedChange = { viewModel.setCheckUsbTethering(it) }
                )
                SwitchItem(
                    title = "检测投屏",
                    subtitle = "投屏中阻止深度睡眠",
                    checked = settings.checkScreenCasting,
                    onCheckedChange = { viewModel.setCheckScreenCasting(it) }
                )
                SwitchItem(
                    title = "检测充电状态",
                    subtitle = "充电时阻止深度睡眠",
                    checked = settings.checkCharging,
                    onCheckedChange = { viewModel.setCheckCharging(it) }
                )
            }

            // 统计数据入口
            ClickableItem(
                title = "统计数据",
                subtitle = "查看优化效果统计",
                onClick = onNavigateToStats
            )

            Spacer(modifier = Modifier.weight(1f))

            // 日志入口
            ClickableItem(
                title = "日志",
                subtitle = "查看应用运行日志",
                onClick = onNavigateToLogs
            )
        }
    }
}

// ========== 原有组件（已移除内部图标） ==========
@Composable
fun StatusCard(settings: AppSettings, viewModel: MainViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (settings.rootGranted)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
fun DeepSleepControlSection(settings: AppSettings, viewModel: MainViewModel) {
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
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
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
fun BackgroundOptimizationSection(settings: AppSettings, viewModel: MainViewModel) {
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
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
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
            onClick = onNavigateToWhitelist
        )
        if (settings.whitelist.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                text = "已添加 ${settings.whitelist.size} 个应用",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GpuOptimizationSection(settings: AppSettings, viewModel: MainViewModel) {
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
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            ClickableItem(
                title = "GPU 模式",
                subtitle = "当前: ${getGpuModeDisplayName(settings.gpuMode)}",
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
fun BatteryOptimizationSection(settings: AppSettings, viewModel: MainViewModel) {
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
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
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
fun CpuOptimizationSection(settings: AppSettings, viewModel: MainViewModel) {
    var showModeDialog by remember { mutableStateOf(false) }

    SectionCard(
        title = "🖥️ CPU 绑定",
        icon = Icons.Default.Memory
    ) {
        SwitchItem(
            title = "启用 CPU 绑定",
            subtitle = "通过 cpuset 控制不同进程组的 CPU 核心分配",
            checked = settings.cpuBindEnabled,
            onCheckedChange = { viewModel.setCpuBindEnabled(it) }
        )
        if (settings.cpuBindEnabled) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            ClickableItem(
                title = "CPU 模式",
                subtitle = "当前: ${getCpuModeDisplayName(settings.cpuMode)}",
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
fun FreezerSection(
    settings: AppSettings,
    viewModel: MainViewModel,
    focusManager: androidx.compose.ui.platform.FocusManager
) {
    var delayText by remember { mutableStateOf(settings.freezeDelay.toString()) }
    val scope = rememberCoroutineScope()

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
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            NumberInputField(
                label = "冻结延迟（秒）",
                value = delayText,
                onValueChange = { newValue ->
                    delayText = newValue
                    newValue.toIntOrNull()?.let {
                        scope.launch { viewModel.setFreezeDelay(it) }
                    }
                },
                focusManager = focusManager
            )
        }
    }
}

// ========== 通用组件 ==========
@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
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
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun ClickableItem(
    title: String,
    subtitle: String,
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
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
fun NumberInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    focusManager: androidx.compose.ui.platform.FocusManager
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            singleLine = true
        )
    }
}

@Composable
fun CpuModeChip(
    mode: String,
    currentMode: String,
    onClick: () -> Unit
) {
    val isSelected = mode == currentMode
    val modeName = when (mode) {
        "daily" -> "日常"
        "standby" -> "待机"
        "default" -> "默认"
        "performance" -> "性能"
        else -> mode
    }

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(modeName) },
        shape = RoundedCornerShape(16.dp)
    )
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
            TextButton(onClick = onDismiss) { Text("取消") }
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
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

// ========== 辅助函数 ==========
fun getCpuModeDisplayName(mode: String): String = when (mode) {
    "performance" -> "性能模式"
    "standby" -> "待机模式"
    "daily" -> "日常模式"
    else -> "默认"
}

fun getGpuModeDisplayName(mode: String): String = when (mode) {
    "performance" -> "性能模式"
    "power_saving" -> "节能模式"
    "default" -> "默认模式"
    else -> "默认"
}