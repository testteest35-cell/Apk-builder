package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeviceOrientation
import com.example.data.model.ProjectEntity
import com.example.ui.MainViewModel
import com.example.ui.screens.simulator.SimulatedAppRuntime
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulatorScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val simulatorState by viewModel.simulatorState.collectAsState()
    val activeProject by viewModel.activeSimulatedProject.collectAsState()
    val allProjects by viewModel.projects.collectAsState()
    val logcatEntries by viewModel.simulatorLogs.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var showProjectPicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Toolbar: Active Project Info & Control Actions
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CyberSurface,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Project selector pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberSurfaceVariant)
                        .clickable { showProjectPicker = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = activeProject?.name ?: "Select Project",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                }

                // Controls
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Orientation
                    IconButton(
                        onClick = { viewModel.toggleSimulatorOrientation() },
                        modifier = Modifier.size(36.dp).testTag("simulator_rotate_button")
                    ) {
                        Icon(
                            if (simulatorState.orientation == DeviceOrientation.PORTRAIT) Icons.Default.ScreenRotation else Icons.Default.ScreenLockPortrait,
                            contentDescription = "Rotate",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Restart
                    IconButton(
                        onClick = { viewModel.restartSimulator() },
                        modifier = Modifier.size(36.dp).testTag("simulator_restart_button")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Restart Sandbox", tint = AmberAccent, modifier = Modifier.size(18.dp))
                    }

                    // Perf overlay toggle
                    IconButton(
                        onClick = { viewModel.toggleSimulatorPerfOverlay() },
                        modifier = Modifier.size(36.dp).testTag("simulator_perf_button")
                    ) {
                        Icon(
                            Icons.Default.Speed,
                            contentDescription = "Performance HUD",
                            tint = if (simulatorState.showDevOverlay) EmeraldGreen else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Logcat toggle
                    IconButton(
                        onClick = { viewModel.toggleSimulatorLogcat() },
                        modifier = Modifier.size(36.dp).testTag("simulator_logcat_button")
                    ) {
                        Icon(
                            Icons.Default.Terminal,
                            contentDescription = "Logcat",
                            tint = if (simulatorState.showLogcat) NeonCyan else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Dark/Light mode in simulator
                    IconButton(
                        onClick = { viewModel.toggleSimulatorDarkTheme() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            if (simulatorState.isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = "Theme",
                            tint = VioletAccentLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Project Picker Dropdown Sheet / Dialog
        if (showProjectPicker) {
            AlertDialog(
                onDismissRequest = { showProjectPicker = false },
                title = { Text("Switch Simulated Project") },
                text = {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(allProjects) { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (p.id == activeProject?.id) CyberSurfaceHighlight else CyberSurfaceVariant)
                                    .clickable {
                                        viewModel.launchInSimulator(p)
                                        showProjectPicker = false
                                    }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Android, contentDescription = null, tint = NeonCyan)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(p.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(p.packageName, color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showProjectPicker = false }) { Text("Close") }
                }
            )
        }

        // Center: The Simulated Device Frame
        val isPortrait = simulatorState.orientation == DeviceOrientation.PORTRAIT

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val phoneWidth = if (isPortrait) 300.dp else 460.dp
            val phoneHeight = if (isPortrait) 580.dp else 300.dp

            // Simulated Device Chassis
            Box(
                modifier = Modifier
                    .size(width = phoneWidth, height = phoneHeight)
                    .shadow(elevation = 20.dp, shape = RoundedCornerShape(32.dp), spotColor = NeonCyan)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF1E293B))
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .border(3.dp, Color(0xFF334155), RoundedCornerShape(32.dp))
                    .padding(8.dp)
            ) {
                // Inner Screen Bezel
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (simulatorState.isDarkMode) Color(0xFF090D16) else Color(0xFFFFFFFF))
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Simulated Android Status Bar
                        SimulatedStatusBar(isDarkMode = simulatorState.isDarkMode, battery = simulatorState.batteryPercent)

                        // Simulated App Viewport
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            SimulatedAppRuntime(
                                project = activeProject,
                                isDarkMode = simulatorState.isDarkMode,
                                onLogcatEvent = { p, tag, msg -> viewModel.addSimulatorLog(p, tag, msg) },
                                modifier = Modifier.fillMaxSize()
                            )

                            // Performance Overlay
                            if (simulatorState.showDevOverlay) {
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp),
                                    color = Color(0xDD000000),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)) {
                                        Text("60 FPS", color = EmeraldGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        Text("${simulatorState.memoryUsageMb} MB RAM", color = NeonCyan, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                        Text("14% CPU", color = AmberAccent, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }

                        // Simulated Android Gesture Navigation Bar
                        SimulatedNavigationBar(
                            onBack = {
                                viewModel.addSimulatorLog("D", "ViewRootImpl", "Back key dispatched")
                            },
                            onHome = {
                                viewModel.restartSimulator()
                            },
                            onRecents = {
                                showProjectPicker = true
                            }
                        )
                    }

                    // Punch Hole Camera cutout
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 4.dp)
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF000000))
                            .border(0.5.dp, Color(0xFF1E293B), CircleShape)
                    )
                }
            }
        }

        // Bottom: Logcat Drawer (expandable)
        AnimatedVisibility(visible = simulatorState.showLogcat) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                color = TerminalBackground,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceHighlight)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Terminal, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Logcat Console (${logcatEntries.size} events)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        TextButton(
                            onClick = { viewModel.toggleSimulatorLogcat() },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Hide", fontSize = 10.sp, color = NeonCyan)
                        }
                    }

                    val listState = rememberLazyListState()
                    LaunchedEffect(logcatEntries.size) {
                        if (logcatEntries.isNotEmpty()) {
                            listState.animateScrollToItem(logcatEntries.size - 1)
                        }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(logcatEntries) { entry ->
                            val color = when (entry.priority) {
                                "E" -> TerminalLogError
                                "W" -> TerminalLogWarning
                                "I" -> TerminalLogInfo
                                else -> TerminalText
                            }
                            Text(
                                text = "${entry.timestamp} ${entry.priority}/${entry.tag}: ${entry.message}",
                                fontSize = 9.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = color,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SimulatedStatusBar(isDarkMode: Boolean, battery: Int) {
    val textColor = if (isDarkMode) Color.White else Color.Black
    val time = SimpleDateFormat("h:mm", Locale.US).format(Date())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(time, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.Default.NetworkWifi, contentDescription = null, tint = textColor, modifier = Modifier.size(12.dp))
            Icon(Icons.Default.NetworkCell, contentDescription = null, tint = textColor, modifier = Modifier.size(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$battery%", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = textColor, modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
private fun SimulatedNavigationBar(
    onBack: () -> Unit,
    onHome: () -> Unit,
    onRecents: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x22000000))
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.Gray)
                .clickable { onHome() }
        )
        IconButton(onClick = onRecents, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.CropSquare, contentDescription = "Recents", tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
    }
}
