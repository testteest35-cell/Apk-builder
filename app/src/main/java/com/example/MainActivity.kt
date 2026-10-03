package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.NavigationTab
import com.example.ui.screens.ApksScreen
import com.example.ui.screens.CompilerScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.SimulatorScreen
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val currentTab by viewModel.currentTab.collectAsState()
                val isCompiling by viewModel.isCompiling.collectAsState()
                val projects by viewModel.projects.collectAsState()
                val artifacts by viewModel.artifacts.collectAsState()

                val context = LocalContext.current
                val snackbarHostState = remember { SnackbarHostState() }
                var showGitHubModal by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    viewModel.toastEvents.collect { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }

                if (showGitHubModal) {
                    com.example.ui.components.GitHubActionsModal(
                        onDismiss = { showGitHubModal = false },
                        onCopySuccess = {
                            Toast.makeText(context, "Workflow YAML copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = CyberBackground,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(NeonCyan),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = CyberBackground,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        "ApkCraft",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 19.sp,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = CyberSurfaceVariant,
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            "STUDIO",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonCyan,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            },
                            actions = {
                                if (isCompiling) {
                                    Row(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(AmberAccent.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(12.dp),
                                            color = AmberAccent,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "BUILDING",
                                            color = AmberAccent,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                IconButton(
                                    onClick = { showGitHubModal = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CloudSync,
                                        contentDescription = "GitHub Actions CI/CD",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = CyberBackground,
                                titleContentColor = TextPrimary
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = CyberSurface,
                            contentColor = TextPrimary,
                            tonalElevation = 8.dp
                        ) {
                            // Tab 1: Projects
                            NavigationBarItem(
                                selected = currentTab == NavigationTab.PROJECTS,
                                onClick = { viewModel.setTab(NavigationTab.PROJECTS) },
                                icon = {
                                    BadgedBox(badge = {
                                        if (projects.isNotEmpty()) {
                                            Badge(containerColor = CyberSurfaceHighlight) {
                                                Text("${projects.size}", color = NeonCyan)
                                            }
                                        }
                                    }) {
                                        Icon(
                                            if (currentTab == NavigationTab.PROJECTS) Icons.Default.FolderZip else Icons.Default.Folder,
                                            contentDescription = "Projects"
                                        )
                                    }
                                },
                                label = { Text("Projects", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CyberBackground,
                                    selectedTextColor = NeonCyan,
                                    indicatorColor = NeonCyan,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("nav_tab_projects")
                            )

                            // Tab 2: Compiler
                            NavigationBarItem(
                                selected = currentTab == NavigationTab.COMPILER,
                                onClick = { viewModel.setTab(NavigationTab.COMPILER) },
                                icon = {
                                    BadgedBox(badge = {
                                        if (isCompiling) {
                                            Badge(containerColor = AmberAccent)
                                        }
                                    }) {
                                        Icon(
                                            if (currentTab == NavigationTab.COMPILER) Icons.Default.Construction else Icons.Default.Build,
                                            contentDescription = "Compiler"
                                        )
                                    }
                                },
                                label = { Text("Compiler", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CyberBackground,
                                    selectedTextColor = NeonCyan,
                                    indicatorColor = NeonCyan,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("nav_tab_compiler")
                            )

                            // Tab 3: Simulator
                            NavigationBarItem(
                                selected = currentTab == NavigationTab.SIMULATOR,
                                onClick = { viewModel.setTab(NavigationTab.SIMULATOR) },
                                icon = {
                                    Icon(
                                        if (currentTab == NavigationTab.SIMULATOR) Icons.Default.PhoneAndroid else Icons.Default.SmartDisplay,
                                        contentDescription = "Simulator"
                                    )
                                },
                                label = { Text("Simulator", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CyberBackground,
                                    selectedTextColor = NeonCyan,
                                    indicatorColor = NeonCyan,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("nav_tab_simulator")
                            )

                            // Tab 4: APKs & Downloads
                            NavigationBarItem(
                                selected = currentTab == NavigationTab.APKS,
                                onClick = { viewModel.setTab(NavigationTab.APKS) },
                                icon = {
                                    BadgedBox(badge = {
                                        if (artifacts.isNotEmpty()) {
                                            Badge(containerColor = EmeraldGreen) {
                                                Text("${artifacts.size}", color = Color.White)
                                            }
                                        }
                                    }) {
                                        Icon(
                                            if (currentTab == NavigationTab.APKS) Icons.Default.DownloadDone else Icons.Default.Download,
                                            contentDescription = "APKs"
                                        )
                                    }
                                },
                                label = { Text("APKs", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CyberBackground,
                                    selectedTextColor = NeonCyan,
                                    indicatorColor = NeonCyan,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("nav_tab_apks")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Crossfade(targetState = currentTab, label = "ScreenTransition") { tab ->
                            when (tab) {
                                NavigationTab.PROJECTS -> ProjectsScreen(viewModel = viewModel)
                                NavigationTab.COMPILER -> CompilerScreen(viewModel = viewModel)
                                NavigationTab.SIMULATOR -> SimulatorScreen(viewModel = viewModel)
                                NavigationTab.APKS -> ApksScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
