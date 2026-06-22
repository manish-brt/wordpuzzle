package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeaderboardEntry
import com.example.ui.viewmodel.GameViewModel

@Composable
fun LeaderboardScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val leaderboardList by viewModel.leaderboard.collectAsState()
    val userStats by viewModel.userStats.collectAsState()

    // Dynamically calculate user's actual leaderboard score
    val currentUserScore = (userStats.stars * 15) + (userStats.completedChallengesCount * 100)

    // Reimagining the list by updating the entry for "You" with current score
    val syncedLeaderboardList = leaderboardList.map {
        if (it.isCurrentUser) {
            it.copy(score = currentUserScore)
        } else {
            it
        }
    }.sortedByDescending { it.score }

    Box(modifier = modifier.fillMaxSize()) {
        // Alpine Blur Theme
        SchoolBackground(theme = "Classroom")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // HALL OF FAME HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Hall of Fame",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                IconButton(onClick = { viewModel.showToast("Settings Coming Soon!") }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // TOGGLE SELECTOR: Global / Friends
            Row(
                modifier = Modifier
                    .width(220.dp)
                    .height(44.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(22.dp))
                    .padding(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(19.dp))
                        .background(Color(0xFFFF5722)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Global",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { viewModel.showToast("Friends leaderboard coming soon!") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Friends",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // TOP 3 PODIUM
            if (syncedLeaderboardList.size >= 3) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Position 2 (Left)
                    PodiumCell(entry = syncedLeaderboardList[1], rank = 2, height = 110.dp)
                    
                    // Position 1 (Center - WordKing)
                    PodiumCell(entry = syncedLeaderboardList[0], rank = 1, height = 150.dp)
                    
                    // Position 3 (Right)
                    PodiumCell(entry = syncedLeaderboardList[2], rank = 3, height = 95.dp)
                }
            }

            // LIST RANKINGS (Starting from Rank 4)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                val scrollingItems = syncedLeaderboardList.drop(3)
                itemsIndexed(scrollingItems) { index, entry ->
                    val rank = index + 4
                    val isUser = entry.isCurrentUser

                    val itemBg = if (isUser) {
                        Color(0xFFFF5722).copy(alpha = 0.15f)
                    } else {
                        Color.White.copy(alpha = 0.12f)
                    }
                    val itemBorderColor = if (isUser) {
                        Color(0xFFFF5722)
                    } else {
                        Color.White.copy(alpha = 0.2f)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(itemBg)
                            .border(1.2.dp, itemBorderColor, RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("leaderboard_card_$rank")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left Details: Rank, Avatar, Username
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Rank index
                                Text(
                                    text = "$rank",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.width(24.dp)
                                )

                                // Avatar with dynamic background
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(entry.avatarColorHex))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = entry.username.take(1).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }

                                Column {
                                    Text(
                                        text = if (isUser) "You (Me)" else entry.username,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                    if (isUser) {
                                        Text(
                                            text = "Top 5% of Players",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFFFDBD1)
                                        )
                                    }
                                }
                            }

                            // Right Coins Score
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = String.format("%,d", entry.score),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "COINS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PodiumCell(
    entry: LeaderboardEntry,
    rank: Int,
    height: Dp
) {
    val isRank1 = rank == 1
    val medalColor = when (rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFB0BEC5) // Silver
        else -> Color(0xFFFFAB91) // Bronze
    }

    val columnBackground = if (isRank1) {
        Brush.verticalGradient(colors = listOf(Color(0xFFFF8C42), Color(0xFFFF5722)))
    } else {
        Brush.verticalGradient(colors = listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.05f)))
    }

    val columnBorderColor = if (isRank1) Color(0xFFFF5722) else Color.White.copy(alpha = 0.25f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.width(96.dp)
    ) {
        // Avatar circle with badge
        Box(
            modifier = Modifier.size(68.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(2.5.dp, medalColor, CircleShape)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(entry.avatarColorHex))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = entry.username.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )
            }
            // Small rank badge
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(medalColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rank",
                    color = if (rank == 2) Color.Black else Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Visual podium column
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(columnBackground)
                .border(1.dp, columnBorderColor, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = entry.username.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = String.format("%,d", entry.score),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isRank1) Color.White else Color(0xFFFFDBD1),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
