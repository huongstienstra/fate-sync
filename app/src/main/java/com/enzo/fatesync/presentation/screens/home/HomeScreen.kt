package com.enzo.fatesync.presentation.screens.home

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enzo.fatesync.R
import com.enzo.fatesync.data.local.AppLanguage
import com.enzo.fatesync.presentation.components.GradientButton
import com.enzo.fatesync.presentation.components.StyledPhotoFrame
import com.enzo.fatesync.ui.theme.HeartPink
import com.enzo.fatesync.ui.theme.Primary
import com.enzo.fatesync.ui.theme.PrimaryLight
import com.enzo.fatesync.ui.theme.Tertiary

@Composable
fun HomeScreen(
    yourPhotoUri: Uri?,
    partnerPhotoUri: Uri?,
    currentLanguage: AppLanguage,
    onYourPhotoClick: () -> Unit,
    onPartnerPhotoClick: () -> Unit,
    onSyncClick: () -> Unit,
    onLanguageChange: (AppLanguage) -> Unit
) {
    val bothPhotosReady = yourPhotoUri != null && partnerPhotoUri != null
    var showLanguageMenu by remember { mutableStateOf(false) }

    val heartScale by animateFloatAsState(
        targetValue = if (bothPhotosReady) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "heartScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Large decorative pink circle - top left
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = (-120).dp, y = (-80).dp)
                .clip(CircleShape)
                .background(PrimaryLight.copy(alpha = 0.5f))
        )

        // Smaller accent circle - bottom right
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 80.dp)
                .clip(CircleShape)
                .background(PrimaryLight.copy(alpha = 0.3f))
        )

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Language selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Surface(
                        onClick = { showLanguageMenu = true },
                        shape = CircleShape,
                        color = Primary.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Primary
                            )
                            Text(
                                text = " ${currentLanguage.code.uppercase()}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Primary
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Primary
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = showLanguageMenu,
                        onDismissRequest = { showLanguageMenu = false }
                    ) {
                        AppLanguage.entries.forEach { language ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = language.displayName,
                                        fontWeight = if (language == currentLanguage) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onLanguageChange(language)
                                    showLanguageMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Title with accent color
            Text(
                text = buildAnnotatedString {
                    append("Find ")
                    withStyle(style = SpanStyle(color = Primary)) {
                        append("Love")
                    }
                    append(",\nConnect Hearts!")
                },
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 28.sp,
                    lineHeight = 36.sp
                ),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.home_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Photo slots with heart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StyledPhotoFrame(
                    photoUri = yourPhotoUri,
                    label = stringResource(R.string.home_you),
                    onClick = onYourPhotoClick,
                    borderGradient = listOf(Primary, Tertiary)
                )

                // Animated heart - always pink, just lighter when inactive
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .scale(heartScale),
                    tint = if (bothPhotosReady) HeartPink else PrimaryLight
                )

                StyledPhotoFrame(
                    photoUri = partnerPhotoUri,
                    label = stringResource(R.string.home_partner),
                    onClick = onPartnerPhotoClick,
                    borderGradient = listOf(Tertiary, PrimaryLight)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Sync button
            AnimatedVisibility(
                visible = bothPhotosReady,
                enter = fadeIn() + scaleIn()
            ) {
                GradientButton(
                    text = stringResource(R.string.home_sync_button),
                    onClick = onSyncClick,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (!bothPhotosReady) {
                Text(
                    text = stringResource(R.string.home_add_photos_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
