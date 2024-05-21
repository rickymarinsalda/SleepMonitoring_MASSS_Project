package com.unipi.sleepmonitoring_masss_library

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.max


interface Classifier{
    fun doInference(input : Array<FloatArray>): Int
}

class ClassifierML(private val context: Context) : Classifier {

    private lateinit var tflite : Interpreter

    private lateinit var modelName: String

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

// @TODO add Ricky's alg
