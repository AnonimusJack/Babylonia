package com.jftech.babylonia.data.models

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

data class User(val ID: String, var Username: String, var Name: String, var ProfilePictureURL: String, var Courses: Array<Course>, var Streak: Int, var Rank: String, var Gold: Int, val DateJoined: LocalDate, var Followers: Array<String>, var Following: Array<String>)
{
    constructor(): this("","", "", "", arrayOf(), 0, "bronze",0, LocalDate.now(), arrayOf(), arrayOf())
    constructor(uid: String): this(uid, "", "", "", arrayOf(), 0, "bronze",0, LocalDate.now(), arrayOf(), arrayOf())

    val Exp: Int
        get() {
            var exp = 0
            for (course in Courses)
                exp += course.Exp
            return exp
        }

    val Medals: Int
        get() {
            var medals = 0
            for (course in Courses)
                medals += course.Medals
            return medals
        }

    fun toCompactJSON(): HashMap<String, Any>
    {
        return hashMapOf<String, Any>(
            Pair("username", Username),
            Pair("name", Name),
            Pair("profile_picture", ProfilePictureURL),
            Pair("streak", Streak),
            Pair("rank", Rank),
            Pair("gold", Gold))
    }

    companion object
    {
        fun fromJSON(id: String, json: HashMap<String, Any>): User
        {
            val username = json["username"] as String
            val name = json["name"] as String
            val profilePictureURL = json["profile_picture"] as String
            val rank = json["rank"] as String
            val stringJoinedDate = json["date_joined"] as String
            val courses: MutableList<Course> = mutableListOf()
            (json["courses"] as ArrayList<HashMap<String, Any>>).mapTo(courses) { courseJSON -> Course.fromJSON(courseJSON) }
            return User(id, username, name, profilePictureURL, courses.toTypedArray(), (json["streak"] as Long).toInt(), rank, (json["gold"] as Long).toInt(), LocalDate.parse(stringJoinedDate, DateTimeFormatter.ofPattern("dd-MM-uuuu")), (json["followers"] as ArrayList<String>).toTypedArray(), (json["following"] as ArrayList<String>).toTypedArray())
        }
    }
}