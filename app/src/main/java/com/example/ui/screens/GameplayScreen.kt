package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.*
import com.example.ui.utils.SoundHapticHelper
import com.example.ui.viewmodel.GameViewModel

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
    val swipeErrorTrigger by viewModel.swipeErrorTrigger.collectAsState()
    val currentSwipeText by viewModel.currentSwipeText.collectAsState()
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

    val themeColor = remember(colorStyle) {
        when (colorStyle) {
            "Red" -> Color(0xFFE53935)
            "Blue" -> Color(0xFF1E88E5)
            else -> Color(0xFFFB8C00)
        }
    }

    val soundEnabled = userStats.soundEnabled
    val hapticEnabled = userStats.hapticEnabled
    val hapticFeedback = LocalHapticFeedback.current

    LaunchedEffect(solvedWords.size) {
        if (solvedWords.isNotEmpty()) {
            SoundHapticHelper.playCorrectWordSound(soundEnabled)
            SoundHapticHelper.triggerMediumHaptic(hapticFeedback, hapticEnabled)
        }
    }

    LaunchedEffect(extraWordsFound.size) {
        if (extraWordsFound.isNotEmpty()) {
            SoundHapticHelper.playBonusWordSound(soundEnabled)
            SoundHapticHelper.triggerLightHaptic(hapticFeedback, hapticEnabled)
        }
    }

    LaunchedEffect(swipeErrorTrigger) {
        if (swipeErrorTrigger) {
            SoundHapticHelper.playErrorSound(soundEnabled)
            SoundHapticHelper.triggerErrorHaptic(hapticFeedback, hapticEnabled)
        }
    }

    LaunchedEffect(celebrationMilestone) {
        if (celebrationMilestone != null) {
            SoundHapticHelper.playCompleteSound(soundEnabled)
            SoundHapticHelper.triggerMediumHaptic(hapticFeedback, hapticEnabled)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        SchoolBackground()

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

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .height(38.dp)
                                .clip(RoundedCornerShape(19.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(19.dp))
                                .padding(horizontal = 10.dp),
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
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }

                        IconButton(
                            onClick = { showPauseMenu = true },
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Pause",
                                tint = Color.White
                            )
                        }
                    }
                }

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

            // BOTTOM PANEL (Interaction Area)
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
                    SwipeFeedbackSection(
                        currentSwipeText = currentSwipeText,
                        swipeErrorTrigger = swipeErrorTrigger
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GlassCircleButton(
                                onClick = { viewModel.shuffleWheel() },
                                icon = Icons.Default.Shuffle
                            )

                            GlassCircleButton(
                                onClick = { showExtraWordsDialog = true },
                                icon = Icons.Default.Star,
                                badgeText = if (extraWordsFound.isNotEmpty()) "${extraWordsFound.size}" else null
                            )
                        }

                        GameLetterWheel(
                            letters = wheelLetters,
                            colorStyle = colorStyle,
                            onSwipeUpdate = { viewModel.updateSwipeText(it) },
                            onSwipeComplete = { viewModel.validateSwipe(it) },
                            soundEnabled = soundEnabled,
                            hapticEnabled = hapticEnabled
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GlassCircleButton(
                                onClick = { viewModel.purchaseSingleHint() },
                                icon = Icons.Default.Lightbulb,
                                badgeText = "25🪙"
                            )

                            GlassCircleButton(
                                onClick = { viewModel.purchaseRocketBooster() },
                                icon = Icons.Default.RocketLaunch,
                                badgeText = "300🪙"
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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

        // Pause Menu Dialog Overlay
        if (showPauseMenu) {
            PauseDialog(
                soundEnabled = soundEnabled,
                hapticEnabled = hapticEnabled,
                onSoundToggle = { viewModel.updateSoundEnabled(it) },
                onHapticToggle = { viewModel.updateHapticEnabled(it) },
                onResume = { showPauseMenu = false },
                onRestart = {
                    viewModel.restartLevel(activeSpec.id)
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
                            @OptIn(ExperimentalLayoutApi::class)
                            (FlowRow(
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
                            })
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

@Composable
fun PauseDialog(
    soundEnabled: Boolean,
    hapticEnabled: Boolean,
    onSoundToggle: (Boolean) -> Unit,
    onHapticToggle: (Boolean) -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onQuit: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f)),
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
                        imageVector = if (soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
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
                    checked = soundEnabled,
                    onCheckedChange = onSoundToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFFF5722),
                        uncheckedThumbColor = Color.LightGray,
                        uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
                    )
                )
            }

            // HAPTIC FEEDBACK TOGGLE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Haptic Feedback",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Switch(
                    checked = hapticEnabled,
                    onCheckedChange = onHapticToggle,
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
                text = "Happy Playing",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}
