package com.example.studymate.ui.mainFragments.home

import android.provider.ContactsContract

class Constants {
    companion object {
        // General

        // Geofencing
        private const val GEOFENCE_EXPIRATION_IN_HOURS: Long = 12
        const val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000
        const val GEOFENCE_RADIUS_IN_METERS = 30f
        const val BACKGROUND_LOCATION_PERMISSION_CODE = 2
        var DaysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        var MeetingTimes = listOf("9AM to 10AM", "10AM to 11AM", "11AM to 12PM", "12PM to 1PM", "2PM to 3PM", "3PM to 4PM", "4PM to 5PM", "5PM to 6PM", "6PM to 7PM", "7PM to 8PM", "8PM to 9PM", "9PM to 10PM")

        // Activity recognition
        const val ACTIVITY_RECOGNITION_PERMISSION_CODE = 3
        const val LOCATION_PERMISSION_REQUEST_CODE = 1

        const val WRITE_EXTERNAL_PERMISSION_CODE = 10

        const val MAX_TESTS_NUM = 200 * 60

        const val ALPHA = 0.8
    }

}