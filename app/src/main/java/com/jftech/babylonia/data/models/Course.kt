package com.jftech.babylonia.data.models

import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

data class Course(val Name: String, var Lessons: Array<Lesson>, var Exp: Int)
{
    constructor(): this("", arrayOf(), 0)

    val Medals: Int
        get() {
            var count = 0
            for (lesson in Lessons)
                count += lesson.Mastery
            return count
        }

    val MaxFloor: Int
        get() {
            var highestFloor = 0
            for (lesson in Lessons)
                if (lesson.Floor > highestFloor)
                    highestFloor = lesson.Floor
            return highestFloor
        }

    fun MaxLevelForFloor(floor: Int): Int
    {
        var highestLevel = 0
        val floorLessons = Lessons.filter { lesson -> lesson.Floor == floor }
        for (lesson in floorLessons )
            if (lesson.Level > highestLevel)
                highestLevel = lesson.Level
        return highestLevel
    }

    companion object
    {
        fun fromJSON(json: HashMap<String, Any>): Course
        {
            val lessons: MutableList<Lesson> =  mutableListOf()
            (json["lessons"] as ArrayList<HashMap<String, Any>>).mapTo(lessons) {lessonJSON -> Lesson.fromJSON(lessonJSON)}
            return Course(json["name"] as String, lessons.toTypedArray(), (json["exp"] as Long).toInt())
        }
    }

    data class Lesson(val ID: String, val Name: String, var Mastery: Int, val Floor: Int, val Level: Int)
    {
        companion object
        {
            fun fromJSON(json: HashMap<String, Any>): Lesson
            {
                val lessonID = json.keys.elementAt(0)
                val lessonJSON = json[lessonID] as HashMap<String, Any>
                return Lesson(lessonID, lessonJSON["name"] as String, (lessonJSON["mastery"] as Long).toInt(), (lessonJSON["floor"] as Long).toInt(), (lessonJSON["level"] as Long).toInt())
            }
        }
    }
}