package com.unipi.sleepmonitoring_masss_library

fun classifySeries(classifier: Classifier, series: TimeSeries): IntArray {

    var results = intArrayOf()

    var i = 0
    // scan through series
    while(i < series.size()) {

        lateinit var input : Array<FloatArray>
        val startTime = series.data[i].timestamp

        var idxToSample : IntArray = intArrayOf()

        var j = i
        while(j < series.size() && series.data[j].timestamp < startTime + (5 * 60 * 1000L))
            idxToSample += j++

        if (j >= series.size())
            return results

        val samples = sampleNValuesFromArray(idxToSample, 5)

        samples.forEach {
            input += series.data[it].datum
        }
        results += classifier.doInference(input)

        i = j
    }

    return results
}

fun sampleNValuesFromArray(arr: IntArray, n: Int): List<Int> {
    if (n > arr.size) {
        throw IllegalArgumentException("Sample size cannot be greater than array size.")
    }

    val indices = arr.indices.shuffled().take(n)
    return indices.map { arr[it] }
}