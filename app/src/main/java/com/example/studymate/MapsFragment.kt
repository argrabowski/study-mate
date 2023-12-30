package com.example.studymate

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.studymate.data.model.ApplicationState
import com.example.studymate.databinding.FragmentMapsBinding
import com.example.studymate.ui.mainFragments.home.Constants.Companion.GEOFENCE_RADIUS_IN_METERS
import com.example.studymate.ui.mainFragments.home.Constants.Companion.LOCATION_PERMISSION_REQUEST_CODE
import com.example.studymate.ui.mainFragments.home.PermissionUtils.LOCATION_PERMISSIONS
import com.example.studymate.ui.mainFragments.home.PermissionUtils.hasPermission
import com.example.studymate.ui.mainFragments.social.SocialFragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*


private const val TAG = "MAPS_FRAGMENT"


class MapsFragment : Fragment(),
    ActivityCompat.OnRequestPermissionsResultCallback {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!
    private var userState = ApplicationState
    private lateinit var map: GoogleMap
    private var initialMapZoom: Boolean = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        binding.mapToolbar.setNavigationOnClickListener {
            navigateBack()
        }

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        Log.d(TAG, "IN MAPs")

        mapFragment.getMapAsync(OnMapReadyCallback { googleMap ->
            // When map is loaded
            userState.getStudySpots().value?.forEach{
                val separated: List<String> = it.lat_lng.split(",")
                val lat = separated[0].toDouble()
                val lng = separated[1].toDouble()
                map = googleMap
                Log.d(TAG, "Lat $lat and long: $lng")
                val loc = LatLng(lat, lng)
                //map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15f))
                map.addCircle(
                    CircleOptions()
                        .center(LatLng(lat, lng))
                        .radius(it.radius.toDouble())
                        .strokeColor(ContextCompat.getColor(requireContext(), R.color.black))
                        .fillColor(ContextCompat.getColor(requireContext(), if(it.parentLocation == null) R.color.transparent_blue else R.color.transparent_salmon))
                )
            }
            checkLocationPermissions()
        })
    }

    @SuppressLint("MissingPermission")
    private fun checkLocationPermissions() {

        // 1. Check if permissions are granted, if so, enable the my location layer
        if(hasPermission(requireContext(), LOCATION_PERMISSIONS)) {
            map.isMyLocationEnabled = true
            Log.d("PERMISSIONS_CHECK", "OPTION 1")
            startLocationUpdates()
            //checkActivityRecognitionPermissions()
            return
        }
        // 2. Otherwise, request permission
        Log.d("PERMISSIONS_CHECK", "OPTION 2")
        ActivityCompat.requestPermissions(requireActivity(), LOCATION_PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE)
        //requestLocationPermissions(this, this@MainActivity,)
    }

    private fun createLocationRequest(): LocationRequest? {
        return LocationRequest.create().apply {
            interval = 100000
            fastestInterval = 5000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun startLocationUpdates() {
        Log.d("Main", "Starting location updates listener")
        val locationReq = createLocationRequest()
        var locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                Log.d("MAIN", "LOCATION RESULT")
                locationResult ?: return
                for (location in locationResult.locations){
                    // Update UI with location data
                    val loc = LatLng(location.latitude, location.longitude)
                    if(!initialMapZoom) {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15f))
                        initialMapZoom = true
                    }

                }
            }
        }
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        if (locationReq != null) {
            fusedLocationClient.requestLocationUpdates(locationReq,
                locationCallback,
                Looper.getMainLooper())
        }

    }



    private fun navigateBack() {
        if (parentFragmentManager.backStackEntryCount > 1)
            parentFragmentManager.popBackStack()
        else {
            val ft: FragmentTransaction = parentFragmentManager.beginTransaction()
            ft.replace(R.id.main_fragment, SocialFragment())
            ft.commit()
        }
    }
}