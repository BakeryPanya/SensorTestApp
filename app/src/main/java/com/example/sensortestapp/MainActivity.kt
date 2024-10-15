package com.example.sensortestapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat.getSystemService
import com.example.sensortestapp.ui.theme.SensorTestAppTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SensorTestAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AccelerationSensor(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AccelerationSensor(modifier: Modifier = Modifier){
    val context = LocalContext.current
    var axisx by remember { mutableStateOf(0f) }
    var axisy by remember { mutableStateOf(0f) }
    var axisz by remember { mutableStateOf(0f) }
    var previousValueX by remember { mutableStateOf(0f) }
    var previousValueY by  remember { mutableStateOf(0f) }
    var previousValueZ by remember { mutableStateOf(0f) }

    var Printaxisx by remember { mutableStateOf(0f) }
    var Printaxisy by remember { mutableStateOf(0f) }
    var Printaxisz by remember { mutableStateOf(0f) }

    LaunchedEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val listener = object : SensorEventListener {
            var filteredValueX = 0f
            var filteredValueY = 0f
            var filteredValueZ = 0f
            val alpha = 0.8f // ハイパスフィルターの係数

            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                    // ハイパスフィルターを適用
                    //これであってるかは吟味が要ります
                    filteredValueX = alpha * (filteredValueX + event.values[0] - previousValueX)
                    filteredValueY = alpha * (filteredValueY + event.values[1] - previousValueY)
                    filteredValueZ = alpha * (filteredValueZ + event.values[2] - previousValueZ)

                    // 前回のセンサー値を更新
                    previousValueX = event.values[0]
                    previousValueY = event.values[1]
                    previousValueZ = event.values[2]

                    // フィルター処理された値を状態変数に代入
                    axisx = filteredValueX
                    axisy = filteredValueY
                    axisz = filteredValueZ
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

            }

        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)

        while(true){
            delay(1000)
            Printaxisx = axisx
            Printaxisy = axisy
            Printaxisz = axisz
        }

    }

    Column(modifier = modifier) {
        Text(text = "X: $Printaxisx, Y: $Printaxisy, Z: $Printaxisz")

        if (abs(axisx) > 3 || abs(axisy) > 3 || abs(axisz) > 3) {
            Text(text = "衝撃を検知しました")
        }else if (abs(axisx) > 1 || abs(axisy) > 1 || abs(axisz) > 1) {
            Text(text = "なでなでされました。")
        }
    }

}
