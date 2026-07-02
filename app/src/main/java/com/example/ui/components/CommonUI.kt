package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun SchoolBackground(modifier: Modifier = Modifier) {
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
                .drawBehind {
                    drawRect(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.15f),
                                Color.Black.copy(alpha = 0.35f)
                            )
                        )
                    )
                }
        )
    }
}

@Composable
fun BounceButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
        label = "bounce_scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        try {
                            awaitRelease()
                        } finally {
                            isPressed = false
                        }
                    },
                    onTap = { onClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        content()
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
fun TactileGradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    brush: Brush = Brush.verticalGradient(colors = listOf(Color(0xFFFF8C42), Color(0xFFFF5722))),
    shadowColor: Color = Color(0xFFB02F00),
    content: @Composable RowScope.() -> Unit
) {
    BounceButton(onClick = onClick, modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(shadowColor, RoundedCornerShape(28.dp))
                .padding(bottom = 4.dp)
                .background(brush, RoundedCornerShape(28.dp))
                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                content = content
            )
        }
    }
}
