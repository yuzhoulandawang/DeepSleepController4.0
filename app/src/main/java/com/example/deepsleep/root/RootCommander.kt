package com.example.deepsleep.root

import android.util.Log
import com.example.deepsleep.BuildConfig
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RootCommander {
    private const val TAG = "RootCommander"

    init {
        Shell.enableVerboseLogging = BuildConfig.DEBUG
        Shell.setDefaultBuilder(
            Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(30)  // 增加超时
        )
    }

    // 确保 shell 存活且具有 root 权限
    private suspend fun ensureShell(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            // 检查当前 shell 是否仍存活且有 root
            if (!Shell.isAlive() || !Shell.isRoot()) {
                Log.d(TAG, "ensureShell: shell not alive or not root, re-initializing...")
                Shell.getShell()  // 重新获取 shell
                true
            } else {
                Log.d(TAG, "ensureShell: shell is alive and root")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "ensureShell failed: ${e.message}")
            false
        }
    }

    suspend fun requestRootAccess(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            Shell.getShell()
            Log.d(TAG, "requestRootAccess: success")
            true
        } catch (e: Exception) {
            Log.e(TAG, "requestRootAccess failed: ${e.message}")
            false
        }
    }

    suspend fun checkRoot(): Boolean = withContext(Dispatchers.IO) {
        if (!ensureShell()) {
            Log.e(TAG, "checkRoot: ensureShell failed")
            return@withContext false
        }
        val result = Shell.cmd("id").exec()
        val success = result.out.any { it.contains("uid=0") }
        if (success) {
            Log.d(TAG, "checkRoot: root confirmed")
        } else {
            Log.e(TAG, "checkRoot: 'id' output does not contain uid=0, out=${result.out}, err=${result.err}")
        }
        success
    }

    suspend fun exec(command: String): Shell.Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "exec: $command")
        ensureShell()  // 每次执行前确保 shell 存活
        val result = Shell.cmd(command).exec()
        if (!result.isSuccess) {
            Log.e(TAG, "exec failed: $command, exitCode=${result.code}, err=${result.err}")
        } else {
            Log.d(TAG, "exec success: $command, out=${result.out}")
        }
        result
    }

    suspend fun exec(vararg commands: String): Shell.Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "exec multiple: ${commands.joinToString("; ")}")
        ensureShell()
        val result = Shell.cmd(*commands).exec()
        if (!result.isSuccess) {
            Log.e(TAG, "exec multiple failed, exitCode=${result.code}, err=${result.err}")
        }
        result
    }

    suspend fun execBatch(commands: List<String>): Shell.Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "exec batch: ${commands.size} commands")
        ensureShell()
        val result = Shell.cmd(*commands.toTypedArray()).exec()
        if (!result.isSuccess) {
            Log.e(TAG, "exec batch failed, exitCode=${result.code}, err=${result.err}")
        }
        result
    }

    suspend fun safeWrite(path: String, value: String): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "safeWrite: $path = $value")
        ensureShell()
        val result = Shell.cmd("printf '%s' \"$value\" > $path").exec()
        if (result.isSuccess) {
            Log.d(TAG, "safeWrite success: $path")
        } else {
            Log.e(TAG, "safeWrite failed: $path, exitCode=${result.code}, err=${result.err}")
        }
        result.isSuccess
    }

    suspend fun readFile(path: String): String? = withContext(Dispatchers.IO) {
        Log.d(TAG, "readFile: $path")
        ensureShell()
        val result = Shell.cmd("cat $path 2>/dev/null").exec()
        if (result.isSuccess) {
            val content = result.out.joinToString("\n")
            Log.d(TAG, "readFile success: $path, content=$content")
            content
        } else {
            Log.e(TAG, "readFile failed: $path, exitCode=${result.code}, err=${result.err}")
            null
        }
    }

    suspend fun fileExists(path: String): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "fileExists: $path")
        ensureShell()
        val result = Shell.cmd("[ -f $path ]").exec()
        val exists = result.isSuccess
        Log.d(TAG, "fileExists: $path exists=$exists")
        exists
    }

    suspend fun mkdir(path: String): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "mkdir: $path")
        ensureShell()
        val result = Shell.cmd("mkdir -p $path").exec()
        if (result.isSuccess) {
            Log.d(TAG, "mkdir success: $path")
        } else {
            Log.e(TAG, "mkdir failed: $path, exitCode=${result.code}, err=${result.err}")
        }
        result.isSuccess
    }
}