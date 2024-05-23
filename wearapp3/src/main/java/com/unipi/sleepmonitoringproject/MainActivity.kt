package com.unipi.sleepmonitoringproject

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.unipi.sleepmonitoring_masss_library.Classifier
import com.unipi.sleepmonitoring_masss_library.ClassifierML
import com.unipi.sleepmonitoring_masss_library.ClassifierStatistical
import com.unipi.sleepmonitoring_masss_library.FileLoader
import com.unipi.sleepmonitoring_masss_library.SleepStage
import com.unipi.sleepmonitoring_masss_library.TimeSeries
import com.unipi.sleepmonitoring_masss_library.classifySeries
import com.unipi.sleepmonitoringproject.presentation.ClassifySessionResults
import com.unipi.sleepmonitoringproject.presentation.MainApp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.time.Instant

@SuppressLint("RestrictedApi")
class MainActivity : ComponentActivity(), SensorEventListener {
    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val capabilityClient by lazy { Wearable.getCapabilityClient(this) }

    private val clientDataViewModel by viewModels<ClientDataViewModel>()

    private val sensorManager: SensorManager by lazy { getSystemService(SENSOR_SERVICE) as SensorManager }

    private var started: Boolean = false
    private var session_start_time = 0L

    private var lastHeart = 0.0f
    private var lastAccel: FloatArray = floatArrayOf(0.0f, 0.0f, 0.0f)

    private var heartTimeSeries = TimeSeries()
    private var accelTimeSeries = TimeSeries()
    private var combinedTimeSeries = TimeSeries()
    private var lastAccelSeriesIndex = 0

