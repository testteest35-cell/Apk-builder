package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BuildArtifactEntity
import com.example.ui.MainViewModel
import com.example.ui.NavigationTab
import com.example.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApksScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val artifacts by viewModel.artifacts.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val context = LocalContext.current

    var selectedArtifactForDetail by remember { mutableStateOf<BuildArtifactEntity?>(null) }
    var pendingDownloadApkPath by remember { mutableStateOf<String?>(null) }

    val createDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.android.package-archive")
    ) { uri: Uri? ->
        if (uri != null && pendingDownloadApkPath != null) {
            viewModel.saveApkToDocument(pendingDownloadApkPath!!, uri)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DownloadForOffline, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "DOWNLOADABLE APK PACKAGES",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    "Export compiled APK binaries to storage, share with devices, or test in simulator.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Summary Stats Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceHighlight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItem(label = "Total APKs", value = "${artifacts.size}")
                    StatItem(
                        label = "Total Size",
                        value = "${artifacts.sumOf { it.apkSizeBytes } / 1024} KB"
                    )
                    StatItem(label = "Target Platform", value = "Android 7+")
                }
            }
        }

        // Artifacts List
        if (artifacts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Android, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No compiled APKs yet", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            "Select any project from the Projects tab and hit 'Compile APK' to build your first binary.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(artifacts) { artifact ->
                ApkCard(
                    artifact = artifact,
                    onDownload = {
                        pendingDownloadApkPath = artifact.apkPath
                        createDocLauncher.launch(File(artifact.apkPath).name)
                    },
                    onShare = {
                        val shareIntent = viewModel.exporter.getShareApkIntent(artifact.apkPath)
                        shareIntent?.let {
                            context.startActivity(Intent.createChooser(it, "Share APK: ${artifact.projectName}"))
                        }
                    },
                    onInstall = {
                        val installIntent = viewModel.exporter.getInstallApkIntent(artifact.apkPath)
                        installIntent?.let {
                            try {
                                context.startActivity(it)
                            } catch (e: Exception) {
                                // Ignore or handled
                            }
                        }
                    },
                    onInspect = {
                        selectedArtifactForDetail = artifact
                    },
                    onTestInSimulator = {
                        val matchingProj = projects.find { it.id == artifact.projectId }
                        if (matchingProj != null) {
                            viewModel.launchInSimulator(matchingProj)
                        } else {
                            viewModel.setTab(NavigationTab.SIMULATOR)
                        }
                    },
                    onDelete = {
                        viewModel.deleteArtifact(artifact)
                    }
                )
            }
        }
    }

    // Detail Dialog
    selectedArtifactForDetail?.let { artifact ->
        ApkDetailModal(
            artifact = artifact,
            onDismiss = { selectedArtifactForDetail = null },
            onCopyHash = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                clipboard?.setPrimaryClip(ClipData.newPlainText("SHA256", artifact.sha256))
            }
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = NeonCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextMuted, fontSize = 10.sp)
    }
}

@Composable
private fun ApkCard(
    artifact: BuildArtifactEntity,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    onInstall: () -> Unit,
    onInspect: () -> Unit,
    onTestInSimulator: () -> Unit,
    onDelete: () -> Unit
) {
    val apkFile = File(artifact.apkPath)
    val dateStr = SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.US).format(Date(artifact.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("apk_card_${artifact.projectName}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceHighlight)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(EmeraldGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.DownloadDone, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(artifact.projectName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(apkFile.name, color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Surface(
                    color = EmeraldGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "${artifact.apkSizeBytes / 1024} KB",
                        color = EmeraldGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metadata Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Version: v${artifact.versionName}", color = TextSecondary, fontSize = 11.sp)
                Text(dateStr, color = TextMuted, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Actions: Download APK, Test in Simulator, Share, Install, Inspect
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Download Button (Primary)
                Button(
                    onClick = onDownload,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).testTag("download_apk_button")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = CyberBackground, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Download", color = CyberBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Test in Simulator Button
                Button(
                    onClick = onTestInSimulator,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CyberBackground, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Simulate", color = CyberBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Share
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }

                // Inspect
                IconButton(
                    onClick = onInspect,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Inspect", tint = NeonCyan, modifier = Modifier.size(18.dp))
                }

                // Delete
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = RedError, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun ApkDetailModal(
    artifact: BuildArtifactEntity,
    onDismiss: () -> Unit,
    onCopyHash: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("APK Artifact Inspector", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow(label = "Application", value = artifact.projectName)
                DetailRow(label = "Package Name", value = artifact.packageName)
                DetailRow(label = "Version", value = "v${artifact.versionName}")
                DetailRow(label = "File Size", value = "${artifact.apkSizeBytes / 1024} KB (${artifact.apkSizeBytes} bytes)")
                DetailRow(label = "Build Time", value = "${artifact.durationMs} ms")
                DetailRow(label = "Permissions", value = artifact.permissions.ifBlank { "None" })
                Divider(color = CyberSurfaceHighlight, modifier = Modifier.padding(vertical = 4.dp))
                Text("SHA-256 Checksum:", color = TextSecondary, fontSize = 11.sp)
                Text(
                    artifact.sha256,
                    color = NeonCyan,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TerminalBackground, RoundedCornerShape(6.dp))
                        .padding(8.dp)
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onCopyHash) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy SHA256")
                }
                Button(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextMuted, fontSize = 11.sp)
        Text(value, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}
