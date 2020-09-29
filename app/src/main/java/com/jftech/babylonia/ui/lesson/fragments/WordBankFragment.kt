package com.jftech.babylonia.ui.lesson.fragments

import android.animation.Animator
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.AppCompatToggleButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isEmpty
import androidx.core.view.iterator
import androidx.core.view.size
import androidx.fragment.app.Fragment
import com.google.android.flexbox.FlexboxLayout
import com.jftech.babylonia.R
import com.jftech.babylonia.connectors.LessonViewModel
import com.jftech.babylonia.interfaces.lesson.IExercisable
import java.util.*

class WordBankFragment(private val words: Array<String>, private val question: String, private val viewModelReference: LessonViewModel): Fragment(), IExercisable
{
    private lateinit var textToTranslateTextView: AppCompatTextView
    private lateinit var sentenceContainer: FlexboxLayout
    private lateinit var wordBankContainer: FlexboxLayout
    override var UserAnswer: String = ""
        get() {return extractAnswerFromWordContainer()}

// TODO: 28/09/2020 Fix this weird thing

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.lesson_word_bank_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        view.post {
            wireViews(view)
            setTextToTranslate()
            depositToWordBank(words)
        }
    }

    private fun depositToWordBank(words: Array<String>)
    {
        for ((tagCount, word) in words.withIndex())
        {
            val wordBubble = buildWordBubble(word, tagCount + 1)
            wordBankContainer.addView(wordBubble)
        }
    }

    private fun buildWordBubble(word: String, tag: Int): View
    {
        val wordBubbleContainer = View.inflate(requireContext(), R.layout.word_bubble_layout, null) as ConstraintLayout
        val wordBubble: AppCompatToggleButton = wordBubbleContainer.findViewById(R.id.word_bubble)
        wordBubbleContainer.tag = tag * -1
        wordBubble.text = word
        wordBubble.textOn = word
        wordBubble.textOff = word
        wordBubble.tag = tag
        wordBubble.setOnClickListener {
            moveWordBubble(wordBubble)
        }
        return wordBubbleContainer
    }

    private fun moveWordBubble(wordBubble: View)
    {
        if (wordBubble.parent == wordBankContainer)
            moveWordBubbleToSentence(wordBubble)
        else
            moveWordBubbleToBank(wordBubble)
    }

    private fun moveWordBubbleToSentence(wordBubbleContainer: View)
    {
        val wordBubble = getBubbleFromContainer(wordBubbleContainer)
        sentenceContainer.addView(wordBubble)
        animateFromBankToSentence(wordBubble, wordBubble.x, wordBubble.y)
    }

    private fun moveWordBubbleToBank(wordBubble: View)
    {
        val bubbleContainer: View = wordBankContainer.findViewWithTag((wordBubble.tag as Int) * -1)
        animateFromSentenceToBank(bubbleContainer, wordBubble)
    }

    private fun getBubbleFromContainer(wordBubbleContainer: View): AppCompatToggleButton
    {
        val wordBubble: AppCompatToggleButton = wordBubbleContainer.findViewById(R.id.word_bubble)
        wordBubble.tag = wordBubbleContainer.tag
        (wordBubbleContainer as ConstraintLayout).removeViewAt(0)
        return wordBubble
    }

    private fun returnBubbleToContainer(wordBubbleContainer: View, wordBubble: View)
    {
        sentenceContainer.removeView(wordBubble)
        Log.d("View's parent", "${wordBubble.parent}")
        val castWordBubbleContainer = wordBubbleContainer as ConstraintLayout
        castWordBubbleContainer.addView(wordBubble)
    }

    private fun animateFromBankToSentence(wordBubble: View, x: Float, y: Float)
    {
        wordBubble.animate().setDuration(500).x(x).y(y).withStartAction {
            sentenceContainer.removeView(wordBubble)
        }.withEndAction {
            if (sentenceContainer.isEmpty())
                viewModelReference.OnAnswerNotEmpty()
            sentenceContainer.addView(wordBubble)
        }.start()
    }

    private fun animateFromSentenceToBank(wordBubbleContainer: View, wordBubble: View)
    {
        wordBubble.animate().setDuration(500).x(wordBubbleContainer.x).y(wordBubbleContainer.y).withEndAction {
            returnBubbleToContainer(wordBubbleContainer, wordBubble)
            if (sentenceContainer.isEmpty())
                viewModelReference.OnAnswerIsEmpy()
        }.start()
    }

    private fun extractAnswerFromWordContainer(): String
    {
        var userAnswer = ""
        for (view in sentenceContainer)
        {
            val bubbleContainer = view as ConstraintLayout
            val wordBubble: AppCompatToggleButton = bubbleContainer.findViewById(R.id.word_bubble)
            userAnswer += wordBubble.text.toString() + " "
        }
        return userAnswer.trim()
    }

    private fun wireViews(view: View)
    {
        textToTranslateTextView = view.findViewById(R.id.lesson_word_bank_text_to_translate_textview)
        sentenceContainer = view.findViewById(R.id.lesson_word_bank_sentence_flexbox)
        wordBankContainer = view.findViewById(R.id.lesson_word_bank_flexbox)
    }

    private fun setTextToTranslate()
    {
        textToTranslateTextView.text = question
    }
}