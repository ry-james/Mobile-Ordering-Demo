package com.ryanjames.swabergersmobilepos.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*
import kotlin.collections.HashMap

@Parcelize
data class LineItem(
    val lineItemId: String,
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
            for ((_, modifiers) in modifiers) {
                for (modifier in modifiers) {
                    price += modifier.priceDelta
                }
            }
            return price * quantity.coerceAtLeast(1)
        }

    val lineItemName: String
        get() {
            return bundle?.bundleName ?: product.productName
        }

    fun deepCopy(): LineItem {
        return this.copy(productsInBundle = HashMap(this.productsInBundle), modifiers = HashMap(this.modifiers))
    }

    companion object {
        val EMPTY: LineItem
            get() = LineItem("", Product.EMPTY, null, hashMapOf(), hashMapOf(), 1)

        fun ofProduct(product: Product): LineItem {
            return EMPTY.copy(lineItemId = UUID.randomUUID().toString(), product = product)
        }
    }
}

@Parcelize
data class ProductModifierGroupKey(val product: Product, val modifierGroup: ModifierGroup) : Parcelable {

    override fun toString(): String {
        return "PRODUCT: ${product.productId}, MODIFIER_GROUP: ${modifierGroup.modifierGroupId}"
    }
}