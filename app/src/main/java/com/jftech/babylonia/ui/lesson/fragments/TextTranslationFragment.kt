package com.jftech.babylonia.ui.lesson.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import com.jftech.babylonia.R
import com.jftech.babylonia.connectors.LessonViewModel
import com.jftech.babylonia.interfaces.lesson.IExercisable

class TextTranslationFragment(private val question: String, private val viewModelReference: LessonViewModel): Fragment(), IExercisable
{
    private lateinit var textToTranslateTextView: AppCompatTextView
    private lateinit var translateEditText: AppCompatEditText
    override var UserAnswer: String = ""
        set(value) {
            if (field.isEmpty() && value.isNotEmpty())
                viewModelReference.OnAnswerNotEmpty()
            else if (value.isEmpty())
                viewModelReference.OnAnswerIsEmpy()
            field = value
        }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.lesson_text_translastion_frament, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        view.post {
            wireViews(view)
            setTextToTranslate()
            translateEditText.addTextChangedListener(object: TextWatcher{
                override fun afterTextChanged(text: Editable?) {}
                override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(text: CharSequence?, start: Int, count: Int, after: Int)
                {
                    UserAnswer = text.toString()
                }
            })
        }
    }

    private fun wireViews(view: View)
    {
        textToTranslateTextView = view.findViewById(R.id.lesson_translate_text_to_translate_textview)
        translateEditText = view.findViewById(R.id.lesson_translate_user_translation_textedit)
    }

    private fun setTextToTranslate()
    {
        textToTranslateTextView.text = question
    }
}