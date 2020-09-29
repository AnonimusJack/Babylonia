package com.jftech.babylonia.ui.main.auxiliary

import android.app.Activity
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.jftech.babylonia.connectors.UserViewModel
import com.jftech.babylonia.data.models.AppSettings
import com.jftech.babylonia.databinding.SettingsFragmentBinding
import java.util.jar.Manifest

const val IMAGE_PICK_CODE = 1000
const val PERMISSION_CODE = 1001

class SettingsFragment(private val userViewModelReference: UserViewModel): DialogFragment()
{
    private lateinit var binding: SettingsFragmentBinding



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = SettingsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        view.post {
            binding.user = userViewModelReference.CurrentUser.value!!
            binding.settings = AppSettings
            binding.settingsChangePasswordButton.setOnClickListener { onChangePasswordClicked() }
            binding.settingsReminderTimeButton.setOnClickListener { onReminderTimeClicked() }
            binding.settingsLogoutButton.setOnClickListener { onLogoutClicked() }
            binding.settingsChangeProfilePictureButton.setOnClickListener { onChangeAvatarClicked() }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        when (requestCode)
        {
            PERMISSION_CODE ->
            {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    pickImageFromGallery()
                else
                    Toast.makeText(this.requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE)
        {
            userViewModelReference.UploadProfilePicute(BitmapFactory.decodeFile(data?.data.toString()))
            binding.settingsProfilePictureImageview.setImageURI(data?.data)
        }
    }

    override fun onDestroy()
    {
        userViewModelReference.OnSettingsFinished()
        super.onDestroy()
    }

    private fun onChangePasswordClicked()
    {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        ChangePasswordDialog().show(transaction, "change_password_dialog")
    }

    private fun onReminderTimeClicked()
    {
        TimePickerDialog(requireContext(), TimePickerDialog.OnTimeSetListener { _, hours, _ -> AppSettings.ReminderTime = hours }, AppSettings.ReminderTime, 0, true).show()
    }

    private fun onLogoutClicked()
    {
        FirebaseAuth.getInstance().signOut()
        requireActivity().finish()
    }

    private fun onChangeAvatarClicked()
    {
        if (requireContext().checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_CODE)
        else
            pickImageFromGallery()
    }

    private fun pickImageFromGallery()
    {
        val pickImageIntent = Intent(Intent.ACTION_PICK)
        pickImageIntent.type = "image/*"
        startActivityForResult(pickImageIntent, IMAGE_PICK_CODE)
    }
}