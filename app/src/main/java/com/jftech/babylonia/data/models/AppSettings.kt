package com.jftech.babylonia.data.models

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.widget.AppCompatButton
import androidx.databinding.BindingAdapter
import com.jftech.babylonia.connectors.UserViewModel

object AppSettings
{
    private lateinit var sharedPreferences: SharedPreferences
    lateinit var AppContext: Context
    var UserViewModelReference: UserViewModel? = null
    var SoundEffectsOn: Boolean = false
        set(value)
        {
            field = value
            sharedPreferences.edit().putBoolean("soundEffectsOn", SoundEffectsOn).apply()
        }
    var MotivationalMessagesOn: Boolean = false
        set(value)
        {
            field = value
            sharedPreferences.edit().putBoolean("motivationalMessagesOn", MotivationalMessagesOn).apply()
        }
    var SpeechExercisesOn: Boolean = false
        set(value)
        {
            field = value
            sharedPreferences.edit().putBoolean("speechExercisesOn", SpeechExercisesOn).apply()
        }
    var HearingExercisesOn: Boolean = false
        set(value)
        {
            field = value
            sharedPreferences.edit().putBoolean("hearingExercisesOn", HearingExercisesOn).apply()
        }
    var LocalReminderOn: Boolean = false
        set(value)
        {
            field = value
            sharedPreferences.edit().putBoolean("localReminderOn", LocalReminderOn).apply()
        }
    var ReminderTime: Int = 10
        set(value)
        {
            field = if (value > 23 || value < 0) 0 else value
            sharedPreferences.edit().putInt("reminderTime", ReminderTime).apply()
        }
    @BindingAdapter("text")
    @JvmStatic
    fun SetTextForReminderTime(button: AppCompatButton, hour: Int)
    {
        button.text = (if (hour > 9) hour.toString() else "0" + hour.toString()) + ":00"
    }

    fun Initialize(context: Context)
    {
        AppContext = context
        sharedPreferences = context.getSharedPreferences(
            "babyloniaAppSettings",
            Context.MODE_PRIVATE
        )
        SoundEffectsOn = sharedPreferences.getBoolean("soundEffectsOn", false)
        MotivationalMessagesOn = sharedPreferences.getBoolean("motivationalMessagesOn", false)
        SpeechExercisesOn = sharedPreferences.getBoolean("speechExercisesOn", false)
        HearingExercisesOn = sharedPreferences.getBoolean("hearingExercisesOn", false)
        LocalReminderOn = sharedPreferences.getBoolean("localReminderOn", false)
        //ReminderTime = sharedPreferences.getInt("reminderTime", 10)
    }
}