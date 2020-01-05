package com.ryanjames.swabergersmobilepos.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class Menu(
    val categories: List<Category>
) {
    companion object {
        val EMPTY = Menu(listOf())
    }
}

data class Category(
    val categoryId: String,
    val categoryName: String,
    val products: List<Product>
) {
    companion object {
        val EMPTY = Category("0", "", listOf())
    }
}

@Parcelize
data class Product(
    val productId: String,
    val productName: String,
    val price: Float,
    val receiptText: String,
    val bundles: List<ProductBundle>,
    val modifierGroups: List<ModifierGroup>
) : Parcelable {
    companion object {
        val EMPTY = Product("", "", 0f, "", listOf(), listOf())
    }
}

@Parcelize
data class ProductBundle(
    val bundleId: String,
    val bundleName: String,
    val price: Float,
    val receiptText: String,
    val productGroups: List<ProductGroup>
) : Parcelable

@Parcelize
data class ModifierGroup(
    val modifierGroupId: String,
    val modifierGroupName: String,
    val action: ModifierGroupAction,
    val defaultSelection: ModifierInfo,
    val options: List<ModifierInfo>
) : Parcelable {
    companion object {
        val EMPTY = ModifierGroup("0", "", ModifierGroupAction.Required, ModifierInfo.EMPTY, listOf())
    }
}

@Parcelize
data class ModifierInfo(
    val modifierId: String,
    val modifierName: String,
    val priceDelta: Float,
    val receiptText: String
) : Parcelable {
    companion object {
        val EMPTY = ModifierInfo("0", "", 0f, "")
    }
}

@Parcelize
data class ProductGroup(
    val productGroupId: String,
    val productGroupName: String,
    val defaultProduct: Product,
    val options: List<Product>
) : Parcelable


sealed class ModifierGroupAction : Parcelable {

    @Parcelize
    object Required : ModifierGroupAction()

    @Parcelize
    object Optional : ModifierGroupAction()

}

