package com.example.data.model

data class ProjectFileNode(
    val name: String,
    val relativePath: String,
    val absolutePath: String,
    val isDirectory: Boolean,
    val sizeBytes: Long = 0L,
    val extension: String = "",
    val children: List<ProjectFileNode> = emptyList()
)
