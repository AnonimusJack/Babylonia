package com.jftech.babylonia.ui.lesson

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.jftech.babylonia.R
import com.jftech.babylonia.connectors.LessonViewModel
import com.jftech.babylonia.data.models.AppSettings
import com.jftech.babylonia.interfaces.lesson.IExercisable
import com.jftech.babylonia.ui.views.LoadingFragment

const val STREAM_TYPE = AudioManager.STREAM_MUSIC



class LessonActivity: AppCompatActivity()
{
    //Sound effects area
    private var failID: Int = 0
    private var successID: Int = 0
    private lateinit var audioManager: AudioManager
    private lateinit var soundPool: SoundPool
    private var soundPoolReady = false
        set(value) {
            field = value
            initLesson()
        }
    //Activity area
    private lateinit var lessonViewModel: LessonViewModel
    private lateinit var checkAnswerButton: AppCompatButton
    private lateinit var returnButton: AppCompatImageButton
    private lateinit var lessonProgressBar: ProgressBar
    private var lessonsLoaded = false
        set(value) {
            field = value
            initLesson()
        }
    private var exerciseFinished: Boolean = false
        set(value) {
            field = value
            checkAnswerButton.text = if (!value) "Check" else "Next"
        }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lesson_activity)
        wireView()
        loadSoundEffects()
        lessonViewModel = LessonViewModel(this, intent.getStringExtra("lesson_id")!!, "English")
        setButtonEvents()
        addLessonLoadedEvent()
        loadPlaceHolderFragment()
    }

    override fun onResume()
    {
        super.onResume()
        lessonViewModel.OnResume()
    }

    override fun onStop()
    {
        super.onStop()
        lessonViewModel.OnStop()
    }

    fun ToggleCheckButton(on: Boolean)
    {
        checkAnswerButton.isEnabled = on
    }

    fun OnExerciseComplete(correctInARow: Int, lessonFragment: Fragment?, success: Boolean)
    {
        exerciseFinished = true
        (supportFragmentManager.findFragmentById(R.id.lesson_container)!!.requireView() as ConstraintLayout).addView(
            requestBlockingView()
        )
        playSoundEffectFor(success)
        lessonProgressBar.progress++
        setNextButtonForAnswer(success)
        updateProgressBarVisuals(correctInARow)
        checkAnswerButton.setOnClickListener { onExerciseFinish(lessonFragment) }
    }

    fun RequestCurrentLessonFragment(): IExercisable
    {
        return supportFragmentManager.findFragmentById(R.id.lesson_container) as IExercisable
    }

    private fun wireView()
    {
        checkAnswerButton = findViewById(R.id.lesson_complete_button)
        returnButton = findViewById(R.id.lesson_topbar_return_button)
        checkAnswerButton.isEnabled = false
        lessonProgressBar = findViewById(R.id.lesson_progressbar)
    }

    private fun setButtonEvents()
    {
        checkAnswerButton.setOnClickListener { lessonViewModel.RequestAnswerCheck() }
        returnButton.setOnClickListener {
            onCanceled()
        }
    }

    private fun onExerciseFinish(nextLessonFragment: Fragment?)
    {
        exerciseFinished = false
        setNextButtonForAnswer(true)
        if (nextLessonFragment != null)
        {
            replaceLessonFragment(nextLessonFragment)
            checkAnswerButton.setOnClickListener { lessonViewModel.RequestAnswerCheck() }
            checkAnswerButton.isEnabled = false
        }
        else
        {
            lessonViewModel.OnLessonFinish()
            onFinishedLesson()
        }
    }

    private fun replaceLessonFragment(lessonFragment: Fragment)
    {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.lesson_container, lessonFragment)
        transaction.commit()
    }

    private fun requestBlockingView(): View
    {
        val params = ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        val view = View(this)
        view.layoutParams = params
        view.isClickable = true
        return view
    }

    private fun updateProgressBarVisuals(correctInARow: Int)
    {
        lessonProgressBar.progressTintList = ColorStateList.valueOf(
            if (correctInARow > 3) Color.parseColor(
                "#FFBF00"
            ) else Color.parseColor("#63FF00")
        )
    }

    private fun setNextButtonForAnswer(correct: Boolean)
    {
        checkAnswerButton.background = if (correct) getDrawable(R.drawable.word_bubble_true) else getDrawable(
            R.drawable.word_bubble_false
        )
    }

    private fun addLessonLoadedEvent()
    {
        lessonViewModel.CurrentLesson.observe(this, Observer {
            if (AppSettings.SoundEffectsOn)
                lessonsLoaded = true
            else
                replaceLessonFragment(lessonViewModel.RequestFirstExerciseFragment())
            lessonProgressBar.max = it.size
        })
    }

    private fun loadSoundEffects()
    {
        if (AppSettings.SoundEffectsOn)
        {
            audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            soundPool = SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(1)
                .build()
            soundPool.setOnLoadCompleteListener { _, _, _ -> soundPoolReady = true
            Log.d("SoundPool", "Build Complete")}

            failID = soundPool.load(this, R.raw.fail, 1)
            successID = soundPool.load(this, R.raw.success, 1)
        }
    }

    private fun initLesson()
    {
        if (lessonsLoaded && soundPoolReady)
            replaceLessonFragment(lessonViewModel.RequestFirstExerciseFragment())
    }

    private fun playSoundEffectFor(userAnswerCorrect: Boolean)
    {
        if (AppSettings.SoundEffectsOn)
            if (soundPoolReady)
                soundPool.play(if (userAnswerCorrect) successID else failID, 1f, 1f, 1, 0, 1f)
    }

    private fun loadPlaceHolderFragment()
    {
        replaceLessonFragment(LoadingFragment())
    }

    private fun onFinishedLesson()
    {
        val returnIntent = Intent()
        returnIntent.putExtra("result", lessonViewModel.CalculateExpForLesson())
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    private fun onCanceled()
    {
        val returnIntent = Intent()
        setResult(RESULT_CANCELED, returnIntent)
        finish()
    }
}