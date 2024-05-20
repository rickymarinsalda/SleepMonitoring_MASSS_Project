package com.example.pingapp

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.max


class Classifier(private val context: Context) {

    private lateinit var tflite : Interpreter

    private lateinit var modelName: String

    init {
//        try {
//            tflite = Interpreter(loadModelFile())
//        } catch (e: Exception) {
//            // Print the stack trace for any other exceptions during initialization
//            e.printStackTrace()
//        }
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

    fun doInference(bpms : FloatArray): Int {
        assert(bpms.size == 5)

        for (i in bpms.indices)
            bpms[i] = (bpms[i] - 50)/105

        val output = Array(1) { FloatArray(3) }
        tflite.run(bpms, output)

        Log.i("MODELLOTFLITEBRUH", "Ran inference on ${output[0][0]}, ${output[0][1]}, ${output[0][2]}")

        val maxV = max(output[0][0], max(output[0][1], output[0][2]))

        if (output[0][0] >= maxV)
            return 0
        if (output[0][1] >= maxV)
            return 1
        if (output[0][2] >= maxV)
            return 2
        throw Exception("NO MA CHE DIAVOLO COM'È POSSIBILE")
    }
}

