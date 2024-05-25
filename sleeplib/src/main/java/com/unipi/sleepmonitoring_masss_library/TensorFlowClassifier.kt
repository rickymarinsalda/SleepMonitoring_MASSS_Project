package com.unipi.sleepmonitoring_masss_library

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt


interface Classifier{
    fun doInference(input : Array<FloatArray>): Int
}

enum class SleepStage(val stage: Int) {
    AWAKE(0), LIGHT_SLEEP(1), DEEP_SLEEP(2), REM(3)
}

const val INTERVAL = 10*60*1_000L

class ClassifierML(private val context: Context) : Classifier {

    private var tflite : Interpreter

    init {
        tflite = Interpreter(loadModelFile())
    }

    // Add a method to load the model file from the assets folder
    private fun loadModelFile(): ByteBuffer {
        val fileDescriptor: AssetFileDescriptor = context.assets.openFd("model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declareLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declareLength)
    }

    /**
     * @param input a 4x5 "matrix", first row contains the bpms and the others the x y z readings
     *              from the accelerometer
     */
    override fun doInference(input : Array<FloatArray>): Int {
        assert(input.size == 4)
        for (floats in input) {
            assert(floats.size == 5)
        }

        // Normalize bpms
        for (i in input[0].indices)
            input[0][i] = (input[0][i] - 50)/105

        val output = Array(1) { FloatArray(4) }
        tflite.run(input, output)

        Log.i("MODELLOTFLITEBRUH", "Ran inference on ${output[0][0]}, ${output[0][1]}, ${output[0][2]}")

        val maxV = max(output[0][0], max(output[0][1], max(output[0][2], output[0][3])))

        if (output[0][0] >= maxV)
            return 0
        if (output[0][1] >= maxV)
            return 1
        if (output[0][2] >= maxV)
            return 2
        if (output[0][3] >= maxV)
            return 3

        // bruh
        throw Exception("NO MA CHE DIAVOLO COM'È POSSIBILE")
    }
}

class ClassifierStatistical() : Classifier {
    companion object {
        const val THETA_DEEP = 0.9688 // Threshold for deep sleep ERA 0.75
        const val THETA_REM = 1.063 // Threshold for REM sleep  ERA 1.2
    }

    var avgSDNN = Float.POSITIVE_INFINITY // We agree!

    private fun calculateRRIntervals(heartRate: FloatArray): List<Long> {
        val RRIntervals = mutableListOf<Long>()

        for (hr in heartRate) {
            if (hr > 0) {
                val RRInterval = (60 / hr * 1000).toLong()
                RRIntervals.add(RRInterval)
            }
        }

        return RRIntervals
    }

    private fun calculateMean(values: LongArray): Float {
        var sum = 0.0f
        for (value in values) {
            sum += value
        }
        return sum / values.size
    }
    private fun calculateMean(values: FloatArray): Float {
        var sum = 0.0f
        for (value in values) {
            sum += value
        }
        return sum / values.size
    }

    private fun calculateSDNN(RRIntervals: LongArray): Float {
        if (RRIntervals.size <= 1)
            return 0.0f

        val meanRR = calculateMean(RRIntervals)
        val differences = FloatArray(RRIntervals.size - 1)

        for (i in 0 until RRIntervals.size - 1) {
            differences[i] = (RRIntervals[i + 1] - RRIntervals[i]).toFloat()
        }

        for (i in differences.indices) {
            differences[i] = (differences[i] - meanRR).pow(2)
        }

        val meanSquaredDifference = calculateMean(differences)
        return sqrt(meanSquaredDifference)
    }

    private fun calculateAverageSDNN(sdnnValues: DoubleArray): Double {
        if(sdnnValues.size == 0)
            return 0.0

        var sum = 0.0
        var count = 0L
        for (sdnn in sdnnValues) {
            sum += sdnn
            if(sdnn != 0.0)
                count++
        }
        return sum / sdnnValues.size
    }

    private fun classifySleepStage(sdnnValue: Float): Int {
        val thetaDeep = avgSDNN * THETA_DEEP
        val thetaRem = avgSDNN * THETA_REM
        Log.d("AlgorithmResult", "ThetaRem: ${thetaRem}")
        Log.d("AlgorithmResult", "thetaDeep: ${thetaDeep}")

        when {
            sdnnValue <= thetaDeep -> return SleepStage.DEEP_SLEEP.stage
            sdnnValue >= thetaRem -> return SleepStage.REM.stage
        }

        return SleepStage.LIGHT_SLEEP.stage
    }

    fun updateSSD(series: TimeSeries) {
        val baseT = series.data.get(0).timestamp
        var currentChunk = 0UL
        var numerator = 0.0
        var bpms = floatArrayOf()

        series.data.forEach {
            if (((it.timestamp - baseT) / INTERVAL).toULong() != currentChunk) {
                currentChunk ++

                if(bpms.size == 0)
                    return@forEach

                val RRIntervals = calculateRRIntervals(bpms)
                val RRIntervalsArray = RRIntervals.toLongArray()
                val sdnn = calculateSDNN(RRIntervalsArray)

                numerator += sdnn
            }

            bpms += floatArrayOf(it.datum[0])
        }

        avgSDNN = (numerator/(currentChunk + 1UL).toDouble()).toFloat()
    }

    override fun doInference(input: Array<FloatArray>): Int {
        val bpms = input[0]

        val RRIntervals = calculateRRIntervals(bpms)
        // Convert RRIntervals to a double array
        val RRIntervalsArray = RRIntervals.toLongArray()
        val sdnn = calculateSDNN(RRIntervalsArray)

        return classifySleepStage(sdnn)
    }
}

