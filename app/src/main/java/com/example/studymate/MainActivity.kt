package com.example.studymate

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.studymate.data.model.*
import com.example.studymate.databinding.ActivityMainBinding
import com.example.studymate.services.CloseService
import com.example.studymate.services.GeofenceBroadcastReceiver
import com.example.studymate.ui.LoadingDialog
import com.example.studymate.ui.mainFragments.home.Constants
import com.example.studymate.ui.mainFragments.home.Department
import com.example.studymate.ui.mainFragments.home.HomeFragment
import com.example.studymate.ui.mainFragments.home.PermissionUtils
import com.example.studymate.ui.mainFragments.profile.ProfileFragment
import com.example.studymate.ui.mainFragments.social.SocialFragment
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

private const val TAG = "MAIN_ACTIVITY"
class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var loadingDialog: LoadingDialog
    private val db = Firebase.firestore
    private val userState = ApplicationState
    private var mGeofenceList: ArrayList<Geofence>? = null
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        loadingDialog = LoadingDialog(this)
        geofencingClient = LocationServices.getGeofencingClient(this)
        val view = binding.root
        bottomNav = binding.mainBottomNavigation
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mGeofenceList = ArrayList()
        if(userState.getCourses().value ==null || userState.getStudySpots().value ==null || userState.getFiles().value ==null) fetchAppData()

        userState.getIsLoading().observe(this){
            if(it) loadingDialog.show()
            else loadingDialog.dismiss()
        }

        startService(Intent(baseContext, CloseService::class.java))
        bottomNav.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.bottom_menu_profile -> {
                    swapFragment("Profile")
                    true
                }
                R.id.bottom_menu_home -> {
                    swapFragment("Home")
                    true
                }
                R.id.bottom_menu_tutor -> {
                    swapFragment("Tutor")
                    true
                }
                else -> false
            }
        }


        val mapFragment = this.supportFragmentManager
            .findFragmentById(R.id.main_map) as SupportMapFragment
        Log.d(TAG, "IN MAPs")

        mapFragment.getMapAsync(OnMapReadyCallback { googleMap ->
            // When map is loaded
            map = googleMap
            checkLocationPermissions()
            Log.d(TAG, "UPDATES")
        })


        setContentView(view)
        swapFragment("Home")
    }

    private val getGeofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }


    @SuppressLint("MissingPermission")
    private fun checkLocationPermissions() {

        // 1. Check if permissions are granted, if so, enable the my location layer
        if(PermissionUtils.hasPermission(this, PermissionUtils.LOCATION_PERMISSIONS)) {
            map.isMyLocationEnabled = true
            Log.d("PERMISSIONS_CHECK", "OPTION 1")
            //startLocationUpdates()
            return
        }
        // 2. Otherwise, request permission
        Log.d("PERMISSIONS_CHECK", "OPTION 2")
        ActivityCompat.requestPermissions(this,
            PermissionUtils.LOCATION_PERMISSIONS,
            Constants.LOCATION_PERMISSION_REQUEST_CODE
        )
        //requestLocationPermissions(this, this@MainActivity,)
    }

    private fun addGeofencesFun() {
        //TODO: Add permissions check
        Log.d("MAIN", "Checking permissions  IN ADD GEOFENCES")
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("GEO_MAIN", "MISSING FINE PERMISSIONS")
            return
        }
        if(ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            Log.d("GEO_MAIN", "MISSING BACKGROUND PERMISSIONS")
            return
        }
        Log.d("MAIN", "WORKING IN ADD GEOFENCES")
        var geoReq = getGeofencingRequest()
        var geoIntent = getGeofencePendingIntent
        if(geoReq != null && geoIntent != null){
            Log.d("MAIN_GEO", "ADDING")
            geofencingClient.addGeofences(geoReq, geoIntent)
                .addOnCompleteListener {task->
                    Log.d("GEO_MAIN", "IN ON COMPLETE")
                    if (task.isSuccessful) {

                        Log.d("MAIN", "TASK IS DONE")
                        Log.d("MAIN", task.toString())

                    } else {
                        // Get the status code for the error and log it using a user-friendly message.
                        val e = task.exception


                        Log.w("GEO_MAIN", "ERROR IN ON COMPLETE")
                        Log.w("GEO_MAIN", e)
                        Log.d("GEO_MAIN", "Checking if ApiException ${(e is ApiException)}")
                        if(e is ApiException){
                            Log.d("GEO_MAIN", "Error status code is: ${e.statusCode}")
                            when (e.statusCode) {
                                GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> Log.d("GEO_MAIN", "GEO UNAVAL")
                                GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> Log.d("GEO_MAIN", "TOO MANY REQ")
                                GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> Log.d("GEO_MAIN", "TOO MANY PENDING INTENTS")
                                else -> Log.d("GEO_MAIN", "Unknown API Exception")
                            }
                        }
                    }
                }
                .addOnSuccessListener {
                    Log.d("GEO_MAIN", "IN ON SUCCESS")
                }
                .addOnFailureListener { Log.d("GEO_MAIN", "IN ON FAILURE") }
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkGeofencingPermissions() {
        Log.d("PERMISSIONS_CHECK", "Checking GEo")
        // 1. Check if permissions are granted, if so, enable the my location layer
        if(PermissionUtils.hasPermission(this, PermissionUtils.BACKGROUND_PERMISSIONS) || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Log.d("PERMISSIONS_CHECK", "OPTION GEO 1")
            addGeofencesFun()
            return
        }
        else {
            // 2. Otherwise, request permission
            Log.d("PERMISSIONS_CHECK", "OPTION GEO 2")
            if(!PermissionUtils.hasPermission(
                    this,
                    PermissionUtils.BACKGROUND_PERMISSIONS
                )
            ){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    Log.d("PERMISSIONS_CHECK", "OPTION WOW")
                    PermissionUtils.requestBackgoundLocationPermission(
                        this,
                        this
                    )
                } else {
                    Log.d("PERMISSIONS_CHECK", "OPTION 12312321")
                    ActivityCompat.requestPermissions(this,
                        PermissionUtils.BACKGROUND_PERMISSIONS,
                        Constants.BACKGROUND_LOCATION_PERMISSION_CODE
                    )
                }
            }
        }
    }

    private fun getGeofencingRequest(): GeofencingRequest? {
        val builder = GeofencingRequest.Builder()
        if((mGeofenceList?.size ?: -1) <= 0) {
            Log.d(TAG, "NO GEO FENCES IN LIST")
            return null
        }
        Log.d("MAIN", "In GEOFENCES REQUEST")
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)

        // Add the geofences to be monitored by geofencing service.
        Log.d("GEO_REQ", mGeofenceList.toString())
        builder.addGeofences(mGeofenceList!!)

        // Return a GeofencingRequest.
        Log.d(TAG, "THe array of geo is: $mGeofenceList" )
        return builder.build()
    }



    private fun fetchAppData() {
        //loadingDialog.show()
        userState.setIsLoading(true)
        db.collection("courses").whereEqualTo("location", userState.getLocation().value).get()
            .addOnCompleteListener { task ->
//                loadingDialog.dismiss()
                fetchFiles()
                if(!task.isSuccessful){
                    Toast.makeText(this, "Unable to fetch app data, please reload", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Unable to fetch app data, please reload")
                    //TODO: Implement more error handling
                    return@addOnCompleteListener
                }
                else {
                    val documents = task.result.documents
                    val departmentList = ArrayList<Department>();
                    val departmentListKeys = ArrayList<String>();
                    val newCourses = ArrayList<Course>()
                    for(document in documents){
                        val documentItem = document.toObject<Course>()
                        if (documentItem != null && documentItem.location !== null) {
                            if(documentItem.department !== null && !departmentListKeys.contains(documentItem.department)) {
                                departmentList.add(Department(documentItem.location, documentItem.department))
                                departmentListKeys.add(documentItem.department)
                            }
                            newCourses.add(documentItem)
                        }
                    }
                    userState.setCourses(newCourses)
                }

            }

    }

    private fun fetchFiles() {
        db.collection("files").whereEqualTo("creator", userState.getUsername().value).get()
            .addOnCompleteListener { task ->
                //loadingDialog.dismiss()
                fetchStudyGroups()
                if(!task.isSuccessful){
                    Toast.makeText(this, "Unable to fetch app data, please reload", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Unable to fetch app data, please reload")
                    //TODO: Implement more error handling
                    return@addOnCompleteListener
                }
                else {
                    val documents = task.result.documents
                    val fileList = ArrayList<File>();
                    for(document in documents){
                        val fileItem = document.toObject<File>()
                        if (fileItem != null) fileList.add(fileItem)

                    }
                    userState.setFiles(fileList)
                }

            }

    }

    private fun fetchStudyGroups() {
        db.collection("studyGroups").whereEqualTo("location", userState.getLocation().value).get()
            .addOnCompleteListener {
                fetchStudySpots()
            }
            .addOnSuccessListener {task ->
                val documents = task.documents
                val studyGroups = ArrayList<StudyGroup>();
                for(document in documents){
                    val group = document.toObject<StudyGroup>()
                    if (group != null) {
                        group.id = document.id
                        studyGroups.add(group)
                    }
                }
                userState.setStudyGroups(studyGroups)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Unable to fetch study group data, please reload", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Unable to fetch study group data, please reload")
            }
    }

    private fun fetchStudySpots() {
        db.collection("studySpots").whereEqualTo("location", userState.getLocation().value).get()
            .addOnCompleteListener { task ->
                //loadingDialog.dismiss()
                userState.setIsLoading(false)
                if(!task.isSuccessful){
                    Toast.makeText(this, "Unable to fetch study spot data, please reload", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Unable to fetch study spot data, please reload")
                    //TODO: Implement more error handling
                    return@addOnCompleteListener
                }
                else {
                    val documents = task.result.documents
                    val studySpots = ArrayList<StudySpot>();
                    for(document in documents){
                        val studySpot = document.toObject<StudySpot>()
                        if (studySpot != null) studySpots.add(studySpot)

                    }

                    userState.setStudySpots(studySpots)
                    for(studySpace in studySpots){
                        generateGeofence(studySpace)
                    }
                    checkGeofencingPermissions()

                }

            }

    }


//    override fun onResume() {
//        super.onResume()
//        Log.d(TAG, "ON resume")
//
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            return
//        }
//        fusedLocationClient.lastLocation
//            .addOnSuccessListener { location : Location? ->
//
//                userState.getStudySpots().value?.forEach { it ->
//                    val separated: List<String> = it.lat_lng.split(",")
//                    val lat = separated[0].toDouble()
//                    val lng = separated[1].toDouble()
//                    val baseLocation = Location("")
//                    baseLocation.latitude = lat
//                    baseLocation.longitude = lng
//                    val checkDist = location?.checkIsInBound(it.radius.toDouble(), baseLocation)
//                    if(checkDist == true) {
//                        db.collection("studySpots").whereEqualTo("location", it.location)
//                            .whereEqualTo("name", it.name).get()
//                            .addOnSuccessListener {task ->
//                                val id = task.documents[0].id
//                                db.collection("studySpots").document(id).update("occupants", FieldValue.increment((1).toDouble()))
//                            }
//
//                    }
//                    Log.d(TAG, "The check is: $checkDist")
//                }
//            }
//
//
//    }
//
//    @Override
//    override fun onStop() {
//        super.onStop()
//        Log.i(TAG, "onStop is called");
//        db.collection("studySpots").whereEqualTo("location", "Worcester Polytechnic Institute").get()
//            .addOnCompleteListener { task ->
//                Log.i(TAG, "GOT TASK");
//                if(!task.isSuccessful){
//                    Log.d(TAG, "Unable to fetch study spot data on destroy")
//                    return@addOnCompleteListener
//                }
//                else {
//                    val documents = task.result.documents
//                    Log.i(TAG, "The result is: $documents");
//                    for(document in documents){
//                        val studySpot = document.toObject<StudySpot>()
//                        if (studySpot != null) {
//                            if(studySpot.occupants > 0) {
//                                db.collection("studySpots").document(document.id).update("occupants", FieldValue.increment((-1).toDouble()))
//                                    .addOnSuccessListener {
//                                        Log.d(TAG, "Successful decrement")
//                                    }
//                            }
//                        }
//
//                    }
//
//                }
//            }
//    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == Constants.BACKGROUND_LOCATION_PERMISSION_CODE) {
            for(res in grantResults){
                if(res == PackageManager.PERMISSION_GRANTED){
                    //Toast.makeText(this, "Background Permissions Granted", Toast.LENGTH_SHORT).show()
                    checkGeofencingPermissions()
                }
                else {
                    Toast.makeText(this, "Required Background Permissions Not Granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
        else if(requestCode == Constants.WRITE_EXTERNAL_PERMISSION_CODE) {
            Log.d(TAG, "IN WRITE EXTERNAL!")
        }

    }


    private fun isMarkerOutsideCircle(
        centerLatLng: LatLng,
        draggedLatLng: LatLng,
        radius: Double
    ): Boolean {
        val distances = FloatArray(1)
        Location.distanceBetween(
            centerLatLng.latitude,
            centerLatLng.longitude,
            draggedLatLng.latitude,
            draggedLatLng.longitude, distances
        )
        return radius < distances[0]
    }

    private fun Location.checkIsInBound(radius: Double, center:Location):Boolean {
        return this.distanceTo(center) < radius //Put it in miles
    }

    private fun generateGeofence(studySpot: StudySpot) {
        val separated: List<String> = studySpot.lat_lng.split(",")
        val lat = separated[0]
        val lng = separated[1]
        Log.d(TAG, "lat: $lat and long: $lng" )
        mGeofenceList!!.add(
            Geofence.Builder() // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(studySpot.name) // Set the circular region of this geofence.
                .setCircularRegion(
                    lat.toDouble(),
                    lng.toDouble(),
                    studySpot.radius.toFloat()
                ) // Set the expiration duration of the geofence. This geofence gets automatically
                // removed after this period of time.
                .setLoiteringDelay(1000)
                .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS) // Set the transition types of interest. Alerts are only generated for these
                // transition. We track entry and exit transitions in this sample.
                .setTransitionTypes(
                    Geofence.GEOFENCE_TRANSITION_DWELL or
                            Geofence.GEOFENCE_TRANSITION_ENTER or
                            Geofence.GEOFENCE_TRANSITION_EXIT
                ) // Create the geofence.
                .build()
        )
    }


    private fun swapFragment(target: String) {
        var fragment: Fragment = when(target) {
            "Profile" -> ProfileFragment()
            "Tutor" -> SocialFragment()
            "Home" -> HomeFragment()
            else -> return
        }

        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.replace(R.id.main_fragment, fragment)
        ft.commit()
    }
}