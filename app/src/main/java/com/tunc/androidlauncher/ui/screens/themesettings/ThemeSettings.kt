package com.tunc.androidlauncher.ui.screens.themesettings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.tunc.androidlauncher.R
import com.tunc.androidlauncher.data.ThemeManager
import com.tunc.androidlauncher.data.ThemeMode
import com.tunc.androidlauncher.data.WallpaperManager
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
    val wallpaperManager = remember { WallpaperManager(context) }
    var selectedTheme by remember { mutableStateOf(themeManager.getThemeMode()) }
    val wallpaperUri by wallpaperManager.wallpaperUriFlow.collectAsStateWithLifecycle()

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            wallpaperManager.setWallpaper(it.toString())
        }
    }

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
                    text = "WALLPAPER & THEME",
                    style = titleLargeStyle.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    ),
                    color = onBackgroundColor
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "WALLPAPER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 3.sp
                        ),
                        color = onBackgroundColor.copy(alpha = 0.3f),
                        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                    )
                }

                item {
                    WallpaperSection(
                        wallpaperUri = wallpaperUri,
                        onSelectWallpaper = { pickImageLauncher.launch("image/*") },
                        onRemoveWallpaper = { wallpaperManager.clearWallpaper() },
                        surfaceColor = surfaceColor,
                        onBackgroundColor = onBackgroundColor,
                        primaryColor = primaryColor,
                        titleMediumStyle = titleMediumStyle,
                        bodySmallStyle = bodySmallStyle
                    )
                }

                item {
                    Text(
                        text = "THEME",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 3.sp
                        ),
                        color = onBackgroundColor.copy(alpha = 0.3f),
                        modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
                    )
                }
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

@Composable
private fun WallpaperSection(
    wallpaperUri: String?,
    onSelectWallpaper: () -> Unit,
    onRemoveWallpaper: () -> Unit,
    surfaceColor: Color,
    onBackgroundColor: Color,
    primaryColor: Color,
    titleMediumStyle: TextStyle,
    bodySmallStyle: TextStyle
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (wallpaperUri != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = surfaceColor.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(Uri.parse(wallpaperUri)),
                            contentDescription = "Current Wallpaper",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Current Wallpaper",
                                style = titleMediumStyle.copy(fontWeight = FontWeight.Medium),
                                color = onBackgroundColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Custom wallpaper is set",
                                style = bodySmallStyle,
                                color = onBackgroundColor.copy(alpha = 0.6f)
                            )
                        }

                        IconButton(
                            onClick = onRemoveWallpaper
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove Wallpaper",
                                tint = onBackgroundColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onSelectWallpaper),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = primaryColor.copy(alpha = 0.15f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (wallpaperUri != null) "Change Wallpaper" else "Set Wallpaper",
                    style = titleMediumStyle.copy(fontWeight = FontWeight.Medium),
                    color = primaryColor
                )
            }
        }
    }
}

