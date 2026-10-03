package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectFileNode
import com.example.engine.SampleProjectType
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val projects by viewModel.projects.collectAsState()
    val selectedProject by viewModel.selectedProject.collectAsState()
    val fileTree by viewModel.projectFileTree.collectAsState()
    val editingFilePath by viewModel.selectedFilePath.collectAsState()
    val editingFileContent by viewModel.selectedFileContent.collectAsState()

    var showFileExplorerDialog by remember { mutableStateOf(false) }

    val zipPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importZipUri(it) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.banner_apk_compiler_1791045126089),
                        contentDescription = "ApkCraft Compiler Workbench",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0x66090D16), Color(0xF0090D16))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Build, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "APK FORGE & SIMULATOR",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp
                            )
                        }
                        Text(
                            "Compile ZIP Projects into Downloadable APKs",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Import source code, run Gradle build engine, and test in real-time sandbox simulator.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 2
                        )
                    }
                }
            }
        }

        // Action Buttons Row: Import ZIP from Storage
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        zipPickerLauncher.launch(
                            arrayOf(
                                "application/zip",
                                "application/x-zip-compressed",
                                "application/octet-stream",
                                "*/*"
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("import_zip_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = CyberBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Import ZIP Source", color = CyberBackground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        // Quick Starter Templates Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "STARTER CODEBASES",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text("1-Tap Import", color = NeonCyan, fontSize = 11.sp)
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        StarterCard(
                            title = "Quantum Pulse",
                            category = "Productivity",
                            icon = Icons.Default.Timer,
                            badgeColor = EmeraldGreen,
                            onClick = { viewModel.loadStarterProject(SampleProjectType.QUANTUM_PULSE) }
                        )
                    }
                    item {
                        StarterCard(
                            title = "Neumorphic Calc",
                            category = "Calculator",
                            icon = Icons.Default.Calculate,
                            badgeColor = NeonCyan,
                            onClick = { viewModel.loadStarterProject(SampleProjectType.NEUMORPHIC_CALC) }
                        )
                    }
                    item {
                        StarterCard(
                            title = "Retro Painter",
                            category = "Touch Canvas",
                            icon = Icons.Default.Palette,
                            badgeColor = VioletAccent,
                            onClick = { viewModel.loadStarterProject(SampleProjectType.RETRO_PAINTER) }
                        )
                    }
                    item {
                        StarterCard(
                            title = "Space Arcade",
                            category = "60FPS Game",
                            icon = Icons.Default.RocketLaunch,
                            badgeColor = AmberAccent,
                            onClick = { viewModel.loadStarterProject(SampleProjectType.SPACE_ARCADE) }
                        )
                    }
                }
            }
        }

        // Projects List Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "IMPORTED CODEBASES (${projects.size})",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        // Projects List Items
        if (projects.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.FolderZip, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No projects imported yet", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            "Tap 'Import ZIP Source' to load an Android project from your storage, or pick a starter codebase above.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(projects) { project ->
                ProjectCard(
                    project = project,
                    isSelected = project.id == selectedProject?.id,
                    onSelect = {
                        viewModel.selectProject(project)
                    },
                    onCompile = {
                        viewModel.startCompilation(project)
                    },
                    onSimulate = {
                        viewModel.launchInSimulator(project)
                    },
                    onViewFiles = {
                        viewModel.selectProject(project)
                        showFileExplorerDialog = true
                    },
                    onDelete = {
                        viewModel.deleteProject(project)
                    }
                )
            }
        }
    }

    // File Explorer & Code Editor Dialog
    if (showFileExplorerDialog && selectedProject != null) {
        FileExplorerModal(
            project = selectedProject!!,
            fileTree = fileTree,
            editingPath = editingFilePath,
            editingContent = editingFileContent,
            onOpenFile = { node -> viewModel.openFile(node) },
            onCloseEditor = { viewModel.closeFileEditor() },
            onSaveContent = { newCode -> viewModel.saveFileEdit(newCode) },
            onDismiss = {
                showFileExplorerDialog = false
                viewModel.closeFileEditor()
            }
        )
    }
}

