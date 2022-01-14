package com.ryanjames.swabergersmobilepos.feature.venuedetail

import androidx.annotation.DrawableRes
import com.ryanjames.swabergersmobilepos.core.StringResourceArgs

data class ViewCircularTileBindingModel(
    val label: StringResourceArgs,
    @DrawableRes val icon: Int,
    val onClickListener: () -> Unit,
    val visibility: Int
)