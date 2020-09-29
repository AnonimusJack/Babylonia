package com.jftech.babylonia.ui.lesson.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import com.jftech.babylonia.R
import com.jftech.babylonia.connectors.LessonViewModel
import com.jftech.babylonia.interfaces.lesson.IExercisable

class ChooseOneFragment(private val options: ArrayList<String>, private val viewModelReference: LessonViewModel): Fragment(), IExercisable
{
    private lateinit var translateTextView: AppCompatTextView
    private lateinit var choiceButtonGroup: Array<AppCompatButton>
    private lateinit var firstChoice: AppCompatButton
    private lateinit var secondChoice: AppCompatButton
    private lateinit var thirdChoice: AppCompatButton
    override var UserAnswer: String = ""
        set(value) {
            if (field.isBlank())
                viewModelReference.OnAnswerNotEmpty()
            else if (value.isBlank())
                viewModelReference.OnAnswerIsEmpy()
            field = value
        }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.lesson_choose_one_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        view.post {
            translateTextView = view.findViewById(R.id.lesson_choice_translate_textview)
            firstChoice = view.findViewById(R.id.lesson_choice_one_button)
            secondChoice = view.findViewById(R.id.lesson_choice_two_button)
            thirdChoice = view.findViewById(R.id.lesson_choice_three_button)
            choiceButtonGroup = arrayOf(firstChoice, secondChoice, thirdChoice)
            setChoicesText()
            setEventsForChoiceButtons()
        }
    }

    private fun setEventsForChoiceButtons()
    {
        for (button in choiceButtonGroup)
            button.setOnClickListener {
                resetButtonHighlightState()
                UserAnswer = button.text.toString()
                button.isSelected = true
            }
    }

    private fun resetButtonHighlightState()
    {
        for (button in choiceButtonGroup)
            button.isSelected = false
    }

    private fun setChoicesText()
    {
        for (i in 0 until 3)
            choiceButtonGroup[i].text = options[i]
    }
}