package com.example.studymate.ui.calendar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.studymate.R
import com.example.studymate.data.model.ApplicationState
import com.example.studymate.databinding.FragmentCalendarBinding
import com.example.studymate.ui.LoadingDialog
import com.example.studymate.ui.mainFragments.home.Constants.Companion.DaysOfWeek
import com.example.studymate.ui.mainFragments.profile.ProfileFragment
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private const val TAG = "SCHEDULER_FRAGMENT"

class SchedulerFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private lateinit var fAuth: FirebaseAuth
    private val db = Firebase.firestore
    private val userState = ApplicationState
    private lateinit var loadingDialog: LoadingDialog
    private val binding get() = _binding!!
    private var map = HashMap<String, ArrayList<Boolean>>()
    private var currentDay = MutableLiveData("Monday")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setScheduleToolbar.setNavigationOnClickListener{
            navigateBack()
        }

        loadingDialog = LoadingDialog(requireActivity())
        for (day in DaysOfWeek){
            map[day] = arrayListOf(false, false, false, false, false, false, false, false, false, false)
        }

        currentDay.observe(viewLifecycleOwner) {
            Log.d(TAG, "The current day is: $it")
            handleSetCards()
        }

        binding.setScheduleToolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.toolbar_set_schedule_submit -> {
                    setSchedule()
                    true
                }
                else -> false
            }
        }

        if(userState.getSchedule().value != null) {
            map = userState.getSchedule().value!!
            handleSetCards()
        }

        binding.scheduleTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    if(DaysOfWeek.indices.contains(tab.position)) currentDay.value = DaysOfWeek[tab.position]
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselect
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselect
            }
        })

        binding.card1.setOnClickListener { binding.card1.setCardBackgroundColor(if(clickSchedulerItem(0)) resources.getColor(R.color.theme_main_color) else resources.getColor(com.google.android.libraries.places.R.color.quantum_grey )) }
        binding.card2.setOnClickListener { binding.card2.setCardBackgroundColor(if(clickSchedulerItem(1)) resources.getColor(R.color.theme_main_color) else resources.getColor(com.google.android.libraries.places.R.color.quantum_grey )) }
        binding.card3.setOnClickListener { binding.card3.setCardBackgroundColor(if(clickSchedulerItem(2)) resources.getColor(R.color.theme_main_color) else resources.getColor(com.google.android.libraries.places.R.color.quantum_grey )) }
        binding.card4.setOnClickListener { binding.card4.setCardBackgroundColor(if(clickSchedulerItem(3)) resources.getColor(R.color.theme_main_color) else resources.getColor(com.google.android.libraries.places.R.color.quantum_grey )) }
        binding.card5.setOnClickListener { binding.card5.setCardBackgroundColor(if(clickSchedulerItem(4)) resources.getColor(R.color.theme_main_color) else resources.getColor(com.google.android.libraries.places.R.color.quantum_grey )) }
        binding.card6.setOnClickListener { binding.card6.setCardBackgroundColor(if(clickSchedulerItem(5)) resources.getColor(R.color.theme_main_color) else resources.getColor(com.google.android.libraries.places.R.color.quantum_grey )) }
        binding.card7.setOnClickListener { binding.card7.setCardBackgroundColor(if(clickSchedulerItem(6)) resources.getColor(R.color.theme_main_color) else resources.getColor(com.google.android.libraries.places.R.color.quantum_grey )) }
        binding.card8.setOnClickListener { binding.card8.setCardBackgroundColor(if(clickSchedulerItem(7)) resources.getColor(R.color.theme_main_color) else resources.getColor(com.google.android.libraries.places.R.color.quantum_grey )) }
        binding.card9.setOnClickListener { binding.card9.setCardBackgroundColor(if(clickSchedulerItem(8)) resources.getColor(R.color.theme_main_color) else resources.getColor(com.google.android.libraries.places.R.color.quantum_grey )) }
        binding.card10.setOnClickListener { binding.card10.setCardBackgroundColor(if(clickSchedulerItem(9)) resources.getColor(R.color.theme_main_color) else resources.getColor(com.google.android.libraries.places.R.color.quantum_grey )) }
    }

    private fun navigateBack() {
        if (parentFragmentManager.backStackEntryCount > 1)
            parentFragmentManager.popBackStack()
        else {
            val ft: FragmentTransaction = parentFragmentManager.beginTransaction()
            ft.replace(R.id.main_fragment, ProfileFragment())
            ft.commit()
        }
    }

    private fun setSchedule() {
        loadingDialog.show()
        Log.d(TAG, "Setting schedule")
        db.collection("users").whereEqualTo("username", userState.getUsername().value).get()
            .addOnSuccessListener {
                Log.d(TAG, "got user")

                db.collection("users").document(it.documents[0].id).update("schedule", map)
                    .addOnSuccessListener {
                        Log.d(TAG, "Successfully set schedule")
                        userState.setSchedule(map)
                        navigateBack()
                    }
                    .addOnCompleteListener {
                        loadingDialog.dismiss()
                    }
            }
            .addOnFailureListener{
                loadingDialog.dismiss()
            }
    }

    private fun handleSetCards() {
        val curr = currentDay.value ?: return
        binding.card1.setCardBackgroundColor(if(map[curr]?.get(0) == true) resources.getColor(R.color.theme_main_color) else resources.getColor(com.google.android.libraries.places.R.color.quantum_grey ))
        binding.card2.setCardBackgroundColor(if(map[curr]?.get(1) == true)  resources.getColor(R.color.theme_main_color) else resources.getColor(com.google.android.libraries.places.R.color.quantum_grey ))
        binding.card3.setCardBackgroundColor(if(map[curr]?.get(2) == true)  resources.getColor(R.color.theme_main_color) else resources.getColor(com.google.android.libraries.places.R.color.quantum_grey ))
        binding.card4.setCardBackgroundColor(if(map[curr]?.get(3) == true)  resources.getColor(R.color.theme_main_color) else resources.getColor(com.google.android.libraries.places.R.color.quantum_grey ))
        binding.card5.setCardBackgroundColor(if(map[curr]?.get(4) == true) resources.getColor(R.color.theme_main_color) else resources.getColor(com.google.android.libraries.places.R.color.quantum_grey ))
        binding.card6.setCardBackgroundColor(if(map[curr]?.get(5) == true)  resources.getColor(R.color.theme_main_color) else resources.getColor(com.google.android.libraries.places.R.color.quantum_grey ))
        binding.card7.setCardBackgroundColor(if(map[curr]?.get(6) == true)  resources.getColor(R.color.theme_main_color) else resources.getColor(com.google.android.libraries.places.R.color.quantum_grey ))
        binding.card8.setCardBackgroundColor(if(map[curr]?.get(7) == true)  resources.getColor(R.color.theme_main_color) else resources.getColor(com.google.android.libraries.places.R.color.quantum_grey ))
        binding.card9.setCardBackgroundColor(if(map[curr]?.get(8) == true) resources.getColor(R.color.theme_main_color) else resources.getColor(com.google.android.libraries.places.R.color.quantum_grey ))
        binding.card10.setCardBackgroundColor(if(map[curr]?.get(9) == true)  resources.getColor(R.color.theme_main_color) else resources.getColor(com.google.android.libraries.places.R.color.quantum_grey ))
    }


    private fun clickSchedulerItem(id:Int): Boolean {
        Log.d(TAG, "Item clicked")
        Log.d(TAG, "Map before: $map")
        val curr = currentDay.value ?: return false
        return if(map.containsKey(curr)) {
            if(map[curr]?.get(id) == true) {
                map[curr]?.set(id, false)
                false
            } else {
                map[curr]?.set(id, true)
                true
            }
        } else {
            map[curr] =
                arrayListOf(false, false, false, false, false, false, false, false, false, false)
            false
        }
    }
}