package com.jftech.babylonia.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.*
import com.jftech.babylonia.connectors.LessonViewModel
import com.jftech.babylonia.data.models.Exercise
import org.json.JSONArray
import org.json.JSONObject

class LessonRepository(private val context: Context, private val lessonViewModelReference: LessonViewModel)
{
    private var fireDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference



    fun OnLessonFinished()
    {
        val sharedPreferences = context.getSharedPreferences("babyloniaCurrentUser", Context.MODE_PRIVATE)
        context.getSharedPreferences("babyloniaCurrentLesson", Context.MODE_PRIVATE).edit().clear().apply()


    }

    fun OnStop()
    {
        val sharedPreferences = context.getSharedPreferences("babyloniaCurrentLesson", Context.MODE_PRIVATE)
        val currentLessonName = sharedPreferences.getString("lesson_name", null)
        if (currentLessonName != null)
            saveCurrentLessonToLocalStorage(sharedPreferences)
    }

    fun RequestLesson(lessonID: String, courseName: String)
    {
        val sharedPreferences = context.getSharedPreferences("babyloniaCurrentLesson", Context.MODE_PRIVATE)
        val currentLessonName = null //sharedPreferences.getString("lesson_name", null)
        if (currentLessonName != null && currentLessonName == lessonID)
            lessonViewModelReference.SetCurrentLesson(getCurrentLessonFromLocalStorage()!!)
        else
            fireDatabase.child("lessons").child(courseName).child(lessonID).addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    val exercises: MutableList<Exercise> = mutableListOf()
                    Log.d("Lesson callback", snapshot.value.toString())
                    Log.d("Lesson callback", snapshot.value!!.javaClass.name)
                    (snapshot.value as ArrayList<HashMap<String, Any>>).mapTo(exercises) { exercise -> Exercise.fromFirebaseJSON (exercise) }
                    sharedPreferences.edit().putString("lesson_name", snapshot.key).commit()
                    lessonViewModelReference.SetCurrentLesson(exercises.toTypedArray())
                }
                override fun onCancelled(error: DatabaseError)
                {
                    Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun getCurrentLessonFromLocalStorage(): Array<Exercise>?
    {
        val jsonString = context.getSharedPreferences("babyloniaCurrentLesson", Context.MODE_PRIVATE).getString("current_lesson", null)
        return if (jsonString != null)
        {
            val exercises: MutableList<Exercise> = mutableListOf()
            val jsonArray = JSONArray(jsonString)
            for (l in 0 until  jsonArray.length())
                exercises.add(Exercise.fromJSON(jsonArray[l] as JSONObject))
            exercises.toTypedArray()
        } else
            null
    }

    private fun saveCurrentLessonToLocalStorage(sharedPreferences: SharedPreferences)
    {
        val currentExercisesJSONObjects: MutableList<JSONObject> = mutableListOf()
        lessonViewModelReference.CurrentLesson.value!!.mapTo(currentExercisesJSONObjects) {it.toJSON()}
        val currentLessonJSONString = JSONArray(currentExercisesJSONObjects).toString()
        sharedPreferences.edit().putString("current_lesson", currentLessonJSONString).apply()
    }
}