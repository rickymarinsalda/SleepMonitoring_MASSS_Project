package com.unipi.sleepmonitoringproject.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.ScalingLazyColumn
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
    started: Boolean = false,
    value_bpm: Float = 0.0f,
    value_acc: FloatArray = floatArrayOf(0.0f,0.0f,0.0f)
)
{
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
        }
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun MainAppPreview() {
    MainApp()
}