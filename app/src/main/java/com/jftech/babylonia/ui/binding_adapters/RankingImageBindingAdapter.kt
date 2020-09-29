package com.jftech.babylonia.ui.binding_adapters

import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.jftech.babylonia.data.models.AppSettings

object RankingImageBindingAdapter
{
    private fun numericalValueForRank(rank: String): Int
    {
        return when (rank)
        {
            "bronze" -> 0
            "silver" -> 1
            "gold" -> 2
            "platinum" -> 3
            "emerald" -> 4
            "sapphire" -> 5
            "ruby" -> 6
            "diamond" -> 7
            "master" -> 8
            "grandmaster" -> 9
            else -> 100
        }
    }

    @BindingAdapter("image")
    @JvmStatic
    fun SetImageForRank(imageView: AppCompatImageView, rank: String)
    {
        val context =  AppSettings.AppContext
        val resourceName = "ranking_icon_" + if (numericalValueForRank(imageView.tag as String) <= numericalValueForRank(rank)) imageView.tag as String else "locked"
        val imageResource = context.resources.getDrawable(context.resources.getIdentifier(resourceName, "drawable", context.packageName))
        imageView.setImageDrawable(imageResource)
    }
}