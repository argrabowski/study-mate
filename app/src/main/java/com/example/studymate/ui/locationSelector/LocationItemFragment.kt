package com.example.studymate.ui.locationSelector

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studymate.BuildConfig
import com.example.studymate.R
import com.example.studymate.databinding.FragmentLocationListBinding
import com.example.studymate.ui.LoadingDialog
import com.example.studymate.ui.register.RegisterFragment
import com.example.studymate.ui.register.RegisterViewModel
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.gms.maps.model.LatLng
import java.util.*
import kotlin.collections.ArrayList
import android.content.Context
import android.location.LocationListener
import android.location.LocationManager
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest


private const val TAG = "LOCATION_SELECT_FRAGMENT"

class LocationItemFragment : Fragment() {
    private var _binding: FragmentLocationListBinding? = null
    private val binding get() = _binding!!
    private var list = ArrayList<Location>()
    private var userLocation = LatLng(0.0, 0.0)
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var adapter: LocationAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationListBinding.inflate(inflater, container, false)
        return binding.root
    }
    private val viewModel: RegisterViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Places.initialize(requireContext(), BuildConfig.MAPS_API_KEY)

        // Create a new PlacesClient instance

        loadingDialog = LoadingDialog(requireActivity())
        // Create a new PlacesClient instance
        val placesClient: PlacesClient = Places.createClient(context)

        println("bobert here")
        nearbySearch()
        adapter = LocationAdapter(list)
        val rv = binding.list


        rv.adapter = adapter;
        adapter.setOnItemClickListener(object : LocationAdapter.OnItemClickListener {
            override fun onItemClick(itemView: View?, location: Location) {
                //Toast.makeText(context, "${location.address} was clicked!", Toast.LENGTH_SHORT).show()
                viewModel.setLocation(location)
                navigateBack()
            }
        })
        // Set the adapter
        rv.layoutManager = LinearLayoutManager(context);

        val autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.ICON_URL,Place.Field.ICON_BACKGROUND_COLOR, Place.Field.ADDRESS_COMPONENTS, Place.Field.TYPES))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onError(status: Status) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "An error occurred: $status")
            }

            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: $place")

                if(place.types?.contains(Place.Type.UNIVERSITY) == false) {
                    Toast.makeText(context, "Must be a University", Toast.LENGTH_SHORT).show()
                    return
                }
                else if(list.any{ it.id == place.id}) {
                    Toast.makeText(context, "Location already in list", Toast.LENGTH_SHORT).show()
                    return
                }
                val loc = Location(place.id, place.name, place.latLng, place.types, place.iconUrl, place.iconBackgroundColor, place.addressComponents, place.address)
                adapter.addItem(loc)

            }


        })


        binding.myToolbar.setNavigationOnClickListener{
            navigateBack()
        }


    }

    private fun nearbySearch() {
        loadingDialog.show()

        val placesClient = Places.createClient(context);

        val placesField = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.TYPES, Place.Field.ICON_URL, Place.Field.ICON_BACKGROUND_COLOR, Place.Field.ADDRESS);

        getUserLocation()

        val radius = 10000
        val type = "university"

        val request = FindCurrentPlaceRequest.newInstance(placesField)

        if (context?.let { ActivityCompat.checkSelfPermission(it, android.Manifest.permission.ACCESS_FINE_LOCATION) } == PackageManager.PERMISSION_GRANTED) {
            val task = placesClient.findCurrentPlace(request)
            println("bobert before task")
            task.addOnSuccessListener { response ->
                val placesList = ArrayList<Location>()
                for (placeLikelihood in response.placeLikelihoods) {
                    val place = placeLikelihood.place
                    if (place.types.contains(Place.Type.UNIVERSITY)) {
                        println("found a place")
                        println(place)
                        val loc = Location(place.id, place.name, place.latLng, place.types, place.iconUrl, place.iconBackgroundColor, place.addressComponents, place.address)
                        placesList.add(loc)
                    }
                }
                adapter.filterList(placesList)
                list = placesList
            }
            task.addOnFailureListener { exception ->
                exception.printStackTrace()
                println("task failed")
            }
            task.addOnCompleteListener {
                println("task complete")
                loadingDialog.dismiss()
            }
        } else {
            println("bobert dang")
            loadingDialog.dismiss()
        }



    }

    private fun getUserLocation() {
        if (context?.let { ActivityCompat.checkSelfPermission(it, android.Manifest.permission.ACCESS_FINE_LOCATION) } == PackageManager.PERMISSION_GRANTED) {
            // The user has granted location permissions
            // Get the user's location
            val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val locationListener = object : LocationListener {

                override fun onLocationChanged(location: android.location.Location) {
                    val userLatitude = location.latitude
                    val userLongitude = location.longitude

                    val userLocation = LatLng(userLatitude, userLongitude)
                }

                override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

                override fun onProviderEnabled(provider: String) {}

                override fun onProviderDisabled(provider: String) {}}
        } else {
            // The user has not granted location permissions
            // Request location permissions from the user
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

    }

    private fun navigateBack() {
        var fragment: Fragment = RegisterFragment()
        val ft: FragmentTransaction = parentFragmentManager.beginTransaction()
        ft.replace(R.id.auth_fragment, fragment)
        ft.commit()
    }


}