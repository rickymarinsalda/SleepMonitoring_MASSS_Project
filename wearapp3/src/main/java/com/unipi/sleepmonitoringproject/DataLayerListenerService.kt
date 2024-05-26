package com.unipi.sleepmonitoringproject

import android.annotation.SuppressLint
import android.content.Intent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class DataLayerListenerService : WearableListenerService() {
    private val messageClient by lazy { Wearable.getMessageClient(this) }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @SuppressLint("VisibleForTests")
    override fun onDataChanged(dataEvents: DataEventBuffer) {
       super.onDataChanged(dataEvents)

        // Quando ricevi l'evento onDataChanged, invia il broadcast


        for (event in dataEvents) {
            val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
            val action = dataMap.getString("mossa")
            if ("pong" == action) {
                // Quando ricevi un evento onDataChanged con l'azione "pong", invia il broadcast
                val intent = Intent("com.unipi.sleepmonitoringproject.ACTION")
                sendBroadcast(intent)
            }
        }


    }
}