package com.example.sih

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.sih.ui.theme.SihTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE
    )

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.entries.all { it.value }
            if (allGranted) {
                Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Some permissions were denied", Toast.LENGTH_SHORT).show()
            }
        }

        enableEdgeToEdge()
        setContent {
            SihTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    SetBarColor(color = MaterialTheme.colorScheme.background)
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        NavHost(navController = navController, startDestination = "home") {
                            composable("home") { HomeScreen(navController) }
                            composable("help") { HelpScreen(navController) }
                            composable("wifi_page") { WifiPage(navController) }
                            composable("nav_page") { NavPage(navController) }
                        }
                        MainScreen(permissionLauncher, requiredPermissions)
                    }
                }
            }
        }
    }

    @Composable
    private fun SetBarColor(color: Color) {
        val systemUiController = rememberSystemUiController()
        SideEffect {
            systemUiController.setSystemBarsColor(
                color = color
            )
        }
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            HeaderSection()
            CardsSection()
            Spacer(modifier = Modifier.height(1.dp))
            ScrollableTab()

            Button(
                onClick = { navController.navigate("help") },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Help & Navigation")
            }
        }
    }
}

@Composable
fun MainScreen(
    permissionLauncher: ActivityResultLauncher<Array<String>>,
    requiredPermissions: Array<String>
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        checkAndRequestPermissions(context, permissionLauncher, requiredPermissions)
    }
}

private fun checkAndRequestPermissions(
    context: Context,
    permissionLauncher: ActivityResultLauncher<Array<String>>,
    requiredPermissions: Array<String>
) {
    val permissionsToRequest = requiredPermissions.filter {
        ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
    }.toTypedArray()

    if (permissionsToRequest.isNotEmpty()) {
        permissionLauncher.launch(permissionsToRequest)
    } else {
        Toast.makeText(context, "All permissions already granted", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun NavPage(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "30 m ahead",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 32.sp,
                    color = Color.Green
                ),
                modifier = Modifier.padding(end = 8.dp)
            )
            Icon(
                painter = painterResource(id = R.drawable.baseline_arrow_upward_24),
                contentDescription = "Arrow Icon",
                tint = Color.Green,
                modifier = Modifier.size(40.dp)
            )
        }

        Box(
            modifier = Modifier
                .width(350.dp)
                .height(700.dp)
                .background(Color.LightGray, shape = RoundedCornerShape(60.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 16.dp)
                ) {
                    Text(
                        text = "Platform 1",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                        color = Color.Black,
                        modifier = Modifier.rotate(270f)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_wifi_24),
                        contentDescription = "WiFi Icon 1",
                        modifier = Modifier.size(32.dp),
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_wifi_24),
                        contentDescription = "WiFi Icon 2",
                        modifier = Modifier.size(32.dp),
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_wifi_24),
                        contentDescription = "WiFi Icon 3",
                        modifier = Modifier.size(32.dp),
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    Text(
                        text = "Bandra Station",
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp),
                        color = Color.Black
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(end = 16.dp)
                ) {
                    Text(
                        text = "Platform 3",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                        color = Color.Black,
                        modifier = Modifier.rotate(90f)
                    )
                }
            }
        }
    }
}