package com.jftech.babylonia.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jftech.babylonia.R
import com.jftech.babylonia.connectors.UserViewModel
import com.jftech.babylonia.data.models.UserRank
import com.jftech.babylonia.ui.recycler_adapters.RankingAdapter

class RankingFragment(private val userViewModelReference: UserViewModel): Fragment()
{
    private lateinit var rankingRecyclerView: RecyclerView



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.ranking_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        view.post {
            wireViews(view)
            userViewModelReference.RequestRanking(this)
        }
    }

    fun OnRankingsRecieved(rankings: Array<UserRank>)
    {
        setupRecyclerView(rankings)
    }


    private fun wireViews(view: View)
    {
        rankingRecyclerView = view.findViewById(R.id.ranking_recyclerview)
    }

    private fun setupRecyclerView(rankings: Array<UserRank>)
    {
        rankings.sortBy { userRank -> userRank.WeeklyExp }
        rankingRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        rankingRecyclerView.adapter = RankingAdapter(rankings, requireContext(), userViewModelReference)
    }
}