package com.jftech.babylonia.ui.main.auxiliary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.jftech.babylonia.R
import com.jftech.babylonia.connectors.UserViewModel

class LessonTopbarFragment(private val userViewModelReference: UserViewModel): Fragment()
{
    enum class TopbarType {Course, Medal, Streak, Gold}
    private lateinit var courseIconImageView: AppCompatImageView
    private lateinit var medalCountTextView: AppCompatTextView
    private lateinit var streakCountTextView: AppCompatTextView
    private lateinit var goldCountTextView: AppCompatTextView



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.main_lesson_topbar_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        view.post {
            wireView(view)
            setAmountsToTextVies()
            setOnClickForContainers()
            userViewModelReference.CurrentCourse.observe(viewLifecycleOwner, Observer {
                onCourseChanged(it.Name)
            })
        }
    }

    private fun setOnClickForContainers()
    {
        courseIconImageView.setOnClickListener { userViewModelReference.RequestPopOverFor(TopbarType.Course) }
    }

    private fun setAmountsToTextVies()
    {
        medalCountTextView.text = userViewModelReference.CurrentCourse.value!!.Medals.toString()
        streakCountTextView.text = userViewModelReference.CurrentUser.value!!.Streak.toString()
        goldCountTextView.text = userViewModelReference.CurrentUser.value!!.Gold.toString()
    }

    private fun wireView(view: View)
    {
        courseIconImageView = view.findViewById(R.id.topbar_course_icon)
        medalCountTextView = view.findViewById(R.id.topbar_medals_textview)
        streakCountTextView = view.findViewById(R.id.topbar_streak_textview)
        goldCountTextView = view.findViewById(R.id.topbar_gold_textview)
    }

    private fun onCourseChanged(courseName: String)
    {
        if (courseName.isNotEmpty())
            courseIconImageView.setImageDrawable(requireContext().getDrawable(requireContext().resources.getIdentifier(courseName.toLowerCase(), "drawable", requireContext().packageName)))
    }
}