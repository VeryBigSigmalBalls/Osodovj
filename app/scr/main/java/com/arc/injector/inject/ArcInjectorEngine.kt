package com.arc.injector.inject

import android.content.Context
import java.io.File

class ArcInjectorEngine(private val context: Context) {
    fun inject(packageUrl: String, destination: String, onProgress: (Int) -> Unit): String {
        if (packageUrl.isBlank()) throw IllegalArgumentException("Package URL is empty in Arc.json.")
        if (!ShizukuInstaller.isReady()) throw IllegalStateException("Shizuku is not ready.")

        val work = File(context.cacheDir, "arc_inject")
        if (work.exists()) work.deleteRecursively()
        work.mkdirs()

        val zip = File(work, "package.zip")
        DownloadManager.download(packageUrl, zip, onProgress)

        val extracted = File(work, "payload")
        ZipTools.extract(zip, extracted)

        val result = ShizukuInstaller.copyTree(context, extracted, destination)
        work.deleteRecursively()

        if (!result.first) throw IllegalStateException(result.second)
        return result.second
    }
}
