package com.example.engine

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class ApkInstallerExporter(private val context: Context) {

    suspend fun saveApkToDocumentUri(sourceApkPath: String, targetDocumentUri: Uri): Result<Long> =
        withContext(Dispatchers.IO) {
            try {
                val sourceFile = File(sourceApkPath)
                if (!sourceFile.exists()) {
                    return@withContext Result.failure(Exception("Source APK file does not exist at $sourceApkPath"))
                }

                val outStream = context.contentResolver.openOutputStream(targetDocumentUri)
                    ?: return@withContext Result.failure(Exception("Cannot open destination stream for $targetDocumentUri"))

                var totalBytes = 0L
                FileInputStream(sourceFile).use { input ->
                    outStream.use { output ->
                        val buffer = ByteArray(8192)
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            totalBytes += read
                        }
                    }
                }
                Result.success(totalBytes)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    fun getShareApkIntent(apkPath: String): Intent? {
        val file = File(apkPath)
        if (!file.exists()) return null

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        return Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.android.package-archive"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, file.name)
            putExtra(Intent.EXTRA_TEXT, "Compiled APK from ApkCraft: ${file.name} (${file.length() / 1024} KB)")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun getInstallApkIntent(apkPath: String): Intent? {
        val file = File(apkPath)
        if (!file.exists()) return null

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
