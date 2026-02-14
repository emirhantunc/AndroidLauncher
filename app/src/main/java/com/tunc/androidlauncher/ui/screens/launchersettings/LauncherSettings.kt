package com.tunc.androidlauncher.ui.screens.launchersettings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tunc.androidlauncher.R


@Composable
fun LauncherSettings(
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues,
    onNavigateToAppLock: () -> Unit = {},
    onBackClick: () -> Unit = {},
    backGroundColor: Color = MaterialTheme.colorScheme.background,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    titleLargeStyle: TextStyle = MaterialTheme.typography.titleLarge,
    titleMediumStyle: TextStyle = MaterialTheme.typography.titleMedium,
) {
    val settingsList = SettingsDataSource.menuData

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backGroundColor)
            .padding(innerPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
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
                        text = stringResource(R.string.settings).uppercase(),
                        style = titleLargeStyle.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        ),
                        color = titleColor
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                settingsList.forEach { section ->

                    item(key = "header_${section.id}") {
                        Column(modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)) {
                            Text(
                                text = stringResource(section.titleResId).uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 3.sp
                                ),
                                color = titleColor.copy(alpha = 0.3f),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            HorizontalDivider(
                                color = titleColor.copy(alpha = 0.1f),
                                thickness = 1.dp
                            )
                        }
                    }

                    items(
                        items = section.settings,
                        key = { setting -> setting.id }
                    ) { setting ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (setting.id == "app_lock") {
                                        onNavigateToAppLock()
                                    }
                                }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(setting.titleResId),
                                    style = titleMediumStyle.copy(fontWeight = FontWeight.Normal),
                                    color = titleColor
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = titleColor.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}