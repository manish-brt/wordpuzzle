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
                        modifier = Modifier.background(Color.White.copy(alpha = 0.85f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF6750A4)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "★ DAILY STUDY",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF6750A4)
                        )
                        Text(
                            text = calendarHeader,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569),
                            letterSpacing = 1.sp
                        )
                    }

                    // Coins
                    Row(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
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
                            color = Color(0xFF1E293B)
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
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
                                .drawBehind {
                                    val strokeWidth = 5.dp.toPx()
                                    val y = size.height - strokeWidth / 2f
                                    drawLine(
                                        color = Color(0xFFCBD5E1),
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y),
                                        strokeWidth = strokeWidth
                                    )
                                }
                        ) {
                          Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Completed",
                                    tint = Color(0xFF6750A4),
                                    modifier = Modifier.size(72.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "DAILY QUIZ CLEAR!",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF6750A4)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Outstanding scholar! You earned the +100 Coins Daily Award and climbed up the competitive leaderboard ranks.",
                                    fontSize = 14.sp,
                                    color = Color(0xFF475569),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier
                                        .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = null,
                                        tint = Color(0xFF6750A4)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Awarded 100🪙",
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF1E293B),
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
            }

            if (!isCompleted) {
                // BOTTOM PANEL (Interaction Area): bg-white rounded-t-[48px] with upper shadow elevation
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .border(
                            width = 1.5.dp,
                            color = Color(0xFFE2E8F0),
                            shape = RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp)
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

                                Surface(
                                    tonalElevation = 6.dp,
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (swipeErrorTrigger) Color(0xFFD32F2F) else Color(0xFF6750A4),
                                    modifier = Modifier
                                        .graphicsLayer(translationX = with(LocalDensity.current) { shakeOffset.toPx() })
                                        .testTag("daily_swiped_feedback_bubble")
                                ) {
                                    Text(
                                        text = currentSwipeText,
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp),
                                        textAlign = TextAlign.Center,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }

                        // Interactive Letter wheel flanking
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left action: Shuffle
                            BounceButton(onClick = { viewModel.shuffleWheel() }) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .background(Color(0xFFF2F2F7), CircleShape)
                                        .border(1.dp, Color(0xFFCBD5E1), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shuffle,
                                        contentDescription = "Shuffle",
                                        tint = Color(0xFF6750A4),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // Circular swipe node wheel (Lilac theme!)
                            GameLetterWheel(
                                letters = wheelLetters,
                                colorStyle = "Red",
                                onSwipeUpdate = { viewModel.updateSwipeText(it) },
                                onSwipeComplete = { viewModel.validateSwipe(it) }
                            )

                            // Right action: Hint
                            BounceButton(onClick = { viewModel.purchaseSingleHint() }) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .background(Color(0xFFF2F2F7), CircleShape)
                                            .border(1.dp, Color(0xFFCBD5E1), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lightbulb,
                                            contentDescription = "Hint",
                                            tint = Color(0xFF6750A4),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "25🪙",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF1E293B),
                                        modifier = Modifier
                                            .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
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
                                    .background(Color(0xFFFFF1F2), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFFFFE4E6), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "DAILY STUDY COMPLETION STREAK: $completedCount DAYS 🔥",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFE11D48)
                                )
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
