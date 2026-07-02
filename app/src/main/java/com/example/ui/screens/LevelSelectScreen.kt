package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StaticLevelSource
import com.example.ui.components.SchoolBackground
import com.example.ui.viewmodel.GameScreen
import com.example.ui.viewmodel.GameViewModel

@Composable
fun LevelSelectScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userStats by viewModel.userStats.collectAsState()
    val activeLevelId = userStats.currentLevelId

    Box(modifier = modifier.fillMaxSize()) {
        // Procedural school background
        SchoolBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(Color.White.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "LEVELS",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                
                // Placeholder balancing spacing
                Box(modifier = Modifier.size(48.dp))
            }

            // Grid scroll view
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(StaticLevelSource.levels.size) { index ->
                    val level = StaticLevelSource.levels[index]
                    val isUnlocked = level.id <= activeLevelId
                    val isCompleted = level.id < activeLevelId

//                    val accentColor = Color(0xFFFF5722) // Track orange color for unlocked cells

                    val shadowColor = Color(0xFFB02F00)
                    val cardBg = if (isUnlocked) Color.White.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.05f)
                    Box(
                        modifier = if (isUnlocked) {
                            Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.85f)
                                .background(shadowColor, RoundedCornerShape(16.dp))
                                .padding(bottom = 4.dp)
                                .background(cardBg, RoundedCornerShape(16.dp))
                                .clickable(enabled = isUnlocked) {
                                    viewModel.loadLevel(level.id)
                                    viewModel.navigate(GameScreen.Gameplay)
                                }
                                .border(
                                    width = if (level.id == activeLevelId) 1.8.dp else 1.dp,
                                    color = if (level.id == activeLevelId) Color(0xFFFF5722) else Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                        } else {
                            Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.85f)
                                .background(cardBg, RoundedCornerShape(16.dp))
                                .clickable(enabled = isUnlocked) {
                                    viewModel.loadLevel(level.id)
                                    viewModel.navigate(GameScreen.Gameplay)
                                }
                                .border(
                                    width = if (level.id == activeLevelId) 1.8.dp else 1.dp,
                                    color = if (level.id == activeLevelId) Color(0xFFFF5722) else Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                        }
                        .testTag("level_select_item_${level.id}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "LEVEL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            
                            Text(
                                text = "${level.id}",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.3f)
                            )

                            if (isUnlocked) {
                                if (isCompleted) {
                                    // 3-star Completed Badge decoration
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(12.dp))
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(12.dp))
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(12.dp))
                                    }
                                } else {
                                    // Playable current level text
                                    Text(
                                        text = "CURRENT",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFFF8C42),
                                        maxLines = 1
                                    )
                                }
                            } else {
                                // Locked level representation
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = Color.White.copy(alpha = 0.25f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
