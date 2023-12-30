package com.example.studymate.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.example.studymate.data.model.ApplicationState
import com.example.studymate.data.model.StudySpot
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

private const val TAG = "GEO_RECEIVER"
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private val db = Firebase.firestore
    private val userState = ApplicationState

    @Override
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "in reciever")
        Log.e(TAG, "The intent is $intent")
        if(intent == null) return
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        Log.e(TAG, "The geo event is $geofencingEvent")
        if (geofencingEvent != null) {
            if (geofencingEvent.hasError()) {
                val errorMessage = GeofenceStatusCodes
                    .getStatusCodeString(geofencingEvent.errorCode)
                Log.e(TAG, errorMessage)
                return
            }
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent?.geofenceTransition
//        val message = "You have $geofenceTransition the geofence"
//        val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
//        toast.show()
        // Test that the reported transition was of interest.
        Log.e(TAG, "The geo transition is $geofenceTransition")

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            val triggeringGeofence = geofencingEvent?.triggeringGeofences

            val idsList = ArrayList<Any>()
            for (geofence in triggeringGeofence!!) {
                idsList.add(geofence.requestId)
                Log.d(TAG, "The id is:  ${geofence.requestId}")
                Log.d(TAG, "The location is: ${userState.getLocation().value}")


                takeGeoAction(geofenceTransition, geofence.requestId)
            }
            val triggeringGeofenceIdsString: String =
                TextUtils.join(", ", idsList)

            sendMessage(context, triggeringGeofenceIdsString, geofenceTransition)

        } else {
            // Log the error.
            Log.e(TAG, "ERROR IN RECEIVER")
        }
    }

    private fun takeGeoAction(action: Int, requestId: String){
        db.collection("studySpots").whereEqualTo("location", userState.getLocation().value)
            .whereEqualTo("name", requestId).get()
            .addOnCompleteListener {task ->
                Log.d(TAG, "Done Getting Item")
                if(!task.isSuccessful){
                    Log.d(TAG, "Unable to fetch study spot data, please reload")
                    return@addOnCompleteListener
                }
                else {
                    val documents = task.result.documents
                    Log.d(TAG, "The docs are: $documents")
                    for(document in documents){
                        val studySpot = document.toObject<StudySpot>()
                        if (studySpot != null) {
                            val username = userState.getUsername().value
                            if(studySpot.members.contains(username) && action != Geofence.GEOFENCE_TRANSITION_EXIT) {
                                Log.d(TAG, "USER_ENTER But already in list")
                            }
                            else if(!studySpot.members.contains(username) && action != Geofence.GEOFENCE_TRANSITION_ENTER) {
                                Log.d(TAG, "USER_EXIT But not in list")
                            }
                            else {
                                Log.d(TAG, "Taking proper geo action")
                                var actionVal = if(action == Geofence.GEOFENCE_TRANSITION_EXIT) -1 else 1
                                var updateMembersAction = if(action == Geofence.GEOFENCE_TRANSITION_EXIT) FieldValue.arrayRemove(username) else FieldValue.arrayUnion(username)
                                if(studySpot.occupants == 0 && actionVal == -1) actionVal = 0
                                db.collection("studySpots").document(document.id)
                                    .update("occupants", FieldValue.increment(actionVal.toDouble()), "members", updateMembersAction)
                                    .addOnSuccessListener {
                                        Log.d(TAG, "Done incrementing occupants for $requestId")
                                        userState.getLocation().value?.let { it1 ->
                                            userState.updateStudySpaceOccupants(
                                                requestId,
                                                it1,
                                                actionVal,
                                            )
                                        }
                                    }
                                    .addOnFailureListener {
                                        Log.d(TAG, "Error incrementing occupants for $requestId")
                                        Log.d(TAG, it.toString())
                                    }
                            }
                        }
                    }
                }
            }
    }

    private fun sendMessage(context: Context?, location: String, geofenceTransition: Int){
        val message = "You are now ${if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) "exiting" else "entering"} the $location Geofence(s)"
        val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast.show()
    }
}