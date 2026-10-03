package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

@Composable
fun GitHubActionsModal(
    onDismiss: () -> Unit,
    onCopySuccess: () -> Unit
) {
    val context = LocalContext.current

    val workflowYaml = """
name: Build Android APK

on:
  push:
    branches: [ main, master ]
  pull_request:
    branches: [ main, master ]
  workflow_dispatch:
    inputs:
      build_type:
        description: 'Build Type'
        required: true
        default: 'debug'
        type: choice
        options: [ 'debug', 'release', 'both' ]

jobs:
  build:
    name: Build & Package APK
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'gradle'
      - name: Restore Keystore & Env
        run: |
          if [ -f "debug.keystore.base64" ]; then
            base64 -d debug.keystore.base64 > debug.keystore
          fi
          [ ! -f ".env" ] && cp .env.example .env
      - name: Grant Execute Permission
        run: chmod +x gradlew
      - name: Build Debug APK
        run: ./gradlew assembleDebug --stacktrace
      - name: Upload Debug APK
        uses: actions/upload-artifact@v4
        with:
          name: ApkCraft-Debug-APK
          path: app/build/outputs/apk/debug/*.apk
    """.trimIndent()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .padding(8.dp),
            color = CyberSurface,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceHighlight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(VioletAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = VioletAccentLight, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("GitHub Actions CI/CD", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Automated APK Builder & Artifacts", color = NeonCyan, fontSize = 11.sp)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Divider(color = CyberSurfaceHighlight, modifier = Modifier.padding(vertical = 10.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Step instructions
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("HOW IT WORKS", color = AmberAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                InstructionStep(num = "1", text = "Push this project to your GitHub repository (via AI Studio settings or git push).")
                                InstructionStep(num = "2", text = "The workflow file at '.github/workflows/build-apk.yml' triggers on every push or pull request.")
                                InstructionStep(num = "3", text = "GitHub Actions spins up an Ubuntu VM, sets up Java 17, and runs './gradlew assembleDebug'.")
                                InstructionStep(num = "4", text = "Download the ready APK directly from the GitHub Actions run under 'Artifacts'.")
                            }
                        }
                    }

                    // Workflow file preview
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "WORKFLOW CONFIG (.github/workflows/build-apk.yml)",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        clipboard?.setPrimaryClip(ClipData.newPlainText("GitHub Workflow", workflowYaml))
                                        onCopySuccess()
                                    },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy YAML", fontSize = 11.sp, color = NeonCyan)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TerminalBackground)
                                    .border(1.dp, CyberSurfaceHighlight, RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = workflowYaml,
                                    color = TextPrimary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Got It", color = CyberBackground, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun InstructionStep(num: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(
            color = CyberSurfaceHighlight,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.size(18.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(num, color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = TextPrimary, fontSize = 11.sp, lineHeight = 15.sp)
    }
}
