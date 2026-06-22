package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LevelSpec
import com.example.ui.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DailyChallengeScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val level by viewModel.dailyLevel.collectAsState()
    val solvedWords by viewModel.dailySolvedWords.collectAsState()
    val isCompleted by viewModel.dailyCompleted.collectAsState()
    val wheelLetters by viewModel.dailyWheelLetters.collectAsState()
    val revealedCoords by viewModel.dailyRevealedCoords.collectAsState()
    val currentSwipeText by viewModel.currentSwipeText.collectAsState()
    val swipeErrorTrigger by viewModel.swipeErrorTrigger.collectAsState()
    val userStats by viewModel.userStats.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val celebrationMilestone by viewModel.celebrationMilestone.collectAsState()

    val todayStr = viewModel.getTodayDateStr()
    
    // Friendly student calendar naming formatting
    val calendarHeader = remember {
        val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdfIn.parse(todayStr) ?: Date()
        val sdfOut = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        sdfOut.format(date).uppercase()
    }

    if (level == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val activeSpec = level!!
    val themeColor = Color(0xFF9C27B0) // Elegant academic purple for Daily challenge branding style

    val soundEnabled = userStats.soundEnabled
    val hapticEnabled = userStats.hapticEnabled
    val hapticFeedback = androidx.compose.ui.platform.LocalHapticFeedback.current

    LaunchedEffect(solvedWords.size) {
        if (solvedWords.isNotEmpty()) {
            com.example.ui.utils.SoundHapticHelper.playCorrectWordSound(soundEnabled)
            com.example.ui.utils.SoundHapticHelper.triggerMediumHaptic(hapticFeedback, hapticEnabled)
        }
    }

    LaunchedEffect(swipeErrorTrigger) {
        if (swipeErrorTrigger) {
            com.example.ui.utils.SoundHapticHelper.playErrorSound(soundEnabled)
            com.example.ui.utils.SoundHapticHelper.triggerErrorHaptic(hapticFeedback, hapticEnabled)
        }
    }

    LaunchedEffect(celebrationMilestone) {
        if (celebrationMilestone != null) {
            com.example.ui.utils.SoundHapticHelper.playCompleteSound(soundEnabled)
            com.example.ui.utils.SoundHapticHelper.triggerMediumHaptic(hapticFeedback, hapticEnabled)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Procedural school background
        SchoolBackground(theme = "Classroom")

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
                // Header stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "DAILY STUDY",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = calendarHeader,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                    }

                    // Coins
                    Row(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = "Coins",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${userStats.coins}",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }

                // Word Grid or Completed Screen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E26)),
                            shape = RoundedCornerShape(32.dp),
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                                .border(2.dp, Color(0xFFFF5722), RoundedCornerShape(32.dp))
                        ) {
                           Column(
                                modifier = Modifier.padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Completed",
                                    tint = Color(0xFFFF5722),
                                    modifier = Modifier.size(72.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "DAILY STUDY COMPLETED!",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Outstanding scholar! You earned the +100 Coins Daily Award and completed your daily educational challenge.",
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Row(
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD700)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Awarded 100🪙",
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    } else {
                        CrosswordGrid(
                            level = activeSpec,
                            solvedWords = solvedWords,
                            revealedCoords = revealedCoords,
                            colorStyle = "Red"
                        )
                    }
                }

            if (!isCompleted) {
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
                        // Swiped feedback bubble
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
                                        .testTag("daily_swiped_feedback_bubble")
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
                            // Left button (Shuffle)
                            GlassCircleButton(
                                onClick = { viewModel.shuffleWheel() },
                                icon = Icons.Default.Shuffle
                            )

                            // Center Wheel
                            GameLetterWheel(
                                letters = wheelLetters,
                                colorStyle = "Red",
                                onSwipeUpdate = { viewModel.updateSwipeText(it) },
                                onSwipeComplete = { viewModel.validateSwipe(it) },
                                soundEnabled = soundEnabled,
                                hapticEnabled = hapticEnabled
                            )

                            // Right button (Hint)
                            GlassCircleButton(
                                onClick = { viewModel.purchaseSingleHint() },
                                icon = Icons.Default.Lightbulb,
                                badgeText = "25🪙"
                            )
                        }

                        // Bottom streak counter footer
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, end = 8.dp, top = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val completedCount = userStats.completedChallengesCount
                            Row(
                                modifier = Modifier
                                    .background(Color(0xFFFFF1F2).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFFFFE4E6).copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "DAILY STUDY COMPLETION STREAK: $completedCount DAYS 🔥",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFD1D1)
                                )
                            }
                        }
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
            Surface(
                color = Color.Black.copy(alpha = 0.9f),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = themeColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = toastMessage ?: "",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Daily Challenge Completion popup overlay celebrate screen
        AnimatedVisibility(
            visible = celebrationMilestone != null,
            enter = scaleIn(initialScale = 0.5f) + fadeIn(),
            exit = scaleOut(targetScale = 0.5f) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth()
                        .border(4.dp, themeColor, RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = celebrationMilestone ?: "AWESOME SCHOLAR!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = themeColor,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = "Success",
                            tint = Color(0xFFFBC02D),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "CHALLENGE BONUSES AWARDED:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFB300))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+100 Coins", fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.dismissCelebration() },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColor)
                        ) {
                            Text("Claim Award", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Inline Row implementation helper
@Composable
private fun Row(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    valignment: Alignment.Vertical,
    halignment: Arrangement.Horizontal,
    content: @Composable RowScope.() -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier,
        horizontalArrangement = halignment,
        verticalAlignment = valignment,
        content = content
    )
}
