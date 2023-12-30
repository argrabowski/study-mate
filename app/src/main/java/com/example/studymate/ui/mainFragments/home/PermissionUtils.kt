package com.example.studymate.ui.mainFragments.home
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment

object PermissionUtils {


    var LOCATION_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    var BACKGROUND_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    var ACTIVITY_RECOGNITION_PERMISSIONS = arrayOf(Manifest.permission.ACTIVITY_RECOGNITION)

    fun hasPermission(context:Context?, permissions: Array<String>): Boolean {
        if( context != null){
            for(value in permissions){
                if(ActivityCompat.checkSelfPermission(context, value) != PackageManager.PERMISSION_GRANTED)
                    return false
            }
        }
        return true
    }

    fun requestBackgoundLocationPermission(context: Context, activity: Activity) {
        AlertDialog.Builder(context)
            .setTitle("Permission Needed!")
            .setMessage("Background Location Permission Needed!, tap \"Allow all time in the next screen\"")
            .setPositiveButton("OK",
                DialogInterface.OnClickListener { dialog, which ->
                    ActivityCompat.requestPermissions(
                        activity, arrayOf(
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ), Constants.BACKGROUND_LOCATION_PERMISSION_CODE
                    )
                })
            .setNegativeButton("CANCEL", DialogInterface.OnClickListener { dialog, which ->
                // User declined for Background Location Permission.
            })
            .create().show()
    }

    fun requestLocationPermissions(context: Context, activity: Activity) {
        AlertDialog.Builder(context)
            .setTitle("Permission Needed!")
            .setMessage("Location Permission Needed!")
            .setPositiveButton("OK",
                DialogInterface.OnClickListener { dialog, which ->
                    ActivityCompat.requestPermissions(
                        activity, LOCATION_PERMISSIONS, Constants.LOCATION_PERMISSION_REQUEST_CODE
                    )
                })
            .setNegativeButton("CANCEL", DialogInterface.OnClickListener { dialog, which ->
                // User declined for Background Location Permission.
            })
            .create().show()
    }

    fun requestActivityRecognitionPermission(context: Context, activity: Activity) {
        AlertDialog.Builder(context)
            .setTitle("Permission Needed!")
            .setMessage("Activity Recognition Permission Needed!")
            .setPositiveButton("OK",
                DialogInterface.OnClickListener { dialog, which ->
                    ActivityCompat.requestPermissions(
                        activity, arrayOf(
                            Manifest.permission.ACTIVITY_RECOGNITION
                        ), Constants.ACTIVITY_RECOGNITION_PERMISSION_CODE
                    )
                })
            .setNegativeButton("CANCEL", DialogInterface.OnClickListener { dialog, which ->
                // User declined for Background Location Permission.
            })
            .create().show()
    }


}