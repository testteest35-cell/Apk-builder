package com.example.engine

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectFileNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ZipProjectManager(private val context: Context) {

    private val projectsDir: File
        get() = File(context.filesDir, "projects").apply { if (!exists()) mkdirs() }

    private val sampleZipDir: File
        get() = File(context.filesDir, "sample_zips").apply { if (!exists()) mkdirs() }

    suspend fun importZipFromUri(uri: Uri): Result<ProjectEntity> = withContext(Dispatchers.IO) {
        try {
            val fileName = queryFileName(uri) ?: "imported_project.zip"
            val projectId = UUID.randomUUID().toString()
            val destDir = File(projectsDir, projectId).apply { mkdirs() }

            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Failed to open input stream for $uri"))

            var totalBytes = 0L
            var fileCount = 0

            ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
                var entry = zipIn.nextEntry
                while (entry != null) {
                    val canonicalDestDirPath = destDir.canonicalPath
                    val entryFile = File(destDir, entry.name)
                    val canonicalEntryFilePath = entryFile.canonicalPath

                    // Zip-slip security protection
                    if (!canonicalEntryFilePath.startsWith(canonicalDestDirPath + File.separator) &&
                        canonicalEntryFilePath != canonicalDestDirPath
                    ) {
                        throw SecurityException("ZIP entry attempts path traversal outside destination: ${entry.name}")
                    }

                    if (entry.isDirectory) {
                        entryFile.mkdirs()
                    } else {
                        entryFile.parentFile?.mkdirs()
                        BufferedOutputStream(FileOutputStream(entryFile)).use { out ->
                            val buffer = ByteArray(8192)
                            var read: Int
                            while (zipIn.read(buffer).also { read = it } != -1) {
                                out.write(buffer, 0, read)
                                totalBytes += read
                            }
                        }
                        fileCount++
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }

            // Inspect project files to parse manifest and gradle info
            val manifestInfo = parseManifest(destDir)
            val gradleInfo = parseGradle(destDir)

            val projectName = manifestInfo.appName
                ?: gradleInfo.projectName
                ?: fileName.removeSuffix(".zip").replace("_", " ").capitalizeWords()

            val packageName = manifestInfo.packageName
                ?: gradleInfo.applicationId
                ?: "com.user.app.${projectId.take(6).lowercase()}"

            val category = detectCategory(projectName, destDir)

            val project = ProjectEntity(
                id = projectId,
                name = projectName,
                packageName = packageName,
                versionName = manifestInfo.versionName ?: gradleInfo.versionName ?: "1.0.0",
                versionCode = manifestInfo.versionCode ?: gradleInfo.versionCode ?: 1,
                minSdk = manifestInfo.minSdk ?: gradleInfo.minSdk ?: 24,
                targetSdk = manifestInfo.targetSdk ?: gradleInfo.targetSdk ?: 34,
                createdAt = System.currentTimeMillis(),
                sourceDirPath = destDir.absolutePath,
                originalZipName = fileName,
                fileCount = fileCount,
                sourceSizeBytes = totalBytes,
                appCategory = category,
                lastBuildStatus = "NEVER_BUILT",
                description = "Imported from $fileName with $fileCount files"
            )

            Result.success(project)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProjectFileTree(sourceDirPath: String): ProjectFileNode = withContext(Dispatchers.IO) {
        val rootDir = File(sourceDirPath)
        if (!rootDir.exists()) {
            return@withContext ProjectFileNode(
                name = "root",
                relativePath = "",
                absolutePath = sourceDirPath,
                isDirectory = true
            )
        }
        buildFileNode(rootDir, rootDir)
    }

    private fun buildFileNode(file: File, rootDir: File): ProjectFileNode {
        val relativePath = file.relativeTo(rootDir).path
        val isDir = file.isDirectory
        val ext = file.extension.lowercase()

        val children = if (isDir) {
            file.listFiles()
                ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                ?.map { buildFileNode(it, rootDir) } ?: emptyList()
        } else {
            emptyList()
        }

        return ProjectFileNode(
            name = file.name,
            relativePath = relativePath,
            absolutePath = file.absolutePath,
            isDirectory = isDir,
            sizeBytes = if (isDir) 0L else file.length(),
            extension = ext,
            children = children
        )
    }

    suspend fun readFileContent(filePath: String): String = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (file.exists() && file.isFile) {
                file.readText()
            } else {
                "// File not found or empty: $filePath"
            }
        } catch (e: Exception) {
            "// Error reading file: ${e.message}"
        }
    }

    suspend fun saveFileContent(filePath: String, newContent: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            file.parentFile?.mkdirs()
            file.writeText(newContent)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteProjectFiles(sourceDirPath: String) = withContext(Dispatchers.IO) {
        try {
            File(sourceDirPath).deleteRecursively()
        } catch (_: Exception) {}
    }

    suspend fun createSampleProject(sampleType: SampleProjectType): ProjectEntity = withContext(Dispatchers.IO) {
        val projectId = UUID.randomUUID().toString()
        val destDir = File(projectsDir, projectId).apply { mkdirs() }

        val spec = when (sampleType) {
            SampleProjectType.QUANTUM_PULSE -> SampleAppTemplates.quantumPulseSpec()
            SampleProjectType.NEUMORPHIC_CALC -> SampleAppTemplates.neumorphicCalcSpec()
            SampleProjectType.RETRO_PAINTER -> SampleAppTemplates.retroPainterSpec()
            SampleProjectType.SPACE_ARCADE -> SampleAppTemplates.spaceArcadeSpec()
        }

        var totalBytes = 0L
        var fileCount = 0

        spec.files.forEach { (relPath, content) ->
            val targetFile = File(destDir, relPath)
            targetFile.parentFile?.mkdirs()
            targetFile.writeText(content)
            totalBytes += targetFile.length()
            fileCount++
        }

        ProjectEntity(
            id = projectId,
            name = spec.name,
            packageName = spec.packageName,
            versionName = spec.versionName,
            versionCode = 1,
            minSdk = 24,
            targetSdk = 34,
            createdAt = System.currentTimeMillis(),
            sourceDirPath = destDir.absolutePath,
            originalZipName = "${spec.slug}_source.zip",
            fileCount = fileCount,
            sourceSizeBytes = totalBytes,
            appCategory = spec.category,
            lastBuildStatus = "NEVER_BUILT",
            description = spec.description
        )
    }

    private fun queryFileName(uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        name = it.getString(index)
                    }
                }
            }
        }
        if (name == null) {
            name = uri.path?.let { p ->
                val cut = p.lastIndexOf('/')
                if (cut != -1) p.substring(cut + 1) else p
            }
        }
        return name
    }

    private fun parseManifest(dir: File): ManifestInfo {
        val manifestFile = findFile(dir, "AndroidManifest.xml") ?: return ManifestInfo()
        return try {
            val content = manifestFile.readText()
            val packageRegex = """package=["']([^"']+)["']""".toRegex()
            val packageName = packageRegex.find(content)?.groupValues?.get(1)

            val labelRegex = """android:label=["']([^"']+)["']""".toRegex()
            val appLabel = labelRegex.find(content)?.groupValues?.get(1)?.removePrefix("@string/")

            val versionNameRegex = """android:versionName=["']([^"']+)["']""".toRegex()
            val versionName = versionNameRegex.find(content)?.groupValues?.get(1)

            val versionCodeRegex = """android:versionCode=["'](\d+)["']""".toRegex()
            val versionCode = versionCodeRegex.find(content)?.groupValues?.get(1)?.toIntOrNull()

            val minSdkRegex = """android:minSdkVersion=["'](\d+)["']""".toRegex()
            val minSdk = minSdkRegex.find(content)?.groupValues?.get(1)?.toIntOrNull()

            val targetSdkRegex = """android:targetSdkVersion=["'](\d+)["']""".toRegex()
            val targetSdk = targetSdkRegex.find(content)?.groupValues?.get(1)?.toIntOrNull()

            ManifestInfo(
                packageName = packageName,
                appName = appLabel,
                versionName = versionName,
                versionCode = versionCode,
                minSdk = minSdk,
                targetSdk = targetSdk
            )
        } catch (e: Exception) {
            ManifestInfo()
        }
    }

    private fun parseGradle(dir: File): GradleInfo {
        val gradleFile = findFile(dir, "build.gradle.kts") ?: findFile(dir, "build.gradle")
            ?: return GradleInfo()
        return try {
            val content = gradleFile.readText()
            val appIdRegex = """applicationId\s*=\s*["']([^"']+)["']""".toRegex()
            val appId = appIdRegex.find(content)?.groupValues?.get(1)

            val verNameRegex = """versionName\s*=\s*["']([^"']+)["']""".toRegex()
            val versionName = verNameRegex.find(content)?.groupValues?.get(1)

            val verCodeRegex = """versionCode\s*=\s*(\d+)""".toRegex()
            val versionCode = verCodeRegex.find(content)?.groupValues?.get(1)?.toIntOrNull()

            val minSdkRegex = """minSdk(?:Version)?\s*=?\s*(\d+)""".toRegex()
            val minSdk = minSdkRegex.find(content)?.groupValues?.get(1)?.toIntOrNull()

            val targetSdkRegex = """targetSdk(?:Version)?\s*=?\s*(\d+)""".toRegex()
            val targetSdk = targetSdkRegex.find(content)?.groupValues?.get(1)?.toIntOrNull()

            GradleInfo(
                applicationId = appId,
                versionName = versionName,
                versionCode = versionCode,
                minSdk = minSdk,
                targetSdk = targetSdk
            )
        } catch (e: Exception) {
            GradleInfo()
        }
    }

    private fun findFile(dir: File, targetName: String): File? {
        val files = dir.listFiles() ?: return null
        for (f in files) {
            if (f.name.equals(targetName, ignoreCase = true)) return f
            if (f.isDirectory) {
                val found = findFile(f, targetName)
                if (found != null) return found
            }
        }
        return null
    }

    private fun detectCategory(name: String, dir: File): String {
        val n = name.lowercase()
        return when {
            n.contains("calc") || n.contains("math") -> "CALCULATOR"
            n.contains("paint") || n.contains("draw") || n.contains("art") -> "CREATIVE"
            n.contains("game") || n.contains("arcade") || n.contains("dodge") || n.contains("play") -> "ARCADE"
            n.contains("task") || n.contains("todo") || n.contains("pulse") || n.contains("timer") -> "PRODUCTIVITY"
            else -> "UTILITY"
        }
    }

    private fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
}

enum class SampleProjectType {
    QUANTUM_PULSE,
    NEUMORPHIC_CALC,
    RETRO_PAINTER,
    SPACE_ARCADE
}

data class ManifestInfo(
    val packageName: String? = null,
    val appName: String? = null,
    val versionName: String? = null,
    val versionCode: Int? = null,
    val minSdk: Int? = null,
    val targetSdk: Int? = null
)

data class GradleInfo(
    val projectName: String? = null,
    val applicationId: String? = null,
    val versionName: String? = null,
    val versionCode: Int? = null,
    val minSdk: Int? = null,
    val targetSdk: Int? = null
)
