package com.arc.injector.inject

import java.io.File
import java.util.zip.ZipFile

object ZipTools {
    fun extract(zip: File, destination: File) {
        destination.mkdirs()
        ZipFile(zip).use { z ->
            z.entries().asSequence().forEach { entry ->
                val target = File(destination, entry.name)
                val base = destination.canonicalFile
                val canonical = target.canonicalFile
                if (!canonical.path.startsWith(base.path + File.separator)) {
                    throw SecurityException("Unsafe ZIP entry: ${entry.name}")
                }
                if (entry.isDirectory) {
                    canonical.mkdirs()
                } else {
                    canonical.parentFile?.mkdirs()
                    z.getInputStream(entry).use { input ->
                        canonical.outputStream().use { output -> input.copyTo(output) }
                    }
                }
            }
        }
    }
}
