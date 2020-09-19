package com.ryanjames.swabergersmobilepos.domain

import com.ryanjames.swabergersmobilepos.R

data class LoadingDialogBinding(
    val visibility: Int,
    val loadingText: String,
    val textColor: Int = R.color.colorBackgroundGray
)