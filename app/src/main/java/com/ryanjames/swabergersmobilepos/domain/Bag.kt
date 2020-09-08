package com.ryanjames.swabergersmobilepos.domain

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BagSummary(
    val lineItems: List<BagLineItem>,
    val price: Float
) : Parcelable {

    // TODO: Replace with API value
    fun tax(): Float {
        return subtotal() * .12f
    }

    // TODO: Replace with API value
    fun subtotal(): Float {
        return price / 1.12f
    }

}

@Parcelize
data class BagLineItem(
    val lineItemId: String,
    val productId: String,
    val bundleId: String?,
    val lineItemName: String,
    val modifiersDisplay: String,
    val price: Float,
    val productsInBundle: HashMap<String, List<String>>,
    val modifiers: HashMap<ProductIdModifierGroupIdKey, List<String>>,
    val quantity: Int
) : Parcelable {

    @IgnoredOnParcel
    var product: Product = Product.EMPTY

    fun deepCopy(): BagLineItem {
        return this.copy(productsInBundle = HashMap(this.productsInBundle), modifiers = HashMap(this.modifiers))
    }

    companion object {
        val EMPTY: BagLineItem
            get() = BagLineItem("", "", null, "", "", 0f, hashMapOf(), hashMapOf(), 0)
    }
}

@Parcelize
data class ProductIdModifierGroupIdKey(val productId: String, val modifierGroupId: String) : Parcelable {

    override fun toString(): String {
        return "PRODUCT: ${productId}, MODIFIER_GROUP: ${modifierGroupId}"
    }
}