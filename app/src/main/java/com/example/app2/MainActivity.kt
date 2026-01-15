package com.example.app2

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.app2.ui.theme.APP2Theme
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private var accelMsg by mutableStateOf("Sin datos")

    private var captureJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        setContent {
            APP2Theme {
                AppUI(
                    accelMsg = accelMsg,
                    onStart = { startCapture() },
                    onStop = { stopCapture() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val x = event.values[0]
            accelMsg = when {
                x > 2 -> "Pantalla apuntando a la izquierda"
                x < -2 -> "Pantalla apuntando a la derecha"
                else -> "Pantalla al centro"
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // -----------------------
    // CAPTURA AUTOMÁTICA
    // -----------------------
    private val registros = mutableStateListOf<String>()
    private var userName: String = ""

    private fun startCapture() {
        if (captureJob != null) return

        captureJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                val hora = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                val linea = "$hora | $userName | $accelMsg"
                registros.add(linea)
                delay(5000)
            }
        }
    }

    private fun stopCapture() {
        captureJob?.cancel()
        captureJob = null

        // Aquí guardarías en SQLite
        // ejemplo: db.insertAll(registros)
    }

    // -----------------------
    // INTERFAZ COMPOSE
    // -----------------------
    @Composable
    fun AppUI(
        accelMsg: String,
        onStart: () -> Unit,
        onStop: () -> Unit
    ) {
        var name by remember { mutableStateOf("") }
        userName = name

        Column(modifier = Modifier.padding(16.dp)) {

            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del usuario") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Text("Dirección: $accelMsg")

            Spacer(Modifier.height(16.dp))

            Row {
                Button(onClick = onStart, modifier = Modifier.weight(1f)) {
                    Text("Iniciar captura")
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onStop, modifier = Modifier.weight(1f)) {
                    Text("Detener captura")
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Registros:")

            LazyColumn {
                items(registros) { item ->
                    Text(item)
                }
            }
        }
    }
}
