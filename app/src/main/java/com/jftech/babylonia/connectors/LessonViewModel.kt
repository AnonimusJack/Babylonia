package com.jftech.babylonia.connectors

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jftech.babylonia.data.LessonRepository
import com.jftech.babylonia.data.models.Exercise
import com.jftech.babylonia.ui.lesson.LessonActivity
import com.jftech.babylonia.ui.lesson.fragments.*
import java.util.*

class LessonViewModel(private val lessonActivityReference: LessonActivity, private val lessonID: String, private val courseName: String): ViewModel()
{
    private val lessonRepository = LessonRepository(lessonActivityReference, this)
    private var lessonFinished = false
    private var currentExercise = 0
    private var correctInARow = 0
    private var mistakes = 0
    private var currentLesson: MutableLiveData<Array<Exercise>> = MutableLiveData()
    val CurrentLesson: LiveData<Array<Exercise>>
        get() {return currentLesson as LiveData<Array<Exercise>> }



    fun OnResume()
    {
        lessonRepository.RequestLesson(lessonID, courseName.toLowerCase())
    }

    fun OnStop()
    {
        if (lessonFinished)
            lessonRepository.OnLessonFinished()
        else
            lessonRepository.OnStop()
    }

    fun OnLessonFinish()
    {
        lessonFinished = true
    }

    fun RequestAnswerCheck()
    {
        val currentLessonFragment = lessonActivityReference.RequestCurrentLessonFragment()
        val answer = currentLesson.value!![currentExercise].CheckIFAnswerIsCorrect(currentLessonFragment.UserAnswer)
        postToActivity(answer)
    }

    fun OnPairExerciseFinished(userAnswer: Boolean)
    {
        postToActivity(userAnswer)
        lessonActivityReference.ToggleCheckButton(true)
    }

    fun OnAnswerNotEmpty()
    {
        lessonActivityReference.ToggleCheckButton(true)
    }

    fun OnAnswerIsEmpy()
    {
        lessonActivityReference.ToggleCheckButton(false)
    }

    fun SetCurrentLesson(exercises: Array<Exercise>)
    {
        currentLesson.value = exercises
    }

    fun RequestFirstExerciseFragment(): Fragment
    {
        return fragmentForType()!!
    }

    fun CalculateExpForLesson(): Int
    {
        val bonus = 5 - mistakes
        return currentExercise + if (bonus < 0) 0 else bonus
    }

    private fun postToActivity(userAnswer: Boolean)
    {
        currentLesson.value!![currentExercise].Finished = userAnswer
        lessonActivityReference.OnExerciseComplete(correctInARow, nextExercise(), userAnswer)
        if (userAnswer)
            correctInARow++
        else
        {
            correctInARow = 0
            mistakes++
        }
    }

    private fun nextExercise(): Fragment?
    {
        currentExercise++
        return fragmentForType()
    }

    private fun fragmentForType(): Fragment?
    {
        val currentExerciseData = if (currentExercise < currentLesson.value!!.size)
            currentLesson.value!![currentExercise]
        else
            Exercise()

        return when(currentExerciseData.Type)
        {
            "choice" -> ChooseOneFragment(currentExerciseData.Questions, this)
            "hearing" -> HearingFragment(currentExerciseData.Questions[0], this)
            "pair" -> PairsFragment(currentExerciseData.Questions, currentExerciseData.Answers, this)
            "speech" -> SpeechFragment(Locale.ENGLISH, currentExerciseData.Questions[0], this)
            "translate" -> TextTranslationFragment(currentExerciseData.Questions[0], this)
            "word_bank" -> WordBankFragment(currentExerciseData.Questions.toTypedArray().copyOfRange(1, currentExerciseData.Questions.lastIndex), currentExerciseData.Questions[0], this)
            else -> null
        }
    }
}