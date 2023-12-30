package com.example.studymate.ui.mainFragments.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.studymate.AuthenticationActivity
import com.example.studymate.R
import com.example.studymate.data.model.ApplicationState
import com.example.studymate.data.model.File
import com.example.studymate.databinding.FragmentProfileBinding
import com.example.studymate.ui.LoadingDialog
import com.example.studymate.ui.addFile.AddFileFragment
import com.example.studymate.ui.calendar.SchedulerFragment
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase


private const val TAG = "PROFILE_FRAGMENT"

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var logoutBtn: Button
    private lateinit var usernameTV: TextView; private lateinit var locationTV: TextView; private lateinit var accountRoleTV: TextView;
    private val userViewModel = ApplicationState
    private var firebaseUser: FirebaseUser? = null
    private val db = Firebase.firestore
    private lateinit var adapter: FileAdapter
    private lateinit var fAuth: FirebaseAuth
    private var files = ArrayList<File>();
    private lateinit var loadingDialog: LoadingDialog
    private val userState = ApplicationState

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fAuth = FirebaseAuth.getInstance()
        firebaseUser = fAuth.currentUser

        usernameTV = binding.profileUsername
        var profileEmail = binding.profileEmail
        val displayNameTV = binding.profileDisplayName
        locationTV = binding.profileLocation
        accountRoleTV = binding.profileAccountRole
        val addFileFAB = binding.addFileFab
        addFileFAB.hide()
        loadingDialog = LoadingDialog(requireActivity())
        binding.profileToolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.toolbar_logout -> {
                    logoutUser()
                    true
                }
                R.id.toolbar_set_schedule -> {
                    startSetSchedule()
                    true
                }
                R.id.toolbar_add_file -> {
                    addNewFile()
                    true
                }
                else -> false
            }
        }

        addFileFAB.setOnClickListener {
            addNewFile()
        }

        binding.resetPassword.setOnClickListener(){
            userState.getEmail().value?.let { it1 ->
                fAuth.sendPasswordResetEmail(it1)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "Email sent.")
                            Toast.makeText(requireContext(), "Password Reset Request Sent to Email", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        val rv = binding.profileFilesRv

        adapter = FileAdapter(files, requireActivity() ,requireContext())
        rv.adapter = adapter;
        adapter.setOnItemClickListener(object : FileAdapter.OnItemClickListener {
            override fun onItemClick(itemView: View?, file: File) {
                val bundle = Bundle()
                bundle.putSerializable("file", file)

                val fileDetailFragment = FileDetailFragment()
                fileDetailFragment.arguments = bundle

                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.main_fragment, fileDetailFragment)
                    addToBackStack(null)
                    commit()
                }
            }
        })

        userState.getFiles().observe(viewLifecycleOwner) {
            if(it == null) return@observe
            files = it
            adapter.filterList(files)
        }

        Log.d(TAG, "The app file date is: " + userState.getFiles().value)
        if(userState.getFiles().value ==null) fetchFiles()
        else if(userState.getFiles().value !== null) files = userState.getFiles().value!!

        binding.profileTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    when(tab.position) {
                        0 -> {
                            binding.profileSection.visibility = View.VISIBLE
                            binding.profileFilesRv.visibility = View.GONE
                            addFileFAB.hide()
                        }
                        1 -> {
                            binding.profileSection.visibility = View.GONE
                            binding.profileFilesRv.visibility = View.VISIBLE
                            addFileFAB.show()
                        }
                        else -> {

                        }
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselect
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselect
            }
        })

        if(firebaseUser == null) {
            val intent = Intent(context, AuthenticationActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        ApplicationState.getUsername().observe(viewLifecycleOwner) { usernameTV.text = it }
        ApplicationState.getLocation().observe(viewLifecycleOwner) { locationTV.text = it }
        ApplicationState.getAccountRole().observe(viewLifecycleOwner) { accountRoleTV.text = it }
        ApplicationState.getDisplayName().observe(viewLifecycleOwner) { displayNameTV.text = it }
        ApplicationState.getEmail().observe(viewLifecycleOwner) {profileEmail.text = it}


        Log.d(TAG, ApplicationState.getUsername().value.toString())


    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        ApplicationState.logoutUser()
        val intent = Intent(context, AuthenticationActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    private fun addNewFile() {
        val ft: FragmentTransaction = parentFragmentManager.beginTransaction()
        ft.replace(R.id.main_fragment, AddFileFragment())
        ft.commit()
        ft.addToBackStack(null)
    }

    private fun startSetSchedule() {
        val ft: FragmentTransaction = parentFragmentManager.beginTransaction()
        ft.replace(R.id.main_fragment, SchedulerFragment())
        ft.commit()
        ft.addToBackStack(null)
    }

    private fun fetchFiles() {
        loadingDialog.show()
        db.collection("files").whereEqualTo("creator", userState.getUsername().value).get()
            .addOnCompleteListener { task ->
                loadingDialog.dismiss()
                if(!task.isSuccessful){
                    Toast.makeText(context, "Unable to fetch app data, please reload", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Unable to fetch app data, please reload")
                    //TODO: Implement more error handling
                    return@addOnCompleteListener
                }
                else {
                    val documents = task.result.documents
                    val fileList = ArrayList<File>();
                    for(document in documents){
                        val fileItem = document.toObject<File>()
                        if (fileItem != null) fileList.add(fileItem)

                    }
                    adapter.filterList(fileList)
                    userState.setFiles(fileList)
                }

            }

    }

}