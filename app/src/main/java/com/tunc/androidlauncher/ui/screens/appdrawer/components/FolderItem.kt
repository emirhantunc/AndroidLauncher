package com.tunc.androidlauncher.ui.screens.appdrawer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tunc.androidlauncher.core.models.AppInfo

@Composable
fun FolderItem(
    folderName: String,
    apps: List<AppInfo>,
    onClick: () -> Unit,
    iconSize: Int = 40,
    bgColor: Color = MaterialTheme.colorScheme.background,
    labelSmall: TextStyle = MaterialTheme.typography.labelSmall,
    onBackGround: Color = MaterialTheme.colorScheme.onBackground
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(bottom = 16.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size((iconSize + 20).dp)
                .clip(RoundedCornerShape(18.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            when {
                apps.isEmpty() -> {
                    Text(
                        text = folderName.take(1).uppercase(),
                        color = onBackGround,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
                apps.size == 1 -> {
                    apps[0].icon?.let { icon ->
                        AsyncImage(
                            model = icon,
                            contentDescription = apps[0].name,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(18.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val gridApps = apps.take(4)
                        val iconSizeSmall = (iconSize + 20) / 2 - 2

                        gridApps.forEachIndexed { index, app ->
                            val xOffset = if (index % 2 == 0) 2.dp else (iconSizeSmall + 4).dp
                            val yOffset = if (index < 2) 2.dp else (iconSizeSmall + 4).dp

                            Box(
                                modifier = Modifier
                                    .offset(x = xOffset, y = yOffset)
                                    .size(iconSizeSmall.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                app.icon?.let { icon ->
                                    AsyncImage(
                                        model = icon,
                                        contentDescription = app.name,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = folderName,
            style = labelSmall,
            color = onBackGround,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 80.dp)
        )
    }
}
