package com.example.studymate.ui.mainFragments.social

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.studymate.MapsFragment
import com.example.studymate.R
import com.example.studymate.data.model.ApplicationState
import com.example.studymate.data.model.StudySpot
import com.example.studymate.databinding.FragmentSocialBinding
import com.example.studymate.ui.LoadingDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase


private const val TAG = "SOCIAL_FRAGMENT"
class SocialFragment : Fragment() {

    private var _binding: FragmentSocialBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: StudySpotAdapter
    private lateinit var fAuth: FirebaseAuth
    private var studySpots = ArrayList<StudySpot>();
    private var allStudySpots = ArrayList<StudySpot>();
    private lateinit var loadingDialog: LoadingDialog
    private val userState = ApplicationState
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSocialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = binding.socialSpotsRv

        val parentLocationPlaces = userState.getStudySpots().value?.filter { spot -> spot.parentLocation == null }
        if(parentLocationPlaces != null) {
            studySpots = parentLocationPlaces as  ArrayList<StudySpot>
        }

        userState.getStudySpots().observe(viewLifecycleOwner) {
            if(it == null) return@observe
            allStudySpots = it
            val parentStudySpots = userState.getStudySpots().value?.filter { spot -> spot.parentLocation == null } as ArrayList<StudySpot>
            studySpots = parentStudySpots
            adapter.filterList(studySpots, allStudySpots)
        }

        val swipeContainer = binding.socialSwipeContainer

        swipeContainer.setOnRefreshListener { // Your code to refresh the list here.
            // Make sure you call swipeContainer.setRefreshing(false)
            // once the network request has completed successfully.
            //fetchStudySpots()
            binding.socialSwipeContainer.isRefreshing = false;
        }

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light);

        val mapFAB = binding.viewMap
        mapFAB.setOnClickListener {
            navigateToMap()
        }

        binding.socialToolbar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId) {
                R.id.toolbar_register_study_spot -> {
                    val ft: FragmentTransaction = parentFragmentManager.beginTransaction()
                    ft.replace(R.id.main_fragment, StudySpotRegisterFragment())
                    ft.commit()
                    ft.addToBackStack(null)
                    true
                }
                else -> false

            }
        }

        loadingDialog = LoadingDialog(requireActivity())
        Log.d(TAG, "The app study spot date is: " + userState.getStudySpots().value)
        if(userState.getStudySpots().value ==null) fetchStudySpots()
        else if(userState.getStudySpots().value !== null) {
            allStudySpots = userState.getStudySpots().value!!
            val parentStudySpots = userState.getStudySpots().value?.filter { spot -> spot.parentLocation == null }
            studySpots = parentStudySpots as ArrayList<StudySpot>
        }

        adapter = StudySpotAdapter(studySpots, allStudySpots)
        rv.adapter = adapter;
        adapter.setOnItemClickListener(object : StudySpotAdapter.OnItemClickListener {
            override fun onItemClick(itemView: View?, studySpot: StudySpot) {
                //Toast.makeText(context, "${studySpot.name} was clicked!", Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun fetchStudySpots() {
        loadingDialog.show()
        db.collection("studySpots").whereEqualTo("location", userState.getLocation().value).get()
            .addOnCompleteListener { task ->
                loadingDialog.dismiss()
                binding.socialSwipeContainer.isRefreshing = false;
                if(!task.isSuccessful){
                    Toast.makeText(context, "Unable to fetch study spot data, please reload", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Unable to fetch study spot data, please reload")
                    //TODO: Implement more error handling
                    return@addOnCompleteListener
                }
                else {
                    Log.d(TAG, "Done fetching study spots")
                    val documents = task.result.documents
                    val studySpots = ArrayList<StudySpot>()
                    val parentStudySpots = ArrayList<StudySpot>()
                    for(document in documents){
                        val studySpot = document.toObject<StudySpot>()
                        if (studySpot != null) studySpots.add(studySpot)
                        if(studySpot != null && studySpot.parentLocation == null) parentStudySpots.add(studySpot)

                    }
                    adapter.filterList(parentStudySpots, studySpots)
                    userState.setStudySpots(studySpots)
                }

            }

    }

    private fun navigateToMap() {
        val ft: FragmentTransaction = parentFragmentManager.beginTransaction()
        ft.replace(R.id.main_fragment, MapsFragment())
        ft.commit()
        ft.addToBackStack(null)
    }
}