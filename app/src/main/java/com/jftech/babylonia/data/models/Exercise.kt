package com.jftech.babylonia.data.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import org.json.JSONArray
import org.json.JSONObject

@IgnoreExtraProperties
data class Exercise(val Type: String, val Questions: ArrayList<String>, val Answers: ArrayList<String>, @Exclude var Finished: Boolean)
{
    constructor(): this("", arrayListOf(), arrayListOf(), false)


    @Exclude
    fun toMap(): Map<String, Any?>
    {
        return mapOf(
            "type" to Type,
            "questions" to Questions,
            "answers" to Answers
        )
    }

    @Exclude
    fun toJSON(): JSONObject
    {
        var json = JSONObject()
        json.put("type", Type)
        json.put("questions", JSONArray(Questions))
        json.put("answers", JSONArray(Answers))
        json.put("finished", Finished)
        return json
    }

    @Exclude
    fun CheckIFAnswerIsCorrect(userAnswer: Any): Boolean
    {
        return if (Type == "pair")
            checkIfAnswerIsCorrectForPair(userAnswer as Pair<Int, Int>)
        else
            checkIfAnswerIsCorrectForTranslation(userAnswer as String)
    }

    @Exclude
    private fun checkIfAnswerIsCorrectForTranslation(userAnswer: String): Boolean
    {
        if (Answers.contains(userAnswer.toLowerCase()))
            return true
        return false
    }

    @Exclude
    private fun checkIfAnswerIsCorrectForPair(userAnswer: Pair<Int, Int>): Boolean
    {
        if (userAnswer.first == userAnswer.second)
            return true
        return false
    }

    companion object
    {
        @Exclude
        fun fromFirebaseJSON(json: HashMap<String, Any>): Exercise
        {
            return Exercise(json["type"] as String, json["questions"] as ArrayList<String>, json["answers"] as ArrayList<String>, false)
        }

        fun fromJSON(json: JSONObject): Exercise
        {
            val questionList: ArrayList<String> = arrayListOf()
            val answerList: ArrayList<String> = arrayListOf()
            val questionsJSONArray = json.getJSONArray("questions")
            val answerJSONArray = json.getJSONArray("answers")
            val questionsCount = questionsJSONArray.length()
            val answersCount = answerJSONArray.length()
            for (q in 0 until questionsCount)
                questionList.add(questionsJSONArray[q] as String)
            for (a in 0 until answersCount)
                answerList.add(answerJSONArray[a] as String)
            return Exercise(json.getString("type"),
            questionList, answerList, json.getBoolean("finished"))
        }
    }
}