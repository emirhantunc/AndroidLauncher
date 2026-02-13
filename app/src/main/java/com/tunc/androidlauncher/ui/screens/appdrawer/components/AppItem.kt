package com.tunc.androidlauncher.ui.screens.appdrawer.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tunc.androidlauncher.core.models.AppInfo
import com.tunc.androidlauncher.core.toBitmap

@Composable
fun AppItem(app: AppInfo, bgColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            app.icon?.let { icon ->
                AsyncImage(
                    model = icon,
                    contentDescription = app.name,
                    modifier = Modifier.size(40.dp)
                )
            } ?: run {
                Text(app.label.take(1), color = Color.White, fontWeight = FontWeight.Bold)

            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = app.name,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.labelSmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.width(64.dp)
        )
    }
}