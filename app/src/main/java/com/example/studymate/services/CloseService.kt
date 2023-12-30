package com.example.studymate.services

import android.app.Service
import android.app.Service.START_NOT_STICKY
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.studymate.data.model.ApplicationState
import com.example.studymate.data.model.StudySpot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

private const val TAG = "ClearService"

class CloseService : Service() {

    private val db = Firebase.firestore

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("ClearService", "Service Started")
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ClearService", "Service Destroyed")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.e("ClearService", "END")
        //Code here
        Log.i(TAG, "onDestroy is called, ${ApplicationState.getLocation().value}");

        db.collection("studySpots").whereEqualTo("location", ApplicationState.getLocation().value).get()
            .addOnCompleteListener { task ->
                Log.i(TAG, "GOT TASK");
                if(!task.isSuccessful){
                    Log.d(TAG, "Unable to fetch study spot data on destroy")
                    return@addOnCompleteListener
                }
                else {
                    val documents = task.result.documents
                    Log.i(TAG, "The result is: $documents");
                    for(document in documents){
                        val studySpot = document.toObject<StudySpot>()
                        if (studySpot != null) {
                            if(studySpot.occupants != 0) {
                                db.collection("studySpots").document(document.id).update("occupants", FieldValue.increment((-1).toDouble()))
                                    .addOnSuccessListener {
                                        Log.d(TAG, "Successful decrement")
                                    }
                            }
                        }

                    }

                }
            }
    }
}