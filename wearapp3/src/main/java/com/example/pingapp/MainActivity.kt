/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.pingapp

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.pingapp.presentation.MainApp
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.unipi.sleepmonitoring_masss_library.TimeSeries
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.time.Instant

@SuppressLint("RestrictedApi")
class MainActivity : ComponentActivity(), SensorEventListener {
    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val capabilityClient by lazy { Wearable.getCapabilityClient(this) }

    private val clientDataViewModel by viewModels<ClientDataViewModel>()

    private val sensorManager: SensorManager by lazy {getSystemService(SENSOR_SERVICE) as SensorManager}

    private var started: Boolean = false
    private var session_start_time = 0L

    private var lastHeart = 0.0f
    private var lastAccel: FloatArray = floatArrayOf(0.0f, 0.0f, 0.0f)

    private var heartTimeSeries = TimeSeries()
    private var accelTimeSeries = TimeSeries()

    override fun onResume() {
        super.onResume()
        dataClient.addListener(clientDataViewModel)
        messageClient.addListener(clientDataViewModel)
        capabilityClient.addListener(
            clientDataViewModel,
            Uri.parse("wear://"),
            CapabilityClient.FILTER_ALL
        )
    }

    override fun onPause() {
        super.onPause()
        dataClient.removeListener(clientDataViewModel)
        messageClient.removeListener(clientDataViewModel)
        capabilityClient.removeListener(clientDataViewModel)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            MainApp(
                onPingClicked = ::onPingClicked,
                onSendClicked = ::onSendClicked,
                onStartStopClicked = ::onStartStopClicked,
                started = started
            )
        }
    }

    private fun onStartStopClicked() {
        started = !started
        if(started)
        {
            val sensorHeartRate = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
            val sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            val successHeart = sensorManager.registerListener(this, sensorHeartRate, SensorManager.SENSOR_DELAY_NORMAL)
            Log.i(TAG, "successHeart=$successHeart")

            val successAcc = sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
            Log.i(TAG, "successAccel=$successAcc")

            Log.i(TAG, "starting session....")
            session_start_time = System.currentTimeMillis()

            // Clearing old session
            heartTimeSeries = TimeSeries()
            accelTimeSeries = TimeSeries()

        } else {
            Log.i(TAG, "stopping session....")
            sensorManager.unregisterListener(this)
        }

        setContent {
            MainApp(
                onPingClicked = ::onPingClicked,
                onSendClicked = ::onSendClicked,
                onStartStopClicked = ::onStartStopClicked,
                started = started,
                value_bpm = lastHeart,
                value_acc = lastAccel
            )
        }
    }

    @SuppressLint("VisibleForTests")
    private fun onPingClicked() {

        Log.i(TAG, "Ping clicked!")
        lifecycleScope.launch {
            try {
                val request = PutDataMapRequest.create("/ping-pong").apply {
                    dataMap.putString("mossa", "ping")
                    dataMap.putLong("timestamp", Instant.now().toEpochMilli())
                }
                    .asPutDataRequest()
                    .setUrgent()

                val result = dataClient.putDataItem(request)

                Log.d(TAG, "DataItem saved: $result")
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (exception: Exception) {
                Log.d(TAG, "Saving DataItem failed: $exception")
            }
        }
    }

    @SuppressLint("VisibleForTests")
    private fun onSendClicked() {
        if(started || session_start_time == 0L) {
            Toast.makeText(applicationContext, "PROIBITO INVIARE SESSIONE DURANTE SESSIONE IN CORSO!", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val request = PutDataMapRequest.create("/ping-pong").apply {
                    dataMap.putLong("start", session_start_time)
                    dataMap.putDataMap("data_heart", heartTimeSeries.serializeToGoogle())
                    dataMap.putDataMap("data_accel", accelTimeSeries.serializeToGoogle())
                }
                    .asPutDataRequest()
                    .setUrgent()

                val result = dataClient.putDataItem(request)

                Log.d(TAG, "DataItem saved: $result")
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (exception: Exception) {
                Log.d(TAG, "Saving DataItem failed: $exception")
            }
        }
    }

    var lastAccelSample = 0L

    override fun onSensorChanged(event: SensorEvent?) {
        if(event == null)
            return

        if(event.sensor.type == Sensor.TYPE_HEART_RATE) {
            lastHeart = event.values[0]
            heartTimeSeries.add(floatArrayOf(lastHeart))
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            if (event.timestamp - lastAccelSample <= 1e9)
                return

            lastAccelSample = event.timestamp
            lastAccel = event.values
            accelTimeSeries.add(lastAccel)
        }

        setContent {
            MainApp(
                onPingClicked = ::onPingClicked,
                onSendClicked = ::onSendClicked,
                onStartStopClicked = ::onStartStopClicked,
                started = started,
                value_bpm = lastHeart,
                value_acc = lastAccel
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //TODO("Not yet implemented")
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}