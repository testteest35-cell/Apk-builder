package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "build_artifacts")
data class BuildArtifactEntity(
    @PrimaryKey
    val id: String,
    val projectId: String,
    val projectName: String,
    val packageName: String,
    val versionName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMs: Long = 0L,
    val apkPath: String,
    val apkSizeBytes: Long = 0L,
    val sha256: String = "",
    val status: String = "SUCCESS",
    val logSummary: String = "",
    val permissions: String = ""
)
