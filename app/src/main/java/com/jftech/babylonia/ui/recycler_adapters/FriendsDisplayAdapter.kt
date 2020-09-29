package com.jftech.babylonia.ui.recycler_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.jftech.babylonia.R
import com.jftech.babylonia.connectors.UserViewModel

class FriendsDisplayAdapter(private val friends: Array<Array<String>>, private val userViewModelReference: UserViewModel): RecyclerView.Adapter<FriendsDisplayAdapter.FriendsDisplayViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsDisplayViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.follower_layout, parent, false)
        return FriendsDisplayViewHolder(view)
    }

    override fun getItemCount(): Int
    {
        return friends.size
    }

    override fun onBindViewHolder(holder: FriendsDisplayViewHolder, position: Int)
    {
        val container: ConstraintLayout = holder.itemView as ConstraintLayout
        val userProfileIcon: AppCompatImageView = container.findViewById(R.id.follower_profile_picture_imageview)
        val userName: AppCompatTextView = container.findViewById(R.id.follower_username_textview)
        val followUnfollowButton: AppCompatButton = container.findViewById(R.id.follower_followunfollow_button)
        val friend = friends[position]
        userViewModelReference.RequestImageForImageContainerWithURL(userProfileIcon, friend[2])
        userName.text = friend[1]
        followUnfollowButton.text = if (userViewModelReference.CurrentUser.value!!.Following.contains(friend[0])) "Unfollow" else "Follow"
        setClickEventForFollowButton(followUnfollowButton, friend[0], friend[1], friend[2])
    }

    private fun setClickEventForFollowButton(button: AppCompatButton, uid: String, username: String, profilePic: String)
    {
        button.setOnClickListener {
            if (button.text == "Follow")
                userViewModelReference.FollowUser(uid, username, profilePic)
            else
                userViewModelReference.UnfollowUser(uid, username, profilePic)
        }
    }

    class FriendsDisplayViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
}