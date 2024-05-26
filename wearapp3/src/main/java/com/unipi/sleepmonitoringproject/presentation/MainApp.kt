package com.unipi.sleepmonitoringproject.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.InlineSlider
import androidx.wear.compose.material.InlineSliderDefaults
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.rememberScalingLazyListState

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun MainApp(
    onPingClicked: () -> Unit = {},
    onStartStopClicked: () -> Unit = {},
    onSendClicked: () -> Unit = {},
    onClassifyClicked: () -> Unit = {},
    onMLChange: (Boolean) -> Unit = {},
    onLoadSeriesFromFile: () -> Unit = {},
    onSamplingRateChange: (Float) -> Unit = {},
    started: Boolean = false,
    readyToClassify: Boolean = false,
    value_bpm: Float = 0.0f,
    value_acc: FloatArray = floatArrayOf(0.0f,0.0f,0.0f),
    useMLDefault: Boolean = false,
    samplingRate: Float = 0.0f
)
{
    var checked by remember { mutableStateOf(useMLDefault) }
    var sliderPosition by remember { mutableFloatStateOf(samplingRate) }

    val scalingLazyListState = rememberScalingLazyListState()
    val acc_str = "[" + value_acc[0] + ", " + value_acc[1] + ", " + value_acc[2] + "] "

    Scaffold(
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = scalingLazyListState) },
        timeText = { TimeText() }
    ) {
        ScalingLazyColumn(
            state = scalingLazyListState,
            contentPadding = PaddingValues(
                horizontal = 8.dp,
                vertical = 2.dp
            )
        ) {
            item {
                Text(text = "$value_bpm bpm")
            }
            item {
                Text(text = "$acc_str m/s²")
            }
            item {
                Button(
                    onClick = onPingClicked,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "PING")
                }
            }
            item {
                Button(
                    onClick = onStartStopClicked,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(if (started) Color.Red else Color.Blue)
                ) {
                    if(started)
                        Text(text = "END SESSION")
                    else
                        Text(text = "START SESSION")
                }
            }
            item {
                Button(
                    onClick = onSendClicked,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Send stuff")
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Use ML classifier?\t")
                    Switch(
                        checked = checked,
                        onCheckedChange = {
                            checked = it
                            onMLChange(checked)
                        }
                    )
                }
            }

            item {
                Button (
                    onClick = onClassifyClicked,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = readyToClassify
                ) {
                    Text(text = "Classify last session")
                }
            }

            item {
                Button (
                    onClick = onLoadSeriesFromFile,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Load sample")
                }
            }

            item {
                Text(text = "Sampling rate")
            }

            item {
                InlineSlider(
                    value = sliderPosition,
                    onValueChange = {
                        sliderPosition = it
                        onSamplingRateChange(sliderPosition)
                                    },
                    increaseIcon = { Icon(InlineSliderDefaults.Increase, "Increase") },
                    decreaseIcon = { Icon(InlineSliderDefaults.Decrease, "Decrease") },
                    valueRange = 0f..60f*5,
                    steps = 1 + (60*5)/30,
                    segmented = true
                )
            }

            item {
                Text(
                    text = if(sliderPosition == 0.0f)
                            "MAX ALLOWED"
                        else
                            sliderPosition.toString() + "s"
                )
            }
        }
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun MainAppPreview() {
    MainApp(readyToClassify = true)
}