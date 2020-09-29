package com.jftech.babylonia.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jftech.babylonia.R
import com.jftech.babylonia.connectors.UserViewModel
import com.jftech.babylonia.ui.recycler_adapters.LessonsViewAdapter

class LessonsFragment(private val userViewModelReference: UserViewModel): Fragment()
{
    private lateinit var lessonsRecyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.lessons_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        view.post {
            wireViews(view)
            setupRecyclerView()
        }
    }

    private fun setupRecyclerView()
    {
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        layoutManager.stackFromEnd = true
        lessonsRecyclerView.layoutManager = layoutManager
        lessonsRecyclerView.adapter = LessonsViewAdapter(userViewModelReference, requireContext())
        lessonsRecyclerView.scrollToPosition(0)
    }

    private fun wireViews(view: View)
    {
        lessonsRecyclerView = view.findViewById(R.id.lesson_groups_recyclerview)
    }
}