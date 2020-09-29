package com.jftech.babylonia.ui.lesson.fragments

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatToggleButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import com.google.android.flexbox.FlexboxLayout
import com.jftech.babylonia.R
import com.jftech.babylonia.connectors.LessonViewModel
import com.jftech.babylonia.interfaces.lesson.IExercisable

class PairsFragment(private  val aPairs: ArrayList<String>, private val bPairs: ArrayList<String>, private val viewModelReference: LessonViewModel): Fragment(), IExercisable
{
    private lateinit var wordPairsContainer: FlexboxLayout
    private val pairCount = aPairs.size
    private var answeredPair = Array<Int>(2) { _ -> 0 }
    private var wrongMatches = 0
        set(value) {
            field = value
            if (field > 3)
                UserAnswer = "false"
        }
    private var matchesCount = 0
        set(value) {
            field = value
            if (field == pairCount)
                UserAnswer = "true"
        }
    override var UserAnswer = ""
        set(value) {
            field = value
            viewModelReference.OnPairExerciseFinished(UserAnswer.toBoolean())
        }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.lesson_pairs_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        view.post {
            wordPairsContainer = view.findViewById(R.id.lesson_pairs_word_pairs_flexbox)
            layoutPairs()
        }
    }

    private fun layoutPairs()
    {
        val pairsCount = aPairs.size + 1
        val pairs: MutableList<View> = mutableListOf()
        for (i in 1 until pairsCount)
        {
            val correctIndex = i - 1
            pairs.add(buildWordBubbleForPair(aPairs[correctIndex], (i * -1)))
            pairs.add(buildWordBubbleForPair(bPairs[correctIndex], i))
        }
        pairs.shuffle()
        for (view in pairs)
            wordPairsContainer.addView(view)
    }

    private fun buildWordBubbleForPair(word: String, tag: Int): View
    {
        val params = FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            setMargins(5)
        }
        val wordBubbleContainer = View.inflate(requireContext(), R.layout.word_bubble_layout, null) as ConstraintLayout
        wordBubbleContainer.layoutParams = params
        val wordBubble: AppCompatToggleButton = wordBubbleContainer.findViewById(R.id.word_bubble)
        wordBubble.text = word
        wordBubble.textOn = word
        wordBubble.textOff = word
        wordBubbleContainer.tag = tag
        wordBubble.tag = tag
        wordBubble.setOnClickListener {
            val castIt = it as AppCompatToggleButton
            if (castIt.isChecked)
                checkedBackground(tag)
            else
                resetBackground(tag)
            onWordClick(tag)
        }
        return wordBubbleContainer
    }

    private fun onWordClick(tag: Int)
    {
        if (onePairIsEmpty() && noPairContainsSelected(tag))
            setPairValue(tag)
        else
            desetPairValue(tag)
        if (answeredPair[0] != 0 && answeredPair[1] != 0)
            if (answeredPair[0] == (answeredPair[1] * -1))
            {
                val tagA = answeredPair[0]
                val tagB = answeredPair[1]
                matchesCount++
                disablePair(tagA, tagB)
                resetPairs(tagA, tagB)
                animateSuccess(wordBubbleForTag(tagA))
                animateSuccess(wordBubbleForTag(tagB))
            }
            else
            {
                wrongMatches++
                visualizeError(answeredPair[0], answeredPair[1])
            }
    }

    private fun setPairValue(value: Int)
    {
        if (answeredPair[0] == 0)
            answeredPair[0] = value
        else
            answeredPair[1] = value
    }

    private fun desetPairValue(tag: Int)
    {
        if (answeredPair[0] != 0)
            answeredPair[0] = 0
        else
            answeredPair[1] = 0
        wordBubbleForTag(tag).isChecked = false
    }

    private fun onePairIsEmpty(): Boolean
    {
        return (answeredPair[0] == 0) || (answeredPair[1] == 0)
    }

    private fun noPairContainsSelected(tag: Int): Boolean
    {
        return (answeredPair[0] != tag) || (answeredPair[1] != tag)
    }

    private fun disablePair(tagA: Int, tagB: Int)
    {
        wordBubbleForTag(tagA).apply {
            isEnabled = false
            setOnClickListener {  }
            isClickable = false
            setTextColor(Color.LTGRAY)
        }
       wordBubbleForTag(tagB).apply {
            isEnabled = false
            setOnClickListener {  }
            isClickable = false
            setTextColor(Color.LTGRAY)
        }
    }

    private fun visualizeError(tagA: Int, tagB: Int)
    {
        resetPairs(tagA, tagB)
        animateFailure(wordBubbleForTag(tagA))
        animateFailure(wordBubbleForTag(tagB))
    }

    private fun animateSuccess(button: AppCompatToggleButton)
    {
        val drawbles = arrayOf(requireContext().getDrawable(R.drawable.word_bubble_true), requireContext().getDrawable(R.drawable.word_bubble_unselected))
        val transition = TransitionDrawable(drawbles)
        button.background = transition
        transition.startTransition(750)
    }

    private fun animateFailure(button: AppCompatToggleButton)
    {
        val drawbles = arrayOf(requireContext().getDrawable(R.drawable.word_bubble_false), requireContext().getDrawable(R.drawable.word_bubble_unselected))
        val transition = TransitionDrawable(drawbles)
        button.background = transition
        transition.startTransition(750)
    }

    private fun resetBackground(tag: Int)
    {
        wordBubbleForTag(tag).background = requireContext().getDrawable(R.drawable.word_bubble_unselected)
    }

    private fun checkedBackground(tag: Int)
    {
        wordBubbleForTag(tag).background = requireContext().getDrawable(R.drawable.word_bubble_selected)
    }

    private fun wordBubbleForTag(tag: Int): AppCompatToggleButton
    {
        return wordPairsContainer.findViewWithTag<ConstraintLayout>(tag).findViewById<AppCompatToggleButton>(R.id.word_bubble)
    }

    private fun resetPairs(tagA: Int, tagB: Int)
    {
        wordBubbleForTag(tagA).isChecked = false
        wordBubbleForTag(tagB).isChecked = false
        answeredPair = Array(2) {_ -> 0}
    }
}