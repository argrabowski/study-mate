package com.example.studymate.ui.mainFragments.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.studymate.R
import com.example.studymate.data.model.Course
import com.example.studymate.data.model.ApplicationState
import com.example.studymate.databinding.FragmentCourseRegisterBinding
import com.example.studymate.ui.LoadingDialog
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private const val TAG = "COURSE_REGISTER_FRAGMENT"

private var POTENTIAL_DEPARTMENTS = listOf("Computer Science", "Mechanical Engineering", "Chemistry", "Data Science", "Humanities", "Aerospace", "Biology", "History", "Physics", "Biomedical Engineering", "Social Science")
class CourseRegisterFragment : Fragment() {
    private lateinit var loadingDialog: LoadingDialog
    private var _binding: FragmentCourseRegisterBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore

    private val userState = ApplicationState
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCourseRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireActivity())
        binding.createCourseToolbar.setNavigationOnClickListener{
            navigateBack()
        }

        binding.courseCreateBtn.setOnClickListener {
            val courseName = binding.classNameInput.text.toString()
            val departmentName = binding.selectDepartmentInput.text.toString()
            val instructorName = binding.classInstructorInput.text.toString()
            val newCourse = Course(courseName, userState.getLocation().value, departmentName, ArrayList<String>(), userState.getUsername().value, instructorName)
            createNewCourse(newCourse)
        }

        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
                formDataChanged(
                    binding.classNameInput.text.toString(),
                    binding.classInstructorInput.text.toString(),
                    binding.selectDepartmentInput.text.toString()
                )
            }
        }

        binding.classNameInput.addTextChangedListener(afterTextChangedListener)
        binding.classInstructorInput.addTextChangedListener(afterTextChangedListener)
        binding.selectDepartmentInput.addTextChangedListener(afterTextChangedListener)

        val adapter: ArrayAdapter<String> = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_account_role,
            POTENTIAL_DEPARTMENTS
        )


        binding.selectDepartmentInput.setAdapter(adapter)
    }

    private fun formDataChanged(name:String, instructor: String, department: String) {
        binding.classInstructorInput.error = null
        binding.classNameInput.error = null
        binding.courseCreateBtn.isEnabled = false
        if(name.isBlank() || name.isEmpty()) {
            binding.classNameInput.error = "Enter a course name"
        }
        else if(instructor.isBlank() || instructor.isEmpty()) {
            binding.classInstructorInput.error = "Enter the name of the instructor"
        }
        else if(department.isBlank() || department.isEmpty()){

        }
        else {
            binding.courseCreateBtn.isEnabled = true
        }
    }

    private fun createNewCourse(course: Course) {
        loadingDialog.show()
        db.collection("courses").document(course.name).get()
            .addOnCompleteListener { task ->
                Log.d(TAG, "Done getting course info")
                val doc = task.result
                if(doc.exists()) {
                    Log.d(TAG, "course already exists")
                    loadingDialog.dismiss()
                    Toast.makeText(context, "This course already exists", Toast.LENGTH_SHORT).show()
                } else {
                    val courseToAdd = hashMapOf(
                        "name" to course.name,
                        "department" to course.department,
                        "instructor" to course.instructor,
                        "creator" to course.creator,
                        "location" to course.location,
                        "studyGroups" to course.studyGroups
                    )
                    db.collection("courses").document(course.name).set(courseToAdd)
                        .addOnCompleteListener { task ->
                            Log.d(TAG, "Done setting course")
                            loadingDialog.dismiss()
                            if (task.isSuccessful) {
                                // Document found in the offline cache
                                Log.d(TAG, "course registered")
                                userState.addCourse(course)
                                navigateBack()
                                Log.d(TAG, "Cached document data: ${task.result}")
                            } else {
                                Log.d(TAG, "Unable to register course")
                                Toast.makeText(context, "Unable to create course, please try again later", Toast.LENGTH_SHORT).show()
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
            ft.replace(R.id.main_fragment, HomeFragment())
            ft.commit()
        }
    }
}