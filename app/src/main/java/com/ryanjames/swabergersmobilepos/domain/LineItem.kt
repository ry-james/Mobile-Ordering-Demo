package com.ryanjames.swabergersmobilepos.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LineItem(
    val id: String,
    val product: Product,
    val bundle: ProductBundle?,
    val productsInBundle: HashMap<ProductGroup, List<Product>>,
    val modifiers: HashMap<ProductModifierGroupKey, List<ModifierInfo>>,
    val quantity: Int
) : Parcelable {

    val unitPrice: Float
        get() = bundle?.price ?: product.price

    val price: Float
        get() {
            var price = unitPrice
            for ((key, modifiers) in modifiers) {
                for (modifier in modifiers) {
                    price += modifier.priceDelta
                }
            }
            return price * quantity
        }

    val lineItemName: String
        get() {
            return bundle?.bundleName ?: product.productName
        }

}

@Parcelize
data class ProductModifierGroupKey(val product: Product, val modifierGroup: ModifierGroup) : Parcelable