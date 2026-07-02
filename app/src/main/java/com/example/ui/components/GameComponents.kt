package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LevelSpec
import com.example.ui.utils.SoundHapticHelper
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameLetterWheel(
    modifier: Modifier = Modifier,
    letters: List<Char>,
    colorStyle: String,
    onSwipeUpdate: (String) -> Unit,
    onSwipeComplete: (String) -> Unit,
    soundEnabled: Boolean = true,
    hapticEnabled: Boolean = true,
) {
    val trackColor = remember(colorStyle) {
        when (colorStyle) {
            "Red" -> Color(0xFFE53935)
            "Blue" -> Color(0xFF1E88E5)
            else -> Color(0xFFFF5722)
        }
    }
    val charcoalColor = Color(0xFF271813)
    val hapticFeedback = LocalHapticFeedback.current
    val density = LocalDensity.current

    val currentOnSwipeUpdate by rememberUpdatedState(onSwipeUpdate)
    val currentOnSwipeComplete by rememberUpdatedState(onSwipeComplete)

    var center by remember { mutableStateOf(Offset.Zero) }
    var radius by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .size(190.dp)
            .onGloballyPositioned { coordinates ->
                val size = coordinates.size
                center = Offset(size.width / 2f, size.height / 2f)
                radius = size.width * 0.35f
            }
            .background(Color.White.copy(alpha = 0.15f), shape = CircleShape)
            .border(1.5.dp, Color.White.copy(alpha = 0.25f), CircleShape)
            .testTag("circular_letter_wheel")
    ) {
        val numLetters = letters.size
        val collisionRadiusPx = with(density) { 24.dp.toPx() }

        val positions = remember(letters, center, radius) {
            if (radius == 0f) emptyList()
            else List(numLetters) { i ->
                val angle = -PI / 2 + i * 2 * PI / numLetters
                Offset(
                    center.x + radius * cos(angle).toFloat(),
                    center.y + radius * sin(angle).toFloat()
                )
            }
        }

        val selectedIndices = remember { mutableStateListOf<Int>() }
        var currentTouchPos by remember { mutableStateOf<Offset?>(null) }

        // Clear selection when letters change (e.g. next level)
        LaunchedEffect(letters) {
            selectedIndices.clear()
            currentTouchPos = null
        }

        val selectedSet by remember { derivedStateOf { selectedIndices.toSet() } }
        val currentWord by remember(letters) {
            derivedStateOf { selectedIndices.map { letters[it] }.joinToString("") }
        }

        val findCollidingNode = remember(positions, collisionRadiusPx) {
            { touch: Offset ->
                positions.indexOfFirst { node ->
                    (touch - node).getDistanceSquared() < collisionRadiusPx * collisionRadiusPx
                }
            }
        }

        fun notifyInteraction() {
            currentOnSwipeUpdate(currentWord)
            SoundHapticHelper.playConnectionSound(soundEnabled)
            SoundHapticHelper.triggerLightHaptic(hapticFeedback, hapticEnabled)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(letters, positions) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.changes.any { it.pressed }) {
                                selectedIndices.clear()
                                val hitIndex = findCollidingNode(event.changes.first().position)
                                if (hitIndex != -1) {
                                    selectedIndices.add(hitIndex)
                                    notifyInteraction()
                                }
                                currentTouchPos = event.changes.first().position

                                val dragPointerId = event.changes.first().id
                                while (true) {
                                    val moveEvent = awaitPointerEvent(PointerEventPass.Main)
                                    if (moveEvent.changes.none { it.pressed }) break

                                    val change =
                                        moveEvent.changes.firstOrNull { it.id == dragPointerId }
                                    if (change != null) {
                                        currentTouchPos = change.position
                                        val collidingIndex = findCollidingNode(change.position)

                                        if (collidingIndex != -1) {
                                            if (collidingIndex !in selectedSet) {
                                                selectedIndices.add(collidingIndex)
                                                notifyInteraction()
                                            } else if (selectedIndices.size >= 2 &&
                                                selectedIndices[selectedIndices.size - 2] == collidingIndex
                                            ) {
                                                selectedIndices.removeAt(selectedIndices.size - 1)
                                                notifyInteraction()
                                            }
                                        }
                                        change.consume()
                                    }
                                }
                                if (selectedIndices.isNotEmpty()) {
                                    currentOnSwipeComplete(currentWord)
                                }
                                selectedIndices.clear()
                                currentTouchPos = null
                            }
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (selectedIndices.isNotEmpty()) {
                    val path = Path().apply {
                        val start = positions[selectedIndices[0]]
                        moveTo(start.x, start.y)
                        for (i in 1 until selectedIndices.size) {
                            val p = positions[selectedIndices[i]]
                            lineTo(p.x, p.y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = trackColor,
                        style = Stroke(
                            width = 20f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    currentTouchPos?.let { touch ->
                        val lastPos = positions[selectedIndices.last()]
                        drawLine(
                            color = trackColor,
                            start = lastPos,
                            end = touch,
                            strokeWidth = 20f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            positions.forEachIndexed { i, pos ->
                val isSelected = i in selectedSet
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.25f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
                    label = "letter_scale"
                )

                Box(
                    modifier = Modifier
                        .offset(
                            x = with(density) { pos.x.toDp() } - 20.dp,
                            y = with(density) { pos.y.toDp() } - 20.dp
                        )
                        .size(40.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .background(
                            color = if (isSelected) trackColor else Color.White,
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
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isSelected) Color.White else charcoalColor
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
    val cellMap = remember(level) { level.computeGridCells() }
    if (cellMap.isEmpty()) return

    val gridBounds = remember(cellMap) {
        val rows = cellMap.keys.map { it.first }
        val cols = cellMap.keys.map { it.second }
        val minRow = rows.minOrNull() ?: 0
        val maxRow = rows.maxOrNull() ?: 0
        val minCol = cols.minOrNull() ?: 0
        val maxCol = cols.maxOrNull() ?: 0
        val numRows = maxRow - minRow + 1
        val numCols = maxCol - minCol + 1
        val maxDim = maxOf(numRows, numCols)

        object {
            val minRow = minRow
            val minCol = minCol
            val numRows = numRows
            val numCols = numCols
            val maxDim = maxDim
        }
    }

    val gridParams = remember(gridBounds.maxDim) {
        val maxDim = gridBounds.maxDim
        val cellSize = when {
            maxDim <= 4 -> 52.dp
            maxDim == 5 -> 48.dp
            maxDim == 6 -> 42.dp
            maxDim == 7 -> 36.dp
            maxDim == 8 -> 32.dp
            maxDim == 9 -> 28.dp
            maxDim == 10 -> 26.dp
            maxDim == 11 -> 24.dp
            maxDim == 12 -> 22.dp
            else -> 20.dp
        }
        val cellPadding = when {
            maxDim <= 4 -> 6.dp
            maxDim <= 6 -> 4.dp
            maxDim <= 9 -> 3.dp
            else -> 2.dp
        }
        val cornerRadiusValue = when {
            maxDim <= 4 -> 12.dp
            maxDim <= 6 -> 10.dp
            maxDim <= 9 -> 8.dp
            else -> 6.dp
        }
        val bottomShadowHeight = when {
            maxDim <= 4 -> 4.dp
            maxDim <= 6 -> 3.dp
            maxDim <= 9 -> 2.dp
            else -> 1.5.dp
        }
        val textFontSize = when {
            maxDim <= 4 -> 24.sp
            maxDim == 5 -> 22.sp
            maxDim == 6 -> 20.sp
            maxDim == 7 -> 18.sp
            maxDim == 8 -> 16.sp
            maxDim == 9 -> 14.sp
            maxDim == 10 -> 13.sp
            maxDim == 11 -> 12.sp
            maxDim == 12 -> 11.sp
            else -> 10.sp
        }
        object {
            val cellSize = cellSize
            val cellPadding = cellPadding
            val cornerRadiusValue = cornerRadiusValue
            val bottomShadowHeight = bottomShadowHeight
            val textFontSize = textFontSize
        }
    }

    val solvedCoordinates = remember(level, solvedWords) {
        val set = mutableSetOf<Pair<Int, Int>>()
        level.placements.forEach { p ->
            if (solvedWords.contains(p.word)) {
                for (i in p.word.indices) {
                    val r = if (p.isHorizontal) p.startRow else p.startRow + i
                    val c = if (p.isHorizontal) p.startCol + i else p.startCol
                    set.add(r to c)
                }
            }
        }
        set
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        for (r in 0 until gridBounds.numRows) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (c in 0 until gridBounds.numCols) {
                    val globalRow = r + gridBounds.minRow
                    val globalCol = c + gridBounds.minCol
                    val coord = Pair(globalRow, globalCol)
                    val letter = cellMap[coord]

                    if (letter != null) {
                        val isSolved =
                            solvedCoordinates.contains(coord) || revealedCoords.contains(coord)
                        GridCell(
                            letter = letter,
                            isSolved = isSolved,
                            cellSize = gridParams.cellSize,
                            cellPadding = gridParams.cellPadding,
                            cornerRadiusValue = gridParams.cornerRadiusValue,
                            bottomShadowHeight = gridParams.bottomShadowHeight,
                            textFontSize = gridParams.textFontSize,
                            testTag = "grid_cell_${globalRow}_${globalCol}"
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .padding(gridParams.cellPadding)
                                .size(gridParams.cellSize)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GridCell(
    letter: Char,
    isSolved: Boolean,
    cellSize: Dp,
    cellPadding: Dp,
    cornerRadiusValue: Dp,
    bottomShadowHeight: Dp,
    textFontSize: androidx.compose.ui.unit.TextUnit,
    testTag: String
) {
    val contentScale by animateFloatAsState(
        targetValue = if (isSolved) 1.0f else 0.9f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
        label = "cell_scale"
    )

    val cellBg = remember(isSolved) {
        if (isSolved) {
            Brush.verticalGradient(colors = listOf(Color(0xFFFF8C42), Color(0xFFFF5722)))
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.15f),
                    Color.White.copy(alpha = 0.08f)
                )
            )
        }
    }

    Box(
        modifier = if (isSolved) {
            Modifier
                .padding(cellPadding)
                .size(cellSize)
                .graphicsLayer(scaleX = contentScale, scaleY = contentScale)
                .background(Color(0xFFB02F00), RoundedCornerShape(cornerRadiusValue))
                .padding(bottom = bottomShadowHeight)
                .background(cellBg, RoundedCornerShape(cornerRadiusValue))
                .border(1.dp, Color(0xFFFF8C42), RoundedCornerShape(cornerRadiusValue))
        } else {
            Modifier
                .padding(cellPadding)
                .size(cellSize)
                .graphicsLayer(scaleX = contentScale, scaleY = contentScale)
                .background(cellBg, RoundedCornerShape(cornerRadiusValue))
                .border(
                    1.2.dp,
                    Color.White.copy(alpha = 0.2f),
                    RoundedCornerShape(cornerRadiusValue)
                )
        }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        if (isSolved) {
            Text(
                text = letter.toString(),
                color = Color.White,
                fontSize = textFontSize,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun SwipeFeedbackSection(
    currentSwipeText: String,
    swipeErrorTrigger: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
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
                },
                label = "swipe_shake"
            )

            Box(
                modifier = Modifier
                    .graphicsLayer(translationX = with(LocalDensity.current) { shakeOffset.toPx() })
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        if (swipeErrorTrigger) {
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFD32F2F),
                                    Color(0xFFC62828)
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFF8C42),
                                    Color(0xFFFF5722)
                                )
                            )
                        }
                    )
                    .border(1.1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(22.dp))
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
}
