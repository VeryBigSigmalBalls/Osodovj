package com.arc.injector.inject

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.io.File

object ShizukuInstaller {
    fun isReady(): Boolean {
        return try {
            Shizuku.pingBinder() &&
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (_: Throwable) {
            false
        }
    }

    fun requestPermission(activity: Activity, requestCode: Int) {
        if (!Shizuku.pingBinder()) return
        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            Shizuku.requestPermission(requestCode)
        }
    }

    fun copyTree(context: Context, source: File, destination: String): Pair<Boolean, String> {
        if (!isReady()) return false to "Shizuku permission is not granted."
        val sourcePath = source.absolutePath.replace("'", "'\''")
        val destPath = destination.replace("'", "'\''")
        val command = "mkdir -p '$destPath' && cp -rp '$sourcePath'/.' '$destPath'/"
        return try {
            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            val stdout = process.inputStream.bufferedReader().readText()
            val stderr = process.errorStream.bufferedReader().readText()
            val code = process.waitFor()
            if (code == 0) true to stdout.ifBlank { "Copied successfully." }
            else false to stderr.ifBlank { "Copy failed with exit code $code." }
        } catch (t: Throwable) {
            false to (t.message ?: "Shizuku command failed.")
        }
    }
}
