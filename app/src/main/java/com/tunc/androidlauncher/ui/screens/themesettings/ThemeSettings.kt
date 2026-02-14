package com.tunc.androidlauncher.ui.screens.themesettings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tunc.androidlauncher.R
import com.tunc.androidlauncher.data.ThemeManager
import com.tunc.androidlauncher.data.ThemeMode
import com.tunc.androidlauncher.ui.screens.themesettings.models.ThemeOption


@Composable
fun ThemeSettings(
    innerPadding: PaddingValues,
    onBackClick: () -> Unit = {},
    onThemeChange: (ThemeMode) -> Unit = {},
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    onBackgroundColor: Color = MaterialTheme.colorScheme.onBackground,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    surfaceColor: Color = MaterialTheme.colorScheme.surface,
    titleLargeStyle: TextStyle = MaterialTheme.typography.titleLarge,
    titleMediumStyle: TextStyle = MaterialTheme.typography.titleMedium,
    bodySmallStyle: TextStyle = MaterialTheme.typography.bodySmall
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager(context) }
    var selectedTheme by remember { mutableStateOf(themeManager.getThemeMode()) }

    val themeOptions = listOf(
        ThemeOption(ThemeMode.SYSTEM, R.string.theme_system, R.string.theme_system_description),
        ThemeOption(ThemeMode.LIGHT, R.string.theme_light, R.string.theme_light_description),
        ThemeOption(ThemeMode.DARK, R.string.theme_dark, R.string.theme_dark_description)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(innerPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = onBackgroundColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.theme_settings_title).uppercase(),
                    style = titleLargeStyle.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    ),
                    color = onBackgroundColor
                )
            }

            Text(
                text = stringResource(R.string.theme_settings_subtitle),
                style = bodySmallStyle,
                color = onBackgroundColor.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(themeOptions) { option ->
                    ThemeOptionItem(
                        option = option,
                        isSelected = selectedTheme == option.mode,
                        onClick = {
                            selectedTheme = option.mode
                            themeManager.saveThemeMode(option.mode)
                            onThemeChange(option.mode)
                        },
                        primaryColor = primaryColor,
                        surfaceColor = surfaceColor,
                        onBackgroundColor = onBackgroundColor,
                        titleMediumStyle = titleMediumStyle,
                        bodySmallStyle = bodySmallStyle
                    )
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
fun ThemeOptionItem(
    option: ThemeOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    primaryColor: Color,
    surfaceColor: Color,
    onBackgroundColor: Color,
    titleMediumStyle: TextStyle,
    bodySmallStyle: TextStyle
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) primaryColor.copy(alpha = 0.15f) else surfaceColor.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(option.titleResId),
                    style = titleMediumStyle.copy(fontWeight = FontWeight.Medium),
                    color = if (isSelected) primaryColor else onBackgroundColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(option.descriptionResId),
                    style = bodySmallStyle,
                    color = onBackgroundColor.copy(alpha = 0.6f)
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
