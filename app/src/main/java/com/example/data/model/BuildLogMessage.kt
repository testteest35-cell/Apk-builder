package com.example.data.model

enum class LogLevel {
    INFO,
    TASK,
    SUCCESS,
    WARNING,
    ERROR
}

data class BuildLogMessage(
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String,
    val message: String,
    val level: LogLevel = LogLevel.INFO
)
