package com.tunc.androidlauncher.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.tunc.androidlauncher.R
import com.tunc.androidlauncher.core.models.AppInfo

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun FolderDialog(
    folderName: String,
    apps: List<AppInfo>,
    onDismiss: () -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onRenameFolder: (String) -> Unit,
    onAppRemove: (AppInfo) -> Unit = {},
    iconSize: Int = 48,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    surfaceColor: Color = MaterialTheme.colorScheme.surface,
    onBackgroundColor: Color = MaterialTheme.colorScheme.onBackground,
    primaryColor: Color = MaterialTheme.colorScheme.primary
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(folderName) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(isEditing) {
        if (isEditing) {
            focusRequester.requestFocus()
        }
    }

    // Blur background dialog
    Dialog(
        onDismissRequest = {
            if (isEditing) {
                isEditing = false
                keyboardController?.hide()
                if (editedName.isNotBlank() && editedName != folderName) {
                    onRenameFolder(editedName)
                }
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(
                    onClick = {
                        if (isEditing) {
                            isEditing = false
                            keyboardController?.hide()
                            if (editedName.isNotBlank() && editedName != folderName) {
                                onRenameFolder(editedName)
                            }
                        } else {
                            onDismiss()
                        }
                    },
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                ),
            contentAlignment = Alignment.Center
        ) {
            // Dialog card - transparent background
            Card(
                modifier = Modifier
                    .width(340.dp)
                    .height(400.dp)
                    .clickable(
                        onClick = { /* Dialog içine tıklama - dışarı kapatmayı engelle */ },
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = surfaceColor.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Klasör adı - tıklanabilir ve düzenlenebilir
                    if (isEditing) {
                        BasicTextField(
                            value = editedName,
                            onValueChange = { editedName = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            textStyle = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = onBackgroundColor,
                                textAlign = TextAlign.Center
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(primaryColor),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    isEditing = false
                                    keyboardController?.hide()
                                    if (editedName.isNotBlank() && editedName != folderName) {
                                        onRenameFolder(editedName)
                                    }
                                }
                            )
                        )
                    } else {
                        Text(
                            text = folderName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = onBackgroundColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editedName = folderName
                                    isEditing = true
                                }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Uygulamalar grid - sabit boyut, kaydırmalı
                    if (apps.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.folder_empty),
                                color = onBackgroundColor.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(apps) { app ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.combinedClickable(
                                        onClick = {
                                            if (!isEditing) {
                                                onAppClick(app)
                                            }
                                        },
                                        onLongClick = {
                                            if (!isEditing) {
                                                onAppRemove(app)
                                            }
                                        }
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(iconSize.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color.Transparent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        app.icon?.let { icon ->
                                            AsyncImage(
                                                model = icon,
                                                contentDescription = app.name,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(14.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = app.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = onBackgroundColor,
                                        fontSize = 11.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.widthIn(max = 64.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
