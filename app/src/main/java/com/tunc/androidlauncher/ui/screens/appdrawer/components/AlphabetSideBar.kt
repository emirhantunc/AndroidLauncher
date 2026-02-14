package com.tunc.androidlauncher.ui.screens.appdrawer.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import org.w3c.dom.Text

@Composable
fun AlphabetSidebar(
    letters: List<String>,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    labelMedium : TextStyle = MaterialTheme.typography.labelMedium,
    modifier: Modifier = Modifier,
    onLetterClick: (String) -> Unit = {}
) {
    val alphabet = ('A'..'Z').toList()

    Column(
        modifier = modifier
            .padding(end = 4.dp)
            .width(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        alphabet.forEach { char ->
            val isPresent = letters.contains(char.toString())
            Text(
                text = char.toString(),
                style = labelMedium,
                color = if (isPresent) primaryColor else Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.clickable(enabled = isPresent) {
                    if (isPresent) {
                        onLetterClick(char.toString())
                    }
                }
            )
        }
    }
}