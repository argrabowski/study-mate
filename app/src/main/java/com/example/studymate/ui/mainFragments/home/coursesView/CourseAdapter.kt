package com.example.studymate.ui.mainFragments.home.coursesView

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studymate.data.model.Course
import com.example.studymate.databinding.FragmentCourseRowBinding


class CourseAdapter(
    private var values: List<Course>,
) : RecyclerView.Adapter<CourseAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(itemView: View?, course: Course)
    }

    fun filterList(filterlist: List<Course>) {
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
            FragmentCourseRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.itemName.text = item.name
        holder.itemInstructor.text = item.instructor
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentCourseRowBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        val itemName: TextView = binding.courseName
        val itemInstructor: TextView = binding.instructorName
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
                val course: Course = values[position]
                listener.onItemClick(itemView, course)
            }
        }


    }

}