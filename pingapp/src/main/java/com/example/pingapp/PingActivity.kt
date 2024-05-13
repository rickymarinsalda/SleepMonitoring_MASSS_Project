package com.example.pingapp

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Bundle
import android.provider.BaseColumns
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.pingapp.db.EventManagerContract
import com.example.pingapp.db.EventManagerDbHelper
import com.example.pingapp.ui.theme.SleepMonitoring_MASSS_ProjectTheme
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.unipi.sleepmonitoring_masss_library.TimeSeries
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.time.Instant

class PingActivity : ComponentActivity() {
    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val capabilityClient by lazy { Wearable.getCapabilityClient(this) }

    private val clientDataViewModel: ClientDataViewModel by viewModels {
        ClientDataViewModelFactory(dbHelper)
    }
    private lateinit var dbHelper: EventManagerDbHelper // helper for db

    override fun onResume() {
        super.onResume()
        dataClient.addListener(clientDataViewModel)
        messageClient.addListener(clientDataViewModel)
        capabilityClient.addListener(
            clientDataViewModel,
            Uri.parse("wear://"),
            CapabilityClient.FILTER_ALL
        )
        // dbHelper = EventManagerDbHelper(this)  // riaprire il db se necessario
    }

    override fun onPause() {
        super.onPause()
        dbHelper.close()
        dataClient.removeListener(clientDataViewModel)
        messageClient.removeListener(clientDataViewModel)
        capabilityClient.removeListener(clientDataViewModel)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = EventManagerDbHelper(this) // inizializzo db

        // Gets the data repository in write mode

        clientDataViewModel.updateGUI = ::onNewTimeSeries
        //enableEdgeToEdge()
        setContent {
            PingView(onPongClick = ::onPongClick)
        }
        setContent {
            PingView(onQueryClick = ::onQueryClick)
        }
    }

    private fun onNewTimeSeries(series: TimeSeries) {
        setContent {
            PingView(onPongClick = ::onPongClick, series = series)
        }
        setContent {
            PingView(onQueryClick = ::onQueryClick)
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

    private fun onQueryClick() {
        // Avvia un'operazione asincrona per eseguire la query del database
        lifecycleScope.launch {
            try {
                Log.d("DatabaseTest", "ENTRA?!?")
                val itemIds = queryDatabaseAndExtractIds(dbHelper)

            } catch (e: Exception) {
                // Gestisci eventuali eccezioni qui
                Log.e("DatabaseTest", "Errore durante l'esecuzione della query: ${e.message}")
            }
        }
    }
    fun queryDatabase(dbHelper: EventManagerDbHelper): Deferred<Cursor?> = CoroutineScope(Dispatchers.IO).async {
        val db = dbHelper.readableDatabase
        Log.d("DatabaseTest", "ENTRATO DENTRO QUERYY DATABASE?!?")
        val projection = arrayOf(BaseColumns._ID, EventManagerContract.SleepEvent.COLUMN_NAME_TIMESTAMP, EventManagerContract.SleepEvent.COLUMN_NAME_EVENT1)
        val selection = "${EventManagerContract.SleepEvent.COLUMN_NAME_EVENT1} = ?"
        val selectionArgs = arrayOf("boh")
        val sortOrder = "${EventManagerContract.SleepEvent.COLUMN_NAME_EVENT1} DESC"
        return@async db.query(
            EventManagerContract.SleepEvent.TABLE_NAME1,
            projection,
            null,
            null,
            null,
            null,
            sortOrder
        )
    }

    suspend fun queryDatabaseAndExtractIds(dbHelper: EventManagerDbHelper): List<Long> {
        val itemIds = mutableListOf<Long>()
        val cursor = queryDatabase(dbHelper).await()
        Log.d("DatabaseTest", "ENTRATO DENTRO EXTRACT!?")
        cursor?.use {
            while (it.moveToNext()) {
                val itemId = it.getLong(it.getColumnIndexOrThrow(BaseColumns._ID))
                itemIds.add(itemId)
                Log.d("DatabaseTest", "ID ottenuto dalla query: $itemId")
            }
        }

        return itemIds
    }

    companion object {
        private const val TAG = "PingActivity"
    }
}




@Composable
fun PingView(
    onPongClick: () -> Unit = {},
    onQueryClick: () -> Unit = {},
    series: TimeSeries = TimeSeries()
) {
    SleepMonitoring_MASSS_ProjectTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column {
                Button(onClick = onPongClick, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Send Pong")
                }
                Button(onClick = onQueryClick, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Query")
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
fun RowScope.TableCell(
    text: String,
    weight: Float
) {
    Text(
        text = text,
        Modifier
            .border(1.dp, Color.Black)
            .weight(weight)
            .padding(8.dp)
    )
}


@Composable
fun TimeSeriesView(series: TimeSeries) {
    // Each cell of a column must have the same weight.
    val column1Weight = .55f // 30%
    val column2Weight = 1 - column1Weight // 70%
    // The LazyColumn will be our table. Notice the use of the weights below
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        // Here is the header
        item {
            Row(Modifier.background(Color.Gray)) {
                TableCell(text = "Timestamp", weight = column1Weight)
                TableCell(text = "Data", weight = column2Weight)
            }
        }
        // Here are all the lines of your table.
        items(series.data) {
            Row(Modifier.fillMaxWidth()) {
                TableCell(
                    text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(it.timestamp),
                    weight = column1Weight
                )
                TableCell(
                    text = it.datum.joinToString { it.toString() },
                    weight = column2Weight
                )
            }
        }
    }
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
fun PreviewFull() {
    val original = TimeSeries()
    for (i in 0..100)
        original.add(floatArrayOf(i*1.0f, i*2.0f, i*-3.0f), i * 1000L * 30L)

    PingView(series = original)
}

@Preview(showBackground = true)
@Composable
fun PreviewEmpty() {
    PingView()
}