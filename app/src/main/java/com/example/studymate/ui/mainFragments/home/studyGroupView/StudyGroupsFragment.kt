package com.example.studymate.ui.mainFragments.home.studyGroupView

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studymate.R
import com.example.studymate.data.model.ApplicationState
import com.example.studymate.data.model.StudyGroup
import com.example.studymate.databinding.FragmentStudyGroupListBinding
import com.example.studymate.ui.mainFragments.home.HomeFragment
import com.example.studymate.ui.mainFragments.home.groupView.GroupFragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private const val TAG = "STUDY_GROUP_FRAGMENT"

class StudyGroupsFragment : Fragment() {
    private var _binding: FragmentStudyGroupListBinding? = null
    private val binding get() = _binding!!
    private var studyGroups =  ArrayList<StudyGroup>();
    private lateinit var adapter: StudyGroupsAdapter
    private val userState = ApplicationState

    private val comparator = Comparator<StudyGroup> { first, second ->
        when {
            first.members.contains(userState.getUsername().value) && !second.members.contains(userState.getUsername().value) -> 1
            !first.members.contains(userState.getUsername().value) && second.members.contains(userState.getUsername().value) -> -1
            else -> 0
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentStudyGroupListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val arguments = arguments
        val selectedCourse = arguments!!.getString("course")
        val selectedDepartment = arguments!!.getString("department")

        userState.getStudyGroups().observe(viewLifecycleOwner) {
            if(it == null) return@observe
            studyGroups=it
            studyGroups.sortedWith(comparator)
            adapter.filterList(studyGroups)
        }

        userState.getStudyGroups().observe(viewLifecycleOwner) {
            if(it == null) return@observe
            studyGroups = it.filter { group -> group.department == selectedDepartment && group.course == selectedCourse && (if(group.isPrivate) group.members.contains(userState.getUsername().value) else true ) } as ArrayList<StudyGroup>
            studyGroups.sortedWith(comparator)
            adapter.filterList(studyGroups)
        }

        if(userState.getStudyGroups().value !== null) {
            studyGroups = userState.getStudyGroups().value!!.filter { group -> group.department == selectedDepartment && group.course == selectedCourse && (if(group.isPrivate) group.members.contains(userState.getUsername().value) else true ) } as ArrayList<StudyGroup>
            studyGroups.sortedWith(comparator)
        }


        val emptyView = binding.studyGroupsEmpty
        val rv = binding.studyGroupList

        binding.studyGroupToolbar.setNavigationOnClickListener{
            navigateBack()
        }

        adapter = StudyGroupsAdapter(studyGroups, requireContext())
        rv.adapter = adapter;
        adapter.setOnItemClickListener(object : StudyGroupsAdapter.OnItemClickListener {
            override fun onItemClick(itemView: View?, studyGroup: StudyGroup) {
                val isMember = studyGroup.members.contains(userState.getUsername().value)
                if(!isMember) {
                    Toast.makeText(context, "You must be a member of the group.", Toast.LENGTH_SHORT).show()
                    return
                }
                val ft: FragmentTransaction = parentFragmentManager.beginTransaction()
                val fragment =  GroupFragment()
                fragment.setStudyGroup(studyGroup)
                ft.replace(R.id.main_fragment, fragment)
                ft.commit()
                ft.addToBackStack(null)
                //Toast.makeText(context, "${studyGroup.name} was clicked!", Toast.LENGTH_SHORT).show()
            }
        })
        // Set the adapter
        rv.layoutManager = LinearLayoutManager(context);




        binding.studyGroupToolbar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId) {
                R.id.toolbar_register_study_group -> {
                    Log.d(TAG, "WOWOW")
                    val ft: FragmentTransaction = parentFragmentManager.beginTransaction()
                    val fragment = RegisterStudyGroupFragment()
                    val arguments = Bundle()
                    arguments.putString("course", selectedCourse)
                    arguments.putString("department", selectedDepartment)
                    fragment.arguments = arguments
                    ft.replace(R.id.main_fragment, fragment)
                    ft.commit()
                    ft.addToBackStack(null)
                    true
                }
                R.id.actionSearch -> {
                    val searchView = menuItem.actionView as SearchView?
                    searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String): Boolean {
                            return false
                        }

                        override fun onQueryTextChange(newText: String): Boolean {
                            Log.d(TAG, "QUERT TEXT CHANGE")
                            val filteredItems:ArrayList<StudyGroup> = filter(newText)
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


    private fun filter(filterItem: String): ArrayList<StudyGroup> {
        val filtered: ArrayList<StudyGroup> = ArrayList()
        Log.d(TAG, "THE FILTER ITEM IS $filterItem")
        // running a for loop to compare elements.

        // running a for loop to compare elements.
        for (item in studyGroups) {
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