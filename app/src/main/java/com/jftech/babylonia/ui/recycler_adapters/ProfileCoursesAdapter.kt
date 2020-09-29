package com.jftech.babylonia.ui.recycler_adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.jftech.babylonia.R
import com.jftech.babylonia.connectors.UserViewModel

class ProfileCoursesAdapter(private val userViewModelReference: UserViewModel, private val context: Context): RecyclerView.Adapter<ProfileCoursesAdapter.ProfileCoursesViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileCoursesViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.profile_else_course_layout, parent, false)
        return ProfileCoursesViewHolder(view)
    }

    override fun getItemCount(): Int = userViewModelReference.ViewedUser.value!!.Courses.size

    override fun onBindViewHolder(holder: ProfileCoursesViewHolder, position: Int)
    {
        val course = userViewModelReference.ViewedUser.value!!.Courses[position]
        val container = holder.itemView as ConstraintLayout
        val courseIcon: AppCompatImageView = container.findViewById(R.id.course_layout_icon_imageview)
        val courseNameTextView: AppCompatTextView = container.findViewById(R.id.course_layout_course_textview)
        val expAmountTextView: AppCompatTextView = container.findViewById(R.id.course_layout_exp_textview)
        courseIcon.setImageDrawable(context.resources.getDrawable(context.resources.getIdentifier(course.Name, "drawable", context.packageName)))
        courseNameTextView.text = course.Name
        expAmountTextView.text = course.Exp.toString()
    }

    class ProfileCoursesViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
}