package com.example.data.model

enum class DeviceOrientation {
    PORTRAIT,
    LANDSCAPE
}

enum class SimulatorDevicePreset(
    val title: String,
    val resolutionLabel: String,
    val aspectRatio: Float
) {
    PIXEL_8_PRO("Pixel 8 Pro", "1344 x 2992 (20:9)", 9f / 20f),
    GALAXY_S24("Galaxy S24", "1080 x 2340 (19.5:9)", 9f / 19.5f),
    FOLD_MAIN("Pixel Fold (Unfolded)", "1840 x 2208 (6:5)", 5f / 6f)
}

data class SimulatorRuntimeState(
    val isRunning: Boolean = true,
    val orientation: DeviceOrientation = DeviceOrientation.PORTRAIT,
    val devicePreset: SimulatorDevicePreset = SimulatorDevicePreset.PIXEL_8_PRO,
    val fps: Int = 60,
    val memoryUsageMb: Int = 42,
    val cpuPercent: Int = 14,
    val isWifiEnabled: Boolean = true,
    val isDarkMode: Boolean = true,
    val showDevOverlay: Boolean = true,
    val showLogcat: Boolean = false,
    val batteryPercent: Int = 98
)

data class RuntimeLogcatEntry(
    val timestamp: String,
    val tag: String,
    val priority: String, // V, D, I, W, E
    val message: String
)
