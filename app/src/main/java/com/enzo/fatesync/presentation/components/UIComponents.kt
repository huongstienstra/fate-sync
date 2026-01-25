package com.enzo.fatesync.presentation.components

import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.enzo.fatesync.ui.theme.Primary
import com.enzo.fatesync.ui.theme.PrimaryLight
import com.enzo.fatesync.ui.theme.Tertiary

/**
 * Decorative pink blob for background decoration
 */
@Composable
fun DecorativeBlob(
    modifier: Modifier = Modifier,
    color: Color = Primary.copy(alpha = 0.15f),
    size: Dp = 200.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .blur(40.dp)
            .clip(CircleShape)
            .background(color)
    )
}

/**
 * Animated floating blob
 */
@Composable
fun AnimatedBlob(
    modifier: Modifier = Modifier,
    color: Color = Primary.copy(alpha = 0.12f),
    size: Dp = 250.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "blob")

    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blobOffset"
    )

    Box(
        modifier = modifier
            .offset(y = offsetY.dp)
            .size(size)
            .blur(50.dp)
            .clip(CircleShape)
            .background(color)
    )
}

/**
 * Styled photo frame with gradient border and shadow
 */
@Composable
fun StyledPhotoFrame(
    photoUri: Uri?,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    borderGradient: List<Color> = listOf(Primary, Tertiary)
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    spotColor = Primary.copy(alpha = 0.3f)
                )
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(colors = borderGradient)
                )
                .padding(4.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = label,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Clean placeholder with camera icon
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = Primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Primary.copy(alpha = 0.5f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Primary
        )
    }
}

/**
 * Gradient button component
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gradient: List<Color> = listOf(Primary, Tertiary, PrimaryLight)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = if (enabled) 8.dp else 0.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = Primary.copy(alpha = 0.4f)
            )
            .clip(RoundedCornerShape(28.dp))
            .background(
                brush = if (enabled) {
                    Brush.horizontalGradient(colors = gradient)
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Gray.copy(alpha = 0.3f),
                            Color.Gray.copy(alpha = 0.3f)
                        )
                    )
                }
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * Styled card with soft shadow
 */
@Composable
fun StyledCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Primary.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        content()
    }
}

/**
 * Outlined button component
 */
@Composable
fun OutlinedGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(listOf(Primary, Tertiary)),
                shape = RoundedCornerShape(28.dp)
            )
            .background(Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Primary
        )
    }
}

/**
 * Screen background with decorative blobs
 */
@Composable
fun DecorativeBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top-left blob
        AnimatedBlob(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-80).dp, y = (-60).dp),
            color = Primary.copy(alpha = 0.12f),
            size = 280.dp
        )

        // Top-right blob
        DecorativeBlob(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = 100.dp),
            color = Tertiary.copy(alpha = 0.10f),
            size = 200.dp
        )

        // Bottom-left blob
        DecorativeBlob(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-40).dp, y = 80.dp),
            color = PrimaryLight.copy(alpha = 0.15f),
            size = 220.dp
        )

        // Bottom-right blob
        AnimatedBlob(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 40.dp),
            color = Primary.copy(alpha = 0.08f),
            size = 300.dp
        )

        content()
    }
}
