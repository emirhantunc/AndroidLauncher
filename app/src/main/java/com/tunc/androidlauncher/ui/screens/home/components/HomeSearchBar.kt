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

@Composable
fun SearchBar(
    primary: Color,
    onSurface: Color,
    onSecondary: Color,
    onSurfaceVariant: Color
) {
    var text by remember { mutableStateOf("") }


    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(12.dp),

        placeholder = {
            Text(
                stringResource(R.string.search_apps_place_holder),
                color = onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
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
            focusedBorderColor = Color.White,
            cursorColor = primary,
            focusedTextColor = onSurface,
            unfocusedTextColor = onSurface
        )
    )
}