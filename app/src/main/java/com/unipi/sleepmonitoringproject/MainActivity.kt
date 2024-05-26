package com.unipi.sleepmonitoringproject


import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.BaseColumns
import android.util.Log
import android.view.Menu
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope


import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.unipi.sleepmonitoring_masss_library.FileLoader
import com.unipi.sleepmonitoring_masss_library.TimeSeries
import com.unipi.sleepmonitoringproject.algorithm_1.SleepStageClassifier
import com.unipi.sleepmonitoringproject.databinding.ActivityMainBinding
import com.unipi.sleepmonitoring_masss_library.db.EventManagerDbHelper
import com.unipi.sleepmonitoring_masss_library.db.EventManagerContract
import com.unipi.sleepmonitoring_masss_library.db.insertIntoDB
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Calendar
import java.util.Date
import java.util.Locale

//  ------------------------


//FRAGMENTS DA AGGIUNGERE: HOME, STATISTICHE, POWER_CONSUMPTION, LOGIN/LOGOUT (FORSE), PROFILO

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var binding: ActivityMainBinding
    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val capabilityClient by lazy { Wearable.getCapabilityClient(this) }

    private val sharedViewModel: SharedViewModel by viewModels()
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
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(MainActivity.TAG, "Requirin permission!")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1)
        } else {
            Log.d(MainActivity.TAG, "You have already permission!")
        }


        dbHelper = EventManagerDbHelper(this) // inizializzo db
        sharedViewModel.dbHelper = dbHelper
        //clearDatabase(dbHelper) // PULISCE IL DB
        //insertHeartRateDataFromFile(this, "8692923_heartrate.txt") // AGGIUNGE AL DB ROBA DA FILE IN /ASSETS
        //insertHeartRateDataFromFile(this, "9618981_heartrate.txt") // AGGIUNGE AL DB ROBA DA FILE IN /ASSETS


        //enableEdgeToEdge()


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fileLoader = FileLoader(binding.root.context, "8692923_heartrate.txt", "8692923_acceleration.txt")
        val newTime = fileLoader.loadData()

        insertIntoDB(dbHelper.writableDatabase, newTime)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            //TODO
            onPongClick()
            Snackbar.make(view, "Start to record your sleep data", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()

            /* Open the recording fragment */
            val navController = findNavController(R.id.nav_host_fragment_content_main)
            navController.navigate(R.id.action_nav_home_to_recordingFragment)
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_stats
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }


    @RequiresApi(Build.VERSION_CODES.O)
     fun onPongClick() {
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

    data class SleepEvent(val timestamp: Long, val value: Float)
    private fun onQueryClick() {
        // Avvia un'operazione asincrona per eseguire la query del database
        lifecycleScope.launch {
            //try {

            val currentDate = System.currentTimeMillis() // data corrente in millisecondi
            val startOfYesterday = getStartOfYesterday(currentDate)
            val endOfToday = getEndOfDay(currentDate)

            Log.d("DatabaseTest", "Start of yesterday: $startOfYesterday")
            Log.d("DatabaseTest", "End of today: $endOfToday")

            val sleepEvents = queryDatabaseAndExtractEvents(dbHelper, startOfYesterday, endOfToday)
            /*
                Qui adesso devo inserire in qualche modo l'algoritmo, che prende in ingresso gli sleepEvents
                e poi restituisce il risultato dell'algoritmo utilizando SleepStageClassifier
            */
            for (event in sleepEvents) {
                val timestamp = event.timestamp
                val value = event.value.toDouble()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val timestamp_stringa = dateFormat.format(Date(timestamp))
                // Log.d("DatabaseTest", "Timestamp: $timestamp_stringa, Value: $value")
            }

            // val itemIds = queryDatabaseAndExtractIds(dbHelper) // questa è una query d'esempio per vedè se va
            // Chiamata all'algoritmo

            val sleepStages = algorithm_1(sleepEvents)
            /*

            val classifierML = ClassifierML(context = this@PingActivity)

            var sleepStages = intArrayOf()

            for (i in sleepEvents.indices step 5) {
                if (i + 4 >= sleepEvents.size)
                    continue

                val bpms = floatArrayOf(
                    sleepEvents[i+0].value,
                    sleepEvents[i+1].value,
                    sleepEvents[i+2].value,
                    sleepEvents[i+3].value,
                    sleepEvents[i+4].value,
                    )

                val assurdo = classifierML.doInference(bpms)

                Log.d(TAG, "Assurdo[$i] -> $assurdo")

                sleepStages += assurdo
            }
            */

            // Creazione del file di output
            // Percorso della directory principale della scheda SD
            val sdCardDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val outputFileName = "output_sonno.txt"

            Log.d(MainActivity.TAG, "====================" + sdCardDirectory.toString())

            // Creazione del file di output nella directory della scheda SD
            val outputFile = File(sdCardDirectory, outputFileName)

            //if (ContextCompat.checkSelfPermission(this@PingActivity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.d(MainActivity.TAG, "Requiring permission!")
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1)
            //} else {
            //    Log.d(TAG, "You have already permission!")
            //}


            // Scrittura dei risultati su file
            FileOutputStream(outputFile).use { fileOutputStream ->
                OutputStreamWriter(fileOutputStream).use { outputStreamWriter ->
                    // Scrittura dei risultati nel file
                    var data_stamp = 0
                    for (stage in sleepStages) {
                        val line = "${data_stamp} ${stage}\n"
                        outputStreamWriter.write(line)
                        data_stamp += 5 * 60
                    }
                }
            }




            // Stampa i risultati dell'algoritmo
            for (stage in sleepStages) {
                Log.d("AlgorithmResult", "Sleep stage: ${stage}")
            }


            //} catch (e: Exception) {
            // Gestisci eventuali eccezioni qui
            //    Log.e("DatabaseTest", "Errore durante l'esecuzione della query: ${e.message}")
            //}
        }
    }

    fun queryDatabase(dbHelper: EventManagerDbHelper, start: Long, end: Long): Deferred<Cursor?> = CoroutineScope(
        Dispatchers.IO).async {
        val db = dbHelper.readableDatabase
        //val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

        //val startString = dateFormat.format(Date(start))
        //val endString = dateFormat.format(Date(end))
        val projection = arrayOf(BaseColumns._ID, EventManagerContract.SleepEvent.COLUMN_NAME_TIMESTAMP, EventManagerContract.SleepEvent.COLUMN_NAME_EVENT1)
        val selection = null
        val selectionArgs = null
        //val selection = "${EventManagerContract.SleepEvent.COLUMN_NAME_TIMESTAMP} >= ? AND ${EventManagerContract.SleepEvent.COLUMN_NAME_TIMESTAMP} <= ?" // Filtra in modo che sia una data compresa tra ieri e oggi
        //val selectionArgs = arrayOf(startString, endString)
        val sortOrder = "${EventManagerContract.SleepEvent.COLUMN_NAME_TIMESTAMP} ASC"
        return@async db.query(
            EventManagerContract.SleepEvent.TABLE_NAME1,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder
        )
    }

    suspend fun queryDatabaseAndExtractEvents(dbHelper: EventManagerDbHelper, startOfYesterday: Long, endOfToday: Long): List<SleepEvent> {
        val sleepEvents = mutableListOf<SleepEvent>()
        val cursor = queryDatabase(dbHelper, startOfYesterday, endOfToday).await()
        cursor?.use {
            while (it.moveToNext()) {
                val timestamp = it.getLong(it.getColumnIndexOrThrow(EventManagerContract.SleepEvent.COLUMN_NAME_TIMESTAMP)) // QUI LA DATA È STRINGA
                val value = it.getFloat(it.getColumnIndexOrThrow(EventManagerContract.SleepEvent.COLUMN_NAME_EVENT1))

                // si converte la stringa del timestamp in millisecondi
                //val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
                // val date = dateFormat.parse(timestampString)
                //val timestamp = date?.time ?: 0L // QUI LA DATA È IN MILLISEC

                sleepEvents.add(SleepEvent(timestamp, value))
            }
        }

        return sleepEvents
    }

    fun algorithm_1(sleep_event :List<SleepEvent>): List<SleepStageClassifier.Companion.SleepStage> {

        val sdnn_list = mutableListOf<Double>()

        // Calcola la deviazione standard ogni 5 minuti
        // ---------------------------------------------------------------------
        val INTERVAL = 5 * 60 * 1000L; // 5 minuti in milli
        val BASE_TIMESTAMP = sleep_event[0].timestamp
        var currentWindow = 0L
        var currentIndex = 0
        Log.i(MainActivity.TAG, "BASE TIMESTAMP: $BASE_TIMESTAMP")
        while (currentIndex < sleep_event.size) {
            val currentTimestamp = sleep_event[currentIndex].timestamp
            val currentValues = mutableListOf<Float>()
            val currentTimestamps = mutableListOf<Long>()
            val rightBound = INTERVAL*(currentWindow+1)

            // Raccogli i valori nei prossimi 5 minuti
            while (currentIndex < sleep_event.size && sleep_event[currentIndex].timestamp - BASE_TIMESTAMP < rightBound) {
                currentValues.add(sleep_event[currentIndex].value)
                currentTimestamps.add(sleep_event[currentIndex].timestamp)
                currentIndex++
            }
            val currentValuesArray = currentValues.toFloatArray()

            // Calcola la deviazione standard per i valori raccolti
            //val stdDev = calculateStandardDeviation(currentValues)
            /* QUESTI QUA A REGOLA NON SERVONO PIÙ
            val y0 = SleepStageClassifier.calculateY0(currentValuesArray)
            val y1 = SleepStageClassifier.calculateY1(currentValuesArray)
            val y2 = SleepStageClassifier.calculateY2(y0, y1)
            */
            val RRIntervals = SleepStageClassifier.calculateRRIntervals(currentValuesArray)
            // Convert RRIntervals to a double array
            val RRIntervalsArray = RRIntervals.toLongArray()
            val sdnn = SleepStageClassifier.calculateSDNN(RRIntervalsArray)
            //if(sdnn != 0.0)
            sdnn_list.add(sdnn)

            if(sdnn > 0)
                Log.d("DeviationCalculation", "Deviazione standard per il periodo ${currentTimestamp} - ${currentTimestamp + INTERVAL}: $sdnn")
            else
                Log.e("DeviationCalculation", "Deviazione standard per il periodo ${currentTimestamp} - ${currentTimestamp + INTERVAL}: $sdnn")

            currentWindow++
        }

        val sdnn_array = sdnn_list.toDoubleArray()
        // Calcola la deviazione standard media (SDNN) degli intervalli RR
        val avgSDNN = SleepStageClassifier.calculateAverageSDNN(sdnn_array)

        // ---------------------------------------------------------------------

        // Classifica gli intervalli RR in base alla SDNN
        val sleepStages = mutableListOf<SleepStageClassifier.Companion.SleepStage>()
        for (sdnn in sdnn_array) {
            val stage = SleepStageClassifier.classifySleepStage(avgSDNN, sdnn)
            sleepStages.add(stage)
        }

        return sleepStages

    }
    fun getStartOfYesterday(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.add(Calendar.DAY_OF_YEAR, -1) // Sottraggo un giorno
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getEndOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
    /*
    -------------------------------------- TEST ----------------------------------
     */
// Funzione per leggere i dati dal file di testo e inserirli nel database
    fun insertHeartRateDataFromFile(context: Context, fileName: String) {
        val db = dbHelper.writableDatabase

        // Legge i dati dal file di testo
        val assetManager = context.assets
        val inputStream = assetManager.open(fileName)
        val reader = BufferedReader(InputStreamReader(inputStream))

        var line: String?
        val timestamp_base = System.currentTimeMillis() // Timestamp di partenza
        var last_timestamp = 0L
        var lines = 0L
        var last_bpm = 0.0f
        //val interval = 1000 * 2 // Intervallo di 2 secondi in millisecondi

        try {
            while (reader.readLine().also { line = it } != null) {
                line?.let {
                    lines++
                    val dataParts = it.split(",") // Divide la linea in due parti
                    if (dataParts.size != 2) {
                        throw Exception("bruh")
                    }

                    val timestamp_grezzo = (dataParts[0].toFloat() * 1000f).toLong();
                    val timestamp =  timestamp_grezzo + timestamp_base;
                    val bpm = dataParts[1].toFloat() // Usa solo la parte dopo la virgola

                    if (timestamp_grezzo < 0.0f)
                        return@let

                    if (bpm.isNaN() or bpm.isInfinite())
                        throw Exception("BRUH IN VIRGOLA MOBILE")

                    if (timestamp - last_timestamp < 2000L && last_timestamp != 0L)
                        return@let

                    val toInsert = mutableListOf<Pair<Long, Float>>()
                    if (timestamp - last_timestamp < 60*1000L || last_timestamp == 0L)
                        toInsert.add(Pair(timestamp, bpm))
                    else { // Interpola
                        val n_ticks = (timestamp - last_timestamp)/90000L
                        for (i in 1..n_ticks) {
                            toInsert.add(Pair(
                                timestamp + i*90000L,
                                (1.0f - i.toFloat()/n_ticks)*last_bpm + (i.toFloat()/n_ticks)*bpm
                            ))
                        }
                    }

                    toInsert.forEach {
                        // Crea un nuovo record
                        val values = ContentValues().apply {
                            put(EventManagerContract.SleepEvent.COLUMN_NAME_TIMESTAMP, it.first)
                            put(EventManagerContract.SleepEvent.COLUMN_NAME_EVENT1, it.second)
                        }

                        // Formatta il timestamp
                        val formattedTimestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date(it.first))
                        val grullo = it.first - last_timestamp

                        Log.d("DatabaseTest", "Inserisco: $formattedTimestamp (${it.first}), BPM: $bpm, diff: $grullo")

                        // Inserisce il record nel database
                        val status = db.insert(EventManagerContract.SleepEvent.TABLE_NAME1, null, values)
                        if (status == -1L)
                            throw Exception("BRUH DB")
                    }

                    last_timestamp = timestamp
                    last_bpm = bpm
                }
            }
        } catch (e: Exception) {
            Log.e("DatabaseTest", "Errore durante la lettura del file o l'inserimento nel database", e)
        } finally {
            reader.close()
            db.close()
            Log.i(MainActivity.TAG, "LETTE $lines LINEE DI MERDA")
        }
    }


    fun clearDatabase(dbHelper: EventManagerDbHelper) {
        val db = dbHelper.writableDatabase
        try {
            db.beginTransaction()
            val tables = arrayOf(
                EventManagerContract.SleepEvent.TABLE_NAME1,
                EventManagerContract.SleepEvent.TABLE_NAME2,
            )

            for (table in tables) {
                val deletedRows = db.delete(table, null, null)
                Log.i("DatabaseClear", "Deleted $deletedRows rows from table $table")
            }

            db.setTransactionSuccessful()
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e("DatabaseClear", "Error while trying to clear database", e)
        } finally {
            db.endTransaction()
            db.close()
        }
    }

// --------------------- FINE CODICE IMPORTATO DA PING APP --------------------------


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    companion object {
        private const val TAG = "MainActivity"
    }
}