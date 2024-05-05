package com.example.pingapp

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.PutDataMapRequest

class ClientDataViewModel :
    ViewModel(),
    DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener
{
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.i("DATA", "ARGGG")
        dataEvents.forEach {
            Log.i("DATA", "$it")
        }
    }

    override fun onMessageReceived(p0: MessageEvent) {
        TODO("Not yet implemented")
    }

    override fun onCapabilityChanged(p0: CapabilityInfo) {
        TODO("Not yet implemented")
    }

}