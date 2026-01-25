package com.enzo.fatesync.presentation.screens.sync

import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.enzo.fatesync.R
import com.enzo.fatesync.domain.model.CompatibilityResult
import com.enzo.fatesync.presentation.screens.analysis.AnalysisState
import com.enzo.fatesync.presentation.screens.analysis.AnalysisViewModel
import com.enzo.fatesync.ui.theme.HeartPink
import com.enzo.fatesync.ui.theme.Primary
import com.enzo.fatesync.ui.theme.PrimaryLight
import com.enzo.fatesync.ui.theme.Tertiary

@Composable
fun SyncScreen(
    yourPhotoUri: Uri,
    partnerPhotoUri: Uri,
    onSyncComplete: (CompatibilityResult) -> Unit,
    onError: (String) -> Unit,
    viewModel: AnalysisViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val heart1Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heart1Offset"
    )

    val heart2Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heart2Offset"
    )

    LaunchedEffect(Unit) {
        viewModel.analyzePhotos(yourPhotoUri.toString(), partnerPhotoUri.toString())
    }

    LaunchedEffect(state) {
        when (val currentState = state) {
            is AnalysisState.FacesDetected -> onSyncComplete(currentState.compatibilityResult)
            is AnalysisState.Error -> onError(currentState.message)
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary),
        contentAlignment = Alignment.Center
    ) {
        // Large heart shape background
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.Center)
                .offset(y = (-80).dp),
            tint = PrimaryLight.copy(alpha = 0.3f)
        )

        // Floating hearts decoration
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .offset(x = (-120).dp, y = (heart1Offset - 180).dp)
                .scale(pulseScale * 0.8f),
            tint = Color.White.copy(alpha = 0.2f)
        )

        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-40).dp, y = (heart2Offset + 120).dp),
            tint = Color.White.copy(alpha = 0.15f)
        )

        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .offset(x = 130.dp, y = (heart2Offset + 200).dp)
                .scale(pulseScale * 0.7f),
            tint = Color.White.copy(alpha = 0.2f)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Photos with heart
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Your photo
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = CircleShape,
                            spotColor = Color.Black.copy(alpha = 0.2f)
                        )
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(4.dp)
                        .clip(CircleShape)
                ) {
                    AsyncImage(
                        model = yourPhotoUri,
                        contentDescription = stringResource(R.string.sync_you),
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Pulsing heart
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .scale(pulseScale),
                    tint = Color.White
                )

                Spacer(modifier = Modifier.width(20.dp))

                // Partner photo
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = CircleShape,
                            spotColor = Color.Black.copy(alpha = 0.2f)
                        )
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(4.dp)
                        .clip(CircleShape)
                ) {
                    AsyncImage(
                        model = partnerPhotoUri,
                        contentDescription = stringResource(R.string.sync_partner),
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(56.dp))

            // Status text
            Text(
                text = "Finding your connection...",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 24.sp
                ),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.sync_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Animated loading dots
            LoadingDots()
        }
    }
}

@Composable
private fun LoadingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    val dot1Scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )

    val dot2Scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 150),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )

    val dot3Scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 300),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .scale(dot1Scale)
                .clip(CircleShape)
                .background(Color.White)
        )
        Box(
            modifier = Modifier
                .size(12.dp)
                .scale(dot2Scale)
                .clip(CircleShape)
                .background(Color.White)
        )
        Box(
            modifier = Modifier
                .size(12.dp)
                .scale(dot3Scale)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}
