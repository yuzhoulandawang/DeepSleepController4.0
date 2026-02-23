package com.example.deepsleep.ui.logs

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deepsleep.data.LogRepository
import com.example.deepsleep.model.LogEntry
import com.example.deepsleep.model.LogLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted

/**
 * 日志页面 ViewModel
 * 管理日志数据的状态和筛选逻辑
 */
class LogsViewModel : ViewModel() {

    companion object {
        private const val TAG = "LogsViewModel"
    }

    // 使用单例 LogRepository
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private val _selectedLevel = MutableStateFlow<LogLevel?>(null)
    val selectedLevel: StateFlow<LogLevel?> = _selectedLevel.asStateFlow()

    val filteredLogs: StateFlow<List<LogEntry>> = combine(
        _logs,
        _selectedLevel
    ) { logs, level ->
        if (level == null) logs else logs.filter { it.level == level }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        refreshLogs()
    }

    fun refreshLogs() {
        viewModelScope.launch {
            _logs.value = LogRepository.readLogs()
        }
    }

    fun setLevelFilter(level: LogLevel?) {
        _selectedLevel.value = level
    }

    fun clearLogs() {
        viewModelScope.launch {
            LogRepository.clearLogs()
            refreshLogs()
        }
    }

    fun exportLogs(context: Context) {
        viewModelScope.launch {
            LogRepository.createShareableFile(context)
        }
    }

    suspend fun getLogSize(): String = LogRepository.getLogSize()
}
