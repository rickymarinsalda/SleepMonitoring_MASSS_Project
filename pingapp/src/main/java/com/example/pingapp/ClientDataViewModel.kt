package com.example.pingapp

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.PutDataMapRequest
import com.unipi.sleepmonitoring_masss_library.TimeSeries

class ClientDataViewModel :
    ViewModel(),
    DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener
{
    var updateGUI: (series: TimeSeries) -> Unit = {}

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.i("DATA", "ARGGG")
        dataEvents.forEach {
            Log.i("DATA", "$it from ${it.dataItem.uri.path}")
            if(it.dataItem.uri.path != "/ping-pong")
                return

            val dataMap = DataMapItem.fromDataItem(it.dataItem).dataMap

            // Not a time-series
            if(! dataMap.containsKey("start")) {
                Log.i("DATA", "Not a time-series!")
                return
            }

            updateGUI(TimeSeries.deserializeFromGoogle(dataMap.getDataMap("data_accel")!!))
        }
    }

    override fun onMessageReceived(p0: MessageEvent) {
        TODO("Not yet implemented")
    }

    override fun onCapabilityChanged(p0: CapabilityInfo) {
        TODO("Not yet implemented")
    }

}