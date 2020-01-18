package com.ryanjames.swabergersmobilepos.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LineItem(
    val product: Product,
    val bundle: ProductBundle?,
    val productsInBundle: HashMap<ProductGroup, List<Product>>,
    val modifiers: HashMap<ProductModifierGroupKey, List<ModifierInfo>>,
    val quantity: Int
) : Parcelable {

    val price: Float
        get() {
            var price = bundle?.price ?: product.price
            for ((key, modifiers) in modifiers) {
                for (modifier in modifiers) {
                    price += modifier.priceDelta
                }
            }
            return price * quantity
        }

}

@Parcelize
data class ProductModifierGroupKey(val product: Product, val modifierGroup: ModifierGroup): Parcelable