package com.example.pingapp

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TableLayout
import android.widget.TableRow
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.pingapp.ui.theme.SleepMonitoring_MASSS_ProjectTheme
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.unipi.sleepmonitoring_masss_library.TimeSeries
import kotlinx.coroutines.*
import java.time.Instant

class PingActivity : ComponentActivity() {
    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val capabilityClient by lazy { Wearable.getCapabilityClient(this) }

    private val clientDataViewModel by viewModels<ClientDataViewModel>()

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
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContent {
            PingView(onPongClick = ::onPongClick)
        }
    }

    private fun onPongClick() {
        lifecycleScope.launch {
            try {
                val request = PutDataMapRequest.create("/ping-pong").apply {
                    dataMap.putString("mossa", "pong")
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

    companion object {
        private const val TAG = "PingActivity"
    }
}

@Composable
fun PingView(
    onPongClick: () -> Unit = {},
    series: TimeSeries = TimeSeries()
) {
    SleepMonitoring_MASSS_ProjectTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column {
                Button(onClick = onPongClick, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Send Pong")
                }
                Greeting(
                        name = "Android",
                modifier = Modifier.padding(innerPadding)
                )
                TimeSeriesView(series = series)
            }
        }
    }
}

@Composable
fun TimeSeriesView(series: TimeSeries) {
    Text(text = "bussy")
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    val original = TimeSeries()
    for (i in 0..100)
        original.add(floatArrayOf(i*1.0f, i*2.0f, i*-3.0f), i * 1000L)

    PingView(series = original)
}