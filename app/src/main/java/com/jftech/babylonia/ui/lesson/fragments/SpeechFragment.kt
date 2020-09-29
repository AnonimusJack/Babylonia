package com.jftech.babylonia.ui.lesson.fragments

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.jftech.babylonia.R
import com.jftech.babylonia.connectors.LessonViewModel
import com.jftech.babylonia.interfaces.lesson.IExercisable
import com.jftech.babylonia.ui.AnimateShake
import java.util.*

class SpeechFragment(private val voiceLocale: Locale, private val question: String, private val viewModelReference: LessonViewModel): Fragment(), RecognitionListener, IExercisable
{
    private val permission = 100
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechIntent: Intent
    private lateinit var textToTranslateTextView: AppCompatTextView
    private lateinit var speechButton: AppCompatButton
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
        speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, voiceLocale)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.lesson_speach_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        view.post {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
            speechRecognizer.setRecognitionListener(this)
            wireViews(view)
            setTextToTranslate()
            speechButton.setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean
                {
                    when (motionEvent?.action) {
                        MotionEvent.ACTION_DOWN -> { startRecognizer() }
                        MotionEvent.ACTION_CANCEL -> { stopRecognizer()  }
                        MotionEvent.ACTION_UP   -> { stopRecognizer() }
                    }
                    return view?.onTouchEvent(motionEvent) ?: true
                }
            })
        }
    }

    override fun onStart()
    {
        super.onStart()
        if (context != null)
        {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
            speechRecognizer.setRecognitionListener(this)
        }
    }

    override fun onStop()
    {
        super.onStop()
        speechRecognizer.destroy()
    }

    override fun onError(error: Int)
    {
        speechButton.AnimateShake(requireContext())
        Toast.makeText(requireContext(), getErrorText(error), Toast.LENGTH_LONG).show()
    }

    override fun onResults(results: Bundle?)
    {
        if (results != null)
        {
            if (results.getIntArray(SpeechRecognizer.CONFIDENCE_SCORES)!![0] > 80)
                UserAnswer = results.getStringArray(SpeechRecognizer.RESULTS_RECOGNITION)!![0]
        }
    }

    override fun onReadyForSpeech(p0: Bundle?) {}

    override fun onRmsChanged(p0: Float) {}

    override fun onBufferReceived(p0: ByteArray?) {}

    override fun onEndOfSpeech() {}

    override fun onPartialResults(p0: Bundle?) {}

    override fun onEvent(p0: Int, p1: Bundle?) {}

    override fun onBeginningOfSpeech() {}

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permission)
        {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                speechRecognizer.startListening(speechIntent)
            else
                Toast.makeText(requireContext(), "Permission Denied!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startRecognizer()
    {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.RECORD_AUDIO), permission)
    }

    private fun stopRecognizer()
    {
        speechRecognizer.stopListening()
    }

    private fun getErrorText(error: Int): String
    {
        var message = ""
        message = when (error)
        {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Didn't understand, please try again."
        }
        return message
    }

    private fun wireViews(view: View)
    {
        textToTranslateTextView = view.findViewById(R.id.lesson_speech_text_to_translate_textview)
        speechButton = view.findViewById(R.id.lesson_speech_toggle_listen_button)
    }

    private fun setTextToTranslate()
    {
        textToTranslateTextView.text = question
    }
}