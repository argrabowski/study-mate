package com.example.studymate.ui.mainFragments.home.studyGroupView

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.studymate.R
import com.example.studymate.data.model.ApplicationState
import com.example.studymate.data.model.StudyGroup
import com.example.studymate.databinding.FragmentRegisterStudyGroupBinding
import com.example.studymate.ui.mainFragments.home.HomeFragment
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


private const val TAG = "REGISTER_STUDY_GROUP_FRAGMENT"
class RegisterStudyGroupFragment : Fragment() {

    private var _binding: FragmentRegisterStudyGroupBinding? = null
    private val binding get() = _binding!!
    private var subBtn: MenuItem? = null
    private val db = Firebase.firestore
    private val userState  = ApplicationState

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterStudyGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.createStudySpotToolbar.setNavigationOnClickListener {
            navigateBack()
        }

        val arguments = arguments

        val selectedCourse = arguments!!.getString("course")
        val selectedDepartment = arguments.getString("department")

        binding.studyGroupMembersInput.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                addChip()
                return@OnKeyListener true
            }
            false
        })

        binding.memberInputLayout.setEndIconOnClickListener {
            addChip()
        }

        binding.studyGroupNameInput.addTextChangedListener {
            if(subBtn != null &&!subBtn!!.isEnabled) {
                subBtn!!.isEnabled = true;
                subBtn!!.icon?.alpha = 255;
            }
        }

        binding.studyGroupDescriptionInput.addTextChangedListener {
            if(subBtn != null && !subBtn!!.isEnabled) {
                subBtn!!.isEnabled = true;
                subBtn!!.icon?.alpha = 255;
            }
        }

        binding.createStudySpotToolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.toolbar_add_study_group_submit -> {
                    subBtn = it
                    if(checkFormData()) {
                        val name = binding.studyGroupNameInput.text.toString()
                        val description = binding.studyGroupDescriptionInput.text.toString()
                        val members = ArrayList<String>();
                        val isChecked = binding.groupPrivateInput.isChecked

                        for (i in 0 until binding.chipGroup.childCount) {
                            val chip = (binding.chipGroup.getChildAt(i) as Chip).text.toString()
                            members.add(chip)
                        }
                        userState.getUsername().value?.let { it1 -> members.add(it1) }
                        Log.d(TAG, "All chip members: $members")

                       registerStudyGroup(StudyGroup(name, userState.getUsername().value ,userState.getLocation().value, selectedDepartment, selectedCourse, description, members, "SCHEDULE_HERE", isChecked, ArrayList()))
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun checkFormData(): Boolean {
        Log.d(TAG, "Checking form data")
        binding.studyGroupNameLayout.error = ""
        binding.studyGroupDescriptionLayout.error = ""
        subBtn!!.isEnabled = false;
        subBtn!!.icon?.alpha = 130;
        val name = binding.studyGroupNameInput.text
        val description = binding.studyGroupDescriptionInput.text
        return if(name == null || name.isEmpty() || name.isBlank()) {
            binding.studyGroupNameLayout.error = "Enter a name"
            false
        }
        else if(description == null || description.isEmpty() || description.isBlank()) {
            binding.studyGroupDescriptionLayout.error = "Enter a description"
            false
        }
        else {
            subBtn!!.isEnabled = true;
            subBtn!!.icon?.alpha = 255;
            true
        }
    }

    private fun registerStudyGroup(group: StudyGroup) {
        val groupToAdd = hashMapOf(
            "name" to group.name,
            "course" to group.course,
            "isPrivate" to group.isPrivate,
            "location" to group.location,
            "members" to group.members,
            "creator" to group.creator,
            "description" to group.description,
            "department" to group.department,
            "meetingSchedule" to group.meetingSchedule,
            "messages" to group.messages
        )
        db.collection("studyGroups").add(groupToAdd)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully Added Group")
                group.id = it.id
                userState.addStudyGroup(group)
                navigateBack()
            }
            .addOnFailureListener {
                Log.d(TAG, "Error Adding Study Group")
                Toast.makeText(context, "Error Adding Study Group", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addChip() {
        val chip = Chip(context)
        chip.text = binding.studyGroupMembersInput.text
        // following lines are for the demo
        chip.isClickable = true
        chip.isChecked = true
        chip.isCheckable = false
        chip.isCloseIconEnabled = true
        chip.setChipIconTintResource(R.color.black)
        chip.chipIcon = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_account_circle_24)
        val chipGroup = binding.chipGroup
        chipGroup.addView(chip as View)
        chip.setOnCloseIconClickListener { chipGroup.removeView(chip as View) }
        binding.studyGroupMembersInput.setText("")
    }


    private fun navigateBack() {
        if (parentFragmentManager.backStackEntryCount > 1)
            parentFragmentManager.popBackStack()
        else {
            val ft: FragmentTransaction = parentFragmentManager.beginTransaction()
            ft.replace(R.id.main_fragment, HomeFragment())
            ft.commit()
        }
    }
}