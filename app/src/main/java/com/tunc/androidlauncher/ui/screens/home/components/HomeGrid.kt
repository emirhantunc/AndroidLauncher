package com.tunc.androidlauncher.ui.screens.home.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tunc.androidlauncher.core.models.AppInfo


@Composable
fun HomeGrid(
    apps: List<AppInfo?>,
    context: Context,
    modifier: Modifier = Modifier,
    onSurfaceVariant: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    backGround: Color = MaterialTheme.colorScheme.background
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        userScrollEnabled = false,
        modifier = modifier.fillMaxWidth()
    ) {
        items(apps.take(4)) { app ->
            Box(contentAlignment = Alignment.Center) {
                HomeIconItem(
                    app = app,
                    context = context,
                    onSurfaceVariant = onSurfaceVariant,
                    backGround = backGround
                )
            }
        }
    }
}

@Composable
private fun HomeIconItem(
    app: AppInfo?,
    context: Context,
    onSurfaceVariant: Color,
    backGround: Color,
    bodySmall: TextStyle = MaterialTheme.typography.bodySmall
) {
    if (app == null) {
        Spacer(modifier = Modifier.size(64.dp))
        return
    }

    Column(
        modifier = Modifier.width(84.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(backGround)
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                .clickable {
                    val launchIntent =
                        context.packageManager.getLaunchIntentForPackage(app.packageName)
                    launchIntent?.let { context.startActivity(it) }
                }, contentAlignment = Alignment.Center
        ) {
            app.icon?.let { icon ->
                AsyncImage(
                    model = icon,
                    contentDescription = app.name,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Text(
            text = app.name,
            style = bodySmall,
            color = onSurfaceVariant,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .widthIn(max = 70.dp)
        )
    }
}