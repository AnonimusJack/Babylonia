package com.jftech.babylonia.data

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.jftech.babylonia.connectors.UserViewModel
import com.jftech.babylonia.data.models.AppSettings
import com.jftech.babylonia.data.models.Course
import com.jftech.babylonia.data.models.User
import com.jftech.babylonia.data.models.UserRank
import com.jftech.babylonia.ui.login.fragments.RegisterFragment
import com.jftech.babylonia.ui.main.fragments.ProfileFragment
import com.jftech.babylonia.ui.main.fragments.RankingFragment
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.lang.Exception
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class UserRepository()
{
    private var fireDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var fireStorage = FirebaseStorage.getInstance().reference
    private val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-uuuu")



    fun OnUserRequested(uid: String, userViewModelReference: UserViewModel, context: Context)
    {
        val sharedPreferences = AppSettings.AppContext.getSharedPreferences("babyloniaCurrentUser", Context.MODE_PRIVATE)
        val currentUserID = null //sharedPreferences.getString("user_id", null)
        val activeUser = true //currentUserID == uid
        if (currentUserID != null && activeUser)
        {
            userViewModelReference.SetCurrentUser(getUserFromLocalStorage(sharedPreferences))
            Log.d("User Repository:", "Loaded User From Local Storage")
        }
        else
        {
            getUserFromDatabase(uid, activeUser, sharedPreferences, userViewModelReference, context)
            Log.d("User Repository:", "Loaded User From Database")
        }
    }

    fun RequestFriendsListFor(userID: String, friendDisplayType: ProfileFragment.FriendDisplayType, fragmentReference: ProfileFragment)
    {
        fireDatabase.child("${friendDisplayType.name.toLowerCase()}/$userID").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                val friends = mutableListOf<Array<String>>()
                if (snapshot.value!! as? ArrayList<HashMap<String, Any>> != null)
                    (snapshot.value as ArrayList<HashMap<String, Any>>).mapTo(friends) { data -> arrayOf(data["id"] as String, data["username"] as String, data["profile_picture"] as String)}
                fragmentReference.SetFriendsAdapterFor(friends.toTypedArray())
            }
            override fun onCancelled(error: DatabaseError) { Toast.makeText(fragmentReference.requireContext(), error.message, Toast.LENGTH_LONG).show() }
        })
    }

    fun RequestRankingFor(rank: String, fragmentReference: RankingFragment)
    {
        fireDatabase.child("ranking").child(rank).addListenerForSingleValueEvent(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                val ranking: MutableList<UserRank> = mutableListOf()
                (snapshot.value as ArrayList<HashMap<String, Any>>).mapTo(ranking) {snapData -> UserRank(snapData)}
                fragmentReference.OnRankingsRecieved(ranking.toTypedArray())
            }
            override fun onCancelled(error: DatabaseError)
            {
                Toast.makeText(fragmentReference.requireContext(), error.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    fun UpdateUserChangedToDatabase(userViewModelReference: UserViewModel, context: Context)
    {
        val user = userViewModelReference.CurrentUser.value!!
        //Update User entry
        fireDatabase.child("users/${user.ID}").updateChildren(user.toCompactJSON()).addOnCompleteListener {
            if (it.isSuccessful)
                Toast.makeText(context, "Changes Saved!", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(context, "Failed to save changes, please try again later..\n ${it.exception!!.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
        //Update Rankings entry
        updateUserDataInRanking(user.ID, user.Rank, hashMapOf(Pair("username", user.Username)), context)
        //Update Friends entries
        updateUserDataInFriends(user.ID, ProfileFragment.FriendDisplayType.Followers.name.toLowerCase(), hashMapOf(Pair("username", user.Username)), context)
        updateUserDataInFriends(user.ID, ProfileFragment.FriendDisplayType.Following.name.toLowerCase(), hashMapOf(Pair("username", user.Username)), context)
    }

    fun ChangeFollowerStateOfUser(userID: String, username: String, profilePic: String, userViewModelReference: UserViewModel, followState: UserViewModel.FollowState, context: Context)
    {
        val user = userViewModelReference.CurrentUser.value!!
        val friend = User(userID).apply {
            Username = username
            ProfilePictureURL = profilePic
        }
        when (followState)
        {
            UserViewModel.FollowState.Follow -> followUser(user, friend, context)
            UserViewModel.FollowState.Unfollow -> unfollowUser(user, friend, context)
        }
    }

    fun UploadProfilePicture(newPicture: Bitmap, userViewModelReference: UserViewModel, context: Context)
    {
        val user = userViewModelReference.CurrentUser.value!!
        val mainProfilePictureRef = fireStorage.child("images/profile_pictures/${user.ID}.jpg")
        val compactProfilePictureRef = fireStorage.child("images/profile_pictures/compact/${user.ID}.jpg")
        val profileStream = ByteArrayOutputStream()
        newPicture.compress(Bitmap.CompressFormat.JPEG, 100, profileStream)
        val profileCompact = Bitmap.createScaledBitmap(newPicture, 20, 20, false)
        val compactStream = ByteArrayOutputStream()
        profileCompact.compress(Bitmap.CompressFormat.JPEG, 100, compactStream)
        mainProfilePictureRef.putStream(ByteArrayInputStream(profileStream.toByteArray())).addOnCompleteListener {
            if (it.isSuccessful)
                mainProfilePictureRef.downloadUrl.addOnCompleteListener {
                    if (it.isSuccessful)
                        userViewModelReference.SetNewProfilePicture(it.result.toString())
                }
            else
                Toast.makeText(context, "Failed to upload picture, please try again later..\n ${it.exception!!.message}", Toast.LENGTH_SHORT).show()
        }
        compactProfilePictureRef.putStream(ByteArrayInputStream(compactStream.toByteArray())).addOnCompleteListener {
            if (it.isSuccessful)
                compactProfilePictureRef.downloadUrl.addOnCompleteListener {
                    if (it.isSuccessful)
                    {
                        fireDatabase.child("users/${user.ID}").updateChildren(hashMapOf<String, Any>(Pair("profile_picture_compact", it.result.toString())))
                        updateUserDataInRanking(user.ID, user.Rank, hashMapOf(Pair("profile_picture", it.result.toString())), context)
                        updateUserDataInFriends(user.ID, ProfileFragment.FriendDisplayType.Followers.name.toLowerCase(), hashMapOf(Pair("profile_picture", it.result.toString())), context)
                        updateUserDataInFriends(user.ID, ProfileFragment.FriendDisplayType.Following.name.toLowerCase(), hashMapOf(Pair("profile_picture", it.result.toString())), context)
                    }
                }
            }
    }

    fun LoadCurrentStreakState(): Boolean
    {
        val sharedPreferences = AppSettings.AppContext.getSharedPreferences("babyloniaCurrentUser", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("streak_status", false)
    }

    fun SaveCurrentStreakState(streakStatus: Boolean)
    {
        val sharedPreferences = AppSettings.AppContext.getSharedPreferences("babyloniaCurrentUser", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("streak_status", streakStatus).commit()
    }

    fun OnLessonFinishedUpdates(exp: Int, lessonExp: Int, userRank: String, streak: Int, uid: String, courseIndex: Int)
    {
        //Update User's course exp
        fireDatabase.child("users/$uid/courses/$courseIndex").updateChildren(hashMapOf<String, Any>(Pair("exp", exp)))
        //Update User's streak
        fireDatabase.child("users/$uid").updateChildren(hashMapOf<String, Any>(Pair("streak", streak), Pair("last_lesson", LocalDate.now().format(dateFormatter))))
        //Update Ranking User's weekly exp
        fireDatabase.child("ranking/$userRank").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                val ranking = snapshot.value as ArrayList<HashMap<String, Any>>
                for ((index, userDBRank) in ranking.withIndex())
                    if (userDBRank["id"] == uid)
                    {
                        val currentExp = (userDBRank["weekly_exp"] as Long).toInt()
                        fireDatabase.child("ranking/$userRank/$index").updateChildren(hashMapOf<String, Any>(Pair("weekly_exp", currentExp + lessonExp)))
                        break
                    }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }








    private fun updateUserDataInRanking(userID: String, userRank: String, data: HashMap<String, Any>, context: Context)
    {
        fireDatabase.child("ranking/${userRank}").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                val ranking = snapshot.value as ArrayList<HashMap<String, Any>>
                val indexOfUser = ranking.indexOfFirst { rankEntry -> rankEntry["id"] == userID }
                fireDatabase.child("ranking/${userRank}/$indexOfUser").updateChildren(data)
                    .addOnCompleteListener { if (it.isSuccessful) Log.d("Update User data", "Ranking updated Successfully") else  Log.d("Update User data", "Failed to update Ranking \n ${it.exception!!.message}")}
            }
            override fun onCancelled(error: DatabaseError) { Toast.makeText(context, "Failed to save changes, please try again later..\n ${error.message}", Toast.LENGTH_SHORT).show()}
        })
    }

    private fun updateUserDataInFriends(userID: String, friendType: String, data: HashMap<String, Any>, context: Context)
    {
        fireDatabase.child(friendType).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                val friendsDatabse = snapshot.value as HashMap<String, Any>
                for (id in friendsDatabse.keys)
                {
                    fireDatabase.child("$friendType/$id").addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot)
                        {
                            val friends = snapshot.value as ArrayList<HashMap<String, Any>>
                            for (friend in friends)
                            {
                                val friendID = friend["id"] as String
                                if ( friendID == userID)
                                {
                                    val indexOfFriend = friends.indexOfFirst { friendData -> friendData["id"] == userID }
                                    fireDatabase.child("$friendType/$id/$indexOfFriend").updateChildren(data)
                                        .addOnFailureListener { Toast.makeText(context, "Failed to save changes, please try again later..\n ${it.message!!}", Toast.LENGTH_SHORT).show() }
                                }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) { Toast.makeText(context, "Failed to save changes, please try again later..\n ${error.message}", Toast.LENGTH_SHORT).show()}
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) { Toast.makeText(context, "Failed to save changes, please try again later..\n ${error.message}", Toast.LENGTH_SHORT).show()}
        })
    }

    private fun followUser(user: User, friend: User, context: Context)
    {
        //add new row to self following
        addFriendTo(user, friend, ProfileFragment.FriendDisplayType.Following, context)
        //add new row to self's User following
        addFriendInUserTo(user, friend, ProfileFragment.FriendDisplayType.Following, context)
        //add new row to user followers
        addFriendTo(friend, user, ProfileFragment.FriendDisplayType.Followers, context)
        //add new row to user's User followers
        addFriendInUserTo(friend, user, ProfileFragment.FriendDisplayType.Followers, context)
    }

    private fun unfollowUser(user: User, friend: User, context: Context)
    {
        //remove user row from self following
        removeFriendFrom(user, friend, ProfileFragment.FriendDisplayType.Following, context)
        //remove user row from self's following
        removeFriendInUserFrom(user, friend, ProfileFragment.FriendDisplayType.Following, context)
        //remove self row from user's followers
        removeFriendFrom(friend, user, ProfileFragment.FriendDisplayType.Followers, context)
        //remove self row from user's User followers
        removeFriendInUserFrom(friend, user, ProfileFragment.FriendDisplayType.Followers, context)
    }

    private fun addFriendTo(user: User, friend: User, friendType: ProfileFragment.FriendDisplayType, context: Context)
    {
        val chosenFriendsType = friendType.name.toLowerCase()
        fireDatabase.child("$chosenFriendsType/${user.ID}").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                val friends = snapshot.value as ArrayList<HashMap<String, Any>>
                friends.add(hashMapOf(Pair("user_id", friend.ID), Pair("username", friend.Username), Pair("profile_picture", friend.ProfilePictureURL)))
                //add the new row
                fireDatabase.child("$chosenFriendsType/${user.ID}").setValue(friends).addOnCompleteListener {
                    if (it.isSuccessful)
                        Log.d("${user.Username} ${friendType.name.capitalize()} ${friend.Username}"," Success!")
                }
            }
            override fun onCancelled(error: DatabaseError) { Toast.makeText(context, "\"${user.Username} ${friendType.name.capitalize()} ${friend.Username}\" Failled \n ${error.message}", Toast.LENGTH_SHORT).show() }
        })
    }

    private fun addFriendInUserTo(user: User, friend: User, friendType: ProfileFragment.FriendDisplayType, context: Context)
    {
        val chosenFriendsType = friendType.name.toLowerCase()
        fireDatabase.child("users/${user.ID}/$chosenFriendsType").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                val friends = snapshot.value as ArrayList<String>
                friends.add(friend.ID)
                //add the new row
                fireDatabase.child("users/${user.ID}/$chosenFriendsType").setValue(friends).addOnCompleteListener {
                    if (it.isSuccessful)
                        Log.d("${user.Username} added ${friend.Username} to  ${friendType.name.capitalize()}"," Success!")
                }
            }
            override fun onCancelled(error: DatabaseError) { Toast.makeText(context, "\"${user.Username} ${friendType.name.capitalize()} ${friend.Username}\" Failled \n ${error.message}", Toast.LENGTH_SHORT).show() }
        })
    }

    private fun removeFriendFrom(user: User, friend: User, friendType: ProfileFragment.FriendDisplayType, context: Context)
    {
        val chosenFriendsType = friendType.name.toLowerCase()
        fireDatabase.child("$chosenFriendsType/${user.ID}").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                val friends = snapshot.value as ArrayList<HashMap<String, Any>>
                friends.removeAt(friends.indexOfFirst { shortUser -> shortUser["user_id"]  == friend.ID })
                //remove the new row
                fireDatabase.child("$chosenFriendsType/${user.ID}").setValue(friends).addOnCompleteListener {
                    if (it.isSuccessful)
                        Log.d("${user.Username} ${friendType.name.capitalize()} ${friend.Username}"," Success!")
                }
            }
            override fun onCancelled(error: DatabaseError) { Toast.makeText(context, "\"${user.Username} ${friendType.name.capitalize()} ${friend.Username}\" Failled \n ${error.message}", Toast.LENGTH_SHORT).show() }
        })
    }

    private fun removeFriendInUserFrom(user: User, friend: User, friendType: ProfileFragment.FriendDisplayType, context: Context)
    {
        val chosenFriendsType = friendType.name.toLowerCase()
        fireDatabase.child("users/${user.ID}/$chosenFriendsType").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                val friends = snapshot.value as ArrayList<String>
                friends.removeAt(friends.indexOfFirst { userid -> userid == friend.ID })
                //remove the new row
                fireDatabase.child("users/${user.ID}/$chosenFriendsType").setValue(friends).addOnCompleteListener {
                    if (it.isSuccessful)
                        Log.d("${user.Username} added ${friend.Username} to  ${friendType.name.capitalize()}"," Success!")
                }
            }
            override fun onCancelled(error: DatabaseError) { Toast.makeText(context, "\"${user.Username} ${friendType.name.capitalize()} ${friend.Username}\" Failled \n ${error.message}", Toast.LENGTH_SHORT).show() }
        })
    }

    private fun getUserFromDatabase(uid: String, activeUser: Boolean, sharedPreferences: SharedPreferences, userViewModelReference: UserViewModel, context: Context)
    {
        fireDatabase.child("users").child(uid).addListenerForSingleValueEvent(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                val snapValue = snapshot.value as HashMap<String, Any>
                val user = User.fromJSON(uid, snapValue)
                if (activeUser)
                {
                    sharedPreferences.edit().putString("user_id", snapshot.key).commit()
                    userViewModelReference.SetCurrentUser(user)
                    userViewModelReference.LastLesson = LocalDate.parse(snapValue["last_lesson"] as String, dateFormatter)
                    val courseName = user.Courses[0].Name
                    userViewModelReference.OnNewCourseSelected(courseName)
                }
                else
                    userViewModelReference.SetViewedUser(user)
            }
            override fun onCancelled(error: DatabaseError) { Toast.makeText(context, error.message, Toast.LENGTH_LONG).show() }
        })
    }

    private fun saveUserToLocalStorage(user: User)
    {
        val appContext = AppSettings.AppContext
        val sharedPreferences = appContext.getSharedPreferences("babyloniaCurrentUser", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("user_id", user.ID).apply()
        sharedPreferences.edit().putString("username", user.Username).apply()
        sharedPreferences.edit().putString("profile_picture", user.ProfilePictureURL).apply()
        sharedPreferences.edit().putString("name", user.Name).apply()
        sharedPreferences.edit().putInt("streak", user.Streak).apply()
        sharedPreferences.edit().putString("rank", user.Rank).apply()
        sharedPreferences.edit().putInt("gold", user.Gold).apply()
        sharedPreferences.edit().putString("date_joined", user.DateJoined.format(dateFormatter)).apply()
        sharedPreferences.edit().putStringSet("followers", user.Followers.toSet()).apply()
        sharedPreferences.edit().putStringSet("following", user.Following.toSet()).apply()
        val courseNames = mutableSetOf<String>()
        user.Courses.mapTo(courseNames) {course -> course.Name }
        sharedPreferences.edit().putStringSet("courses", courseNames).apply()
    }

    private fun getUserFromLocalStorage(sharedPreferences: SharedPreferences): User
    {
        val userID = sharedPreferences.getString("user_id", "")!!
        val username = sharedPreferences.getString("username", "")!!
        //val email = sharedPreferences.getString("email", "")!!
        val profilePicture =  sharedPreferences.getString("profile_picture", "")!!
        val courses = getCoursesForUserFromLocalStorage(sharedPreferences.getStringSet("courses", setOf())!!)
        val name = sharedPreferences.getString("name", "")!!
        val streak = sharedPreferences.getInt("streak", 0)
        val rank = sharedPreferences.getString("rank", "bronze")!!
        val gold = sharedPreferences.getInt("gold", 0)
        val dateJoined = sharedPreferences.getString("date_joined", "")!!
        val followers = sharedPreferences.getStringSet("followers", setOf())!!
        val following = sharedPreferences.getStringSet("following", setOf())!!
        return User(userID, username, name, profilePicture, courses, streak, rank, gold, LocalDate.parse(dateJoined, dateFormatter), followers.toTypedArray(), following.toTypedArray())
    }

    private fun saveCoursesForUserToLocalStorage(courses: Array<Course>)
    {
        val appContext = AppSettings.AppContext
        for (course in courses)
        {
            val sharedPreferences = appContext.getSharedPreferences("babyloniaCurrentUser{$course}Course", Context.MODE_PRIVATE)
            val lessonIDs = mutableListOf<String>()
            course.Lessons.mapTo(lessonIDs) { lesson -> lesson.ID }
            val lessonNames = mutableListOf<String>()
            course.Lessons.mapTo(lessonNames) { lesson -> lesson.Name }
            val lessonMasteries = mutableListOf<String>()
            course.Lessons.mapTo(lessonMasteries) { lesson -> lesson.Mastery.toString() }
            val lessonFloors = mutableListOf<String>()
            course.Lessons.mapTo(lessonFloors) { lesson -> lesson.Floor.toString() }
            val lessonLevels = mutableListOf<String>()
            course.Lessons.mapTo(lessonLevels) { lesson -> lesson.Level.toString() }
            sharedPreferences.edit().putStringSet("lesson_ids", lessonIDs.toSet()).apply()
            sharedPreferences.edit().putStringSet("lesson_names", lessonNames.toSet()).apply()
            sharedPreferences.edit().putStringSet("lesson_masteries", lessonMasteries.toSet()).apply()
            sharedPreferences.edit().putStringSet("lesson_floors", lessonFloors.toSet()).apply()
            sharedPreferences.edit().putStringSet("lesson_levels", lessonLevels.toSet()).apply()
            sharedPreferences.edit().putString("name", course.Name).apply()
            sharedPreferences.edit().putInt("exp", course.Exp).apply()
        }
    }

    private fun getCoursesForUserFromLocalStorage(courseNames: Set<String>): Array<Course>
    {
        val appContext = AppSettings.AppContext
        val courses: MutableList<Course> = mutableListOf()
        courseNames.mapTo(courses) {
            val sharedPreferences = appContext.getSharedPreferences("babyloniaCurrentUser{$it}Course", Context.MODE_PRIVATE)
            val courseLessons: MutableList<Course.Lesson> = mutableListOf()
            val lessonIDs = sharedPreferences.getStringSet("lesson_ids", setOf())!!
            val lessonNames = sharedPreferences.getStringSet("lesson_names", setOf())!!
            val lessonMasteries = sharedPreferences.getStringSet("lesson_masteries", setOf())!!
            val lessonFloors = sharedPreferences.getStringSet("lesson_floors", setOf())!!
            val lessonLevels = sharedPreferences.getStringSet("lesson_levels", setOf())!!
            val name = sharedPreferences.getString("name", "")!!
            val exp = sharedPreferences.getInt("exp", 0)
            for (i in 0 until lessonIDs.size)
                courseLessons.add(Course.Lesson(lessonIDs.elementAt(i), lessonNames.elementAt(i), lessonMasteries.elementAt(i).toInt(), lessonFloors.elementAt(i).toInt(), lessonLevels.elementAt(i).toInt()))
            Course(name, courseLessons.toTypedArray(), exp)
        }
        return courses.toTypedArray()
    }






    companion object
    {
        fun PostRegistration(uid: String, email: String, fragmentReference: RegisterFragment)
        {
            val fireDatabase = FirebaseDatabase.getInstance().reference
            createNewUserDataInDB(uid, email, fireDatabase)
            addFriendsListToUser(uid, fireDatabase)
            addUserToRanking(uid, fireDatabase, fragmentReference)
        }

        private fun createNewUserDataInDB(uid: String, email: String, fireDatabase: DatabaseReference)
        {
            fireDatabase.child("users").child(uid).setValue(
                hashMapOf<String, Any>(
                    Pair("username", ""),
                    Pair("email", email),
                    Pair("profile_picture", ""),
                    Pair("courses", listOf("")),
                    Pair("name", ""),
                    Pair("streak", 0),
                    Pair("rank", "bronze"),
                    Pair("gold", 0),
                    Pair("date_joined", LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-uuuu"))),
                    Pair("last_lesson", "01-01-2000"),
                    Pair("followers", listOf("")),
                    Pair("following", listOf("")))
            ).addOnCompleteListener {
                if (it.isSuccessful)
                    Log.d("PostRegister", "Successfully Created User Data!")
                else
                    Log.d((it.exception as FirebaseAuthException).errorCode, it.exception!!.localizedMessage!!)
            }
        }

        private fun addUserToRanking(uid: String, fireDatabase: DatabaseReference, fragmentReference: RegisterFragment)
        {
            fireDatabase.child("ranking/bronze").addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    val newRanking = mutableListOf<HashMap<String,Any>>()
                    (snapshot.value as ArrayList<HashMap<String, Any>>).mapTo(newRanking) { userRank -> userRank}
                    newRanking.add(hashMapOf<String, Any>(
                        Pair("id", uid),
                        Pair("username", ""),
                        Pair("profile_picture", ""),
                        Pair("weekly_exp", 0)))
                    fireDatabase.child("ranking/bronze").setValue(newRanking).addOnCompleteListener {
                        if (it.isSuccessful)
                        {
                            Log.d("PostRegister", "User Successfully Added to ranking!")
                            fragmentReference.OnUserRegistrationComplete()
                        }
                        else
                            Log.d((it.exception as FirebaseAuthException).errorCode, it.exception!!.localizedMessage!!)
                    }
                }

                override fun onCancelled(error: DatabaseError)
                {
                    Log.d((error as FirebaseAuthException).errorCode, error.localizedMessage!!)
                }
            })
        }

        private fun addFriendsListToUser(uid: String, fireDatabase: DatabaseReference)
        {
            fireDatabase.child("followers")
                .setValue(hashMapOf(Pair(uid, arrayListOf<HashMap<String, Any>>(hashMapOf(Pair("0", " "))))))
                .addOnCompleteListener {
                    if (it.isSuccessful)
                    {
                        Log.d("PostRegister", "User Can now have followers")
                    }
                    else
                        Log.d((it.exception as FirebaseAuthException).errorCode, it.exception!!.localizedMessage!!)
                    fireDatabase.child("following")
                        .setValue(hashMapOf(Pair(uid, arrayListOf<HashMap<String, Any>>(hashMapOf(Pair("0", " "))))))
                        .addOnCompleteListener {
                            if (it.isSuccessful)
                            {
                                Log.d("PostRegister", "User can now follow others")
                            }
                            else
                                Log.d((it.exception as FirebaseAuthException).errorCode, it.exception!!.localizedMessage!!)
                        }
                }
        }




        class ImageLoader(private val imageContainer: AppCompatImageView): AsyncTask<String, Void, Bitmap>()
        {
            override fun doInBackground(vararg urls: String): Bitmap?
            {
                val url = urls[0]
                var image: Bitmap? = null
                try
                {
                    val input = URL(url).openStream()
                    image = BitmapFactory.decodeStream(input)
                }
                catch (e: Exception)
                {
                    Log.e("Error", e.message!!)
                    e.printStackTrace()
                }
                return image
            }

            override fun onPostExecute(result: Bitmap?)
            {
                if (result != null)
                    imageContainer.setImageBitmap(result)
            }
        }
    }
}