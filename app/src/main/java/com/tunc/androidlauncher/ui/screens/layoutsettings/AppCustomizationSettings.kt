package com.tunc.androidlauncher.ui.screens.layoutsettings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.tunc.androidlauncher.core.models.AppInfo
import com.tunc.androidlauncher.data.AppCustomizationManager
import com.tunc.androidlauncher.data.AppManager
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppCustomizationSettings(
    onBackClick: () -> Unit,
    innerPadding: PaddingValues = PaddingValues(0.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    onBackground: Color = MaterialTheme.colorScheme.onBackground,
    surface: Color = MaterialTheme.colorScheme.surface,
    primary: Color = MaterialTheme.colorScheme.primary
) {
    val context = LocalContext.current
    val appManager = remember { AppManager.getInstance(context) }
    val customizationManager = remember { AppCustomizationManager(context) }

    val allApps by appManager.allApps.collectAsStateWithLifecycle()
    val customizations by customizationManager.getAllCustomizations().collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedApp by remember { mutableStateOf<AppInfo?>(null) }
    var showCustomizeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        appManager.loadApps()
    }

    var offsetX by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(innerPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX > 100) {
                                onBackClick()
                            }
                            offsetX = 0f
                        },
                        onDragCancel = {
                            offsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            val newOffset = offsetX + dragAmount
                            offsetX = if (newOffset > 0) newOffset else 0f
                        }
                    )
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = onBackground
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = "App Customization",
                        style = MaterialTheme.typography.titleLarge,
                        color = onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Customize app icons and names",
                        style = MaterialTheme.typography.bodySmall,
                        color = onBackground.copy(alpha = 0.6f)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allApps) { app ->
                    val customization = customizations.find { it.packageName == app.packageName }
                    val displayName = customization?.customName ?: app.label
                    val iconUri = customization?.customIconUri

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedApp = app
                                showCustomizeDialog = true
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    if (iconUri != null) {
                                        Image(
                                            painter = rememberAsyncImagePainter(Uri.parse(iconUri)),
                                            contentDescription = displayName,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        app.icon?.let { icon ->
                                            Image(
                                                painter = rememberAsyncImagePainter(icon),
                                                contentDescription = displayName,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(12.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }

                                Column {
                                    Text(
                                        text = displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = onBackground,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (customization != null) {
                                        Text(
                                            text = "Customized",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = primary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Customize",
                                tint = onBackground.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCustomizeDialog && selectedApp != null) {
        CustomizeAppDialog(
            app = selectedApp!!,
            appManager = appManager,
            customizationManager = customizationManager,
            onDismiss = { showCustomizeDialog = false },
            onBackground = onBackground,
            surface = surface,
            primary = primary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomizeAppDialog(
    app: AppInfo,
    appManager: AppManager,
    customizationManager: AppCustomizationManager,
    onDismiss: () -> Unit,
    onBackground: Color,
    surface: Color,
    primary: Color
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val customization by customizationManager.getCustomization(app.packageName).collectAsStateWithLifecycle(initialValue = null)

    var customName by remember { mutableStateOf("") }
    var customIconUri by remember { mutableStateOf<String?>(null) }
    var showNameInput by remember { mutableStateOf(false) }

    LaunchedEffect(customization) {
        customName = customization?.customName ?: app.label
        customIconUri = customization?.customIconUri
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            customIconUri = it.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = surface,
        title = {
            Text(
                text = "Customize ${app.label}",
                color = onBackground
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .clickable { pickImageLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (customIconUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(Uri.parse(customIconUri)),
                                contentDescription = "Custom Icon",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            app.icon?.let { icon ->
                                Image(
                                    painter = rememberAsyncImagePainter(icon),
                                    contentDescription = app.label,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Change Icon",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = { showNameInput = !showNameInput },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (showNameInput) "Hide Name Editor" else "Change Name")
                }

                if (showNameInput) {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("App Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primary,
                            focusedLabelColor = primary
                        )
                    )
                }

                if (customization != null) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                appManager.updateAppCustomization(app.packageName, null, null)
                                customIconUri = null
                                customName = app.label
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset to Default")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        appManager.updateAppCustomization(
                            packageName = app.packageName,
                            iconUri = customIconUri,
                            name = if (customName != app.label) customName else null
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = primary
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = onBackground)
            }
        }
    )
}
