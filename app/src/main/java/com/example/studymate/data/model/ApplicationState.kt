package com.example.studymate.data.model

import android.util.Log
import androidx.lifecycle.MutableLiveData

object ApplicationState {
    private val username = MutableLiveData<String>()
    private val schedule = MutableLiveData<HashMap<String, ArrayList<Boolean>>>()
    private val email = MutableLiveData<String>()
    private val displayName = MutableLiveData<String>()
    private val locationName = MutableLiveData<String>()
    private val accountRole = MutableLiveData<String>()
    private val courses = MutableLiveData<ArrayList<Course>>()
    private val files = MutableLiveData<ArrayList<File>>()
    private val studySpots = MutableLiveData<ArrayList<StudySpot>>()
    private val isLoading = MutableLiveData(false)
    private val studyGroups = MutableLiveData<ArrayList<StudyGroup>>()

    fun logoutUser(){
        this.username.value = null
        this.email.value = null
        this.displayName.value = null
        this.locationName.value = null
        this.accountRole.value = null
        this.courses.value = null
        this.files.value = null
        this.studySpots.value = null
        this.schedule.value = null
    }

    fun loginUser(user: User) {
        Log.d("USER_VIEW_MODEL", user.toString())
        this.username.value = user.username
        this.email.value = user.email
        this.displayName.value = user.displayName
        this.locationName.value = user.location
        this.accountRole.value = user.accountRole
        this.schedule.value = user.schedule
    }

    fun setUsername(username:String){
        this.username.value = username
    }

    fun getCourses(): MutableLiveData<ArrayList<Course>> {
        return this.courses
    }

    fun addCourse(course: Course) {
        val list = this.courses.value
        list?.add(course)
        this.courses.value = list
    }

    fun getIsLoading(): MutableLiveData<Boolean> {
        return this.isLoading
    }

    fun getSchedule(): MutableLiveData<HashMap<String, ArrayList<Boolean>>> {
        return  this.schedule
    }

    fun setSchedule(newSch: HashMap<String, ArrayList<Boolean>>) {
        this.schedule.value = newSch
    }


    fun getStudyGroups(): MutableLiveData<ArrayList<StudyGroup>> {
        return this.studyGroups
    }

    fun addStudyGroup(group: StudyGroup) {
        val list = this.studyGroups.value
        list?.add(group)
        this.studyGroups.value = list
    }

    fun updateStudyGroup(group: StudyGroup) {
        val list = this.studyGroups.value
        list?.filter { it.id == group.id }?.forEach { it.members = group.members }
        this.studyGroups.value = list
    }



    fun updateStudySpaceOccupants(name: String, location:String, amount: Int) {
        val spaces = this.studySpots.value?.map { space ->
            if(space.name == name && space.location == location) {
               space.occupants += amount
                if(amount == -1) space.members.remove(this.username.value)
                else if(amount == 1) space.members.add(this.username.value)
            }
            space
        }
        this.studySpots.value = spaces as ArrayList<StudySpot>?

    }

    fun setIsLoading(isLoading: Boolean){
        this.isLoading.value = isLoading
    }

    fun setCourses(courses: ArrayList<Course>?){
        this.courses.value = courses
    }

    fun setStudyGroups(groups: ArrayList<StudyGroup>?){
        this.studyGroups.value = groups
    }

    fun getFiles(): MutableLiveData<ArrayList<File>> {
        return this.files
    }

    fun addFile(file: File) {
        val list = this.files.value
        list?.add(file)
        this.files.value = list
    }

    fun setFiles(files: ArrayList<File>?){
        this.files.value = files
    }

    fun getStudySpots(): MutableLiveData<ArrayList<StudySpot>> {
        return this.studySpots
    }

    fun addStudySpot(studySpot: StudySpot) {
        val list = this.studySpots.value
        list?.add(studySpot)
        this.studySpots.value = list
    }

    fun setStudySpots(studySpots: ArrayList<StudySpot>?){
        this.studySpots.value = studySpots
    }

    fun getUsername(): MutableLiveData<String> {
        return this.username
    }

    fun setAccountRole(username:String){
        this.accountRole.value = username
    }

    fun getAccountRole(): MutableLiveData<String> {
        return this.accountRole
    }

    fun setLocation(location: String){
        this.locationName.value = location
    }

    fun getLocation(): MutableLiveData<String> {
        return this.locationName
    }

    fun setEmail(email:String){
        this.email.value = email
    }

    fun getEmail(): MutableLiveData<String> {
        return this.email
    }

    fun setDisplayName(displayName:String){
        this.displayName.value = displayName
    }

    fun getDisplayName(): MutableLiveData<String> {
        return this.displayName
    }


}