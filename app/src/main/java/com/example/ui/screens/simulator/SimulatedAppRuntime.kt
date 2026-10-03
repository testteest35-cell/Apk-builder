package com.example.ui.screens.simulator

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectEntity
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun SimulatedAppRuntime(
    project: ProjectEntity?,
    isDarkMode: Boolean,
    onLogcatEvent: (priority: String, tag: String, message: String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (project == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(if (isDarkMode) Color(0xFF121824) else Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No app loaded in simulator.\nSelect or build a project to start.",
                color = if (isDarkMode) Color.White else Color.Black,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val context = LocalContext.current
    fun triggerHaptic() {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(25)
            }
        } catch (_: Exception) {}
    }

    val slug = project.originalZipName.lowercase()
    val name = project.name.lowercase()

    when {
        slug.contains("quantum") || name.contains("quantum") || project.appCategory == "PRODUCTIVITY" -> {
            QuantumPulseApp(
                project = project,
                isDarkMode = isDarkMode,
                onLog = onLogcatEvent,
                onHaptic = { triggerHaptic() },
                modifier = modifier
            )
        }
        slug.contains("calc") || name.contains("calc") || project.appCategory == "CALCULATOR" -> {
            NeumorphicCalcApp(
                project = project,
                isDarkMode = isDarkMode,
                onLog = onLogcatEvent,
                onHaptic = { triggerHaptic() },
                modifier = modifier
            )
        }
        slug.contains("paint") || name.contains("paint") || project.appCategory == "CREATIVE" -> {
            RetroPainterApp(
                project = project,
                isDarkMode = isDarkMode,
                onLog = onLogcatEvent,
                onHaptic = { triggerHaptic() },
                modifier = modifier
            )
        }
        slug.contains("space") || slug.contains("arcade") || project.appCategory == "ARCADE" -> {
            SpaceArcadeApp(
                project = project,
                isDarkMode = isDarkMode,
                onLog = onLogcatEvent,
                onHaptic = { triggerHaptic() },
                modifier = modifier
            )
        }
        else -> {
            GenericImportedApp(
                project = project,
                isDarkMode = isDarkMode,
                onLog = onLogcatEvent,
                onHaptic = { triggerHaptic() },
                modifier = modifier
            )
        }
    }
}

// ----------------------------------------------------
// 1. Quantum Pulse (Productivity & Focus Timer)
// ----------------------------------------------------
@Composable
private fun QuantumPulseApp(
    project: ProjectEntity,
    isDarkMode: Boolean,
    onLog: (String, String, String) -> Unit,
    onHaptic: () -> Unit,
    modifier: Modifier
) {
    val bg = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val textPrimary = if (isDarkMode) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val cardBg = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFFFFFFF)

    var timerSeconds by remember { mutableIntStateOf(25 * 60) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var completedSessions by remember { mutableIntStateOf(3) }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning && timerSeconds > 0) {
            delay(1000)
            timerSeconds--
            if (timerSeconds == 0) {
                isTimerRunning = false
                completedSessions++
                onLog("I", "FocusEngine", "Pomodoro interval finished! Cycle #$completedSessions")
                onHaptic()
            }
        }
    }

    var tasks by remember {
        mutableStateOf(
            listOf(
                "Refactor Kotlin coroutines dispatcher" to true,
                "Audit DEX byte alignments" to true,
                "Implement Material 3 Dynamic Shapes" to false,
                "Profile memory leaks with LeakCanary" to false
            )
        )
    }
    var newTaskText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .padding(12.dp)
    ) {
        // App bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(project.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textPrimary)
                Text("v${project.versionName} • Productivity Suite", fontSize = 11.sp, color = Color(0xFF06B6D4))
            }
            Badge(containerColor = Color(0xFF10B981)) {
                Text("$completedSessions Cycles Done", color = Color.White, modifier = Modifier.padding(4.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Timer Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("DEEP FOCUS INTERVAL", fontSize = 11.sp, color = Color(0xFF94A3B8), letterSpacing = 1.sp)
                val mins = timerSeconds / 60
                val secs = timerSeconds % 60
                Text(
                    text = String.format("%02d:%02d", mins, secs),
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF06B6D4)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            isTimerRunning = !isTimerRunning
                            onHaptic()
                            onLog("I", "TimerControl", if (isTimerRunning) "Timer started" else "Timer paused")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTimerRunning) Color(0xFFEF4444) else Color(0xFF10B981)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isTimerRunning) "Pause" else "Focus")
                    }

                    OutlinedButton(
                        onClick = {
                            isTimerRunning = false
                            timerSeconds = 25 * 60
                            onHaptic()
                            onLog("D", "TimerControl", "Timer reset to 25:00")
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tasks Header
        Text("Objectives & Sprint Tasks", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = textPrimary)
        Spacer(modifier = Modifier.height(6.dp))

        // Add task input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newTaskText,
                onValueChange = { newTaskText = it },
                placeholder = { Text("New task...", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(
                onClick = {
                    if (newTaskText.isNotBlank()) {
                        tasks = listOf(newTaskText.trim() to false) + tasks
                        onLog("I", "TaskManager", "Added task: ${newTaskText.trim()}")
                        newTaskText = ""
                        onHaptic()
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFF06B6D4), RoundedCornerShape(10.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(tasks.indices.toList()) { idx ->
                val (tTitle, tDone) = tasks[idx]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardBg, RoundedCornerShape(8.dp))
                        .clickable {
                            tasks = tasks.toMutableList().also { it[idx] = tTitle to !tDone }
                            onHaptic()
                            onLog("D", "TaskManager", "Toggled task '$tTitle' -> ${!tDone}")
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = tDone,
                        onCheckedChange = { checked ->
                            tasks = tasks.toMutableList().also { it[idx] = tTitle to checked }
                            onHaptic()
                            onLog("D", "TaskManager", "Toggled task '$tTitle' -> $checked")
                        }
                    )
                    Text(
                        text = tTitle,
                        fontSize = 12.sp,
                        color = if (tDone) Color(0xFF94A3B8) else textPrimary,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 6.dp)
                    )
                    IconButton(
                        onClick = {
                            val removed = tasks[idx].first
                            tasks = tasks.toMutableList().also { it.removeAt(idx) }
                            onLog("D", "TaskManager", "Deleted task '$removed'")
                            onHaptic()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 2. Neumorphic Calc (Precision Scientific Calculator)
// ----------------------------------------------------
@Composable
private fun NeumorphicCalcApp(
    project: ProjectEntity,
    isDarkMode: Boolean,
    onLog: (String, String, String) -> Unit,
    onHaptic: () -> Unit,
    modifier: Modifier
) {
    val bg = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFE2E8F0)
    val textPrimary = if (isDarkMode) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val displayBg = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFF1F5F9)

    var displayValue by remember { mutableStateOf("0") }
    var expression by remember { mutableStateOf("") }
    var history by remember { mutableStateOf(listOf("12 × 8 = 96", "sqrt(144) = 12")) }

    fun onKey(key: String) {
        onHaptic()
        onLog("D", "CalcEngine", "Key pressed: $key")
        when (key) {
            "C" -> {
                displayValue = "0"
                expression = ""
            }
            "DEL" -> {
                displayValue = if (displayValue.length > 1) displayValue.dropLast(1) else "0"
            }
            "±" -> {
                displayValue = if (displayValue.startsWith("-")) displayValue.substring(1) else "-$displayValue"
            }
            "%" -> {
                val num = displayValue.toDoubleOrNull() ?: 0.0
                displayValue = (num / 100.0).toString()
            }
            "√" -> {
                val num = displayValue.toDoubleOrNull() ?: 0.0
                val res = kotlin.math.sqrt(num)
                history = listOf("√$displayValue = $res") + history.take(4)
                displayValue = res.toString()
            }
            "sin" -> {
                val num = displayValue.toDoubleOrNull() ?: 0.0
                val res = sin(Math.toRadians(num))
                history = listOf("sin($displayValue) = $res") + history.take(4)
                displayValue = res.toString()
            }
            "=" -> {
                val res = try {
                    evaluateSimpleExpr(expression + displayValue)
                } catch (_: Exception) {
                    "Error"
                }
                history = listOf("${expression}${displayValue} = $res") + history.take(4)
                displayValue = res
                expression = ""
                onLog("I", "CalcEngine", "Computed: $res")
            }
            "+", "-", "×", "÷" -> {
                expression = "$displayValue $key "
                displayValue = "0"
            }
            else -> {
                displayValue = if (displayValue == "0") key else displayValue + key
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .padding(10.dp)
    ) {
        // App header
        Text(project.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textPrimary)
        Text("Scientific Precision • 64-bit Engine", fontSize = 10.sp, color = Color(0xFF06B6D4))

        Spacer(modifier = Modifier.height(6.dp))

        // Display
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = displayBg),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    expression.ifEmpty { history.firstOrNull() ?: "Ready" },
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    displayValue,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Keypad buttons
        val rows = listOf(
            listOf("C", "DEL", "±", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("√", "0", ".", "=")
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            rows.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    row.forEach { k ->
                        val isOp = k in listOf("÷", "×", "-", "+", "=")
                        val isSpecial = k in listOf("C", "DEL", "±", "√")
                        val btnColor = when {
                            k == "=" -> Color(0xFF10B981)
                            isOp -> Color(0xFF06B6D4)
                            isSpecial -> if (isDarkMode) Color(0xFF334155) else Color(0xFFCBD5E1)
                            else -> if (isDarkMode) Color(0xFF1E293B) else Color(0xFFFFFFFF)
                        }
                        val textColor = when {
                            k == "=" || isOp -> Color.White
                            else -> textPrimary
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .background(btnColor)
                                .clickable { onKey(k) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(k, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
                        }
                    }
                }
            }
        }
    }
}

private fun evaluateSimpleExpr(expr: String): String {
    val clean = expr.trim()
    val parts = clean.split(" ")
    if (parts.size >= 3) {
        val a = parts[0].toDoubleOrNull() ?: 0.0
        val op = parts[1]
        val b = parts[2].toDoubleOrNull() ?: 0.0
        val res = when (op) {
            "+" -> a + b
            "-" -> a - b
            "×" -> a * b
            "÷" -> if (b != 0.0) a / b else return "DIV/0"
            else -> a
        }
        return if (res % 1.0 == 0.0) res.toLong().toString() else "%.4f".format(res)
    }
    return expr
}

// ----------------------------------------------------
// 3. Retro Pixel Painter (Touch Canvas Drawing)
// ----------------------------------------------------
@Composable
private fun RetroPainterApp(
    project: ProjectEntity,
    isDarkMode: Boolean,
    onLog: (String, String, String) -> Unit,
    onHaptic: () -> Unit,
    modifier: Modifier
) {
    val colors = listOf(
        Color(0xFF22D3EE),
        Color(0xFF10B981),
        Color(0xFFF59E0B),
        Color(0xFFEF4444),
        Color(0xFFA855F7),
        Color(0xFFEC4899),
        Color(0xFFFFFFFF),
        Color(0xFF000000)
    )
    var selectedColor by remember { mutableStateOf(colors[0]) }
    var brushStrokeWidth by remember { mutableFloatStateOf(8f) }
    var lines by remember { mutableStateOf(listOf<DrawStroke>()) }
    var currentLine by remember { mutableStateOf<List<Offset>?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1D))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(project.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                Text("Pixel Canvas • Touch Drawing Engine", fontSize = 10.sp, color = Color(0xFF06B6D4))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = {
                        if (lines.isNotEmpty()) {
                            lines = lines.dropLast(1)
                            onHaptic()
                            onLog("D", "Canvas", "Undo stroke")
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Undo, contentDescription = "Undo", tint = Color.White, modifier = Modifier.size(18.dp))
                }
                IconButton(
                    onClick = {
                        lines = emptyList()
                        onHaptic()
                        onLog("I", "Canvas", "Canvas cleared")
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Drawing Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF151C2C))
                .border(1.dp, Color(0xFF26334D), RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentLine = listOf(offset)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentLine = (currentLine ?: emptyList()) + change.position
                        },
                        onDragEnd = {
                            currentLine?.let {
                                lines = lines + DrawStroke(it, selectedColor, brushStrokeWidth)
                                onLog("D", "Canvas", "Stroke completed with ${it.size} points")
                            }
                            currentLine = null
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw completed lines
                lines.forEach { stroke ->
                    for (i in 0 until stroke.points.size - 1) {
                        drawLine(
                            color = stroke.color,
                            start = stroke.points[i],
                            end = stroke.points[i + 1],
                            strokeWidth = stroke.width,
                            cap = StrokeCap.Round
                        )
                    }
                }
                // Draw current line
                currentLine?.let { pts ->
                    for (i in 0 until pts.size - 1) {
                        drawLine(
                            color = selectedColor,
                            start = pts[i],
                            end = pts[i + 1],
                            strokeWidth = brushStrokeWidth,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Color Palette
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            colors.forEach { c ->
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(c)
                        .border(
                            width = if (selectedColor == c) 3.dp else 1.dp,
                            color = if (selectedColor == c) Color.White else Color.Gray,
                            shape = CircleShape
                        )
                        .clickable {
                            selectedColor = c
                            onHaptic()
                            onLog("D", "Canvas", "Selected palette color")
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Brush Width Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Brush: ${brushStrokeWidth.toInt()}px", fontSize = 11.sp, color = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Slider(
                value = brushStrokeWidth,
                onValueChange = { brushStrokeWidth = it },
                valueRange = 2f..28f,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private data class DrawStroke(val points: List<Offset>, val color: Color, val width: Float)

// ----------------------------------------------------
// 4. Space Dodge Arcade (60FPS Interactive Game)
// ----------------------------------------------------
@Composable
private fun SpaceArcadeApp(
    project: ProjectEntity,
    isDarkMode: Boolean,
    onLog: (String, String, String) -> Unit,
    onHaptic: () -> Unit,
    modifier: Modifier
) {
    var playerX by remember { mutableFloatStateOf(0.5f) } // normalized 0f to 1f
    var score by remember { mutableIntStateOf(0) }
    var highScore by remember { mutableIntStateOf(1420) }
    var isGameOver by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }

    data class Asteroid(val id: Int, var x: Float, var y: Float, val speed: Float, val radius: Float)
    var asteroids by remember { mutableStateOf(listOf<Asteroid>()) }

    // Game loop
    LaunchedEffect(isPlaying, isGameOver) {
        var nextId = 0
        while (isPlaying && !isGameOver) {
            delay(33) // ~30-60 FPS tick
            score += 2

            // spawn asteroids
            if (Random.nextFloat() < 0.15f) {
                asteroids = asteroids + Asteroid(
                    id = nextId++,
                    x = Random.nextFloat(),
                    y = -0.05f,
                    speed = Random.nextFloat() * 0.018f + 0.012f,
                    radius = Random.nextFloat() * 0.04f + 0.03f
                )
            }

            // update asteroids
            val updated = mutableListOf<Asteroid>()
            for (ast in asteroids) {
                ast.y += ast.speed
                // collision check (player is around y=0.88f)
                val dx = ast.x - playerX
                val dy = ast.y - 0.88f
                val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                if (dist < ast.radius + 0.05f) {
                    isGameOver = true
                    if (score > highScore) highScore = score
                    onHaptic()
                    onLog("W", "GameEngine", "COLLISION DETECTED! Game Over. Final Score: $score")
                    break
                }
                if (ast.y < 1.1f) {
                    updated.add(ast)
                }
            }
            if (!isGameOver) asteroids = updated
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF030712))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(project.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            Text("Score: $score | Best: $highScore", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Game Arena
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val newX = (change.position.x / size.width).coerceIn(0.08f, 0.92f)
                        playerX = newX
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Starfield background
                for (i in 0..25) {
                    val sx = (sin(i * 123.4f) * 0.5f + 0.5f) * w
                    val sy = (sin(i * 456.7f) * 0.5f + 0.5f) * h
                    drawCircle(Color(0x44FFFFFF), radius = 1.5f, center = Offset(sx, sy))
                }

                // Draw asteroids
                asteroids.forEach { ast ->
                    drawCircle(
                        color = Color(0xFFEF4444),
                        radius = ast.radius * w,
                        center = Offset(ast.x * w, ast.y * h)
                    )
                }

                // Draw player ship
                val px = playerX * w
                val py = 0.88f * h
                val shipPath = Path().apply {
                    moveTo(px, py - 18.dp.toPx())
                    lineTo(px - 14.dp.toPx(), py + 12.dp.toPx())
                    lineTo(px, py + 6.dp.toPx())
                    lineTo(px + 14.dp.toPx(), py + 12.dp.toPx())
                    close()
                }
                drawPath(shipPath, color = Color(0xFF22D3EE))
                drawCircle(Color(0xFFFBBF24), radius = 4.dp.toPx(), center = Offset(px, py + 8.dp.toPx()))
            }

            if (isGameOver) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xCC000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("GAME OVER", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFEF4444))
                        Text("Score: $score", fontSize = 16.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                score = 0
                                asteroids = emptyList()
                                isGameOver = false
                                onHaptic()
                                onLog("I", "GameEngine", "New Game Started")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text("Play Again")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Touch Control Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    playerX = (playerX - 0.12f).coerceAtLeast(0.08f)
                    onHaptic()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Left", tint = Color.White)
                Text(" Left")
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = {
                    playerX = (playerX + 0.12f).coerceAtMost(0.92f)
                    onHaptic()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
            ) {
                Text("Right ")
                Icon(Icons.Default.ArrowForward, contentDescription = "Right", tint = Color.White)
            }
        }
    }
}

// ----------------------------------------------------
// 5. Generic Imported App (Dynamic UI Simulator)
// ----------------------------------------------------
@Composable
private fun GenericImportedApp(
    project: ProjectEntity,
    isDarkMode: Boolean,
    onLog: (String, String, String) -> Unit,
    onHaptic: () -> Unit,
    modifier: Modifier
) {
    val bg = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val textPrimary = if (isDarkMode) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val cardBg = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFFFFFFF)

    var inputMessage by remember { mutableStateOf("") }
    var notificationEnabled by remember { mutableStateOf(true) }
    var logsCount by remember { mutableIntStateOf(1) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .padding(12.dp)
    ) {
        // App header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(project.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textPrimary)
                Text(project.packageName, fontSize = 11.sp, color = Color(0xFF06B6D4))
            }
            Badge(containerColor = Color(0xFF10B981)) {
                Text("RUNNING", color = Color.White, modifier = Modifier.padding(4.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Component Sandbox", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary)
                Text("Parsed from extracted AndroidManifest.xml & bytecode", fontSize = 11.sp, color = Color(0xFF94A3B8))

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = inputMessage,
                    onValueChange = { inputMessage = it },
                    label = { Text("Simulate Input Event") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Push Notifications", fontSize = 13.sp, color = textPrimary)
                    Switch(
                        checked = notificationEnabled,
                        onCheckedChange = {
                            notificationEnabled = it
                            onHaptic()
                            onLog("I", "NotificationManager", "Notification permission toggled: $it")
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        logsCount++
                        onHaptic()
                        onLog("I", "MainActivity", "Interactive button tapped! Msg: '${inputMessage.ifBlank { "Default test click" }}'")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06B6D4))
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Trigger Activity Action", color = Color.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Manifest Metadata", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                Text("• Version: ${project.versionName} (code ${project.versionCode})", fontSize = 12.sp, color = Color(0xFF94A3B8))
                Text("• Min SDK: ${project.minSdk} | Target SDK: ${project.targetSdk}", fontSize = 12.sp, color = Color(0xFF94A3B8))
                Text("• Total Project Files: ${project.fileCount}", fontSize = 12.sp, color = Color(0xFF94A3B8))
                Text("• Source Size: ${project.sourceSizeBytes / 1024} KB", fontSize = 12.sp, color = Color(0xFF94A3B8))
                Text("• Original Archive: ${project.originalZipName}", fontSize = 12.sp, color = Color(0xFF94A3B8))
            }
        }
    }
}
