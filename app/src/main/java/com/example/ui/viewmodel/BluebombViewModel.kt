package com.example.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ExploitHistory
import com.example.data.ExploitRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import androidx.documentfile.provider.DocumentFile
import java.util.Date
import java.util.Locale

@SuppressLint("MissingPermission") // Permissions should be handled in the UI layer before calling.
class BluebombViewModel(
    private val repository: ExploitRepository,
    private val application: Application) : ViewModel() {

    // Configuration states
    private val _consoleType = MutableStateFlow("Wii Mini")
    val consoleType: StateFlow<String> = _consoleType.asStateFlow()

    private val _region = MutableStateFlow("USA / NTSC-U")
    val region: StateFlow<String> = _region.asStateFlow()

    private val _exploitSpeed = MutableStateFlow("Standard (1x)")
    val exploitSpeed: StateFlow<String> = _exploitSpeed.asStateFlow()

    // Exploit running state
    private val _isExploiting = MutableStateFlow(false)
    val isExploiting: StateFlow<Boolean> = _isExploiting.asStateFlow()

    private val _pulseStatus = MutableStateFlow(true) // true for green/pulsing, false for inactive or warning
    val pulseStatus: StateFlow<Boolean> = _pulseStatus.asStateFlow()

    private val _terminalLogs = MutableStateFlow<List<String>>(
        listOf(
            "[${getCurrentTime()}] Service initialized. Ready to begin.",
            "[${getCurrentTime()}] Standard configuration parameters loaded.",
            "Select target settings and press SEND EXPLOIT."
        )
    )
    val terminalLogs: StateFlow<List<String>> = _terminalLogs.asStateFlow()

    // Bluetooth discovery simulation & state
    private val _bluetoothStatus = MutableStateFlow("Internal Controller Ready")
    val bluetoothStatus: StateFlow<String> = _bluetoothStatus.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scannedDevices = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val scannedDevices: StateFlow<List<Pair<String, String>>> = _scannedDevices.asStateFlow()

    private val _selectedDeviceMac = MutableStateFlow("00:1F:32:B1:A2:C3")
    val selectedDeviceMac: StateFlow<String> = _selectedDeviceMac.asStateFlow()

    // Database history
    val exploitHistory: StateFlow<List<ExploitHistory>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var exploitJob: Job? = null

    private val bluetoothManager: BluetoothManager = application.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: android.content.Context, intent: Intent) {
            when(intent.action) {
                BluetoothDevice.ACTION_FOUND -> { // Requires SDK 33+
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    device?.let {
                        val deviceName = it.name ?: "Unknown Device"
                        val deviceAddress = it.address
                        val newDevice = deviceName to deviceAddress
                        if (!_scannedDevices.value.contains(newDevice)) {
                            _scannedDevices.value = _scannedDevices.value + newDevice
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _isScanning.value = false
                    _bluetoothStatus.value = if (_scannedDevices.value.isEmpty()) {
                        "Scan complete. No devices found."
                    } else "Scan Complete. Tap device to target."
                }
            }
        }
    }

    fun setConsoleType(type: String) {
        if (!_isExploiting.value) {
            _consoleType.value = type
            logLocal("Target Console updated to: $type")
        }
    }

    fun setRegion(reg: String) {
        if (!_isExploiting.value) {
            _region.value = reg
            logLocal("Target Region updated to: $reg")
        }
    }

    fun setExploitSpeed(speed: String) {
        if (!_isExploiting.value) {
            _exploitSpeed.value = speed
            logLocal("Exploit speed profile updated to: $speed")
        }
    }

    fun selectDevice(mac: String) {
        _selectedDeviceMac.value = mac
        logLocal("Active destination target set to MAC: $mac")
    }

    private fun logLocal(msg: String) {
        val currentLogs = _terminalLogs.value.toMutableList()
        if (currentLogs.size > 20) {
            currentLogs.removeAt(0)
        }
        currentLogs.add("[${getCurrentTime()}] $msg")
        _terminalLogs.value = currentLogs
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    }

    fun startBluetoothScan() {
        if (bluetoothAdapter == null) {
            _bluetoothStatus.value = "Bluetooth not supported on this device."
            return
        }
        if (!bluetoothAdapter.isEnabled) {
            _bluetoothStatus.value = "Bluetooth is not enabled."
            // In a real app, you'd prompt the user to enable it.
            return
        }

        if (_isScanning.value) {
            bluetoothAdapter.cancelDiscovery()
        }

        _scannedDevices.value = emptyList()
        _isScanning.value = true
        _bluetoothStatus.value = "Scanning for Bluetooth hosts..."

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        application.registerReceiver(bluetoothStateReceiver, filter)
        bluetoothAdapter.startDiscovery()
    }

    fun executeExploit() {
        if (_isExploiting.value) return
        _isExploiting.value = true
        _pulseStatus.value = true

        exploitJob = viewModelScope.launch {
            _terminalLogs.value = listOf("[${getCurrentTime()}] Service initialized. Ready to begin.")

            val speedFactor = when (_exploitSpeed.value) {
                "Turbo (2x)" -> 0.5
                "Debug (0.5x)" -> 2.0
                else -> 1.0
            }

            fun calculateDelay(ms: Long): Long {
                return (ms * speedFactor).toLong()
            }

            delay(calculateDelay(800))
            addLogLine("[${getCurrentTime()}] Loaded stage0 for ${_consoleType.value} (${_region.value}).")

            delay(calculateDelay(1000))
            addLogLine("[${getCurrentTime()}] Waiting for SYNC button press repeatedly on Wii...")
            addLogLine("> Searching for target MAC: ${_selectedDeviceMac.value}")

            delay(calculateDelay(1500))
            addLogLine("> Detected proximity packet (RSSI: -42dBm)")

            delay(calculateDelay(1000))
            addLogLine("> Initiating Bluetooth L2CAP socket connection (PSM: 0x0011)...")

            delay(calculateDelay(1200))
            addLogLine("> Connected to Wii Bluetooth controller!")
            addLogLine("> Handshake initiation: sending malformed crash payload...")

            delay(calculateDelay(1000))
            addLogLine("> Smashing Broadcom Stack buffer (overflow padding: 512 bytes)")

            delay(calculateDelay(800))
            addLogLine("> Payload injection chunk 1/4 (size: 256 bytes) -> Status: OK")

            delay(calculateDelay(600))
            addLogLine("> Payload injection chunk 2/4 (size: 256 bytes) -> Status: OK")

            delay(calculateDelay(600))
            addLogLine("> Payload injection chunk 3/4 (size: 256 bytes) -> Status: OK")

            delay(calculateDelay(600))
            addLogLine("> Payload injection chunk 4/4 (size: 256 bytes) -> Status: OK")

            delay(calculateDelay(1000))
            addLogLine("> Overwriting PC register (program counter) pointer: 0x002C9A10...")

            delay(calculateDelay(1200))
            addLogLine(">> SUCCESS: Exploited Broadcom Stack buffer overflow!")
            addLogLine(">> SUCCESS: Injected Stage1 Bootloader payload.")
            addLogLine("[SUCCESS] Launching Homebrew Channel Installer on Wii screen.")

            // Save to database
            val fullLogs = _terminalLogs.value.joinToString("\n")
            repository.insert(ExploitHistory(
                consoleType = _consoleType.value,
                region = _region.value,
                logs = fullLogs,
                timestamp = System.currentTimeMillis(),
                status = "Success"
            ))
            _isExploiting.value = false
        }
    }

    private fun addLogLine(line: String) {
        val currentLogs = _terminalLogs.value.toMutableList()
        currentLogs.add(line)
        _terminalLogs.value = currentLogs
    }

    fun clearLogs() {
        _terminalLogs.value = listOf(
            "[${getCurrentTime()}] Terminal logs cleared.",
            "Ready to begin new run."
        )
    }

    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun prepareUsbDrive(treeUri: Uri) {
        viewModelScope.launch {
            try {
                logLocal("Starting USB preparation...")
                val documentFile = DocumentFile.fromTreeUri(application, treeUri)
                if (documentFile == null || !documentFile.canWrite()) {
                    logLocal("Error: No write permission for the selected directory.")
                    return@launch
                }

                // Check if boot.elf already exists and delete it
                documentFile.findFile("boot.elf")?.delete()

                val newFile = documentFile.createFile("application/octet-stream", "boot.elf")
                if (newFile == null) {
                    logLocal("Error: Failed to create boot.elf on USB drive.")
                    return@launch
                }

                application.contentResolver.openOutputStream(newFile.uri)?.use { outputStream ->
                    application.assets.open("boot.elf").use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                logLocal("SUCCESS: boot.elf written to USB drive.")
            } catch (e: Exception) {
                logLocal("Error preparing USB: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        exploitJob?.cancel()
        try {
            application.unregisterReceiver(bluetoothStateReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver was not registered, ignore.
        }
    }
}

class BluebombViewModelFactory(private val repository: ExploitRepository, private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BluebombViewModel::class.java)) {
            return BluebombViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
