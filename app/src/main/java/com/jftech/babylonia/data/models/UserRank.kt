package com.jftech.babylonia.data.models

import java.util.*
import kotlin.collections.HashMap

data class UserRank(val UserID: String, val Username: String, val ProfilePictureUrl: String, val WeeklyExp: Int)
{
    constructor(json: HashMap<String, Any>) : this(json["id"] as String, json["username"] as String, json["profile_picture"] as String, (json["weekly_exp"] as Long).toInt())
}