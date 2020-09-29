package com.jftech.babylonia.ui.main.auxiliary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import com.jftech.babylonia.R
import com.jftech.babylonia.connectors.UserViewModel

class NavbarFragment(private val userViewModelReference: UserViewModel): Fragment()
{
    private lateinit var lessonTabIcon: AppCompatImageView
    private lateinit var profileTabIcon: AppCompatImageView
    private lateinit var rankingTabIcon: AppCompatImageView
    private lateinit var storeTabIcon: AppCompatImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.navbar_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        view.post {
            wireViews(view)
            setOnclickEvents()
        }
    }


    private fun wireViews(view: View)
    {
        lessonTabIcon = view.findViewById(R.id.navbar_lessons_icon)
        profileTabIcon = view.findViewById(R.id.navbar_profile_icon)
        rankingTabIcon = view.findViewById(R.id.navbar_ranking_icon)
        storeTabIcon = view.findViewById(R.id.navbar_store_icon)
    }

    private fun setOnclickEvents()
    {
        lessonTabIcon.setOnClickListener { userViewModelReference.CurrentTab = UserViewModel.TabType.Lessons }
        profileTabIcon.setOnClickListener { userViewModelReference.RequestToShowUserProfile(userViewModelReference.CurrentUser.value!!.ID) }
        rankingTabIcon.setOnClickListener { userViewModelReference.CurrentTab = UserViewModel.TabType.Ranking }
        storeTabIcon.setOnClickListener { Toast.makeText(requireContext(), "Store Coming soon!!", Toast.LENGTH_SHORT).show() }
    }
}