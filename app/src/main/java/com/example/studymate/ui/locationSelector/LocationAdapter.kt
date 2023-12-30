package com.example.studymate.ui.locationSelector

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.studymate.data.model.StudyGroup
import com.example.studymate.databinding.FragmentLocationRowBinding


class LocationAdapter(
    private var values: ArrayList<Location>,
) : RecyclerView.Adapter<LocationAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(itemView: View?, location: Location)
    }

    fun filterList(filterlist: ArrayList<Location>) {
        values = filterlist
        notifyDataSetChanged()
    }

    fun addItem(item: Location) {
        values.add(item)
        notifyDataSetChanged()
    }

    private lateinit var listener: OnItemClickListener

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentLocationRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.itemName.text = item.name
        holder.subtext.text = item.address
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentLocationRowBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        val itemName: TextView = binding.locationName
        val subtext: TextView = binding.itemSubtext
        init {
            // Attach a click listener to the entire row view
            itemView.setOnClickListener(this)
        }

        override fun toString(): String {
            return super.toString() + " '" + subtext.text + "'"
        }

        @Override
        override fun onClick(p0: View?) {
            val position = absoluteAdapterPosition // gets item position
            //Log.d("ASD", "CLICK")
            if (position != RecyclerView.NO_POSITION) { // Check if an item was deleted, but the user clicked it before the UI removed it
                val location: Location = values[position]
                listener.onItemClick(itemView, location)
            }
        }


    }


}