@Composable
private fun StarterCard(
    title: String,
    category: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceHighlight)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(category, color = TextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
private fun ProjectCard(
    project: ProjectEntity,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onCompile: () -> Unit,
    onSimulate: () -> Unit,
    onViewFiles: () -> Unit,
    onDelete: () -> Unit
) {
    val borderColor = if (isSelected) NeonCyan else CyberSurfaceHighlight

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("project_card_${project.name}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        border = androidx.compose.foundation.BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberSurfaceHighlight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Android, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(project.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(project.packageName, color = TextSecondary, fontSize = 11.sp)
                    }
                }

                // Category Tag
                Surface(
                    color = CyberSurfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        project.appCategory,
                        color = NeonCyanBright,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Specs Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SpecBadge(label = "Version", value = "v${project.versionName}")
                SpecBadge(label = "Files", value = "${project.fileCount}")
                SpecBadge(label = "Size", value = "${project.sourceSizeBytes / 1024} KB")
                SpecBadge(label = "Target", value = "API ${project.targetSdk}")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Pill
                val (statusText, statusBg, statusFg) = when (project.lastBuildStatus) {
                    "SUCCESS" -> Triple("Compiled APK Ready", EmeraldGreen.copy(alpha = 0.2f), EmeraldGreen)
                    "BUILDING" -> Triple("Compiling...", AmberAccent.copy(alpha = 0.2f), AmberAccent)
                    "FAILED" -> Triple("Build Failed", RedError.copy(alpha = 0.2f), RedError)
                    else -> Triple("Not Compiled", TextMuted.copy(alpha = 0.2f), TextMuted)
                }

                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        statusText,
                        color = statusFg,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // View Files / Code
                    IconButton(
                        onClick = onViewFiles,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Code, contentDescription = "View Files", tint = NeonCyan, modifier = Modifier.size(18.dp))
                    }

                    // Run in Simulator
                    IconButton(
                        onClick = onSimulate,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.PlayCircle, contentDescription = "Simulator", tint = EmeraldGreen, modifier = Modifier.size(20.dp))
                    }

                    // Compile Button
                    Button(
                        onClick = onCompile,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Construction, contentDescription = null, tint = CyberBackground, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Compile", color = CyberBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // Delete
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecBadge(label: String, value: String) {
    Column {
        Text(label, color = TextMuted, fontSize = 9.sp)
        Text(value, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

// ----------------------------------------------------
// File Explorer & Code Editor Modal
// ----------------------------------------------------
@Composable
private fun FileExplorerModal(
    project: ProjectEntity,
    fileTree: ProjectFileNode?,
    editingPath: String?,
    editingContent: String?,
    onOpenFile: (ProjectFileNode) -> Unit,
    onCloseEditor: () -> Unit,
    onSaveContent: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var editorText by remember(editingContent) { mutableStateOf(editingContent ?: "") }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .padding(8.dp),
            color = CyberSurface,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceHighlight)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                // Modal Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(project.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            if (editingPath != null) editingPath.substringAfterLast("/") else "Source Explorer (${project.fileCount} files)",
                            color = NeonCyan,
                            fontSize = 11.sp
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (editingPath != null) {
                            TextButton(
                                onClick = { onSaveContent(editorText) },
                                colors = ButtonDefaults.textButtonColors(contentColor = EmeraldGreen)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save")
                            }
                            IconButton(onClick = onCloseEditor) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back to Files", tint = TextSecondary)
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }
                }

                Divider(color = CyberSurfaceHighlight, modifier = Modifier.padding(vertical = 8.dp))

                // Content View: Code Editor OR File Tree
                if (editingPath != null) {
                    // Code Editor view
                    OutlinedTextField(
                        value = editorText,
                        onValueChange = { editorText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = TextPrimary
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = TerminalBackground,
                            unfocusedContainerColor = TerminalBackground,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberSurfaceHighlight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                } else {
                    // File Tree view
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (fileTree != null) {
                            renderFileNode(fileTree, depth = 0, onOpenFile = onOpenFile)
                        } else {
                            item {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = NeonCyan)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.renderFileNode(
    node: ProjectFileNode,
    depth: Int,
    onOpenFile: (ProjectFileNode) -> Unit
) {
    if (depth > 0) { // skip root wrapper
        item(key = node.absolutePath) {
            val icon = if (node.isDirectory) {
                Icons.Default.Folder
            } else {
                when (node.extension) {
                    "kt", "java" -> Icons.Default.Code
                    "xml" -> Icons.Default.Description
                    "gradle", "kts" -> Icons.Default.Settings
                    "png", "jpg", "webp" -> Icons.Default.Image
                    else -> Icons.Default.InsertDriveFile
                }
            }

            val iconColor = if (node.isDirectory) AmberAccent else NeonCyan

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = (depth * 14).dp)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        if (!node.isDirectory) onOpenFile(node)
                    }
                    .padding(vertical = 6.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = node.name,
                    color = if (node.isDirectory) TextPrimary else TextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (node.isDirectory) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
                if (!node.isDirectory && node.sizeBytes > 0) {
                    Text(
                        "${node.sizeBytes} B",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }

    if (node.isDirectory) {
        node.children.forEach { child ->
            renderFileNode(child, depth + 1, onOpenFile)
        }
    }
}
