package com.jftech.babylonia.ui.login.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.jftech.babylonia.R
import com.jftech.babylonia.data.UserRepository
import com.jftech.babylonia.ui.AnimateShake
import com.jftech.babylonia.ui.login.LoginActivity

class RegisterFragment: Fragment()
{
    private lateinit var authenticator: FirebaseAuth
    private lateinit var loginButton: AppCompatButton
    private lateinit var emailTextEdit: AppCompatEditText
    private lateinit var passwordTextEdit: AppCompatEditText
    private  lateinit var registerButton: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        authenticator = FirebaseAuth.getInstance()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.login_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        loginButton = view.findViewById(R.id.login_button)
        emailTextEdit = view.findViewById(R.id.login_email_textedit)
        passwordTextEdit = view.findViewById(R.id.login_password_textedit)
        registerButton = view.findViewById(R.id.register_button)
        registerButton.text = getString(R.string.register_login_button)
        loginButton.text = "Sign Up"
        loginButton.setOnClickListener {
            authenticator.createUserWithEmailAndPassword(emailTextEdit.text.toString(), passwordTextEdit.text.toString()).addOnCompleteListener { task ->
                if (task.isSuccessful)
                {
                    UserRepository.PostRegistration(authenticator.uid!!, emailTextEdit.text.toString(), this)
                }
                else
                    loginFailed((task.exception as FirebaseAuthException).errorCode, task.exception!!.localizedMessage!!)
            }
        }
        registerButton.setOnClickListener {
            (requireContext() as LoginActivity).RetunToLoginPage()
        }
    }

    fun OnUserRegistrationComplete()
    {
        (requireContext() as LoginActivity).Login(authenticator.currentUser!!.uid)
    }

    private fun loginFailed(error: String, message: String)
    {
        when (error)
        {
            "ERROR_WEAK_PASSWORD" -> passwordTextEdit.AnimateShake(requireContext())
            "ERROR_INVALID_EMAIL" -> emailTextEdit.AnimateShake(requireContext())
            "ERROR_EMAIL_ALREADY_IN_USE" -> emailTextEdit.AnimateShake(requireContext())
            else -> this.requireView().AnimateShake(requireContext())
        }
        Toast.makeText(this.requireContext(), message, Toast.LENGTH_LONG).show()
    }
}