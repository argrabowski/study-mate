package com.example.studymate.ui.mainFragments.home

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studymate.R
import com.example.studymate.data.model.ApplicationState
import com.example.studymate.data.model.Course
import com.example.studymate.data.model.StudyGroup
import com.example.studymate.databinding.FragmentHomeBinding
import com.example.studymate.ui.LoadingDialog
import com.example.studymate.ui.mainFragments.home.coursesView.CoursesFragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase


private const val TAG = "HOME_FRAGMENT"
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var departments = ArrayList<Department>();
    private var courses=  ArrayList<Course>();
    private lateinit var adapter: DepartmentAdapter
    private lateinit var loadingDialog: LoadingDialog
    private val db = Firebase.firestore
    private val userState = ApplicationState

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireActivity())
        userState.getCourses().observe(viewLifecycleOwner) {
            if(it == null) return@observe
            courses = it
            val departmentList = ArrayList<Department>()
            val departmentListKeys = ArrayList<String>()
            for(course in it){
                if (course != null && course.location !== null) {
                    if(course.department !== null && !departmentListKeys.contains(course.department)) {
                        departmentListKeys.add(course.department)
                        departmentList.add(Department(course.location, course.department))
                    }
                }
            }
            departments = departmentList
            adapter.filterList(departmentList)
        }
        Log.d(TAG, "The app data is: " + userState.getCourses().value)
        if(userState.getCourses().value ==null && userState.getIsLoading()?.value == false) fetchAppData()
        else if(userState.getCourses().value !== null) courses = userState.getCourses().value!!
        // departments.add(Department("AD", "Computer Science"))
        val emptyView = binding.departmentsEmpty
        val rv = binding.departmentList

        adapter = DepartmentAdapter(departments)
        rv.adapter = adapter;
        adapter.setOnItemClickListener(object : DepartmentAdapter.OnItemClickListener {
            override fun onItemClick(itemView: View?, department: Department) {
                val ft: FragmentTransaction = parentFragmentManager.beginTransaction()
                val fragment = CoursesFragment()
                val arguments = Bundle()
                arguments.putString("department", department.name)
                fragment.arguments = arguments
                ft.replace(R.id.main_fragment, fragment)
                ft.commit()
                ft.addToBackStack(null)
            }
        })
        // Set the adapter
        rv.layoutManager = LinearLayoutManager(context);

        val swipeContainer = binding.homeRefresh

        swipeContainer.setOnRefreshListener { // Your code to refresh the list here.
            // Make sure you call swipeContainer.setRefreshing(false)
            // once the network request has completed successfully.
            fetchAppData()
        }

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light);



        binding.homeToolbar.setOnMenuItemClickListener { menuItem ->
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
                            val filteredItems:List<Department> = filter(newText)
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


    private fun fetchAppData() {
        Log.d(TAG, "Fetching app data in Home")
        userState.setIsLoading(true)
        db.collection("courses").whereEqualTo("location", userState.getLocation().value).get()
            .addOnCompleteListener { task ->
                fetchStudyGroups()
                if(!task.isSuccessful){
                    Toast.makeText(context, "Unable to fetch app data, please reload", Toast.LENGTH_SHORT).show()
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
                    adapter.filterList(departmentList)
                    userState.setCourses(newCourses)
                }

            }

    }

    private fun fetchStudyGroups() {
        db.collection("studyGroups").whereEqualTo("location", userState.getLocation().value).get()
            .addOnCompleteListener{
                userState.setIsLoading(false)
                binding.homeRefresh.isRefreshing = false;
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
                Log.d(TAG, "Unable to fetch study group data, please reload")
            }
    }

    private fun filter(filterItem: String): List<Department> {
        val filtered: ArrayList<Department> = ArrayList<Department>()
        Log.d(TAG, "THE FILTER ITEM IS $filterItem")
        // running a for loop to compare elements.

        // running a for loop to compare elements.
        for (item in departments) {
            if(item.name === null) continue
            if (item.name.lowercase().contains(filterItem.lowercase())) {
                filtered.add(item)
            }
        }

        // at last we are passing that filtered
        // list to our adapter class.
        return filtered

    }


}