package com.jftech.babylonia.ui.main.auxiliary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.jftech.babylonia.R
import com.jftech.babylonia.databinding.SettingsChangePasswordDiaglogFragmentBinding
import com.jftech.babylonia.ui.AnimateShake

class ChangePasswordDialog: DialogFragment()
{
    private lateinit var binding: SettingsChangePasswordDiaglogFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = SettingsChangePasswordDiaglogFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        view.post {
            binding.settingsChangePasswordApplyButton.setOnClickListener { onApplyButtonClicked() }
            binding.settingsChangePasswordCancelButton.setOnClickListener { onCancelButtonClicked() }
        }
    }

    private fun onApplyButtonClicked()
    {
        if (binding.settingsChangePasswordTextedit.text!!.isNotEmpty() && binding.settingsOldPasswordTextedit.text!!.isNotEmpty())
        {
            val user = FirebaseAuth.getInstance().currentUser
            val credentials = EmailAuthProvider.getCredential(user!!.email!!, binding.settingsOldPasswordTextedit.text.toString())
            user.reauthenticate(credentials).addOnCompleteListener {
                if (it.isSuccessful)
                {
                    user.updatePassword(binding.settingsChangePasswordTextedit.text.toString()).addOnCompleteListener {
                        if (it.isSuccessful)
                        {
                            Toast.makeText(requireContext(), "Password Changed successfully!", Toast.LENGTH_SHORT).show()
                            dismiss()
                        }
                        else
                            Toast.makeText(requireContext(), it.exception!!.message, Toast.LENGTH_LONG).show()
                    }
                }
                else
                    Toast.makeText(requireContext(), it.exception!!.message, Toast.LENGTH_LONG).show()
            }
        }
        else
        {
            binding.settingsChangePasswordApplyButton.AnimateShake(requireContext())
            Toast.makeText(requireContext(), "Password or Old Password fields cannot be empty", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onCancelButtonClicked()
    {
        dismiss()
    }
}