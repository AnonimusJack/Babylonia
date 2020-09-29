package com.jftech.babylonia.ui.recycler_adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.jftech.babylonia.R
import com.jftech.babylonia.connectors.UserViewModel
import com.jftech.babylonia.data.models.Course

class CoursesPopOverAdapter(var courses: Array<CourseHolder>, private val context: Context, private val userViewModelReference: UserViewModel): RecyclerView.Adapter<CoursesPopOverAdapter.CourseViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.course_recycle_layout, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int)
    {
        val courseImageView: AppCompatImageView = holder.itemView.findViewById(R.id.course_recycle_imageview)
        val selectedName = courses[position].name
        courseImageView.setImageDrawable(context.getDrawable(context.resources.getIdentifier(selectedName.toLowerCase(), "drawable", context.packageName)))
        if (courses[position].selected)
            holder.itemView.background = context.getDrawable(R.drawable.selected_course_background)
        courseImageView.setOnClickListener {
            if (selectedName == "addCourse")
            {
                Toast.makeText(context, "There are currently no new courses to add.", Toast.LENGTH_SHORT).show()
            }
            else
            {
                UpdateSelected(selectedName)
                userViewModelReference.OnNewCourseSelected(selectedName)
            }
        }
    }

    override fun getItemCount(): Int { return courses.size }

    fun UpdateSelected(name: String)
    {
        val index = courses.indexOfFirst { course -> course.name == name }
        for (course in courses)
            course.selected = false
        courses[index].selected = true
        notifyItemRangeChanged(0, courses.size)
    }

    class  CourseViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    data class CourseHolder(val name: String, var selected: Boolean)
}