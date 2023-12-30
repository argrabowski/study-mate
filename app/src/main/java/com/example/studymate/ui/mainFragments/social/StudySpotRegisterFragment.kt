package com.example.studymate.ui.mainFragments.social

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.studymate.R
import com.example.studymate.data.model.ApplicationState
import com.example.studymate.data.model.StudySpot
import com.example.studymate.databinding.FragmentStudySpotRegisterBinding
import com.example.studymate.ui.LoadingDialog
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private const val TAG = "REGISTER_STUDY_SPOT_FRAGMENT"
private var RADIUS_SIZES = listOf("Small", "Medium", "Large")

class StudySpotRegisterFragment : Fragment() {
    private lateinit var loadingDialog: LoadingDialog
    private var _binding: FragmentStudySpotRegisterBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore
    private var subBtn: MenuItem? = null
    private var isChecked = false

    private val userState = ApplicationState
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentStudySpotRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireActivity())
        binding.createStudySpotToolbar.setNavigationOnClickListener{
            navigateBack()
        }

        val parentLocationPlaces = userState.getStudySpots().value?.filter { spot -> spot.parentLocation == null }
            ?.map{ spot -> spot.name}

        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
                checkFormData(
                    binding.studySpotNameInput.text.toString(),
                    binding.studySpotDescriptionInput.text.toString(),
                    binding.selectRadiusInput.text.toString(),
                    binding.selectParentInput.text.toString(),
                    binding.longitudeInput.text.toString(),
                    binding.latitudeInput.text.toString()
                )
            }
        }

        binding.hasParentCheck.setOnCheckedChangeListener { buttonView, isChecke ->
            isChecked = isChecke
            Log.d("WOWO", "Is checked: $isChecke")
            if(isChecke) binding.selectParentLayout.visibility = View.VISIBLE
            else binding.selectParentLayout.visibility = View.GONE
        }

        binding.studySpotNameInput.addTextChangedListener(afterTextChangedListener)
        binding.studySpotDescriptionInput.addTextChangedListener(afterTextChangedListener)
        binding.selectRadiusInput.addTextChangedListener(afterTextChangedListener)
        binding.longitudeInput.addTextChangedListener(afterTextChangedListener)
        binding.latitudeInput.addTextChangedListener(afterTextChangedListener)

        val parentAdapter: ArrayAdapter<String> = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_account_role,
            parentLocationPlaces ?: ArrayList<String>()
        )

        binding.selectParentInput.setAdapter(parentAdapter)

        val locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Add permissions calls

            return
        }
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val longitude: Double? = location?.longitude
        val latitude: Double? = location?.latitude

        if(longitude != null && latitude != null){
            binding.latitudeInput.setText(latitude.toString())
            binding.longitudeInput.setText(longitude.toString())
        }

        binding.createStudySpotToolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.toolbar_add_study_spot_submit -> {
                    val name = binding.studySpotNameInput.text.toString()
                    val description  = binding.studySpotDescriptionInput.text.toString()
                    val parentLocationTV = binding.selectParentInput.text
                    val parentLocation = if(parentLocationTV.isEmpty() || parentLocationTV.isBlank()) null else parentLocationTV.toString()
                    val maxOccupantsTV = binding.studySpotMaxOccupantsInput.text.toString()
                    val maxOccupants = if(maxOccupantsTV.isEmpty() || maxOccupantsTV.isBlank()) null else maxOccupantsTV.toInt()
                    val lon = binding.longitudeInput.text.toString()
                    val lat = binding.latitudeInput.text.toString()
                    val latLon = "$lat,$lon "

                    Log.d(TAG, "Max occupants is: $maxOccupants")
                    val radiusSize = binding.selectRadiusInput.text.toString().toIntOrNull()
                    subBtn = it
                    if(userState.getAccountRole().value?.lowercase() != "admin") {
                        Toast.makeText(context, "Only Admins can register new study spots", Toast.LENGTH_SHORT).show()
                    }
                    else if(checkFormData(name, description, binding.selectRadiusInput.text.toString(), parentLocation, lon, lat)) {
                        it.isEnabled = true;
                        it.icon?.alpha = 255;
                        val newStudySpot = StudySpot(name, description, userState.getLocation().value, parentLocation, latLon, userState.getUsername().value, radiusSize, maxOccupants, 0, ArrayList<String>())
                        createNewStudySpot(newStudySpot)
                    }
                    else {
                        it.isEnabled = false;
                        it.icon?.alpha = 130;
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun checkFormData(name:String, description: String, radius: String, parentLocation: String?, lon: String, lat:String): Boolean {
        binding.studySpotNameInput.error = null
        binding.studySpotDescriptionInput.error = null
        if(subBtn != null){
            subBtn!!.isEnabled = false;
            subBtn!!.icon?.alpha = 130;
        }
        return if(name.isBlank() || name.isEmpty()) {
            binding.studySpotNameInput.error = "Enter a name"
            false
        } else if(description.isBlank() || description.isEmpty() || description.length > 20) {
            binding.studySpotDescriptionInput.error = "Enter a valid description"
            false
        }
        else if(lat.isBlank() || lat.isEmpty()) {
            binding.latitudeInput.error = "Enter a valid latitude"
            false
        }
        else if(lon.isBlank() || lon.isEmpty()) {
            binding.longitudeInput.error = "Enter a valid longitude"
            false
        }
        else if(isChecked && parentLocation != null && (parentLocation.isEmpty() || parentLocation.isBlank())) {
            //binding.studySpotDescriptionInput.error = "Enter a valid description"
            false
        }
        else if(radius.isBlank() || radius.isEmpty()) {
            binding.selectRadiusInput.error = "Enter a radius size"
            false
        }
        else if(radius.toInt() > 50) {
            binding.selectRadiusInput.error = "Max radius size is 50 meters"
            false
        }
        else {
            if(subBtn != null) {
                subBtn!!.isEnabled = true;
                subBtn!!.icon?.alpha = 255;
            }
            true
        }

    }

    private fun createNewStudySpot(studySpot: StudySpot) {
        loadingDialog.show()
        db.collection("studySpots").whereEqualTo("location", userState.getLocation().value).whereEqualTo("name", studySpot.name).get()
            .addOnCompleteListener { task ->
                Log.d(TAG, "Done getting study spot info")

                if(task.isSuccessful && !task.result.isEmpty) {
                    Log.d(TAG, "study spot already exists")
                    loadingDialog.dismiss()
                    Toast.makeText(context, "This study spot already exists", Toast.LENGTH_SHORT).show()
                }
                else if(task.isSuccessful && task.result.size() >= 100) {
                    Log.d(TAG, "max study spot amount hit")
                    loadingDialog.dismiss()
                    Toast.makeText(context, "Error: Max number of study spots allowed already reached", Toast.LENGTH_SHORT).show()
                }
                else {
                    val studySpotToAdd = hashMapOf(
                        "name" to studySpot.name,
                        "description" to studySpot.description,
                        "location" to studySpot.location,
                        "parentLocation" to studySpot.parentLocation,
                        "radius" to studySpot.radius,
                        "maxOccupants" to studySpot.maxOccupants,
                        "occupants" to studySpot.occupants,
                        "lat_lng" to studySpot.lat_lng,
                        "members" to studySpot.members
                    )
                    db.collection("studySpots").add(studySpotToAdd)
                        .addOnCompleteListener { task ->
                            Log.d(TAG, "Done setting study spot")
                            loadingDialog.dismiss()
                            if (task.isSuccessful) {
                                // Document found in the offline cache
                                Log.d(TAG, "study spot registered")
                                ApplicationState.addStudySpot(studySpot)
                                navigateBack()
                                Log.d(TAG, "Cached document data: ${task.result}")
                            } else {
                                Log.d(TAG, "Unable to register study spot")
                                Toast.makeText(context, "Unable to create study spot, please try again later", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
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