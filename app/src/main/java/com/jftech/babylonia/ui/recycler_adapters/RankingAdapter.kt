package com.jftech.babylonia.ui.recycler_adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.jftech.babylonia.R
import com.jftech.babylonia.connectors.UserViewModel
import com.jftech.babylonia.data.models.UserRank

class RankingAdapter(private val rankings: Array<UserRank>, private val context: Context, private val userViewModelReference: UserViewModel): RecyclerView.Adapter<RankingAdapter.RankingViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.profile_rank_recycle_layout, parent, false)
        return RankingViewHolder(view)
    }

    override fun getItemCount(): Int = rankings.size

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int)
    {
        val userRank = rankings[position]
        val container = holder.itemView as ConstraintLayout
        val rankTextView: AppCompatTextView = container.findViewById(R.id.profile_rank_number_textview)
        val userProfilePictureImageView: AppCompatImageView = container.findViewById(R.id.profile_rank_profile_picture_imageview)
        val usernameTextView: AppCompatTextView = container.findViewById(R.id.profile_rank_username_textview)
        val weeklyExpTextView: AppCompatTextView = container.findViewById(R.id.profile_rank_weekly_exp_textview)
        rankTextView.text = position.toString()
        userViewModelReference.RequestImageForImageContainerWithURL(userProfilePictureImageView, userRank.ProfilePictureUrl)
        usernameTextView.text = userRank.Username
        weeklyExpTextView.text = "${userRank.WeeklyExp} xp"
        setOnClickEvent(usernameTextView, userRank)
    }

    private fun setOnClickEvent(view: View, userRank: UserRank)
    {
        view.setOnClickListener {
            userViewModelReference.RequestToShowUserProfile(userRank.UserID)
        }
    }

    class RankingViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
}