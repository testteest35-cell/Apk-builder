package com.example.engine

data class ProjectTemplateSpec(
    val name: String,
    val slug: String,
    val packageName: String,
    val versionName: String,
    val category: String,
    val description: String,
    val files: Map<String, String>
)

object SampleAppTemplates {

    fun quantumPulseSpec(): ProjectTemplateSpec {
        return ProjectTemplateSpec(
            name = "Quantum Pulse",
            slug = "quantum_pulse",
            packageName = "com.quantum.pulse",
            versionName = "1.2.0",
            category = "PRODUCTIVITY",
            description = "High-performance productivity dashboard with Pomodoro focus cycles, priority task matrix, and haptic cadence tracking.",
            files = mapOf(
                "AndroidManifest.xml" to """
                    <?xml version="1.0" encoding="utf-8"?>
                    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                        package="com.quantum.pulse">
                        <uses-permission android:name="android.permission.VIBRATE" />
                        <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
                        <application
                            android:allowBackup="true"
                            android:icon="@mipmap/ic_launcher"
                            android:label="Quantum Pulse"
                            android:roundIcon="@mipmap/ic_launcher_round"
                            android:theme="@style/Theme.Material3.Dark">
                            <activity
                                android:name=".MainActivity"
                                android:exported="true">
                                <intent-filter>
                                    <action android:name="android.intent.action.MAIN" />
                                    <category android:name="android.intent.category.LAUNCHER" />
                                </intent-filter>
                            </activity>
                        </application>
                    </manifest>
                """.trimIndent(),
                "build.gradle.kts" to """
                    plugins {
                        id("com.android.application")
                        id("org.jetbrains.kotlin.android")
                        id("org.jetbrains.kotlin.plugin.compose")
                    }
                    android {
                        namespace = "com.quantum.pulse"
                        compileSdk = 34
                        defaultConfig {
                            applicationId = "com.quantum.pulse"
                            minSdk = 24
                            targetSdk = 34
                            versionCode = 12
                            versionName = "1.2.0"
                        }
                    }
                    dependencies {
                        implementation(platform("androidx.compose:compose-bom:2024.09.00"))
                        implementation("androidx.compose.material3:material3")
                        implementation("androidx.compose.ui:ui")
                        implementation("androidx.activity:activity-compose:1.9.0")
                    }
                """.trimIndent(),
                "src/main/java/com/quantum/pulse/MainActivity.kt" to """
                    package com.quantum.pulse

                    import android.os.Bundle
                    import androidx.activity.ComponentActivity
                    import androidx.activity.compose.setContent
                    import androidx.compose.foundation.layout.*
                    import androidx.compose.material3.*
                    import androidx.compose.runtime.*
                    import androidx.compose.ui.Modifier
                    import androidx.compose.ui.unit.dp

                    class MainActivity : ComponentActivity() {
                        override fun onCreate(savedInstanceState: Bundle?) {
                            super.onCreate(savedInstanceState)
                            setContent {
                                MaterialTheme {
                                    Surface(modifier = Modifier.fillMaxSize()) {
                                        Text(text = "Quantum Pulse Focus & Task Engine", modifier = Modifier.padding(16.dp))
                                    }
                                }
                            }
                        }
                    }
                """.trimIndent(),
                "src/main/res/values/strings.xml" to """
                    <resources>
                        <string name="app_name">Quantum Pulse</string>
                        <string name="focus_start">Ignite Focus</string>
                        <string name="task_header">Active Objectives</string>
                    </resources>
                """.trimIndent(),
                "README.md" to """
                    # Quantum Pulse
                    A modern Android productivity application built with Jetpack Compose.
                    Features customizable pomodoro intervals, priority tags, and clean cyber aesthetics.
                """.trimIndent()
            )
        )
    }

    fun neumorphicCalcSpec(): ProjectTemplateSpec {
        return ProjectTemplateSpec(
            name = "Neumorphic Calc",
            slug = "neumorphic_calc",
            packageName = "com.apex.calc",
            versionName = "2.0.4",
            category = "CALCULATOR",
            description = "Scientific and financial precision calculator with tactile tactile keypads, expression history, and floating-point solver.",
            files = mapOf(
                "AndroidManifest.xml" to """
                    <?xml version="1.0" encoding="utf-8"?>
                    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                        package="com.apex.calc">
                        <uses-permission android:name="android.permission.VIBRATE" />
                        <application
                            android:label="Neumorphic Calc"
                            android:theme="@style/Theme.Material3.Dark">
                            <activity
                                android:name=".MainActivity"
                                android:exported="true">
                                <intent-filter>
                                    <action android:name="android.intent.action.MAIN" />
                                    <category android:name="android.intent.category.LAUNCHER" />
                                </intent-filter>
                            </activity>
                        </application>
                    </manifest>
                """.trimIndent(),
                "build.gradle.kts" to """
                    plugins {
                        id("com.android.application")
                        id("org.jetbrains.kotlin.android")
                    }
                    android {
                        namespace = "com.apex.calc"
                        compileSdk = 34
                        defaultConfig {
                            applicationId = "com.apex.calc"
                            minSdk = 24
                            targetSdk = 34
                            versionCode = 20
                            versionName = "2.0.4"
                        }
                    }
                """.trimIndent(),
                "src/main/java/com/apex/calc/MainActivity.kt" to """
                    package com.apex.calc

                    import android.os.Bundle
                    import androidx.activity.ComponentActivity
                    import androidx.activity.compose.setContent
                    import androidx.compose.material3.*

                    class MainActivity : ComponentActivity() {
                        override fun onCreate(savedInstanceState: Bundle?) {
                            super.onCreate(savedInstanceState)
                            setContent {
                                Text("Neumorphic Math Engine Ready")
                            }
                        }
                    }
                """.trimIndent(),
                "src/main/res/values/strings.xml" to """
                    <resources>
                        <string name="app_name">Neumorphic Calc</string>
                        <string name="precision">64-bit IEEE 754</string>
                    </resources>
                """.trimIndent()
            )
        )
    }

