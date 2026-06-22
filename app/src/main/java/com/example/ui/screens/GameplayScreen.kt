package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.LevelSpec
import com.example.ui.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.blur

@Composable
fun SchoolBackground(theme: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        AsyncImage(
            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuD262uVQL6SmFuBnicY-7BRCVTVPDI8aDpWvd3d1ghhPgTLInUheAjCHSY9a23a7BtFinkbD4E5Qtv0rbpsSp5OtkjCz4xLizw-_UK4fvqkYOQZf-w6y2dbFZwk8PbyQGYcIBcB49ByWGhBWTCjhfwtqoSq1wr4TBvQ9tXgaxUhyomP6HGpttErSSt6YXhAU6_P7coyxAoSBJPRgu1Y3kwVNkXo2yT8agZF9a1WvNQd0IWAw8ennHwiL_Yn4rJ1NUAkHDCb-umfMNw-",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(6.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.15f),
                            Color.Black.copy(alpha = 0.35f)
                        )
                    )
                )
        )
    }
}

// Custom interactive scale-down bounce click button modifier
@Composable
fun BounceButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium)
    )

    Box(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        isPressed = true
                        val up = waitForUpOrCancellation()
                        isPressed = false
                        if (up != null) {
                            onClick()
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

private suspend fun androidx.compose.ui.input.pointer.AwaitPointerEventScope.awaitFirstDown(
    requireUnconsumed: Boolean = true
): androidx.compose.ui.input.pointer.PointerInputChange {
    var event: androidx.compose.ui.input.pointer.PointerEvent
    do {
        event = awaitPointerEvent()
    } while (!event.changes.any { if (requireUnconsumed) !it.isConsumed && it.pressed else it.pressed })
    val change = event.changes.first { if (requireUnconsumed) !it.isConsumed && it.pressed else it.pressed }
    return change
}

private suspend fun androidx.compose.ui.input.pointer.AwaitPointerEventScope.waitForUpOrCancellation(): androidx.compose.ui.input.pointer.PointerInputChange? {
    while (true) {
        val event = awaitPointerEvent()
        val change = event.changes.firstOrNull() ?: return null
        if (!change.pressed) {
            return change
        }
        val isCanceled = event.changes.any { it.isConsumed }
        if (isCanceled) {
            return null
        }
    }
}


@Composable
fun GlassCircleButton(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    badgeText: String? = null,
    badgeColor: Color = Color(0xFFFF5722)
) {
    BounceButton(onClick = onClick, modifier = modifier) {
        Box(
            modifier = Modifier.size(60.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .align(Alignment.TopCenter)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .border(1.5.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            if (badgeText != null) {
                Box(
                    modifier = Modifier
                        .height(18.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(badgeColor)
                        .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(9.dp))
                        .padding(horizontal = 6.dp)
                        .align(Alignment.BottomCenter),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeText,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun PauseDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onQuit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var soundEffectsEnabled by remember { mutableStateOf(true) }
    var bgMusicEnabled by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(310.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White.copy(alpha = 0.12f))
                .border(2.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(32.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PAUSED",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // RESUME BUTTON
            TactileGradientButton(
                onClick = onResume,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "RESUME",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // RESTART BUTTON
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(26.dp))
                    .clickable { onRestart() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RESTART",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SOUND EFFECTS TOGGLE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (soundEffectsEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sound Effects",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Switch(
                    checked = soundEffectsEnabled,
                    onCheckedChange = { soundEffectsEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFFF5722),
                        uncheckedThumbColor = Color.LightGray,
                        uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
                    )
                )
            }

            // MUSIC TOGGLE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Background Music",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Switch(
                    checked = bgMusicEnabled,
                    onCheckedChange = { bgMusicEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFFF5722),
                        uncheckedThumbColor = Color.LightGray,
                        uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // QUIT LEVEL TEXT
            Text(
                text = "QUIT LEVEL",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onQuit() }
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Decor / Zen Subtext
            Text(
                text = "🪷 ZEN MODE ACTIVE",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun GameLetterWheel(
    letters: List<Char>,
    colorStyle: String,
    onSwipeUpdate: (String) -> Unit,
    onSwipeComplete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Elegant Orange connected tracking color
    val trackOrange = Color(0xFFFF5722)
    val unselectedTextCharcoal = Color(0xFF271813)

    BoxWithConstraints(
        modifier = modifier
            .size(280.dp)
            .background(Color.White.copy(alpha = 0.15f), shape = CircleShape)
            .border(1.5.dp, Color.White.copy(alpha = 0.25f), CircleShape)
            .testTag("circular_letter_wheel")
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val center = Offset(widthPx / 2f, heightPx / 2f)
        val radius = widthPx * 0.35f

        val numLetters = letters.size
        val density = LocalDensity.current
        val collisionRadiusPx = with(density) { 32.dp.toPx() }

        val positions = remember(letters, widthPx, heightPx) {
            (0 until numLetters).map { i ->
                val angle = -Math.PI / 2 + i * 2 * Math.PI / numLetters
                Offset(
                    center.x + radius * cos(angle).toFloat(),
                    center.y + radius * sin(angle).toFloat()
                )
            }
        }

        val selectedIndices = remember { mutableStateListOf<Int>() }
        var currentTouchPos by remember { mutableStateOf<Offset?>(null) }

        // Find closest colliding node
        fun findCollidingNode(touch: Offset): Int {
            for (i in positions.indices) {
                val node = positions[i]
                val distSq = (touch.x - node.x) * (touch.x - node.x) + (touch.y - node.y) * (touch.y - node.y)
                if (distSq < collisionRadiusPx * collisionRadiusPx) {
                    return i
                }
            }
            return -1
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(letters, positions) {
                    awaitPointerEventScope {
                        while (true) {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            selectedIndices.clear()
                            
                            val hitIndex = findCollidingNode(down.position)
                            if (hitIndex != -1) {
                                selectedIndices.add(hitIndex)
                                onSwipeUpdate(letters[hitIndex].toString())
                            }
                            currentTouchPos = down.position

                            var dragPointerId = down.id
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Main)
                                val anyPressed = event.changes.any { it.pressed }
                                if (!anyPressed) {
                                    break
                                }
                                val change = event.changes.firstOrNull { it.id == dragPointerId }
                                if (change != null) {
                                    val currentPos = change.position
                                    currentTouchPos = currentPos

                                    val collidingIndex = findCollidingNode(currentPos)
                                    if (collidingIndex != -1) {
                                        if (!selectedIndices.contains(collidingIndex)) {
                                            selectedIndices.add(collidingIndex)
                                            val word = selectedIndices.map { letters[it] }.joinToString("")
                                            onSwipeUpdate(word)
                                        } else if (selectedIndices.size >= 2 && selectedIndices[selectedIndices.size - 2] == collidingIndex) {
                                            // Handle back-swipe retraction feedback
                                            selectedIndices.removeAt(selectedIndices.size - 1)
                                            val word = selectedIndices.map { letters[it] }.joinToString("")
                                            onSwipeUpdate(word)
                                        }
                                    }
                                    change.consume()
                                }
                            }

                            // Swipe completed
                            if (selectedIndices.isNotEmpty()) {
                                onSwipeComplete(selectedIndices.map { letters[it] }.joinToString(""))
                            }
                            selectedIndices.clear()
                            currentTouchPos = null
                        }
                    }
                }
        ) {
            // Draw real-time connections using Track Orange
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (selectedIndices.size > 0) {
                    // Draw established lines
                    for (idx in 0 until selectedIndices.size - 1) {
                        val start = positions[selectedIndices[idx]]
                        val end = positions[selectedIndices[idx + 1]]
                        drawLine(
                            color = trackOrange,
                            start = start,
                            end = end,
                            strokeWidth = 20f,
                            cap = StrokeCap.Round
                        )
                    }
                    // Draw line to current touch point
                    val currentTouch = currentTouchPos
                    if (currentTouch != null) {
                        val start = positions[selectedIndices.last()]
                        drawLine(
                            color = trackOrange,
                            start = start,
                            end = currentTouch,
                            strokeWidth = 20f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // Draw interactive scale-up alphabets
            for (i in 0 until numLetters) {
                val isSelected = selectedIndices.contains(i)
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.25f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
                )

                val offset = positions[i]
                val dpX = with(density) { offset.x.toDp() }
                val dpY = with(density) { offset.y.toDp() }

                Box(
                    modifier = Modifier
                        .offset(x = dpX - 28.dp, y = dpY - 28.dp)
                        .size(56.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .background(
                            color = if (isSelected) trackOrange else Color.White,
                            shape = CircleShape
                        )
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color.White else Color(0xFFE2E8F0),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letters[i].toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isSelected) Color.White else unselectedTextCharcoal
                    )
                }
            }
        }
    }
}

@Composable
fun CrosswordGrid(
    level: LevelSpec,
    solvedWords: Set<String>,
    revealedCoords: Set<Pair<Int, Int>>,
    colorStyle: String,
    modifier: Modifier = Modifier
) {
    val cellMap = level.computeGridCells()
    if (cellMap.isEmpty()) return

    val minRow = cellMap.keys.minOf { it.first }
    val maxRow = cellMap.keys.maxOf { it.first }
    val minCol = cellMap.keys.minOf { it.second }
    val maxCol = cellMap.keys.maxOf { it.second }

    val numRows = maxRow - minRow + 1
    val numCols = maxCol - minCol + 1

    // Soft containment grid box: glassmorphic light overlay
    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.12f), shape = RoundedCornerShape(20.dp))
            .border(1.2.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        for (r in 0 until numRows) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (c in 0 until numCols) {
                    val globalRow = r + minRow
                    val globalCol = c + minCol
                    val coord = Pair(globalRow, globalCol)
                    val letter = cellMap[coord]

                    if (letter != null) {
                        // Check if this letter should be revealed
                        val isPartofSolvedWord = level.placements.any { p ->
                            solvedWords.contains(p.word) && isCoordInPlacement(globalRow, globalCol, p)
                        }
                        val isRevealedByHint = revealedCoords.contains(coord)
                        val isSolved = isPartofSolvedWord || isRevealedByHint

                        val contentScale by animateFloatAsState(
                            targetValue = if (isSolved) 1.0f else 0.9f,
                            animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium)
                        )

                        val cellBg = if (isSolved) {
                            Brush.verticalGradient(colors = listOf(Color(0xFFFF8C42), Color(0xFFFF5722)))
                        } else {
                            Brush.linearGradient(colors = listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.08f)))
                        }

                        Box(
                            modifier = Modifier
                                .padding(3.dp)
                                .size(44.dp)
                                .graphicsLayer(scaleX = contentScale, scaleY = contentScale)
                                .background(
                                    brush = cellBg,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .drawBehind {
                                    if (isSolved) {
                                        // Thick bottom border 4dp for bevel shadow depth
                                        val strokeWidth = 4.dp.toPx()
                                        val y = size.height - strokeWidth / 2f
                                        drawLine(
                                            color = Color(0xFFB02F00), // Dark red orange bevel shadow
                                            start = Offset(0f, y),
                                            end = Offset(size.width, y),
                                            strokeWidth = strokeWidth
                                        )
                                    }
                                }
                                .border(
                                    width = if (isSolved) 1.dp else 1.2.dp,
                                    color = if (isSolved) Color(0xFFFF8C42) else Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .testTag("grid_cell_${globalRow}_${globalCol}"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSolved) {
                                Text(
                                    text = letter.toString(),
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    } else {
                        // Invisible grid spacing spacing
                        Box(modifier = Modifier.padding(3.dp).size(44.dp))
                    }
                }
            }
        }
    }
}


fun isCoordInPlacement(r: Int, c: Int, p: com.example.data.model.WordPlacement): Boolean {
    for (i in p.word.indices) {
        val currR = if (p.isHorizontal) p.startRow else p.startRow + i
        val currC = if (p.isHorizontal) p.startCol + i else p.startCol
        if (currR == r && currC == c) return true
    }
    return false
}

@Composable
fun GameplayScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val level by viewModel.activeLevel.collectAsState()
    val solvedWords by viewModel.solvedWords.collectAsState()
    val revealedCoords by viewModel.revealedCoords.collectAsState()
    val wheelLetters by viewModel.wheelLetters.collectAsState()
    val currentSwipeText by viewModel.currentSwipeText.collectAsState()
    val swipeErrorTrigger by viewModel.swipeErrorTrigger.collectAsState()
    val userStats by viewModel.userStats.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val celebrationMilestone by viewModel.celebrationMilestone.collectAsState()
    val extraWordsFound by viewModel.foundExtraWords.collectAsState()

    var showExtraWordsDialog by remember { mutableStateOf(false) }
    var showPauseMenu by remember { mutableStateOf(false) }

    if (level == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val activeSpec = level!!
    val colorStyle = activeSpec.colorStyle

    val themeColor = when (colorStyle) {
        "Red" -> Color(0xFFE53935)
        "Blue" -> Color(0xFF1E88E5)
        else -> Color(0xFFFB8C00)
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Full screen procedural background
        SchoolBackground(theme = activeSpec.themeName)

        // Main game HUD
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP AREA (Header & Grid)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Stats HUD
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Menu burger button to pause
                    IconButton(
                        onClick = { showPauseMenu = true },
                        modifier = Modifier.background(Color.White.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Pause",
                            tint = Color.White
                        )
                    }

                    // Title Banner (Level & Theme)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "LEVEL ${activeSpec.id}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = activeSpec.themeName.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFDBD1),
                            letterSpacing = 1.sp
                        )
                    }

                    // Coins tracker (White with opacity, white text, monetization icon)
                    Row(
                        modifier = Modifier
                            .height(38.dp)
                            .clip(RoundedCornerShape(19.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(19.dp))
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = "Coins",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${userStats.coins}",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }

                // Crossword puzzle grid list centering
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CrosswordGrid(
                        level = activeSpec,
                        solvedWords = solvedWords,
                        revealedCoords = revealedCoords,
                        colorStyle = colorStyle
                    )
                }
            }

            // BOTTOM PANEL (Interaction Area): Glassmorphic panel with upper shadow elevation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(
                        width = 1.5.dp,
                        color = Color.White.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(top = 16.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Middle swipe bubble row
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentSwipeText.isNotEmpty()) {
                            val shakeOffset by animateDpAsState(
                                targetValue = if (swipeErrorTrigger) 10.dp else 0.dp,
                                animationSpec = keyframes {
                                    durationMillis = 300
                                    0.dp at 0
                                    (-8).dp at 75
                                    8.dp at 150
                                    (-4).dp at 225
                                    0.dp at 300
                                }
                            )

                            Box(
                                modifier = Modifier
                                    .graphicsLayer(translationX = with(LocalDensity.current) { shakeOffset.toPx() })
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(
                                        if (swipeErrorTrigger) {
                                            Brush.verticalGradient(colors = listOf(Color(0xFFD32F2F), Color(0xFFC62828)))
                                        } else {
                                            Brush.verticalGradient(colors = listOf(Color(0xFFFF8C42), Color(0xFFFF5722)))
                                        }
                                    )
                                    .border(1.2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(22.dp))
                                    .padding(horizontal = 24.dp, vertical = 6.dp)
                                    .testTag("swiped_feedback_bubble")
                            ) {
                                Text(
                                    text = currentSwipeText,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    // Main interactive wheel flanked by controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // LEFT SIDE BUTTONS: Shuffle & Extras Dialog
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Shuffle
                            GlassCircleButton(
                                onClick = { viewModel.shuffleWheel() },
                                icon = Icons.Default.Shuffle
                            )

                            // Extra/Favorites Book
                            GlassCircleButton(
                                onClick = { showExtraWordsDialog = true },
                                icon = Icons.Default.Star,
                                badgeText = if (extraWordsFound.isNotEmpty()) "${extraWordsFound.size}" else null
                            )
                        }

                        // CENTRE: Interactive Letter Wheel (Lilac-purple colored!)
                        GameLetterWheel(
                            letters = wheelLetters,
                            colorStyle = colorStyle,
                            onSwipeUpdate = { viewModel.updateSwipeText(it) },
                            onSwipeComplete = { viewModel.validateSwipe(it) }
                        )

                        // RIGHT SIDE BUTTONS: Hint & Rocket Booster with price labels
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Standard Hint bulb
                            GlassCircleButton(
                                onClick = { viewModel.purchaseSingleHint() },
                                icon = Icons.Default.Lightbulb,
                                badgeText = "25🪙"
                            )

                            // Rocket booster
                            GlassCircleButton(
                                onClick = { viewModel.purchaseRocketBooster() },
                                icon = Icons.Default.RocketLaunch,
                                badgeText = "300🪙"
                            )
                        }
                    }

                    // Bottom status indicator row: Rank and Daily Streak from Design HTML
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Rank indicator Glass pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = "Rank",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "RANK #${userStats.currentLevelId}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Right: Dynamic Streak card
                        val completedCount = userStats.completedChallengesCount
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFF5722).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFFFF5722).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "STREAK: $completedCount DAYS 🔥",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFDBD1)
                            )
                        }
                    }
                }
            }
        }

        // Animated general Toast notification alerts
        AnimatedVisibility(
            visible = toastMessage != null,
            enter = slideInVertically(initialOffsetY = { -100 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -100 }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp)
        ) {
            val isCelebratory = toastMessage != null && listOf("AWESOME!", "MARVELOUS!", "BRILLIANT!", "EXCELLENT!", "SUPERB!").contains(toastMessage)
            
            Surface(
                color = if (isCelebratory) Color(0xFFFF5722) else Color.Black.copy(alpha = 0.9f),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .border(
                        1.5.dp,
                        if (isCelebratory) Color.White else Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(24.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isCelebratory) Icons.Default.WorkspacePremium else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (isCelebratory) Color(0xFFFFD700) else Color(0xFFFF5722),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = toastMessage ?: "",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Level Victory milestone Overlay Celebrate Screen
        AnimatedVisibility(
            visible = celebrationMilestone != null,
            enter = scaleIn(initialScale = 0.5f) + fadeIn(),
            exit = scaleOut(targetScale = 0.5f) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E26)),
                    shape = RoundedCornerShape(32.dp),
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                        .border(2.dp, Color(0xFFFF5722), RoundedCornerShape(32.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = celebrationMilestone ?: "LEVEL CLEARED!",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = "Stars",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(90.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "AWARDS FOR EXCELLENCE:",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.5f),
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("+3 Stars", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFD700))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("+25 Coins", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(28.dp))
                        
                        TactileGradientButton(
                            onClick = { viewModel.dismissCelebration() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text("CONTINUE", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        // Pause Menu Dialog Overlay Overlay
        if (showPauseMenu) {
            PauseDialog(
                onResume = { showPauseMenu = false },
                onRestart = {
                    viewModel.loadLevel(activeSpec.id)
                    showPauseMenu = false
                },
                onQuit = {
                    showPauseMenu = false
                    onBack()
                }
            )
        }

        // Dialogue overlay: Extra Words dictionary
        if (showExtraWordsDialog) {
            Dialog(onDismissRequest = { showExtraWordsDialog = false }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "EXTRA WORDS",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = themeColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Find extra academic words that are not part of the primary crossword to earn +5 Coins!",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (extraWordsFound.isEmpty()) {
                            Text(
                                text = "No extra words discovered yet in this level.\nSwipe words like DARE, EAR, REAL, RAN to try!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.DarkGray,
                                modifier = Modifier.padding(vertical = 12.dp),
                                textAlign = TextAlign.Center
                            )
                        } else {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                maxItemsInEachRow = 3
                            ) {
                                for (w in extraWordsFound) {
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(w, fontWeight = FontWeight.Bold) },
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { showExtraWordsDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColor)
                        ) {
                            Text("Awesome", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    maxItemsInEachRow: Int = Int.MAX_VALUE,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        maxItemsInEachRow = maxItemsInEachRow,
        content = { content() }
    )
}
