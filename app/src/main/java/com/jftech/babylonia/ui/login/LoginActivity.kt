package com.jftech.babylonia.ui.login

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.jftech.babylonia.R
import com.jftech.babylonia.data.models.AppSettings
import com.jftech.babylonia.ui.login.fragments.LoginFragment
import com.jftech.babylonia.ui.login.fragments.RegisterFragment
import com.jftech.babylonia.ui.main.MainActivity

class LoginActivity: AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
        AppSettings.Initialize(applicationContext)
        replaceFragment(LoginFragment())
    }

    fun Login(userID: String)
    {
        val intentToMain = Intent(this, MainActivity::class.java)
        startActivity(intentToMain)
    }

    fun GoToRegisterationPage()
    {
        replaceFragment(RegisterFragment())
    }

    fun RetunToLoginPage()
    {
        replaceFragment(LoginFragment())
    }

    private fun replaceFragment(fragment: Fragment)
    {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.login_activity_fragment_container, fragment)
        transaction.commit()
    }
}