    private var useMLClassifier = false
    private var samplingRate = 0.0f


    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Qui chiamo il metodo del bottone che voglio attivare
            onStartStopClicked()
            if (!started && session_start_time != 0L) {
                onSendClicked()
            }
        }
    }
    @Composable
    fun MainAppWrapper() {
        fun f(x: Boolean) {useMLClassifier = x}
        fun g(x: Float) {samplingRate = x}

        MainApp(
            onPingClicked = ::onPingClicked,
            onSendClicked = ::onSendClicked,
            onStartStopClicked = ::onStartStopClicked,
            onClassifyClicked = ::onClassifyClicked,
            onLoadSeriesFromFile = ::onLoadSampleClicked,
            onMLChange = ::f,
            onSamplingRateChange = ::g,
            started = started,
            value_bpm = lastHeart,
            value_acc = lastAccel,
            readyToClassify = combinedTimeSeries.data.isNotEmpty(),
            useMLDefault = useMLClassifier,
            samplingRate = samplingRate
        )
    }

    override fun onResume() {
        super.onResume()
        dataClient.addListener(clientDataViewModel)
        messageClient.addListener(clientDataViewModel)
        registerReceiver(receiver, IntentFilter("com.unipi.sleepmonitoringproject.ACTION"))
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
        unregisterReceiver(receiver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            MainAppWrapper()
        }
    }

    private fun onStartStopClicked() {
        started = !started
        if (started) {
            val sensorHeartRate = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
            val sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

            Log.i(TAG, "Start sampling at $samplingRate")

            val successHeart = sensorManager.registerListener(
                this,
                sensorHeartRate,
                if(samplingRate == 0.0f)
                    SensorManager.SENSOR_DELAY_NORMAL
                else
                    samplingRate.toInt() * 1_000_000 // us
            )
            Log.i(TAG, "successHeart=$successHeart")

            val successAcc = sensorManager.registerListener(
                this,
                sensorAccelerometer,
                if(samplingRate == 0.0f)
                    SensorManager.SENSOR_DELAY_NORMAL
                else
                    samplingRate.toInt() * 1_000_000 // us
            )
            Log.i(TAG, "successAccel=$successAcc")

            Log.i(TAG, "starting session....")
            session_start_time = System.currentTimeMillis()

            // Clearing old session
            heartTimeSeries = TimeSeries()
            accelTimeSeries = TimeSeries()
            combinedTimeSeries = TimeSeries()
            lastAccelSeriesIndex = 0

        } else {
            Log.i(TAG, "stopping session....")
            sensorManager.unregisterListener(this)
        }

        setContent {
            MainAppWrapper()
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
        if (started || session_start_time == 0L) {
            Toast.makeText(
                applicationContext,
                "PROIBITO INVIARE SESSIONE DURANTE SESSIONE IN CORSO!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        lifecycleScope.launch {
            try {
                val request = PutDataMapRequest.create("/ping-pong").apply {
                    dataMap.putLong("start", session_start_time)
                    dataMap.putDataMap("combined_series", combinedTimeSeries.serializeToGoogle())
                    //dataMap.putDataMap("data_heart", heartTimeSeries.serializeToGoogle())
                    //dataMap.putDataMap("data_accel", accelTimeSeries.serializeToGoogle())
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

    fun onClassifyClicked() {
        if(combinedTimeSeries.data.isEmpty()) {
            Toast.makeText(
                applicationContext,
                "NON HAI REGISTRATO NIENTE!!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val classifier: Classifier
        if (!useMLClassifier) {
            classifier = ClassifierStatistical()
            classifier.updateSSD(combinedTimeSeries)
        } else {
            classifier = ClassifierML(applicationContext)
        }

        val res = classifySeries(classifier, combinedTimeSeries)
        val baseT = combinedTimeSeries.data[0].timestamp
        var results: Array<Pair<Long, String>> = emptyArray()

        for (i in res.indices) {
            results += Pair(
                baseT + 5*60*1000L*i.toLong(),
                SleepStage.entries[res[i]].toString().removeSuffix("_SLEEP")
            )
        }

        setContent {
            ClassifySessionResults(
                onBackClick = ::onExitClassifyResults,
                data = results
            )
        }
    }

    fun onExitClassifyResults() {
        setContent {
            MainAppWrapper()
        }
    }

    private fun onLoadSampleClicked() {
        val fileLoader = FileLoader(
            applicationContext,
            "8692923_heartrate.txt",
            "8692923_acceleration.txt"
        )

        combinedTimeSeries = fileLoader.loadData(60*5*1000L)

        lastHeart = 999.9f
        lastAccel = floatArrayOf(999.9f, 999.9f, 999.9f)
        session_start_time = 60*5*1000L

        setContent {
            MainAppWrapper()
        }

        Toast.makeText(
            applicationContext,
            "Caricati ${combinedTimeSeries.data.size} elementi",
            Toast.LENGTH_SHORT
        ).show()
    }

    var lastAccelSample = 0L

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null)
            return

        if (event.sensor.type == Sensor.TYPE_HEART_RATE) {
            // misread
            if (event.values[0] == 0.0f)
                return

            lastHeart = event.values[0]
            heartTimeSeries.add(floatArrayOf(lastHeart))

            val denominator = accelTimeSeries.size() - lastAccelSeriesIndex
            val numerator = floatArrayOf(0.0f, 0.0f, 0.0f)

            if (denominator <= 1) {
                combinedTimeSeries.add(floatArrayOf(lastHeart) + lastAccel)
                return
            }

            // Compute average acceleration during period
            for (i in lastAccelSeriesIndex + 1..<accelTimeSeries.data.size) {
                numerator[0] += accelTimeSeries.data[i].datum[0]
                numerator[1] += accelTimeSeries.data[i].datum[1]
                numerator[2] += accelTimeSeries.data[i].datum[2]
            }

            numerator[0] /= denominator.toFloat()
            numerator[1] /= denominator.toFloat()
            numerator[2] /= denominator.toFloat()

            combinedTimeSeries.add(floatArrayOf(lastHeart) + numerator)
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            if (event.timestamp - lastAccelSample <= 250)
                return

            lastAccelSample = event.timestamp
            lastAccel = event.values
            accelTimeSeries.add(lastAccel)
        }

        setContent {
            MainAppWrapper()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //TODO("Not yet implemented")
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}