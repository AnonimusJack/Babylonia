package com.jftech.babylonia.connectors

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.jftech.babylonia.data.UserRepository
import com.jftech.babylonia.data.models.Course
import com.jftech.babylonia.data.models.User
import com.jftech.babylonia.ui.main.auxiliary.CoursesPopOver
import com.jftech.babylonia.ui.main.auxiliary.LessonTopbarFragment
import com.jftech.babylonia.ui.main.MainActivity
import com.jftech.babylonia.ui.main.fragments.ProfileFragment
import com.jftech.babylonia.ui.main.fragments.RankingFragment
import java.time.LocalDate


//Retain cycle warning for reference
class UserViewModel(private val mainActivityReference: MainActivity): ViewModel()
{
    enum class TabType {Lessons, Profile, Ranking, Store}
    enum class FollowState { Follow, Unfollow}
    private val userRepository = UserRepository()
    private val timerHandler = Handler(Looper.getMainLooper())
    var RankingTimeLeft: Array<Int> = arrayOf(6,23,59)
    var StreakOn = false
    lateinit var LastLesson: LocalDate
    var UsedUserID: String
    private var currentCourse: MutableLiveData<Course> = MutableLiveData(Course())
    val CurrentCourse: LiveData<Course>
        get() {return currentCourse as LiveData<Course>}
    private var currentUser: MutableLiveData<User> = MutableLiveData(User())
    val CurrentUser: LiveData<User>
        get() {return  currentUser as LiveData<User>}
    private var viewedUser: MutableLiveData<User> = MutableLiveData()
    val ViewedUser: LiveData<User>
        get() {return  viewedUser as LiveData<User>}
    var CurrentTab: TabType = TabType.Lessons
        set(value) {
            field = value
            mainActivityReference.OnTabSet(field)
        }

    init
    {
        val uid = FirebaseAuth.getInstance().uid!!
        userRepository.OnUserRequested(uid, this, this.mainActivityReference)
        UsedUserID = uid
        StreakOn = userRepository.LoadCurrentStreakState()
        timerHandler.post(object: Runnable { override fun run() { timerTick(); timerHandler.postDelayed(this, 60000)} })
    }


    fun RequestPopOverFor(topbarType: LessonTopbarFragment.TopbarType)
    {
        mainActivityReference.ShowPopOver(when(topbarType){
            LessonTopbarFragment.TopbarType.Course -> CoursesPopOver(this)
            else -> Fragment()
        }, topbarType.name)
    }

    fun OnSettingsFinished()
    {
        userRepository.UpdateUserChangedToDatabase(this, mainActivityReference)
    }

    fun OnNewCourseSelected(name: String)
    {
        currentCourse.value = currentUser.value!!.Courses.firstOrNull { course -> course.Name == name }
    }

    fun RequestToViewUser(id: String)
    {
        userRepository.OnUserRequested(id, this, mainActivityReference)
    }

    fun SetCurrentUser(user: User)
    {
        currentUser.value = user
    }

    fun SetViewedUser(user: User)
    {
        viewedUser.value = user
    }

    fun SetNewProfilePicture(newURL: String)
    {
        currentUser.value!!.ProfilePictureURL = newURL
    }

    fun RequestImageForImageContainerWithURL(container: AppCompatImageView, url: String)
    {
        UserRepository.Companion.ImageLoader(container).execute(url)
    }

    fun UploadProfilePicute(picture: Bitmap)
    {
        userRepository.UploadProfilePicture(picture , this, mainActivityReference)
    }

    fun FollowUser(userID: String, username: String, profilePic: String)
    {
        userRepository.ChangeFollowerStateOfUser(userID, username, profilePic, this, FollowState.Follow, mainActivityReference)
    }

    fun UnfollowUser(userID: String, username: String, profilePic: String)
    {
        userRepository.ChangeFollowerStateOfUser(userID, username, profilePic, this, FollowState.Unfollow, mainActivityReference)
    }


    fun RequestRanking(fragmentReference: RankingFragment)
    {
        userRepository.RequestRankingFor(currentUser.value!!.Rank, fragmentReference)
    }

    fun RequestFriendsListFor(user: String, friendDisplayType: ProfileFragment.FriendDisplayType, fragmentReference: ProfileFragment)
    {
        userRepository.RequestFriendsListFor(user, friendDisplayType, fragmentReference)
    }

    fun RequestToShowUserProfile(userID: String)
    {
        UsedUserID = userID
        mainActivityReference.OnUserProfileRequested()
    }

    fun OnLessonFinished(exp: Int)
    {
        // TODO: 28/09/2020 Add streak calculation function
        val user = currentUser.value!!
        val cCourse = currentCourse.value!!
        userRepository.SaveCurrentStreakState(StreakOn)
        userRepository.OnLessonFinishedUpdates(cCourse.Exp + exp, exp, user.Rank, user.Streak, user.ID, user.Courses.indexOfFirst { course -> course.Name == cCourse.Name })
    }



    private fun timerTick()
    {
        if (RankingTimeLeft[2] == 0)
        {
            RankingTimeLeft[2] = 59
            if (RankingTimeLeft[1] == 0)
            {
                if (RankingTimeLeft[0] == 0)
                    RankingTimeLeft = arrayOf(6, 23, 59)
                else
                    RankingTimeLeft[0]--
            }
            else
                RankingTimeLeft[1]--
        }
        else
            RankingTimeLeft[2]--
    }




    class UserViewModelFactory(private val mainActivityReference: MainActivity): ViewModelProvider.Factory
    {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T = modelClass.getConstructor(MainActivity::class.java).newInstance(mainActivityReference)
    }
}