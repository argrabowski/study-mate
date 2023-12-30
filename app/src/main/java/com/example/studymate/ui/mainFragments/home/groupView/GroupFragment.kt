package com.example.studymate.ui.mainFragments.home.groupView

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studymate.R
import com.example.studymate.data.model.*
import com.example.studymate.databinding.FragmentGroupBinding
import com.example.studymate.ui.LoadingDialog
import com.example.studymate.ui.mainFragments.home.Constants.Companion.DaysOfWeek
import com.example.studymate.ui.mainFragments.home.Constants.Companion.MeetingTimes
import com.example.studymate.ui.mainFragments.home.Department
import com.example.studymate.ui.mainFragments.home.HomeFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

private const val TAG = "GROUP_FRAGMENT"

class GroupFragment : Fragment() {

    private var _binding: FragmentGroupBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MessagesAdapter
    private var messages=  ArrayList<Message>();
    private var userState = ApplicationState
    private val db = Firebase.firestore
    private var studyGroup: StudyGroup? = null
    private lateinit var rv: RecyclerView
    private lateinit var loadingDialog: LoadingDialog

    fun setStudyGroup(studyGroup: StudyGroup) {
        this.studyGroup = studyGroup
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.groupToolbar.setNavigationOnClickListener {
            navigateBack()
        }

        loadingDialog = LoadingDialog(requireActivity())
        binding.groupToolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.toolbar_group_schedule_meeting -> {
                    if(studyGroup != null) handleScheduleMeeting(studyGroup!!)
                    true
                }
                else -> false
            }
        }


        if(studyGroup != null) {
            binding.groupNameView.text = studyGroup!!.name
            binding.groupDetailsView.text = studyGroup!!.department + " - " + studyGroup!!.course


            studyGroup!!.messages.forEach {
                messages.add(Gson().fromJson(it, Message::class.java))
            }

            listenForMessages(studyGroup!!)
        }

        binding.sendMsgBtn.setOnClickListener {
            val msg = Gson().toJson(Message(userState.getUsername().value, binding.enterText.text.toString()))
            studyGroup?.let { it1 -> addMessage(it1, msg) }
        }
        rv = binding.groupMessagedList

        adapter = MessagesAdapter(messages)
        rv.adapter = adapter;
        rv.layoutManager = LinearLayoutManager(context);
        rv.scrollToPosition(adapter.itemCount -1);

        
    }

    private fun addMessage(group: StudyGroup, message:String) {
        if(message.isEmpty() || message.isBlank()) return
        db.collection("studyGroups").document(group.id).update("messages", FieldValue.arrayUnion(message))
            .addOnSuccessListener {
                Log.d(TAG, "Added Message")
            }
            .addOnFailureListener() {
                Log.d(TAG, "Failure Adding Message")
            }
            .addOnCompleteListener {
                binding.enterText.setText("")
            }
    }

    private fun handleScheduleMeeting(group: StudyGroup) {
        loadingDialog.show()
        db.collection("studyGroups").document(group.id).get()
            .addOnSuccessListener {
                Log.d(TAG, "GOT GROUP")
                db.collection("users").whereIn("username", group.members).get()
                    .addOnSuccessListener { it ->
                        var meetingTimeIndex:Int = -1
                        var meetingDay:String = ""
                        val scheduleMap = HashMap<String, ArrayList<Int>>()
                        val membersWithSchedule = ArrayList<User>()
                        it.documents.forEach {
                            val item = it.toObject<User>()
                            if(item?.schedule != null && item?.schedule!!.values.isNotEmpty()) membersWithSchedule.add(item)
                        }
                        for(day in DaysOfWeek) {
                            for(document in it){
                                val documentItem = document.toObject<User>()
                                if(documentItem.schedule != null && documentItem.schedule.containsKey(day)) {
                                    var i = 0
                                    for (avail in documentItem.schedule[day]!!){
                                        val orig = scheduleMap.getOrDefault(day, ArrayList())
                                        if(orig.indices.contains(i) && avail) orig[i] += 1
                                        else orig.add(if(avail) 1 else 0)
                                        scheduleMap[day] = orig
                                        i += 1
                                    }
                                }
                            }
                        }

                        Log.d(TAG, "Members are: $membersWithSchedule")
                        for(key in scheduleMap.keys) {
                            if(scheduleMap[key]?.maxOrNull()!! == membersWithSchedule.size) {
                                meetingTimeIndex = scheduleMap[key]?.indices?.maxBy { it2 -> scheduleMap[key]?.get(it2) ?: -1 } ?: -1
                                meetingDay = key
                                break
                            }
                        }

                        if(meetingTimeIndex > -1) {
                            Log.d(TAG, "Next Meeting BY MAX is: $meetingTimeIndex")
                        }
                        else {
                            for(day in scheduleMap.keys) {
                                Log.d(TAG, "The schedule map for $day, is: $scheduleMap")
                                Log.d(TAG, "Max value is: ${scheduleMap[day]?.maxOrNull()!!} versus ${membersWithSchedule.size *.5}")
                                //Log.d(TAG, membersWithSchedule.toString())
                                if(scheduleMap[day]?.maxOrNull()!! > membersWithSchedule.size*.5) {
                                    Log.d(TAG, "The next meeting day is: ${scheduleMap[day]?.indices?.maxBy { it2 -> scheduleMap[day]?.get(it2) ?: -1 } ?: -1}")
                                    meetingTimeIndex = scheduleMap[day]?.indices?.maxBy { it2 -> scheduleMap[day]?.get(it2) ?: -1 } ?: -1
                                    meetingDay = day
                                    break
                                }
                            }

                            if(meetingTimeIndex == -1){
                                Log.d(TAG, "No meetings possible")
                                Toast.makeText(requireContext(), "No possible meeting times", Toast.LENGTH_SHORT).show()
                            }
                            else {
                                Log.d(TAG, "Next Meeting is: $meetingTimeIndex")
                            }
                        }

                        if(meetingTimeIndex > -1) {
                            handleMeetingDialog(group, meetingDay, MeetingTimes[meetingTimeIndex])
                        }
                        loadingDialog.dismiss()
                    }
                    .addOnFailureListener {
                        loadingDialog.dismiss()
                        Log.d(TAG, "Error in schedule meeting - cant get members")
                        Toast.makeText(requireContext(), "Error Scheduling Meeting", Toast.LENGTH_SHORT).show()
                    }

            }
            .addOnFailureListener {
                loadingDialog.dismiss()
                Log.d(TAG, "Error in schedule meeting - cant get GROUP")
                Toast.makeText(requireContext(), "Error Scheduling Meeting", Toast.LENGTH_SHORT).show()
            }
    }

    private fun handleMeetingDialog(group: StudyGroup, day: String, meetingTime: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Schedule Meeting")
            .setMessage("Schedule a meeting for this upcoming $day from $meetingTime")
            .setNeutralButton("Cancel") { dialog, which ->

            }
            .setPositiveButton("Submit") { dialog, which ->
                var msg = userState.getUsername().value + " would like to meet this upcoming $day from $meetingTime"
                msg = Gson().toJson(Message("SYSTEM", msg))
                addMessage(group, msg)
            }
            .show()
    }

    private fun listenForMessages(group: StudyGroup) {
        val docRef = db.collection("studyGroups").document(group.id)
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data?.getValue("messages") as ArrayList<*>
                val newMessages = ArrayList<Message>()
                data.forEach {
                    Log.d(TAG, it.toString())
                    Log.d(TAG, it.javaClass.name)
                    newMessages.add(Gson().fromJson(it.toString(), Message::class.java))
                }
                adapter.filterList(newMessages)
                rv.scrollToPosition(adapter.itemCount -1);
                Log.d(TAG, "The new messages are:  $newMessages")

            } else {
                Log.d(TAG, "Current data: null")
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