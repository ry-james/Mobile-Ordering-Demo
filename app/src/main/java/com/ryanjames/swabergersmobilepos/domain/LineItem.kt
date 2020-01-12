package com.ryanjames.swabergersmobilepos.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LineItem(
    val product: Product,
    val bundle: ProductBundle?,
    val productsInBundle: HashMap<ProductGroup, List<Product>>,
    val modifiers: HashMap<Pair<Product, ModifierGroup>, List<ModifierInfo>>,
    val quantity: Int
) : Parcelable