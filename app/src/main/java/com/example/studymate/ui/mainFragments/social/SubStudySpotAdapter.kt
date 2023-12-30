package com.example.studymate.ui.mainFragments.social

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studymate.data.model.StudySpot
import com.example.studymate.databinding.StudySpotSubRowBinding


class SubStudySpotAdapter(
    private var values: List<StudySpot>,
) : RecyclerView.Adapter<SubStudySpotAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(itemView: View?, studySpot: StudySpot)
    }

    fun filterList(filterlist: List<StudySpot>) {
        // below line is to add our filtered
        // list in our course array list.
        values = filterlist
        // below line is to notify our adapter
        // as change in recycler view data.
        notifyDataSetChanged()
    }

    private lateinit var listener: OnItemClickListener

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            StudySpotSubRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.itemName.text = item.name
        holder.itemCount.text = if(item.maxOccupants != null) "${item.occupants} / ${item.maxOccupants}" else "Total: ${item.occupants}"
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: StudySpotSubRowBinding) : RecyclerView.ViewHolder(binding.root) {
        val itemName: TextView = binding.cardSubTitle
        val itemCount: TextView = binding.cardStudySubCount

        override fun toString(): String {
            return super.toString() + " '" + itemName.text + "'"
        }


    }

}