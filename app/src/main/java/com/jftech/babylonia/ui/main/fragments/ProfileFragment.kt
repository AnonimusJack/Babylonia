package com.jftech.babylonia.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.jftech.babylonia.R
import com.jftech.babylonia.connectors.UserViewModel
import com.jftech.babylonia.data.models.User
import com.jftech.babylonia.ui.recycler_adapters.FriendsDisplayAdapter
import com.jftech.babylonia.ui.recycler_adapters.ProfileCoursesAdapter
import java.time.format.DateTimeFormatter

class ProfileFragment(private val userViewModelReference: UserViewModel, private val userID: String): Fragment()
{
    enum class FriendDisplayType {Followers, Following}
    private val selfProfile = userID == userViewModelReference.CurrentUser.value!!.ID
    //User layout
    private lateinit var profilePictureImageView: AppCompatImageView
    private lateinit var fullnameTextView: AppCompatTextView
    private lateinit var usernameTextView: AppCompatTextView
    private lateinit var joiningDateTextView: AppCompatTextView
    private lateinit var followersFollowingTextView: AppCompatTextView
    private lateinit var coursesContainer: FlexboxLayout
    private lateinit var followUnfollowButton: AppCompatButton
    //Statistics layout
    private lateinit var dayStreakTextView: AppCompatTextView
    private lateinit var totalExpTextView: AppCompatTextView
    private lateinit var totalMedalsTextView: AppCompatTextView
    private lateinit var rankTextView: AppCompatTextView
    //Courses layout
    private lateinit var coursesRecyclerView: RecyclerView
    //Friends layout
    private lateinit var followingButton: AppCompatButton
    private lateinit var followersButton: AppCompatButton
    private lateinit var friendsRecyclerView: RecyclerView




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return if (selfProfile)
        {
            inflater.inflate(R.layout.profile_fragment, container, false)
        }
        else
        {
            userViewModelReference.ViewedUser.observe(viewLifecycleOwner, Observer {
                layoutDataForUserLayout(it)
                layoutDataForStatisticsLayout(it)
                layoutDataForCoursesLayout()
            })
            inflater.inflate(R.layout.profile_else_layout, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        view.post {
            wireVies(view)
            if (selfProfile)
            {
                val user = userViewModelReference.CurrentUser.value!!
                layoutDataForUserLayout(user)
                layoutDataForStatisticsLayout(user)
                layoutDataForFriendsLayout(user)
            }
            else
                userViewModelReference.RequestToViewUser(userID)
        }
    }

    fun SetFriendsAdapterFor(friendsRequestedList: Array<Array<String>>)
    {
        friendsRecyclerView.adapter = FriendsDisplayAdapter(friendsRequestedList, userViewModelReference)
        friendsRecyclerView.invalidate()
    }

    private fun layoutDataForUserLayout(user: User)
    {
        fullnameTextView.text = user.Name
        usernameTextView.text = user.Username
        userViewModelReference.RequestImageForImageContainerWithURL(profilePictureImageView, user.ProfilePictureURL)
        joiningDateTextView.text = user.DateJoined.format(DateTimeFormatter.ofPattern("MMM yyyy"))
        followersFollowingTextView.text = "${user.Followers.size} Followers / Following ${user.Following.size}"
        if (selfProfile)
            for (course in user.Courses)
                coursesContainer.addView(buildImageViewForCourse(course.Name))
        else
            if (userViewModelReference.CurrentUser.value!!.Following.contains(userViewModelReference.ViewedUser.value!!.ID))
                followUnfollowButton.text = resources.getString(R.string.unfollow_button)
            else
                followUnfollowButton.text = resources.getString(R.string.follow_button)
    }

    private fun layoutDataForStatisticsLayout(user: User)
    {
        totalMedalsTextView.text = user.Medals.toString()
        dayStreakTextView.text = user.Streak.toString()
        totalExpTextView.text = user.Exp.toString()
        rankTextView.text = user.Rank
    }

    private fun layoutDataForCoursesLayout()
    {
        coursesRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        coursesRecyclerView.adapter = ProfileCoursesAdapter(userViewModelReference, requireContext())
    }

    private fun layoutDataForFriendsLayout(user: User)
    {
        friendsRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        userViewModelReference.RequestFriendsListFor(user.ID, FriendDisplayType.Followers, this)
        followingButton.setOnClickListener { userViewModelReference.RequestFriendsListFor(user.ID, FriendDisplayType.Following, this) }
        followersButton.setOnClickListener { userViewModelReference.RequestFriendsListFor(user.ID, FriendDisplayType.Followers, this) }
    }


    private fun wireVies(view: View)
    {
        profilePictureImageView = view.findViewById(R.id.profile_profile_picture_imageview)
        fullnameTextView = view.findViewById(R.id.profile_fullname_textview)
        usernameTextView = view.findViewById(R.id.profile_username_textview)
        joiningDateTextView = view.findViewById(R.id.profile_datejoined_textview)
        followersFollowingTextView = view.findViewById(R.id.profile_followers_textview)
        dayStreakTextView = view.findViewById(R.id.profile_streak_count_textview)
        totalExpTextView = view.findViewById(R.id.profile_exp_points_textview)
        totalMedalsTextView = view.findViewById(R.id.profile_medals_count_textview)
        rankTextView = view.findViewById(R.id.profile_rank_textview)
        friendsRecyclerView = view.findViewById(R.id.profile_friends_recyclerview)
        followersButton = view.findViewById(R.id.profile_followers_show_button)
        followingButton = view.findViewById(R.id.profile_following_show_button)
        if (selfProfile)
            coursesContainer = view.findViewById(R.id.profile_courses_container)
        else
        {
            coursesRecyclerView = view.findViewById(R.id.profile_courses_else_container)
            followUnfollowButton = view.findViewById(R.id.profile_follow_unfollow_button)
        }
    }

    private fun buildImageViewForCourse(courseName: String): AppCompatImageView
    {
        val params = FlexboxLayout.LayoutParams(45,45)
        params.apply { setMargins(10,10,0,10) }
        val courseImageView = AppCompatImageView(requireContext())
        courseImageView.apply {
            layoutParams = params
        }
        courseImageView.setImageDrawable(resources.getDrawable(resources.getIdentifier(courseName.toLowerCase(), "drawable", requireContext().packageName)))
        return courseImageView
    }
}