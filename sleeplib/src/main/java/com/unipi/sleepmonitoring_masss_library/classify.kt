package com.unipi.sleepmonitoring_masss_library

fun classifySeries(classifier: Classifier, series: TimeSeries): IntArray {

    var results = intArrayOf()

    var i = 0
    // scan through series
    while(i < series.size()) {

        val input : Array<FloatArray> = arrayOf(
            floatArrayOf(), floatArrayOf(), floatArrayOf(), floatArrayOf(),
        )
        val startTime = series.data[i].timestamp

        var idxToSample : IntArray = intArrayOf()

        var j = i
        while(j < series.size() && series.data[j].timestamp < startTime + (10 * 60 * 1000L))
            idxToSample += j++

        if (idxToSample.isEmpty())
            return results

        val samples = sampleNValuesFromArray(idxToSample, 5)

        samples.forEach {
            input[0] += floatArrayOf(series.data[it].datum[0])
            input[1] += floatArrayOf(series.data[it].datum[1])
            input[2] += floatArrayOf(series.data[it].datum[2])
            input[3] += floatArrayOf(series.data[it].datum[3])
        }

        results += classifier.doInference(input)

        i = j
    }

    return results
}

fun sampleNValuesFromArray(arr_: IntArray, n: Int): List<Int> {

    var arr = arr_
    while(arr.size < 5) {
        arr.shuffle()
        arr += arr[0]
    }

    val indices = arr.indices.shuffled().take(n)
    return indices.map { arr[it] }
}