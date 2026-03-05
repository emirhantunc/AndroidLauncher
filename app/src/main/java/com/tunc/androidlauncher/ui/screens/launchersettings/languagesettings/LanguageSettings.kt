package com.tunc.androidlauncher.ui.screens.launchersettings.languagesettings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tunc.androidlauncher.data.LocaleManager
import com.tunc.androidlauncher.data.LanguageOption
import kotlin.math.roundToInt

@Composable
fun LanguageSettings(
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues,
    onBackClick: () -> Unit = {},
    backGroundColor: Color = MaterialTheme.colorScheme.background,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    titleLargeStyle: TextStyle = MaterialTheme.typography.titleLarge,
    primaryColor: Color = MaterialTheme.colorScheme.primary
) {
    val context = LocalContext.current
    val localeManager = remember { LocaleManager(context) }

    var currentLanguage by remember { mutableStateOf(localeManager.getCurrentLanguage()) }
    val languages = remember { localeManager.getAvailableLanguages() }

    var offsetX by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backGroundColor)
            .padding(innerPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX > 100) {
                                onBackClick()
                            }
                            offsetX = 0f
                        },
                        onDragCancel = {
                            offsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            val newOffset = offsetX + dragAmount
                            offsetX = if (newOffset > 0) newOffset else 0f
                        }
                    )
                }
                .padding(horizontal = 24.dp)
        ) {
            // Header
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = titleColor
                        )
                    }
                    Text(
                        text = when (currentLanguage) {
                            LocaleManager.LANGUAGE_TURKISH -> "DİL"
                            else -> "LANGUAGE"
                        },
                        style = titleLargeStyle.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        ),
                        color = titleColor
                    )
                }
            }

            // Language List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(languages) { language ->
                    LanguageItem(
                        language = language,
                        isSelected = currentLanguage == language.code,
                        onClick = {
                            currentLanguage = language.code
                            localeManager.setLanguage(language.code)
                        },
                        titleColor = titleColor,
                        primaryColor = primaryColor
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageItem(
    language: LanguageOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    titleColor: Color,
    primaryColor: Color,
    surfaceColor: Color = MaterialTheme.colorScheme.surface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .background(surfaceColor)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = language.displayName,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = if (isSelected) primaryColor else titleColor
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(primaryColor),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}
