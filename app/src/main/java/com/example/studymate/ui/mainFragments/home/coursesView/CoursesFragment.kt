package com.example.studymate.ui.mainFragments.home.coursesView

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studymate.R
import com.example.studymate.data.model.ApplicationState
import com.example.studymate.data.model.Course
import com.example.studymate.databinding.FragmentCourseListBinding
import com.example.studymate.ui.mainFragments.home.CourseRegisterFragment
import com.example.studymate.ui.mainFragments.home.HomeFragment
import com.example.studymate.ui.mainFragments.home.studyGroupView.StudyGroupsFragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private const val TAG = "COURSES_FRAGMENT"

class CoursesFragment : Fragment() {
    private var _binding: FragmentCourseListBinding? = null
    private val binding get() = _binding!!
    private var courses=  ArrayList<Course>();
    private lateinit var adapter: CourseAdapter
    private val userState = ApplicationState

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCourseListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val arguments = arguments
        val selectedDepartment = arguments!!.getString("department")

        userState.getCourses().observe(viewLifecycleOwner) {
            if(it == null) return@observe
            courses = it.filter { course -> course.department == selectedDepartment } as ArrayList<Course>
            adapter.filterList(courses)
        }

        if(userState.getCourses().value !== null) courses = userState.getCourses().value!!.filter { course -> course.department == selectedDepartment } as ArrayList<Course>

        val emptyView = binding.coursesEmpty
        val rv = binding.courseList

        binding.coursesToolbar.setNavigationOnClickListener{
            navigateBack()
        }

        adapter = CourseAdapter(courses)
        rv.adapter = adapter;
        adapter.setOnItemClickListener(object : CourseAdapter.OnItemClickListener {
            override fun onItemClick(itemView: View?, course: Course) {
                val ft: FragmentTransaction = parentFragmentManager.beginTransaction()
                val fragment = StudyGroupsFragment()
                val arguments = Bundle()
                arguments.putString("course", course.name)
                arguments.putString("department", course.department)
                fragment.arguments = arguments
                ft.replace(R.id.main_fragment, fragment)
                ft.commit()
                ft.addToBackStack(null)
            }
        })
        // Set the adapter
        rv.layoutManager = LinearLayoutManager(context);




        binding.coursesToolbar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId) {
                R.id.toolbar_register_course -> {
                    val ft: FragmentTransaction = parentFragmentManager.beginTransaction()
                    ft.replace(R.id.main_fragment, CourseRegisterFragment())
                    ft.commit()
                    ft.addToBackStack(null)
                    true
                }
                R.id.actionSearch -> {
                    val searchView = menuItem.actionView as SearchView?
                    searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String): Boolean {
                            Log.d(TAG, "QUERT TEXT SUBMIT")
                            return false
                        }

                        override fun onQueryTextChange(newText: String): Boolean {
                            Log.d(TAG, "QUERT TEXT CHANGE")
                            val filteredItems:List<Course> = filter(newText)
                            adapter.filterList(filteredItems)
                            if(filteredItems.isEmpty()){
                                rv.visibility = View.GONE;
                                emptyView.visibility = View.VISIBLE;
                            } else {
                                rv.visibility = View.VISIBLE;
                                emptyView.visibility = View.GONE;
                                adapter.filterList(filteredItems)
                            }
                            return false
                        }
                    })
                    true
                }
                else -> false

            }
        }
    }


    private fun filter(filterItem: String): List<Course> {
        val filtered: ArrayList<Course> = ArrayList<Course>()
        Log.d(TAG, "THE FILTER ITEM IS $filterItem")
        // running a for loop to compare elements.

        // running a for loop to compare elements.
        for (item in courses) {
            if(item.name === null) continue
            if (item.name.lowercase().contains(filterItem.lowercase())) {
                filtered.add(item)
            }
        }

        // at last we are passing that filtered
        // list to our adapter class.
        return filtered

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