package com.example.nearfind

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.nearfind.service.BluetoothScanService
import com.example.nearfind.ui.navigation.NavGraph
import com.example.nearfind.ui.theme.NearFindTheme
import com.example.nearfind.util.PermissionManager

class MainActivity : ComponentActivity() {

    // Obtenemos la instancia de UserManager a través del AppContainer
    private val userManager by lazy {
        (application as NearFindApplication).appContainer.userManager
    }

    // Variable para rastrear si los permisos han sido concedidos
    private var permissionsGranted by mutableStateOf(false)

    // Variable para rastrear si el Bluetooth está habilitado
    private var isBluetoothEnabled by mutableStateOf(false)

    // Obtener el adaptador Bluetooth
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val requiredPermissions = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.FOREGROUND_SERVICE
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            add(Manifest.permission.BLUETOOTH)
            add(Manifest.permission.BLUETOOTH_ADMIN)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            add(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
        }
    }.toTypedArray()

    // Registro para solicitar permisos
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsGranted = permissions.values.all { it }
        if (permissionsGranted) {
            Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show()
            checkBluetoothState()
        } else {
            Toast.makeText(this, "Algunos permisos no fueron concedidos", Toast.LENGTH_LONG).show()
        }
    }

    // Registro para solicitar la activación del Bluetooth
    private val requestBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (bluetoothAdapter?.isEnabled == true) {
            isBluetoothEnabled = true
            Toast.makeText(this, "Bluetooth activado", Toast.LENGTH_SHORT).show()
        } else {
            isBluetoothEnabled = false
            Toast.makeText(this, "El Bluetooth es necesario para usar la aplicación", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Solicitar permisos al iniciar la actividad
        requestPermissionLauncher.launch(requiredPermissions)

        setContent {
            NearFindTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Solicitar permisos si aún no se han concedido
                    LaunchedEffect(Unit) {
                        if (!permissionsGranted) {
                            requestPermissionLauncher.launch(requiredPermissions)
                        } else {
                            checkBluetoothState()
                        }
                    }

                    NavGraph(
                        navController = navController,
                        permissionManager = PermissionManager(this, requiredPermissions),
                        startScanService = {
                            if (permissionsGranted && isBluetoothEnabled) {
                                startBluetoothScanService()
                            } else if (!permissionsGranted) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Se requieren permisos para escanear dispositivos",
                                    Toast.LENGTH_LONG
                                ).show()
                                requestPermissionLauncher.launch(requiredPermissions)
                            } else if (!isBluetoothEnabled) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Es necesario activar el Bluetooth",
                                    Toast.LENGTH_LONG
                                ).show()
                                requestBluetoothEnable()
                            }
                        },
                        isBluetoothEnabled = isBluetoothEnabled,
                        isUserRegistered = userManager.isUserRegistered(),
                        startDestination = if (userManager.isUserRegistered()) "home" else "onboarding",
                        requestBluetoothEnable = { requestBluetoothEnable() }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Verificar el estado del Bluetooth cada vez que la actividad vuelve a primer plano
        checkBluetoothState()
    }

    // Verificar el estado del Bluetooth
    private fun checkBluetoothState() {
        isBluetoothEnabled = bluetoothAdapter?.isEnabled == true
        if (!isBluetoothEnabled) {
            requestBluetoothEnable()
        }
    }

    // Solicitar al usuario que active el Bluetooth
    private fun requestBluetoothEnable() {
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter!!.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                requestBluetoothLauncher.launch(enableBtIntent)
            } else {
                isBluetoothEnabled = true
            }
        } else {
            // El dispositivo no soporta Bluetooth
            Toast.makeText(
                this,
                "Este dispositivo no soporta Bluetooth",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun startBluetoothScanService() {
        // Solo iniciar el servicio si los permisos han sido concedidos y el Bluetooth está habilitado
        if (permissionsGranted && isBluetoothEnabled) {
            val serviceIntent = Intent(this, BluetoothScanService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } else if (!permissionsGranted) {
            Toast.makeText(
                this,
                "No se pueden escanear dispositivos sin los permisos necesarios",
                Toast.LENGTH_LONG
            ).show()
            requestPermissionLauncher.launch(requiredPermissions)
        } else if (!isBluetoothEnabled) {
            Toast.makeText(
                this,
                "Es necesario activar el Bluetooth",
                Toast.LENGTH_LONG
            ).show()
            requestBluetoothEnable()
        }
    }
}