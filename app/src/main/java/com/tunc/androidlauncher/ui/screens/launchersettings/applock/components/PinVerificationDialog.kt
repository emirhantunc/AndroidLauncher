package com.tunc.androidlauncher.ui.screens.launchersettings.applock.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.tunc.androidlauncher.R

@Composable
fun PinVerificationDialog(
    onDismiss: () -> Unit,
    onPinVerified: () -> Unit,
    verifyPin: (String) -> Boolean,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    bodyMediumStyle : TextStyle = MaterialTheme.typography.bodyMedium,
    errorColor : Color = MaterialTheme.colorScheme.error
) {
    var pin by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    LaunchedEffect(pin) {
        if (pin.length == 4) {
            if (verifyPin(pin)) {
                onPinVerified()
            } else {
                showError = true
                pin = ""
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (showError) {
                    Text(
                        text = stringResource(R.string.app_lock_incorrect_pin),
                        style = bodyMediumStyle,
                        color = errorColor,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                PinInputSection(
                    pin = pin,
                    onPinChange = {
                        pin = it
                        showError = false
                    },
                    title = stringResource(R.string.app_lock_enter_pin),
                )
            }
        }
    }
}
