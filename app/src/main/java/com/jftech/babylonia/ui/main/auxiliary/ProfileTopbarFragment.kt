package com.jftech.babylonia.ui.main.auxiliary

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import com.jftech.babylonia.R
import com.jftech.babylonia.connectors.UserViewModel

class ProfileTopbarFragment(private val userViewModelReference: UserViewModel): Fragment()
{
    private lateinit var settingsButton: AppCompatImageButton



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.profile_topbar_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        view.post {
            wireViews(view)
            settingsButton.setOnClickListener {
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                SettingsFragment(userViewModelReference).show(transaction, "settings_dialog")
            }
        }
    }


    private fun wireViews(view: View)
    {
        settingsButton = view.findViewById(R.id.profile_settings_button)
    }
}