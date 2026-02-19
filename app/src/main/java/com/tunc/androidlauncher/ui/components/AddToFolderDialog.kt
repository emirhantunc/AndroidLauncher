package com.tunc.androidlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.tunc.androidlauncher.data.database.AppFolder
import kotlinx.coroutines.launch

@Composable
fun AddToFolderDialog(
    appName: String,
    appPackageName: String,
    folders: List<AppFolder>,
    onDismiss: () -> Unit,
    onCreateNewFolder: (String) -> Unit,
    onSelectFolder: (Long) -> Unit,
    surfaceColor: Color = MaterialTheme.colorScheme.surface,
    onBackgroundColor: Color = MaterialTheme.colorScheme.onBackground,
    primaryColor: Color = MaterialTheme.colorScheme.primary
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$appName - Klasöre Ekle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = onBackgroundColor,
                    modifier = Modifier.padding(16.dp)
                )

                HorizontalDivider(color = onBackgroundColor.copy(alpha = 0.1f))

                ListItem(
                    headlineContent = {
                        Text("Yeni Klasör Oluştur", color = primaryColor, fontWeight = FontWeight.Medium)
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = null,
                            tint = primaryColor
                        )
                    },
                    modifier = Modifier.clickable {
                        showCreateDialog = true
                    }
                )

                if (folders.isNotEmpty()) {
                    HorizontalDivider(color = onBackgroundColor.copy(alpha = 0.1f))

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(folders) { folder ->
                            ListItem(
                                headlineContent = {
                                    Text(folder.name, color = onBackgroundColor)
                                },
                                leadingContent = {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = onBackgroundColor
                                    )
                                },
                                modifier = Modifier.clickable {
                                    onSelectFolder(folder.id)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { folderName ->
                onCreateNewFolder(folderName)
                showCreateDialog = false
                onDismiss()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    suggestedCategory: String = "",
    surfaceColor: Color = MaterialTheme.colorScheme.surface,
    onBackgroundColor: Color = MaterialTheme.colorScheme.onBackground,
    primaryColor: Color = MaterialTheme.colorScheme.primary
) {
    var folderName by remember { mutableStateOf(suggestedCategory) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Yeni Klasör",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = onBackgroundColor
                )

                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("Klasör Adı") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        focusedLabelColor = primaryColor,
                        cursorColor = primaryColor
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("İptal", color = onBackgroundColor)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (folderName.isNotBlank()) {
                                onConfirm(folderName)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        enabled = folderName.isNotBlank()
                    ) {
                        Text("Oluştur")
                    }
                }
            }
        }
    }
}
