package com.jftech.babylonia.ui.main

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.jftech.babylonia.R
import com.jftech.babylonia.connectors.UserViewModel
import com.jftech.babylonia.ui.main.auxiliary.LessonTopbarFragment
import com.jftech.babylonia.ui.main.auxiliary.NavbarFragment
import com.jftech.babylonia.ui.main.auxiliary.ProfileTopbarFragment
import com.jftech.babylonia.ui.main.auxiliary.RankingTopbarFragment
import com.jftech.babylonia.ui.main.fragments.LessonsFragment
import com.jftech.babylonia.ui.main.fragments.ProfileFragment
import com.jftech.babylonia.ui.main.fragments.RankingFragment

const val LESSON_REQUEST_CODE = 111



class MainActivity : AppCompatActivity()
{
    private lateinit var userViewModelReference: UserViewModel



    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        userViewModelReference = ViewModelProvider(this, UserViewModel.UserViewModelFactory(this)).get(UserViewModel::class.java)
        userViewModelReference.CurrentCourse.observe(this, Observer { layoutForTab(userViewModelReference.CurrentTab) })
    }

    override fun onResume()
    {
        super.onResume()
        layoutForTab(userViewModelReference.CurrentTab)
        replaceFragmentFor(R.id.main_activity_tabbar, NavbarFragment(userViewModelReference))
    }

    override fun onBackPressed() { moveTaskToBack(true) }

    fun ShowPopOver(popoverFragment: Fragment, popOverTag: String)
    {
        val popOverFragment = supportFragmentManager.findFragmentByTag(popOverTag)
        if (popOverFragment != null)
            removePopOver(popOverFragment)
        else
            replaceFragmentFor(R.id.main_activity_popover, popoverFragment, popOverTag)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LESSON_REQUEST_CODE && resultCode == Activity.RESULT_OK)
            userViewModelReference.OnLessonFinished(data!!.getIntExtra("result", 0))
    }

    fun OnTabSet(tabType: UserViewModel.TabType)
    {
        layoutForTab(tabType)
    }

    fun OnUserProfileRequested()
    {
        layoutForTab(UserViewModel.TabType.Profile)
    }

    private fun layoutForTab(tabType: UserViewModel.TabType)
    {
        when (tabType)
        {
            UserViewModel.TabType.Lessons ->
            {
                replaceFragmentFor(R.id.main_activity_topbar, LessonTopbarFragment(userViewModelReference))
                replaceFragmentFor(R.id.main_activity_fragment_container, LessonsFragment(userViewModelReference))
            }
            UserViewModel.TabType.Profile ->
            {
                replaceFragmentFor(R.id.main_activity_topbar, ProfileTopbarFragment(userViewModelReference))
                replaceFragmentFor(R.id.main_activity_fragment_container, ProfileFragment(userViewModelReference, userViewModelReference.UsedUserID))
            }
            UserViewModel.TabType.Ranking ->
            {
                replaceFragmentFor(R.id.main_activity_topbar, RankingTopbarFragment(userViewModelReference))
                replaceFragmentFor(R.id.main_activity_fragment_container, RankingFragment(userViewModelReference))
            }
            UserViewModel.TabType.Store -> TODO()
        }
    }

    private fun replaceFragmentFor(container: Int, fragment: Fragment, tag: String? = null)
    {
        val transaction = supportFragmentManager.beginTransaction()
        if (tag != null)
            transaction.replace(container, fragment, tag)
        else
            transaction.replace(container, fragment)
        transaction.commit()
    }

    private fun removePopOver(popOverFragment: Fragment)
    {
        val transactin = supportFragmentManager.beginTransaction()
        transactin.remove(popOverFragment)
        transactin.commit()
    }
}