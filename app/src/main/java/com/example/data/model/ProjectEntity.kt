package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val packageName: String,
    val versionName: String = "1.0.0",
    val versionCode: Int = 1,
    val minSdk: Int = 24,
    val targetSdk: Int = 34,
    val createdAt: Long = System.currentTimeMillis(),
    val sourceDirPath: String,
    val originalZipName: String,
    val fileCount: Int = 0,
    val sourceSizeBytes: Long = 0L,
    val appCategory: String = "CUSTOM",
    val lastBuildStatus: String = "NEVER_BUILT", // NEVER_BUILT, BUILDING, SUCCESS, FAILED
    val latestApkPath: String? = null,
    val latestApkSize: Long = 0L,
    val description: String = ""
)
