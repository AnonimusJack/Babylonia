package com.jftech.babylonia.ui.recycler_adapters

import android.content.Context
import android.content.Intent
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.jftech.babylonia.R
import com.jftech.babylonia.connectors.UserViewModel
import com.jftech.babylonia.data.models.Course
import com.jftech.babylonia.ui.lesson.LessonActivity
import com.jftech.babylonia.ui.main.LESSON_REQUEST_CODE
import com.jftech.babylonia.ui.main.MainActivity

class LessonsViewAdapter(private val userViewModelReference: UserViewModel, private val context: Context): RecyclerView.Adapter<LessonsViewAdapter.LessonsViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonsViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.lessons_group_layout, parent, false)
        return LessonsViewHolder(view)
    }

    override fun onBindViewHolder(holder: LessonsViewHolder, position: Int)
    {
        val lessonGroupContainer = holder.itemView as ConstraintLayout
        val lessonsContainer: FlexboxLayout = lessonGroupContainer.findViewById(R.id.lesson_group)
        val floorImageView: AppCompatImageView = lessonGroupContainer.findViewById(R.id.lesson_group_floor_imageview)
        buildFloorImageView(position, floorImageView)
        populateFloorWithLessons(position, lessonsContainer)
    }

    override fun getItemCount(): Int { return userViewModelReference.CurrentCourse.value!!.MaxFloor + 1}

    private fun populateFloorWithLessons(position: Int,lessonsContainer: FlexboxLayout)
    {
        val maxLevel = userViewModelReference.CurrentCourse.value!!.MaxLevelForFloor(position)
        for (level in 1..maxLevel)
        {
            val lessonsForThisLevel = userViewModelReference.CurrentCourse.value!!.Lessons.filter { lesson -> lesson.Level == level }
            val levelContainer = FlexboxLayout(context)
            for (lesson in lessonsForThisLevel)
                levelContainer.addView(buildLessonView(lesson))
            lessonsContainer.addView(levelContainer)
        }
    }

    private fun buildLessonView(lesson: Course.Lesson): ConstraintLayout
    {
        val lessonLayout = LayoutInflater.from(context).inflate(R.layout.lesson_group_lesson_layout, null) as ConstraintLayout
        val lessonImage: AppCompatImageView = lessonLayout.findViewById(R.id.lesson_group_lesson_imageview)
        val lessonTextView: AppCompatTextView = lessonLayout.findViewById(R.id.lesson_group_lesson_textview)
        lessonImage.setImageDrawable(context.getDrawable(context.resources.getIdentifier(lesson.Name.toLowerCase().replace("\\s".toRegex(), ""), "drawable", context.packageName)))
        lessonImage.setOnClickListener {
            val lessonIntent = Intent(context, LessonActivity::class.java)
            lessonIntent.putExtra("lesson_id", lesson.ID)
            (context as MainActivity).startActivityForResult(lessonIntent, LESSON_REQUEST_CODE)
        }
        lessonTextView.text = lesson.Name
        return lessonLayout
    }

    private fun buildFloorImageView(position: Int, imageView: AppCompatImageView)
    {
        //imageView.setImageDrawable(context.getDrawable(context.resources.getIdentifier("floor $position", "drawable", context.packageName)))
        imageView.setOnClickListener { Log.d("Floor Challenge", "Initiated~") }
        // TODO: 07/09/2020 OnClickRequestFloorTest
    }

    class LessonsViewHolder(viewItem: View) : RecyclerView.ViewHolder(viewItem)
}