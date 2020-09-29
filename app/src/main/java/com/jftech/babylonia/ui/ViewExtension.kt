package com.jftech.babylonia.ui

import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils
import com.jftech.babylonia.R

fun View.AnimateShake(context: Context)
{
    this.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
}