package com.tunc.androidlauncher.ui.screens.appdrawer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.tunc.androidlauncher.R

@Composable
fun RowScope.SearchBarSection(
    primary: Color = MaterialTheme.colorScheme.primary,
    surface: Color = MaterialTheme.colorScheme.surface,
    onSurfaceVariant: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onSurface: Color = MaterialTheme.colorScheme.onSurface,
    bodyMedium: TextStyle = MaterialTheme.typography.bodyMedium
) {
    var searchQuery by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .weight(1f)
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(surface.copy(alpha = 0.5f)), contentAlignment = Alignment.CenterStart
    ) {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),

            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = onSurfaceVariant
                )
            },

            placeholder = {
                Text(
                    text = stringResource(R.string.search_apps_place_holder),
                    color = onSurfaceVariant,
                    style = bodyMedium
                )
            },

            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                cursorColor = primary,
                focusedTextColor = onSurface,
                unfocusedTextColor = onSurface
            ),
            shape = RoundedCornerShape(12.dp),
            textStyle = bodyMedium
        )
    }
}
