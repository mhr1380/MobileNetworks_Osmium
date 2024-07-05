package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.CellInfoLte
import android.telephony.TelephonyManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(viewModel, Modifier.padding(innerPadding))
                }
            }
        }

        // Request necessary permissions
        requestPermissionsIfNecessary()
    }

    private fun requestPermissionsIfNecessary() {
        val permissions = mutableListOf<String>()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_PHONE_STATE)
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), REQUEST_PERMISSION_CODE)
        } else {
            // Permissions are already granted
            viewModel.fetchCellInfo(this)
            getDeviceCurrentLocation()
        }
    }

    private fun getDeviceCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        lifecycleScope.launch {
            while (true) {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener {
                        Log.d("mhr", "lat: ${it.latitude} long: ${it.longitude}")
                    }.addOnFailureListener {
                        Log.d("mhr", "error: ${it.message}")
                    }
                delay(3000)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted, start the tasks
                viewModel.fetchCellInfo(this)
                getDeviceCurrentLocation()
            } else {
                Log.d("mhr", "Permissions not granted by the user.")
            }
        }
    }

    companion object {
        const val REQUEST_PERMISSION_CODE = 100
    }
}

data class CellInfoData(
    val ci: Int,
    val tac: Int,
    val mcc: String?,
    val mnc: String?
)

class MainViewModel : ViewModel() {
    private val _cellInfoList = mutableStateListOf<CellInfoData>()
    val cellInfoList: List<CellInfoData> get() = _cellInfoList

    fun addCellInfo(cellInfo: CellInfoData) {
        _cellInfoList.clear() // Clear previous data to show only the latest
        _cellInfoList.add(cellInfo)
    }

    fun fetchCellInfo(context: Context) {
        viewModelScope.launch {
            while (true) {
                val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager? ?: return@launch
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    &&
                    ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    &&
                    ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_PHONE_STATE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val cellInfoList = telephonyManager.allCellInfo
                    for (cellInfo in cellInfoList) {
                        if (cellInfo is CellInfoLte) {
                            val lteCellInfo = cellInfo as CellInfoLte
                            val cellIdentity = lteCellInfo.cellIdentity

                            val ci = cellIdentity.ci
                            val tac = cellIdentity.tac
                            val mcc = cellIdentity.mccString
                            val mnc = cellIdentity.mncString
                            Log.d("mhr", "${cellIdentity.mccString}")
                            if(cellIdentity.mccString!==null){
                                addCellInfo(CellInfoData(ci, tac, mcc, mnc))
                            }
                        }
                    }
                }
                delay(3000)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val cellInfoList by remember { mutableStateOf(viewModel.cellInfoList) }

    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "Cell Info")
        Spacer(modifier = Modifier.height(8.dp))
        for (cellInfo in cellInfoList) {
            CellInfoCard(cellInfo)
        }
    }
}

@Composable
fun CellInfoCard(cellInfo: CellInfoData) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Cell ID: ${cellInfo.ci}")
            Text(text = "TAC: ${cellInfo.tac}")
            Text(text = "MCC: ${cellInfo.mcc ?: "Unknown"}")
            Text(text = "MNC: ${cellInfo.mnc ?: "Unknown"}")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MyApplicationTheme {
        MainScreen(MainViewModel())
    }
}
