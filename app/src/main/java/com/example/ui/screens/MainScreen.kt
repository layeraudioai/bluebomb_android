package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ExploitHistory
import com.example.ui.theme.*
import com.example.ui.viewmodel.BluebombViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: BluebombViewModel) {
    var activeTab by remember { mutableStateOf("Flasher") }
    val context = LocalContext.current
    var showScanDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    // Collect flows
    val consoleType by viewModel.consoleType.collectAsStateWithLifecycle()
    val region by viewModel.region.collectAsStateWithLifecycle()
    val exploitSpeed by viewModel.exploitSpeed.collectAsStateWithLifecycle()
    val isExploiting by viewModel.isExploiting.collectAsStateWithLifecycle()
    val pulseStatus by viewModel.pulseStatus.collectAsStateWithLifecycle()
    val terminalLogs by viewModel.terminalLogs.collectAsStateWithLifecycle()
    val bluetoothStatus by viewModel.bluetoothStatus.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val scannedDevices by viewModel.scannedDevices.collectAsStateWithLifecycle()
    val selectedMac by viewModel.selectedDeviceMac.collectAsStateWithLifecycle()
    val historyList by viewModel.exploitHistory.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = "Logo",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "Bluebomb Sender",
                            fontWeight = FontWeight.SemiBold,
                            color = OnBackgroundDark,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Information",
                            tint = OnSurfaceVariantDark
                        )
                    }
                    IconButton(onClick = {
                        Toast.makeText(context, "High Density Theme Configured", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = OnSurfaceVariantDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundLight
                ),
                modifier = Modifier.border(width = 1.dp, color = OutlineGrey).statusBarsPadding()
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceVariantLight,
                tonalElevation = 0.dp,
                modifier = Modifier.border(width = 1.dp, color = OutlineGrey).navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = activeTab == "Flasher",
                    onClick = { activeTab = "Flasher" },
                    label = { Text("Flasher", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == "Flasher") Icons.Filled.Bolt else Icons.Outlined.Bolt,
                            contentDescription = "Flasher"
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OnPrimaryBlueContainer,
                        selectedTextColor = OnBackgroundDark,
                        indicatorColor = PrimaryBlueContainer,
                        unselectedIconColor = OnSurfaceVariantDark,
                        unselectedTextColor = OnSurfaceVariantDark
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "History",
                    onClick = { activeTab = "History" },
                    label = { Text("History", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == "History") Icons.Filled.History else Icons.Outlined.History,
                            contentDescription = "History"
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OnPrimaryBlueContainer,
                        selectedTextColor = OnBackgroundDark,
                        indicatorColor = PrimaryBlueContainer,
                        unselectedIconColor = OnSurfaceVariantDark,
                        unselectedTextColor = OnSurfaceVariantDark
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "Guide",
                    onClick = { activeTab = "Guide" },
                    label = { Text("Guide", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == "Guide") Icons.Filled.MenuBook else Icons.Outlined.MenuBook,
                            contentDescription = "Guide"
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OnPrimaryBlueContainer,
                        selectedTextColor = OnBackgroundDark,
                        indicatorColor = PrimaryBlueContainer,
                        unselectedIconColor = OnSurfaceVariantDark,
                        unselectedTextColor = OnSurfaceVariantDark
                    )
                )
            }
        },
        containerColor = BackgroundLight
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(BackgroundLight)
        ) {
            when (activeTab) {
                "Flasher" -> FlasherTab(
                    consoleType = consoleType,
                    region = region,
                    exploitSpeed = exploitSpeed,
                    isExploiting = isExploiting,
                    pulseStatus = pulseStatus,
                    terminalLogs = terminalLogs,
                    bluetoothStatus = bluetoothStatus,
                    selectedMac = selectedMac,
                    onConsoleTypeChange = { viewModel.setConsoleType(it) },
                    onRegionChange = { viewModel.setRegion(it) },
                    onSpeedChange = { viewModel.setExploitSpeed(it) },
                    onTriggerExploit = { viewModel.executeExploit() },
                    onOpenScan = {
                        viewModel.startBluetoothScan()
                        showScanDialog = true
                    },
                    onClearLogs = { viewModel.clearLogs() }
                )
                "History" -> HistoryTab(
                    historyList = historyList,
                    onDelete = { viewModel.deleteHistoryItem(it) },
                    onClearAll = { viewModel.clearHistory() }
                )
                "Guide" -> GuideTab(
                    consoleType = consoleType,
                    region = region,
                    onPrepareUsb = { viewModel.prepareUsbDrive(it) }
                )
            }
        }
    }

    // Bluetooth discovery scan dialog
    if (showScanDialog) {
        AlertDialog(
            onDismissRequest = { showScanDialog = false },
            title = {
                Text(
                    text = "Bluetooth Targets",
                    fontWeight = FontWeight.Bold,
                    color = OnBackgroundDark
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Filled.BluetoothSearching,
                                contentDescription = "Bluetooth Status",
                                tint = PrimaryBlue
                            )
                        }
                        Text(
                            text = bluetoothStatus,
                            fontSize = 14.sp,
                            color = OnSurfaceVariantDark
                        )
                    }

                    HorizontalDivider(color = OutlineGrey)

                    if (scannedDevices.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isScanning) "Searching for consoles nearby..." else "No devices found yet.",
                                fontSize = 13.sp,
                                color = OnSurfaceVariantDark,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            items(scannedDevices) { (name, mac) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (selectedMac == mac) PrimaryBlueContainer else Color.Transparent)
                                        .clickable {
                                            viewModel.selectDevice(mac)
                                            showScanDialog = false
                                        }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = name,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = if (selectedMac == mac) OnPrimaryBlueContainer else OnBackgroundDark
                                        )
                                        Text(
                                            text = mac,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp,
                                            color = if (selectedMac == mac) OnPrimaryBlueContainer.copy(alpha = 0.7f) else OnSurfaceVariantDark
                                        )
                                    }
                                    if (selectedMac == mac) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Selected",
                                            tint = OnPrimaryBlueContainer,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (!isScanning) {
                            viewModel.startBluetoothScan()
                        }
                    }
                ) {
                    Text("Re-Scan", color = PrimaryBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showScanDialog = false }) {
                    Text("Close", color = OnSurfaceVariantDark)
                }
            }
        )
    }

    // Info Dialog
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Text(
                    text = "About Bluebomb",
                    fontWeight = FontWeight.Bold,
                    color = OnBackgroundDark
                )
            },
            text = {
                Text(
                    text = "Bluebomb is a Bluetooth exploit that targets a memory-corruption vulnerability in the Broadcom Bluetooth controller stack found in Nintendo Wii and Wii mini consoles.\n\nThis Android app serves as an interactive companion, configuration client, and step-by-step homebrew wizard. Because modern non-root mobile devices prevent raw Bluetooth frame creation, it provides testing simulations and exports fully functional terminal payloads you can run on any Linux device.",
                    fontSize = 14.sp,
                    color = OnSurfaceVariantDark,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Understand", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun FlasherTab(
    consoleType: String,
    region: String,
    exploitSpeed: String,
    isExploiting: Boolean,
    pulseStatus: Boolean,
    terminalLogs: List<String>,
    bluetoothStatus: String,
    selectedMac: String,
    onConsoleTypeChange: (String) -> Unit,
    onRegionChange: (String) -> Unit,
    onSpeedChange: (String) -> Unit,
    onTriggerExploit: () -> Unit,
    onOpenScan: () -> Unit,
    onClearLogs: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var regionExpanded by remember { mutableStateOf(false) }
    var speedExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Connection Status Card
        item {
            Card(
                onClick = onOpenScan,
                colors = CardDefaults.cardColors(
                    containerColor = PrimaryBlueContainer
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "BLUETOOTH STACK",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OnPrimaryBlueContainer.copy(alpha = 0.8f),
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Internal Controller Ready",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnPrimaryBlueContainer
                        )
                        Text(
                            text = "Target MAC: $selectedMac",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = OnPrimaryBlueContainer.copy(alpha = 0.7f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(PrimaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bluetooth,
                            contentDescription = "Bluetooth Status",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Configuration Panel Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = SurfaceVariantLight
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Target Console Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Target Console",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = OnSurfaceVariantDark
                        )
                        Row(
                            modifier = Modifier
                                .background(OutlineGrey, CircleShape)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Wii Mini", "Standard Wii").forEach { type ->
                                val selected = consoleType == type
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (selected) PrimaryBlue else Color.Transparent)
                                        .clickable { onConsoleTypeChange(type) }
                                        .padding(horizontal = 14.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = type,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (selected) Color.White else OnSurfaceVariantDark
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = OutlineGrey.copy(alpha = 0.5f))

                    // Region Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Region",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = OnSurfaceVariantDark
                        )
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { regionExpanded = true }
                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = region,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                                Icon(
                                    imageVector = Icons.Filled.ExpandMore,
                                    contentDescription = "Expand Region",
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = regionExpanded,
                                onDismissRequest = { regionExpanded = false },
                                modifier = Modifier.background(SurfaceLight)
                            ) {
                                listOf("USA / NTSC-U", "PAL / EUR", "JPN / NTSC-J", "KOR").forEach { r ->
                                    DropdownMenuItem(
                                        text = { Text(r, color = OnBackgroundDark) },
                                        onClick = {
                                            onRegionChange(r)
                                            regionExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = OutlineGrey.copy(alpha = 0.5f))

                    // Speed Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Exploit Speed",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = OnSurfaceVariantDark
                        )
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { speedExpanded = true }
                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = exploitSpeed,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                                Icon(
                                    imageVector = Icons.Filled.ExpandMore,
                                    contentDescription = "Expand Speed",
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = speedExpanded,
                                onDismissRequest = { speedExpanded = false },
                                modifier = Modifier.background(SurfaceLight)
                            ) {
                                listOf("Standard (1x)", "Turbo (2x)", "Debug (0.5x)").forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text(s, color = OnBackgroundDark) },
                                        onClick = {
                                            onSpeedChange(s)
                                            speedExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Terminal Output Card (Weighted size)
        item {
            val listState = rememberLazyListState()
            LaunchedEffect(terminalLogs.size) {
                if (terminalLogs.isNotEmpty()) {
                    listState.animateScrollToItem(terminalLogs.size - 1)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(TerminalBg)
                    .border(width = 1.dp, color = OnSurfaceVariantDark, shape = RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "OUTPUT TERMINAL",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreyText,
                        letterSpacing = 1.2.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = onClearLogs,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteSweep,
                                contentDescription = "Clear logs",
                                tint = GreyText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        // Pulsing status dot
                        if (isExploiting) {
                            val infiniteTransition = rememberInfiniteTransition()
                            val alpha by infiniteTransition.animateFloat(
                                initialValue = 0.3f,
                                targetValue = 1.0f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(600, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(PulseRed.copy(alpha = alpha))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (pulseStatus) Color(0xFF81C784) else PulseRed)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(terminalLogs) { log ->
                        val logColor = when {
                            log.startsWith("[SUCCESS]") -> Color(0xFFD1E4FF)
                            log.startsWith(">> SUCCESS") -> Color(0xFFD1E4FF)
                            log.startsWith(">") -> Color(0xFFBBDEFB)
                            log.contains("Waiting") -> YellowAccent
                            else -> TerminalText
                        }

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = log,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = logColor,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Action Button
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onTriggerExploit,
                    enabled = !isExploiting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        disabledContainerColor = PrimaryBlue.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (isExploiting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "EXPLOITING...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.RocketLaunch,
                            contentDescription = "Send Exploit",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SEND EXPLOIT",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
                Text(
                    text = "Ensure your Wii Mini is powered on and the SYNC button is pressed repeatedly before starting.",
                    fontSize = 10.sp,
                    color = OnSurfaceVariantDark,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
fun HistoryTab(
    historyList: List<ExploitHistory>,
    onDelete: (Int) -> Unit,
    onClearAll: () -> Unit
) {
    var selectedHistoryLog by remember { mutableStateOf<ExploitHistory?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Exploit Runs",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = OnBackgroundDark
            )
            if (historyList.isNotEmpty()) {
                TextButton(
                    onClick = onClearAll,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Clear All", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear All", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.HistoryToggleOff,
                        contentDescription = "No history",
                        tint = OnSurfaceVariantDark.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No recorded runs yet.",
                        fontSize = 14.sp,
                        color = OnSurfaceVariantDark
                    )
                    Text(
                        text = "Launch an exploit simulation from the Flasher tab to see it logged here.",
                        fontSize = 12.sp,
                        color = OnSurfaceVariantDark.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(historyList) { history ->
                    val dateFormatted = SimpleDateFormat("MMM dd, yyyy - HH:mm:ss", Locale.getDefault()).format(Date(history.timestamp))
                    Card(
                        onClick = { selectedHistoryLog = history },
                        colors = CardDefaults.cardColors(containerColor = SurfaceVariantLight),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = history.consoleType,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = OnBackgroundDark
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(PrimaryBlueContainer)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = history.status,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 9.sp,
                                            color = OnPrimaryBlueContainer
                                        )
                                    }
                                }
                                Text(
                                    text = "Region: ${history.region}",
                                    fontSize = 12.sp,
                                    color = OnSurfaceVariantDark
                                )
                                Text(
                                    text = dateFormatted,
                                    fontSize = 11.sp,
                                    color = OnSurfaceVariantDark.copy(alpha = 0.7f)
                                )
                            }
                            IconButton(onClick = { onDelete(history.id) }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete run",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialog for viewing past log terminal
    if (selectedHistoryLog != null) {
        AlertDialog(
            onDismissRequest = { selectedHistoryLog = null },
            title = {
                Text(
                    text = "Terminal Output Log",
                    fontWeight = FontWeight.Bold,
                    color = OnBackgroundDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Console: ${selectedHistoryLog?.consoleType} (${selectedHistoryLog?.region})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurfaceVariantDark
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(TerminalBg)
                            .padding(12.dp)
                    ) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            val lines = selectedHistoryLog?.logs?.split("\n") ?: emptyList()
                            items(lines) { line ->
                                val logColor = when {
                                    line.startsWith("[SUCCESS]") -> Color(0xFFD1E4FF)
                                    line.startsWith(">> SUCCESS") -> Color(0xFFD1E4FF)
                                    line.startsWith(">") -> Color(0xFFBBDEFB)
                                    else -> TerminalText
                                }
                                Text(
                                    text = line,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = logColor,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedHistoryLog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun GuideTab(consoleType: String, region: String, onPrepareUsb: (android.net.Uri) -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    // Generate Linux command line matching selections
    val consoleParam = if (consoleType == "Wii Mini") "wii-mini" else "wii"
    val regionParam = when (region) {
        "USA / NTSC-U" -> "usa"
        "PAL / EUR" -> "pal"
        "JPN / NTSC-J" -> "jpn"
        else -> "kor"
    }
    val generatedCommand = "sudo ./bluebomb-helper.sh -c $consoleParam -r $regionParam"

    val usbPrepareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { onPrepareUsb(it) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Bluebomb Installation Guide",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = OnBackgroundDark
            )
            Text(
                text = "Follow these precise, step-by-step instructions to prepare your files and execute the actual hack on your console.",
                fontSize = 13.sp,
                color = OnSurfaceVariantDark,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Section 1: Requirements
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceVariantLight),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.ListAlt, contentDescription = "Hardware", tint = PrimaryBlue)
                        Text(text = "1. Requirements", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnBackgroundDark)
                    }
                    Text(
                        text = "• A USB Flash Drive formatted to FAT32.\n" +
                               "• A USB OTG adapter/cable (Required for Wii Mini, as its internal USB slot is tucked under or you may need to plug it into the back slot).\n" +
                               "• The HackMii Installer package (extract the 'boot.elf' file).\n" +
                               "• A computer running Linux (Ubuntu, Mint, Debian, or Raspberry Pi) to launch the payload over native Bluetooth.",
                        fontSize = 13.sp,
                        color = OnSurfaceVariantDark,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { usbPrepareLauncher.launch(null) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Usb,
                            contentDescription = "Prepare USB",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Prepare USB Drive",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Section 2: Preparation
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceVariantLight),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.SdCard, contentDescription = "SD Card Prep", tint = PrimaryBlue)
                        Text(text = "2. USB Preparation", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnBackgroundDark)
                    }
                    Text(
                        text = "1. Plug your USB Flash Drive into your computer.\n" +
                               "2. Ensure it is formatted to FAT32.\n" +
                               "3. From the HackMii Installer you downloaded, place the 'boot.elf' file in the root of your USB drive.\n" +
                               "5. Safely eject the USB drive and plug it into the USB port of your Wii/Wii Mini.",
                        fontSize = 13.sp,
                        color = OnSurfaceVariantDark,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Section 3: Execution Script generator (Highly Operational Utility!)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PrimaryBlueContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Terminal, contentDescription = "Terminal commands", tint = OnPrimaryBlueContainer)
                        Text(
                            text = "3. Native Execution Terminal Command",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = OnPrimaryBlueContainer
                        )
                    }
                    Text(
                        text = "Copy this Linux terminal bash execution command to easily trigger Bluebomb natively on your computer, automatically customized based on your Flasher settings:",
                        fontSize = 12.sp,
                        color = OnPrimaryBlueContainer.copy(alpha = 0.8f),
                        lineHeight = 16.sp
                    )

                    // Box showing terminal command
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TerminalBg)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = generatedCommand,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = TerminalText,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(generatedCommand))
                                    Toast.makeText(context, "Command copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ContentCopy,
                                    contentDescription = "Copy command",
                                    tint = PrimaryBlueContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "Note: Ensure your PC Bluetooth receiver is fully enabled before running.",
                        fontSize = 11.sp,
                        color = OnPrimaryBlueContainer.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Section 4: Execution Sequence
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceVariantLight),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Tv, contentDescription = "Console Action", tint = PrimaryBlue)
                        Text(text = "4. Console Sequence", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnBackgroundDark)
                    }
                    Text(
                        text = "1. Turn off your Wii/Wii Mini completely (Unplug power for 5 seconds, then plug back in).\n" +
                               "2. Run the generated script command on your Linux console.\n" +
                               "3. Power ON the console.\n" +
                               "4. DO NOT connect any controllers. Immediately start pressing the red SYNC button on your console repeatedly (located on the front for Wii Mini, or under SD flap for standard Wii).\n" +
                               "5. Keep pressing SYNC until your terminal detects the connection and triggers the buffer overflow dump.\n" +
                               "6. After a few seconds, the HackMii Homebrew Installer will load on your TV screen. Connect a Wii Remote to continue installation!",
                        fontSize = 13.sp,
                        color = OnSurfaceVariantDark,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
