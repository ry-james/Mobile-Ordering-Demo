package com.ryanjames.swabergersmobilepos.domain

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.ryanjames.swabergersmobilepos.R

data class LoadingDialogBinding(
    val visibility: Int,
    @StringRes
    val loadingText: Int,
    @ColorRes
    val textColor: Int = R.color.colorBackgroundGray
)

data class MesssageViewBinding(
    @DrawableRes
    val image: Int,
    @StringRes
    val title: Int,
    @StringRes
    val message: Int,
    val visibility: Int
)