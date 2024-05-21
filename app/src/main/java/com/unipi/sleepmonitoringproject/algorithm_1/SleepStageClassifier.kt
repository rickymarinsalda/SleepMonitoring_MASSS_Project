package com.unipi.sleepmonitoringproject.algorithm_1

import android.util.Log
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class SleepStageClassifier {
    companion object {
        const val THETA_DEEP = 0.9688 // Threshold for deep sleep ERA 0.75
        const val THETA_REM = 1.063 // Threshold for REM sleep  ERA 1.2

        enum class SleepStage {
            LIGHT,
            DEEP,
            REM
        }

        fun calculateY0(heartRate: FloatArray): FloatArray {
            val y0 = FloatArray(heartRate.size)
            for (n in 1 until heartRate.size - 1) {
                y0[n] = abs(heartRate[n + 1] - heartRate[n - 1])
            }
            return y0
        }

        fun calculateY1(heartRate: FloatArray): FloatArray {
            val y1 = FloatArray(heartRate.size)
            for (n in 2 until heartRate.size - 2) {
                y1[n] = abs(heartRate[n + 2] - 2 * heartRate[n] + heartRate[n - 2])
            }
            return y1
        }

        fun calculateY2(y0: FloatArray, y1: FloatArray): FloatArray {
            val y2 = FloatArray(y0.size)
            for (i in y0.indices) {
                y2[i] = (1.3 * y0[i] + 1.1 * y1[i]).toFloat()
                if (y2[i] < 1.0) {
                    y2[i] = 0.0F // Set small peaks below the threshold to zero
                }
            }
            return y2
        }

        fun calculateRRIntervals(heartRate: FloatArray): List<Long> {
            val RRIntervals = mutableListOf<Long>()

            for (hr in heartRate) {
                if (hr > 0) {
                    val RRInterval = (60 / hr * 1000).toLong()
                    RRIntervals.add(RRInterval)
                }
            }

            return RRIntervals
        }

        fun calculateMean(values: LongArray): Double {
            var sum = 0.0
            for (value in values) {
                sum += value
            }
            return sum / values.size
        }
        fun calculateMean(values: DoubleArray): Double {
            var sum = 0.0
            for (value in values) {
                sum += value
            }
            return sum / values.size
        }

        fun calculateSDNN(RRIntervals: LongArray): Double {
            if (RRIntervals.size <= 1)
                return 0.0

            val meanRR = calculateMean(RRIntervals)
            val differences = DoubleArray(RRIntervals.size - 1)

            for (i in 0 until RRIntervals.size - 1) {
                differences[i] = (RRIntervals[i + 1] - RRIntervals[i]).toDouble()
            }

            for (i in differences.indices) {
                differences[i] = (differences[i] - meanRR).pow(2)
            }

            val meanSquaredDifference = calculateMean(differences)
            return sqrt(meanSquaredDifference)
        }

        fun calculateAverageSDNN(sdnnValues: DoubleArray): Double {
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

        fun classifySleepStage(avgSDNN: Double, sdnnValue_nth: Double): SleepStage {
            val thetaDeep = avgSDNN * THETA_DEEP
            val thetaRem = avgSDNN * THETA_REM
            Log.d("AlgorithmResult", "ThetaRem: ${thetaRem}")
            Log.d("AlgorithmResult", "thetaDeep: ${thetaDeep}")

            when {
                sdnnValue_nth <= thetaDeep -> return SleepStage.DEEP
                sdnnValue_nth >= thetaRem -> return SleepStage.REM
            }

            return SleepStage.LIGHT
        }
    }
}