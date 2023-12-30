package com.example.studymate.ui.mainFragments.home.studyGroupView

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.studymate.R
import com.example.studymate.data.model.ApplicationState
import com.example.studymate.data.model.StudyGroup
import com.example.studymate.databinding.FragmentStudyGroupRowBinding
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


private const val TAG = "STUDY_GROUP_ADAPTER"

class StudyGroupsAdapter(
    private var values: List<StudyGroup>,
    private val context: Context
) : RecyclerView.Adapter<StudyGroupsAdapter.ViewHolder>() {

    private val db = Firebase.firestore
    private var userState = ApplicationState

    interface OnItemClickListener {
        fun onItemClick(itemView: View?, studyGroup: StudyGroup)
    }


    fun filterList(filterlist: ArrayList<StudyGroup>) {
        values = filterlist
        notifyDataSetChanged()
    }

    private lateinit var listener: OnItemClickListener

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentStudyGroupRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.itemName.text = item.name
        holder.itemDescription.text = item.description
        val isMemberOf = item.members.contains(userState.getUsername().value)
        holder.starBtn.setOnClickListener {
            Log.d(TAG, "STAR CLICKED")
            Log.d(TAG, "The item is: $item")


            handleStudyGroupMemberAction(item, userState.getUsername().value, isMemberOf)

        }
        holder.starBtn.setImageResource(if(isMemberOf) R.drawable.baseline_star_24_large_gold else R.drawable.baseline_star_border_24_large_gold)
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentStudyGroupRowBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        val itemName: TextView = binding.groupName
        val itemDescription: TextView = binding.groupDescription
        val starBtn: ImageButton = binding.starBtn
        init {
            // Attach a click listener to the entire row view
            itemView.setOnClickListener(this)
        }

        override fun toString(): String {
            return super.toString() + " '" + itemName.text + "'"
        }

        @Override
        override fun onClick(p0: View?) {
            val position = absoluteAdapterPosition // gets item position
            //Log.d("ASD", "CLICK")
            if (position != RecyclerView.NO_POSITION) { // Check if an item was deleted, but the user clicked it before the UI removed it
                val studyGroup: StudyGroup = values[position]
                listener.onItemClick(itemView, studyGroup)
            }
        }


    }


    private fun handleStudyGroupMemberAction(group: StudyGroup, username:String?, isMember: Boolean) {
        Log.d(TAG, "Joining group")
        val action = if(isMember)  FieldValue.arrayRemove(username) else FieldValue.arrayUnion(username)
        db.collection("studyGroups").document(group.id).update("members", action)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully Handled Group Action Group")
                if(!isMember) {
                    group.members.add(username)
                }
                else {
                    group.members.remove(username)
                }
                userState.updateStudyGroup(group)
            }
            .addOnFailureListener {
                Log.d(TAG, "Failure Joining Group")
                Toast.makeText(context, "Unable to join group", Toast.LENGTH_SHORT).show()
            }
    }

}