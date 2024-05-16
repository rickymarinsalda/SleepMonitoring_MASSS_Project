package com.example.pingapp.algorithm_1

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class SleepStageClassifier {
    companion object {
        const val THETA_DEEP = 0.75 // Threshold for deep sleep
        const val THETA_REM = 1.2 // Threshold for REM sleep

        enum class SleepStage {
            LIGHT,
            DEEP,
            REM
        }

        fun calculateY0(heartRate: DoubleArray): DoubleArray {
            val y0 = DoubleArray(heartRate.size)
            for (n in 1 until heartRate.size - 1) {
                y0[n] = abs(heartRate[n + 1] - heartRate[n - 1])
            }
            return y0
        }

        fun calculateY1(heartRate: DoubleArray): DoubleArray {
            val y1 = DoubleArray(heartRate.size)
            for (n in 2 until heartRate.size - 2) {
                y1[n] = abs(heartRate[n + 2] - 2 * heartRate[n] + heartRate[n - 2])
            }
            return y1
        }

        fun calculateY2(y0: DoubleArray, y1: DoubleArray): DoubleArray {
            val y2 = DoubleArray(y0.size)
            for (i in y0.indices) {
                y2[i] = 1.3 * y0[i] + 1.1 * y1[i]
                if (y2[i] < 1.0) {
                    y2[i] = 0.0 // Set small peaks below the threshold to zero
                }
            }
            return y2
        }

        fun calculateRRIntervals(timestamps: List<Double>): List<Double> {
            val RRIntervals = mutableListOf<Double>()
            val sortedTimestamps = timestamps.sorted()

            for (i in 0 until sortedTimestamps.size - 1) {
                val RRInterval = sortedTimestamps[i + 1] - sortedTimestamps[i]
                RRIntervals.add(RRInterval)
            }

            return RRIntervals
        }

        fun calculateMean(values: DoubleArray): Double {
            var sum = 0.0
            for (value in values) {
                sum += value
            }
            return sum / values.size
        }

        fun calculateSDNN(RRIntervals: DoubleArray): Double {
            val meanRR = calculateMean(RRIntervals)
            val differences = DoubleArray(RRIntervals.size - 1)

            for (i in 0 until RRIntervals.size - 1) {
                differences[i] = RRIntervals[i + 1] - RRIntervals[i]
            }

            for (i in differences.indices) {
                differences[i] = (differences[i] - meanRR).pow(2)
            }

            val meanSquaredDifference = calculateMean(differences)
            return sqrt(meanSquaredDifference)
        }

        fun calculateAverageSDNN(sdnnValues: DoubleArray): Double {
            var sum = 0.0
            for (sdnn in sdnnValues) {
                sum += sdnn
            }
            return sum / sdnnValues.size
        }

        fun classifySleepStage(avgSDNN: Double, sdnnValues: DoubleArray): SleepStage {
            val thetaDeep = avgSDNN * THETA_DEEP
            val thetaRem = avgSDNN * THETA_REM

            for (sdnn in sdnnValues) {
                when {
                    sdnn <= thetaDeep -> return SleepStage.DEEP
                    sdnn >= thetaRem -> return SleepStage.REM
                }
            }
            return SleepStage.LIGHT
        }
    }
}