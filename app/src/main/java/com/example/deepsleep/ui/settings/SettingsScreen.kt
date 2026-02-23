package com.example.deepsleep.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * 设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: SettingsViewModel = viewModel()
    val settings by viewModel.settings.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
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
            // ========== 深度 Doze 配置 ==========
            SettingsSection(title = "深度 Doze 配置") {
                SwitchSetting(
                    title = "启用深度 Doze",
                    subtitle = "息屏后自动进入 Device Idle 模式",
                    checked = settings.deepDozeEnabled,
                    onCheckedChange = { viewModel.setDeepDozeEnabled(it) }
                )
                
                if (settings.deepDozeEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    SliderSetting(
                        title = "延迟进入时间",
                        value = settings.deepDozeDelaySeconds.toFloat(),
                        valueRange = 0f..300f,
                        onValueChange = { viewModel.setDeepDozeDelaySeconds(it.toInt()) },
                        label = "${settings.deepDozeDelaySeconds} 秒"
                    )
                    
                    SwitchSetting(
                        title = "强制 Doze 模式",
                        subtitle = "禁用 motion 检测，强制进入 Doze",
                        checked = settings.deepDozeForceMode,
                        onCheckedChange = { viewModel.setDeepDozeForceMode(it) }
                    )
                }
            }
            
            HorizontalDivider()
            
            // ========== 深度睡眠配置（Hook 版本） ==========
            SettingsSection(title = "深度睡眠（Hook 版本）") {
                SwitchSetting(
                    title = "启用深度睡眠 Hook",
                    subtitle = "息屏后强制进入深度休眠，屏蔽自动退出",
                    checked = settings.deepSleepHookEnabled,
                    onCheckedChange = { viewModel.setDeepSleepHookEnabled(it) }
                )
                
                if (settings.deepSleepHookEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    SliderSetting(
                        title = "延迟进入时间",
                        value = settings.deepSleepDelaySeconds.toFloat(),
                        valueRange = 0f..300f,
                        onValueChange = { viewModel.setDeepSleepDelaySeconds(it.toInt()) },
                        label = "${settings.deepSleepDelaySeconds} 秒"
                    )
                    
                    SwitchSetting(
                        title = "阻止自动退出",
                        subtitle = "屏蔽移动、广播等自动退出条件",
                        checked = settings.deepSleepBlockExit,
                        onCheckedChange = { viewModel.setDeepSleepBlockExit(it) }
                    )
                    
                    SliderSetting(
                        title = "状态检查间隔",
                        value = settings.deepSleepCheckInterval.toFloat(),
                        valueRange = 5f..60f,
                        onValueChange = { viewModel.setDeepSleepCheckInterval(it.toInt()) },
                        label = "${settings.deepSleepCheckInterval} 秒"
                    )
                }
            }
            
            HorizontalDivider()
            
            // ========== 系统省电模式联动 ==========
            SettingsSection(title = "系统省电模式") {
                SwitchSetting(
                    title = "睡眠时开启省电模式",
                    subtitle = "进入深度睡眠时自动开启系统省电",
                    checked = settings.enablePowerSaverOnSleep,
                    onCheckedChange = { viewModel.setEnablePowerSaverOnSleep(it) }
                )
                
                SwitchSetting(
                    title = "唤醒时关闭省电模式",
                    subtitle = "退出深度睡眠时自动关闭系统省电",
                    checked = settings.disablePowerSaverOnWake,
                    onCheckedChange = { viewModel.setDisablePowerSaverOnWake(it) }
                )
            }
            
            HorizontalDivider()
            
            // ========== CPU 调度优化配置 ==========
            SettingsSection(title = "CPU 调度优化") {
                SwitchSetting(
                    title = "启用 CPU 调度优化",
                    subtitle = "优化 WALT 调度器参数",
                    checked = settings.cpuOptimizationEnabled,
                    onCheckedChange = { viewModel.setCpuOptimizationEnabled(it) }
                )
                
                if (settings.cpuOptimizationEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    SwitchSetting(
                        title = "自动切换 CPU 模式",
                        subtitle = "亮屏/息屏时自动切换模式",
                        checked = settings.autoSwitchCpuMode,
                        onCheckedChange = { viewModel.setAutoSwitchCpuMode(it) }
                    )
                    
                    if (settings.autoSwitchCpuMode) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "亮屏模式",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
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
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "息屏模式",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
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
            
            HorizontalDivider()
            
            // ========== 场景检测配置 ==========
            SettingsSection(title = "场景检测") {
                SwitchSetting(
                    title = "检测流量活跃",
                    subtitle = "有活跃流量时阻止深度睡眠",
                    checked = settings.checkNetworkTraffic,
                    onCheckedChange = { viewModel.setCheckNetworkTraffic(it) }
                )
                
                SwitchSetting(
                    title = "检测音频播放",
                    subtitle = "有音频播放时阻止深度睡眠",
                    checked = settings.checkAudioPlayback,
                    onCheckedChange = { viewModel.setCheckAudioPlayback(it) }
                )
                
                SwitchSetting(
                    title = "检测导航应用",
                    subtitle = "导航应用运行时阻止深度睡眠",
                    checked = settings.checkNavigation,
                    onCheckedChange = { viewModel.setCheckNavigation(it) }
                )
                
                SwitchSetting(
                    title = "检测通话状态",
                    subtitle = "通话中阻止深度睡眠",
                    checked = settings.checkPhoneCall,
                    onCheckedChange = { viewModel.setCheckPhoneCall(it) }
                )
                
                SwitchSetting(
                    title = "检测 NFC/P2P",
                    subtitle = "NFC 传输中阻止深度睡眠",
                    checked = settings.checkNfcP2p,
                    onCheckedChange = { viewModel.setCheckNfcP2p(it) }
                )
                
                SwitchSetting(
                    title = "检测 WiFi 热点",
                    subtitle = "热点开启时阻止深度睡眠",
                    checked = settings.checkWifiHotspot,
                    onCheckedChange = { viewModel.setCheckWifiHotspot(it) }
                )
                
                SwitchSetting(
                    title = "检测 USB 网络共享",
                    subtitle = "USB 共享时阻止深度睡眠",
                    checked = settings.checkUsbTethering,
                    onCheckedChange = { viewModel.setCheckUsbTethering(it) }
                )
                
                SwitchSetting(
                    title = "检测投屏",
                    subtitle = "投屏中阻止深度睡眠",
                    checked = settings.checkScreenCasting,
                    onCheckedChange = { viewModel.setCheckScreenCasting(it) }
                )
                
                SwitchSetting(
                    title = "检测充电状态",
                    subtitle = "充电时阻止深度睡眠",
                    checked = settings.checkCharging,
                    onCheckedChange = { viewModel.setCheckCharging(it) }
                )
            }
            
            // 底部留白
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
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
fun SwitchSetting(
    title: String,
    subtitle: String? = null,
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
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SliderSetting(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    label: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
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
        label = { Text(modeName) }
    )
}
