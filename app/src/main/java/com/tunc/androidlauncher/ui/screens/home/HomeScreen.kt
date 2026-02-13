package com.tunc.androidlauncher.ui.screens.home

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tunc.androidlauncher.core.findApp
import com.tunc.androidlauncher.core.getInstalledApps
import com.tunc.androidlauncher.core.models.AppInfo
import com.tunc.androidlauncher.core.toBitmap
import com.tunc.androidlauncher.ui.screens.home.components.BottomBar
import com.tunc.androidlauncher.ui.screens.home.components.HomeGrid
import com.tunc.androidlauncher.ui.screens.home.components.LockScreenClock
import com.tunc.androidlauncher.ui.screens.home.components.SearchBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.collections.isNotEmpty

val ColorBackgroundDark = Color(0xFF000000)
val ColorSurfaceDark = Color(0xFF101622)
val ColorTextWhite = Color(0xFFFFFFFF)
val ColorTextGray = Color(0xFF71717a)
val ColorBorder = Color(0xFF27272a)

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(innerPaddingValues: PaddingValues, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var gridApps by remember { mutableStateOf<List<AppInfo?>>(emptyList()) }
    var dockApps by remember { mutableStateOf<List<AppInfo?>>(emptyList()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val apps = getInstalledApps(context)

            val phone = findApp(apps, listOf("dialer", "phone", "call", "telefon"))
            val sms = findApp(apps, listOf("message", "sms", "messaging", "mesaj"))
            val browser = findApp(apps, listOf("chrome", "browser", "internet", "web"))
            val camera = findApp(apps, listOf("camera", "kamera", "foto"))

            gridApps = listOf(phone, sms, browser, camera)
            dockApps = listOf(phone, sms, browser, camera)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackgroundDark)
            .padding(16.dp)
            .padding(innerPaddingValues)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SearchBar(
                primary = MaterialTheme.colorScheme.primary,
                onSurface = MaterialTheme.colorScheme.onSurface,
                onSecondary = MaterialTheme.colorScheme.onSecondary,
                onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(26.dp))

            LockScreenClock(
                onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant,
                onSurface = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (gridApps.isNotEmpty()) {
                HomeGrid(
                    apps = gridApps,
                    context = context,
                    onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant,
                    backGround = MaterialTheme.colorScheme.background
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            BottomBar(apps = dockApps, context = context)
        }
    }
}







