package com.example.studymate.ui.mainFragments.social

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.studymate.R
import com.example.studymate.data.model.File
import com.example.studymate.data.model.StudySpot
import com.example.studymate.databinding.StudySpotRowBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class StudySpotAdapter(
    private var values: ArrayList<StudySpot>,
    private var fullList: List<StudySpot>
) : RecyclerView.Adapter<StudySpotAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(itemView: View?, studySpot: StudySpot)
    }

    fun filterList(parentList: ArrayList<StudySpot>, fullList: ArrayList<StudySpot>) {
        // below line is to add our filtered
        // list in our course array list.
        values = parentList
        this.fullList = fullList
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
            StudySpotRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    // Clean all elements of the recycler
    fun clear() {
        values = ArrayList()
        notifyDataSetChanged()
    }

    // Add a list of items -- change to type used
    fun addAll(newList: ArrayList<StudySpot>) {
        values.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.itemName.text = item.name
        holder.itemCount.text = if(item.maxOccupants != null) "${item.occupants} / ${item.maxOccupants}" else "Total: ${item.occupants}"
        holder.itemDescription.text = item.description

        val childLocations = fullList.filter { spot -> spot.parentLocation == item.name}
        Log.d("WOWOW", values.toString())
        Log.d("WOWOW", fullList.toString())
        if(childLocations.isNotEmpty()) {
            val adapterRv = SubStudySpotAdapter(childLocations)
            holder.itemSubRV.adapter = adapterRv
        }
        else Log.d("WOWOW", "EMPTY")
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: StudySpotRowBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        val itemName: TextView = binding.cardTitle
        val itemCount: TextView = binding.cardStudyCount
        val itemDescription: TextView = binding.cardSubText
        val itemDropDownIcon: ImageView = binding.dropdownIcon
        val itemDetailSection: LinearLayout = binding.studySpotDetails
        val itemSubRV: RecyclerView = binding.subStudyList
        var expanded = false

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
                val studySpot: StudySpot = values[position]
                expanded = !expanded
                if(expanded) {
                    itemDetailSection.visibility = View.VISIBLE
                    itemDropDownIcon.setImageResource(R.drawable.baseline_keyboard_arrow_up_24_white)
                }
                else {
                    itemDetailSection.visibility = View.GONE
                    itemDropDownIcon.setImageResource(R.drawable.baseline_keyboard_arrow_down_24_white)
                }
                listener.onItemClick(itemView, studySpot)
            }
        }


    }

    private fun setClipboard(context: Context, text: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            val clipboard =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as android.text.ClipboardManager
            clipboard.text = text
        } else {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Text", text)
            clipboard.setPrimaryClip(clip)
        }
    }

}