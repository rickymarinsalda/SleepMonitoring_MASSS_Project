package com.unipi.sleepmonitoringproject.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.rememberScalingLazyListState
import java.text.SimpleDateFormat

@Composable
fun ClassifySessionResults(
    data: Array<Pair<Long, String>> = arrayOf(),
    onBackClick: () -> Unit = {}
) {
    val scalingLazyListState = rememberScalingLazyListState()

    Scaffold(
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = scalingLazyListState) },
        timeText = { TimeText() },
    ) {
        Column(
            Modifier.padding(top = 20.dp)
        ) {
            Button(
                onClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
            ) {
                Text(text = "back")
            }
            TimeSeriesView(series = data)
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
fun TimeSeriesView(series: Array<Pair<Long, String>>) {
    // Each cell of a column must have the same weight.
    val column1Weight = .55f // 30%
    val column2Weight = 1 - column1Weight // 70%
    // The LazyColumn will be our table. Notice the use of the weights below
    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Here is the header
        item {
            Row(Modifier.background(Color.Gray)) {
                TableCell(text = "PERIOD", weight = column1Weight)
                TableCell(text = "STAGE", weight = column2Weight)
            }
        }
        // Here are all the lines of your table.
        items(series) {
            Row(Modifier.fillMaxWidth()) {
                TableCell(
                    text = SimpleDateFormat("HH:mm").format(it.first),
                    weight = column1Weight
                )
                TableCell(
                    text = it.second,
                    weight = column2Weight
                )
            }
        }
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun ClassifySessionResultsPreview() {
    ClassifySessionResults()
}

const val SAMPLES_PREVIEW = 50

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun ClassifySessionResultsWithDataPreview() {
    val stages = arrayOf("AWAKE", "LIGHT", "DEEP", "REM")
    var series = arrayOf<Pair<Long, String>>()
    for (i in 0..SAMPLES_PREVIEW)
        series += Pair(
            0L + i.toLong() * 1000L * 5L * 60L,
            stages[i % stages.size]
        )

    ClassifySessionResults(series)
}