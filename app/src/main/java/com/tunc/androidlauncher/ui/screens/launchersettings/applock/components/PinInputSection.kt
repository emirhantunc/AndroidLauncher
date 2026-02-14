package com.tunc.androidlauncher.ui.screens.launchersettings.applock.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun PinInputSection(
    pin: String,
    onPinChange: (String) -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    onBackgroundColor: Color = MaterialTheme.colorScheme.onBackground,
    surfaceColor: Color = MaterialTheme.colorScheme.surface,
    titleLargeStyle: TextStyle = MaterialTheme.typography.titleLarge
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Text(
            text = title,
            style = titleLargeStyle.copy(fontWeight = FontWeight.SemiBold),
            color = onBackgroundColor,
            textAlign = TextAlign.Center
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (index < pin.length) primaryColor else surfaceColor.copy(alpha = 0.3f)
                        )
                )
            }
        }

        OutlinedTextField(
            value = pin,
            onValueChange = { if (it.length <= 4) onPinChange(it) },
            modifier = Modifier
                .width(1.dp)
                .height(1.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            )
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(3) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(3) { col ->
                        val number = row * 3 + col + 1
                        NumberButton(
                            number = number.toString(),
                            onClick = {
                                if (pin.length < 4) {
                                    onPinChange(pin + number)
                                }
                            }
                        )
                        Spacer(modifier = modifier.height(6.dp))
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Spacer(modifier = Modifier.size(64.dp))
                NumberButton(
                    number = "0",
                    onClick = {
                        if (pin.length < 4) {
                            onPinChange(pin + "0")
                        }
                    }
                )
                Spacer(modifier = modifier.width(6.dp))
                NumberButton(
                    number = "⌫",
                    onClick = {
                        if (pin.isNotEmpty()) {
                            onPinChange(pin.dropLast(1))
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun NumberButton(
    number: String,
    onClick: () -> Unit,
    surfaceColor: Color = MaterialTheme.colorScheme.surface,
    onBackgroundColor: Color  = MaterialTheme.colorScheme.onBackground,
    headlineMediumStyle : TextStyle = MaterialTheme.typography.headlineMedium
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(64.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = surfaceColor.copy(alpha = 0.5f),
            contentColor = onBackgroundColor
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = number,
            style = headlineMediumStyle.copy(fontWeight = FontWeight.Medium),
            color = onBackgroundColor
        )
    }
}
