package com.ryanjames.swabergersmobilepos.domain

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.ryanjames.swabergersmobilepos.R

data class LoadingDialogBinding(
    val visibility: Int,
    val loadingText: String,
    @ColorRes
    val textColor: Int = R.color.colorBackgroundGray
)

data class ErrorViewBinding(
    @DrawableRes
    val image: Int,
    val title: String,
    val message: String,
    val visibility: Int
)