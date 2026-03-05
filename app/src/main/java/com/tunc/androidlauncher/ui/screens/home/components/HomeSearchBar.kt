package com.tunc.androidlauncher.ui.screens.home.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tunc.androidlauncher.R

import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

@Composable
fun HomeSearchBar(
    modifier: Modifier = Modifier,
    primary: Color = MaterialTheme.colorScheme.primary,
    onSurface: Color = MaterialTheme.colorScheme.onSurface,
    onSecondary: Color = MaterialTheme.colorScheme.onSecondary,
    onSurfaceVariant: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    bodyMedium : TextStyle = MaterialTheme.typography.bodyMedium
) {
    var text by remember { mutableStateOf("") }


    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        modifier = modifier.fillMaxWidth(),

        shape = RoundedCornerShape(12.dp),

        placeholder = {
            Text(
                stringResource(R.string.search_apps_place_holder),
                color = onSurfaceVariant,
                style = bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                Icons.Outlined.Search,
                contentDescription = "Search", tint = onSecondary
            )
        },

        singleLine = true,

        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
            focusedBorderColor = onSurface,
            cursorColor = primary,
            focusedTextColor = onSurface,
            unfocusedTextColor = onSurface
        )
    )
}