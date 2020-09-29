package com.jftech.babylonia.ui.main.auxiliary

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jftech.babylonia.R
import com.jftech.babylonia.connectors.UserViewModel
import com.jftech.babylonia.ui.recycler_adapters.CoursesPopOverAdapter

class CoursesPopOver(private var userViewModelReference: UserViewModel): Fragment()
{
    private var coursesRecyclerView: RecyclerView? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.courses_popover_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        view.post {
            wireViews(view)
            buildRecyclerView()
        }
    }

    override fun onResume()
    {
        super.onResume()
        if (view != null)
            if (coursesRecyclerView != null)
                (coursesRecyclerView!!.adapter as CoursesPopOverAdapter).UpdateSelected(userViewModelReference.CurrentCourse.value!!.Name)
    }


    private fun buildRecyclerView()
    {
        coursesRecyclerView!!.layoutManager = LinearLayoutManager(this.requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val formatedCourses: MutableList<CoursesPopOverAdapter.CourseHolder> = mutableListOf()
        userViewModelReference.CurrentUser.value!!.Courses.mapTo(formatedCourses) { course -> CoursesPopOverAdapter.CourseHolder(course.Name, false) }
        formatedCourses.add(CoursesPopOverAdapter.CourseHolder("addCourse", false))
        coursesRecyclerView!!.adapter = CoursesPopOverAdapter(formatedCourses.toTypedArray(), requireContext(), userViewModelReference)
        (coursesRecyclerView!!.adapter as CoursesPopOverAdapter).UpdateSelected(userViewModelReference.CurrentCourse.value!!.Name)
    }

    private fun wireViews(view: View)
    {
        coursesRecyclerView = view.findViewById(R.id.courses_recyclerview)
    }
}