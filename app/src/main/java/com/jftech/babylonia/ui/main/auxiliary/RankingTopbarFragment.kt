package com.jftech.babylonia.ui.main.auxiliary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jftech.babylonia.connectors.UserViewModel
import com.jftech.babylonia.databinding.RankingTopbarFragmentBinding
import com.jftech.babylonia.ui.binding_adapters.RankingImageBindingAdapter

class RankingTopbarFragment(private val userViewModelReference: UserViewModel): Fragment()
{
    private lateinit var binding: RankingTopbarFragmentBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = RankingTopbarFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding.user = userViewModelReference.CurrentUser.value!!
        binding.viewModel = userViewModelReference
        binding.rankImageAdapter = RankingImageBindingAdapter
    }
}