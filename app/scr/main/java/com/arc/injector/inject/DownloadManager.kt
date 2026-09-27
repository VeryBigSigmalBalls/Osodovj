package com.arc.injector.inject

import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object DownloadManager {
    fun download(urlText: String, destination: File, onProgress: (Int) -> Unit) {
        require(urlText.startsWith("https://")) { "Package URL must use HTTPS." }
        val connection = (URL(urlText).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15000
            readTimeout = 30000
            requestMethod = "GET"
        }
        connection.connect()
        if (connection.responseCode !in 200..299) {
            throw IllegalStateException("Download failed: HTTP ${connection.responseCode}")
        }
        val total = connection.contentLengthLong
        connection.inputStream.use { input ->
            destination.outputStream().use { output ->
                val buffer = ByteArray(64 * 1024)
                var done = 0L
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    output.write(buffer, 0, count)
                    done += count
                    if (total > 0) onProgress(((done * 100) / total).toInt().coerceIn(0, 100))
                }
            }
        }
        connection.disconnect()
    }
}