    fun retroPainterSpec(): ProjectTemplateSpec {
        return ProjectTemplateSpec(
            name = "Retro Pixel Painter",
            slug = "retro_painter",
            packageName = "com.studio.painter",
            versionName = "1.0.0",
            category = "CREATIVE",
            description = "8-bit digital art canvas with pressure simulation, layer blending, custom palette mixer, and PNG export.",
            files = mapOf(
                "AndroidManifest.xml" to """
                    <?xml version="1.0" encoding="utf-8"?>
                    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                        package="com.studio.painter">
                        <application
                            android:label="Retro Pixel Painter"
                            android:theme="@style/Theme.Material3.Dark">
                            <activity
                                android:name=".MainActivity"
                                android:exported="true">
                                <intent-filter>
                                    <action android:name="android.intent.action.MAIN" />
                                    <category android:name="android.intent.category.LAUNCHER" />
                                </intent-filter>
                            </activity>
                        </application>
                    </manifest>
                """.trimIndent(),
                "build.gradle.kts" to """
                    plugins {
                        id("com.android.application")
                        id("org.jetbrains.kotlin.android")
                    }
                    android {
                        namespace = "com.studio.painter"
                        compileSdk = 34
                        defaultConfig {
                            applicationId = "com.studio.painter"
                            minSdk = 24
                            targetSdk = 34
                            versionCode = 1
                            versionName = "1.0.0"
                        }
                    }
                """.trimIndent(),
                "src/main/java/com/studio/painter/MainActivity.kt" to """
                    package com.studio.painter

                    import android.os.Bundle
                    import androidx.activity.ComponentActivity
                    import androidx.activity.compose.setContent
                    import androidx.compose.material3.Text

                    class MainActivity : ComponentActivity() {
                        override fun onCreate(savedInstanceState: Bundle?) {
                            super.onCreate(savedInstanceState)
                            setContent {
                                Text("Pixel Studio Canvas")
                            }
                        }
                    }
                """.trimIndent()
            )
        )
    }

    fun spaceArcadeSpec(): ProjectTemplateSpec {
        return ProjectTemplateSpec(
            name = "Space Dodge Arcade",
            slug = "space_arcade",
            packageName = "com.arcade.spacedodge",
            versionName = "1.5.0",
            category = "ARCADE",
            description = "High-octane 60FPS retro sci-fi arcade runner with dynamic obstacle physics, shield pickups, and leaderboard scoring.",
            files = mapOf(
                "AndroidManifest.xml" to """
                    <?xml version="1.0" encoding="utf-8"?>
                    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                        package="com.arcade.spacedodge">
                        <uses-permission android:name="android.permission.VIBRATE" />
                        <application
                            android:label="Space Dodge Arcade"
                            android:theme="@style/Theme.Material3.Dark">
                            <activity
                                android:name=".MainActivity"
                                android:exported="true">
                                <intent-filter>
                                    <action android:name="android.intent.action.MAIN" />
                                    <category android:name="android.intent.category.LAUNCHER" />
                                </intent-filter>
                            </activity>
                        </application>
                    </manifest>
                """.trimIndent(),
                "build.gradle.kts" to """
                    plugins {
                        id("com.android.application")
                        id("org.jetbrains.kotlin.android")
                    }
                    android {
                        namespace = "com.arcade.spacedodge"
                        compileSdk = 34
                        defaultConfig {
                            applicationId = "com.arcade.spacedodge"
                            minSdk = 24
                            targetSdk = 34
                            versionCode = 15
                            versionName = "1.5.0"
                        }
                    }
                """.trimIndent(),
                "src/main/java/com/arcade/spacedodge/MainActivity.kt" to """
                    package com.arcade.spacedodge

                    import android.os.Bundle
                    import androidx.activity.ComponentActivity
                    import androidx.activity.compose.setContent
                    import androidx.compose.material3.Text

                    class MainActivity : ComponentActivity() {
                        override fun onCreate(savedInstanceState: Bundle?) {
                            super.onCreate(savedInstanceState)
                            setContent {
                                Text("Space Dodge Arcade Running")
                            }
                        }
                    }
                """.trimIndent()
            )
        )
    }
}
