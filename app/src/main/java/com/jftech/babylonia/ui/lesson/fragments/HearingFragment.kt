package com.jftech.babylonia.ui.lesson.fragments

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import com.jftech.babylonia.R
import com.jftech.babylonia.connectors.LessonViewModel
import com.jftech.babylonia.interfaces.lesson.IExercisable

class HearingFragment(private val recordURL: String, private val viewModelReference: LessonViewModel): Fragment(), IExercisable
{
    private lateinit var voiceClipPlayer: MediaPlayer
    private lateinit var startVoiceClipButton: AppCompatImageButton
    private lateinit var startSlowVoiceClipButton: AppCompatImageButton
    private lateinit var lessonUserAnswerTextEdit: AppCompatEditText
    override var UserAnswer: String = ""
        set(value) {
            if (field.isEmpty() && value.isNotEmpty())
                viewModelReference.OnAnswerNotEmpty()
            else if (value.isEmpty())
                viewModelReference.OnAnswerIsEmpy()
            field = value
        }


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        voiceClipPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(recordURL)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.lesson_hearing_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        view.post {
            startVoiceClipButton = view.findViewById(R.id.lesson_hearing_hear_button)
            startSlowVoiceClipButton = view.findViewById(R.id.lesson_hearing_hear_slowly_button)
            lessonUserAnswerTextEdit = view.findViewById(R.id.lesson_hearing_translation_textedit)
            startVoiceClipButton.setOnClickListener { playVoiceClipWithSpeed(1f) }
            startSlowVoiceClipButton.setOnClickListener { playVoiceClipWithSpeed(0.25f) }
            lessonUserAnswerTextEdit.addTextChangedListener(object: TextWatcher {
                override fun afterTextChanged(text: Editable?) {}
                override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(text: CharSequence?, start: Int, count: Int, after: Int)
                {
                    UserAnswer = text.toString()
                }
            })
        }
    }

    override fun onResume()
    {
        super.onResume()
        voiceClipPlayer.prepare()
    }

    override fun onPause()
    {
        super.onPause()
        voiceClipPlayer.stop()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        voiceClipPlayer.release()
    }

    private fun playVoiceClipWithSpeed(playBackSpeed: Float)
    {
        voiceClipPlayer.playbackParams = voiceClipPlayer.playbackParams.apply { speed = playBackSpeed }
        voiceClipPlayer.start()
    }
}