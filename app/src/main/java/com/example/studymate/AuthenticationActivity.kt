package com.example.studymate

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.*
import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.studymate.data.model.User
import com.example.studymate.data.model.ApplicationState
import com.example.studymate.databinding.ActivityAuthenticationBinding
import com.example.studymate.ui.login.LoginFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

private const val TAG = "ACTIVITY_LOGIN"
private const val LOCATION_REQUEST_CODE = 1

class AuthenticationActivity : AppCompatActivity() {


    private lateinit var binding: ActivityAuthenticationBinding
    private lateinit var fAuth: FirebaseAuth
    private val userViewModel = ApplicationState
    private val db = Firebase.firestore

    @Override
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = fAuth.currentUser
        if(currentUser != null && currentUser.email != null){
            //TODO: Add proper error checking here
            loginUser(currentUser.email!!)
        } else {
            userViewModel.logoutUser()
        }
        requestLocationPermissions()
    }


    private fun hasLocationPermissions() = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
        this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED


    //private fun hasBackgroundLocationPermission() = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermissions() {
        var permissionsNeeded = mutableListOf<String>()
        if(!hasLocationPermissions()) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
//        if(!hasBackgroundLocationPermission()) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
//                Log.d("PERMISSIONS_CHECK", "OPTION WOW")
//                requestBackgroundLocationPermission()
//            } else {
//                Log.d("PERMISSIONS_CHECK", "OPTION 12312321")
//                ActivityCompat.requestPermissions(this,
//                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), LOCATION_REQUEST_CODE)
//            }
//        }

        if(permissionsNeeded.isNotEmpty()){
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), LOCATION_REQUEST_CODE)
        }

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == LOCATION_REQUEST_CODE && grantResults.isNotEmpty()) {
            for(i in grantResults.indices) {
                if(grantResults[i] == PackageManager.PERMISSION_GRANTED) Log.d(TAG, "Permission Granted: " + permissions[i])
                else {
                    Log.d(TAG, "Permission Not Granted: " + permissions[i])
                    Toast.makeText(this, "Location Permissions Suggested", Toast.LENGTH_SHORT).show()
                    //this.finish()
                    return
                }
            }
        }
    }

    fun loginUser(email: String) {
        db.collection("users").whereEqualTo("email", email).get()
            .addOnCompleteListener {task ->
                if(task.isSuccessful && !task.result.isEmpty) {
                    val loggedInUser = task.result.documents[0].toObject<User>()
                    if (loggedInUser != null) {
                        Log.d(TAG, "GOT THE USER $loggedInUser")
                        userViewModel.loginUser(loggedInUser)
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else {
                        FirebaseAuth.getInstance().signOut()
                    }
                }
                else{
                    FirebaseAuth.getInstance().signOut()
                }
            }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)

        fAuth = FirebaseAuth.getInstance()

        val view = binding.root
        setContentView(view)
        var fragment: Fragment = LoginFragment()
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.replace(R.id.auth_fragment, fragment)
        ft.commit()
    }
}