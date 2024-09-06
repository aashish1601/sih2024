package com.example.sih

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WifiPage(navController: NavController) {
    val context = LocalContext.current
    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    var isWifiEnabled by remember { mutableStateOf(wifiManager.isWifiEnabled) }
    var availableNetworks by remember { mutableStateOf(emptyList<ScanResult>()) }
    var lastScanTime by remember { mutableStateOf("") }

    // Function to update WiFi scan results
    fun updateWifiScan() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            wifiManager.startScan()
            availableNetworks = wifiManager.scanResults
            lastScanTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        }
    }

    // Refresh data every 10 seconds
    LaunchedEffect(isWifiEnabled) {
        while (isWifiEnabled) {
            updateWifiScan()
            delay(10000) // 10 seconds delay
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "WiFi Networks",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = if (isWifiEnabled) "WiFi is ON" else "WiFi is OFF",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = isWifiEnabled,
                onCheckedChange = { enabled ->
                    wifiManager.isWifiEnabled = enabled
                    isWifiEnabled = enabled
                    if (enabled) {
                        updateWifiScan()
                    } else {
                        availableNetworks = emptyList()
                    }
                }
            )
        }

        if (isWifiEnabled) {
            Text(
                text = "Last scan: $lastScanTime",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (availableNetworks.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(availableNetworks) { network ->
                        NetworkCard(network)
                    }
                }
            } else {
                Text(
                    text = "No networks found.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(top = 16.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text("Back")
        }
    }
}

@Composable
fun NetworkCard(network: ScanResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = network.SSID,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("BSSID: ${network.BSSID}")
            Text("Frequency: ${network.frequency} MHz")
            Text("Channel: ${getChannelForFrequency(network.frequency)}")
            Text("Band: ${if (network.frequency > 5000) "5 GHz" else "2.4 GHz"}")
            Text("Signal Strength: ${network.level} dBm")
            Text("Capabilities: ${network.capabilities}")


            SignalStrengthBar(signalStrength = network.level)
        }
    }
}

@Composable
fun SignalStrengthBar(signalStrength: Int) {
    val normalizedStrength = normalizeSignalStrength(signalStrength)
    val barColor = when {
        normalizedStrength > 0.7 -> Color.Green
        normalizedStrength > 0.4 -> Color.Yellow
        else -> Color.Red
    }

    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text("Signal Strength Bar:")
        Box(
            modifier = Modifier
                .height(20.dp)
                .fillMaxWidth()
                .background(Color.LightGray)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(normalizedStrength)
                    .background(barColor)
            )
        }
    }
}

fun normalizeSignalStrength(signalStrength: Int): Float {
    // Normalize signal strength from dBm to a value between 0 and 1
    // Typical values: -100 dBm (very weak) to -50 dBm (very strong)
    return (signalStrength + 100) / 50f.coerceIn(0f, 1f)
}

fun getChannelForFrequency(frequency: Int): Int {
    return when {
        frequency >= 2412 && frequency <= 2484 -> (frequency - 2412) / 5 + 1
        frequency >= 5170 && frequency <= 5825 -> (frequency - 5170) / 5 + 34
        else -> -1
    }
}
