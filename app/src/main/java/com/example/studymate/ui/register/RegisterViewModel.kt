package com.example.studymate.ui.register

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.studymate.ui.locationSelector.Location

class RegisterViewModel : ViewModel() {
    private val username = MutableLiveData<String>()
    private val email = MutableLiveData<String>()
    private val displayName = MutableLiveData<String>()
    private val password = MutableLiveData<String>()
    private val location = MutableLiveData<Location>()
    private val accountRole = MutableLiveData<String>()

    fun reset(){
        this.username.value = null
        this.email.value = null
        this.displayName.value = null
        this.password.value = null
        this.location.value = null
        this.accountRole.value = null
    }


    fun setUsername(username:String){
        this.username.value = username
    }

    fun getUsername(): String? {
        return this.username.value
    }

    fun setAccountRole(username:String){
        this.accountRole.value = username
    }

    fun getAccountRole(): String? {
        return this.accountRole.value
    }

    fun setLocation(location: Location){
        this.location.value = location
    }

    fun getLocation(): MutableLiveData<Location> {
        return this.location
    }

    fun setEmail(email:String){
        this.email.value = email
    }

    fun getEmail(): String? {
        return this.email.value
    }

    fun setPassword(password:String){
        this.password.value = password
    }
    fun getPassword(): String? {
        return this.password.value
    }

    fun setDisplayName(displayName:String){
        this.displayName.value = displayName
    }

    fun getDisplayName(): String? {
        return this.displayName.value
    }


}