package com.example.studymate.ui.mainFragments.home.groupView

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studymate.data.model.ApplicationState
import com.example.studymate.data.model.StudyGroup
import com.example.studymate.databinding.FragmentMessageRowBinding


class MessagesAdapter(
    private var values: List<Message>,
) : RecyclerView.Adapter<MessagesAdapter.ViewHolder>() {


    fun filterList(filteredList: ArrayList<Message>) {
        values = filteredList
        notifyDataSetChanged()
    }

    private var userState = ApplicationState

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentMessageRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]

        if(item.sender == userState.getUsername().value) {
            holder.receiverLayout.visibility = View.GONE
            holder.senderMessage.visibility = View.VISIBLE
            holder.senderMessage.text = item.message
        }
        else {
            holder.receiverLayout.visibility = View.VISIBLE
            holder.senderMessage.visibility = View.GONE
            holder.sentBy.text = "Sent By ${item.sender}"
            holder.receiverMessage.text = item.message
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentMessageRowBinding) : RecyclerView.ViewHolder(binding.root) {
        val senderMessage: TextView = binding.senderMessage
        val receiverMessage: TextView = binding.receiverMessage
        val receiverLayout: LinearLayout = binding.receiverMessageLayout
        val sentBy: TextView = binding.sendByMsg
    }

}