package com.ryanjames.swabergersmobilepos.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Menu(
    val categories: List<Category>
) : Parcelable {
    companion object {
        val EMPTY: Menu
            get() = Menu(listOf())
    }
}

@Parcelize
data class Category(
    val categoryId: String,
    val categoryName: String,
    val products: List<Product>
) : Parcelable {
    companion object {
        val EMPTY: Category
            get() = Category("0", "", listOf())
    }
}

@Parcelize
data class Product(
    val productId: String,
    val productName: String,
    val productDescription: String,
    val price: Float,
    val receiptText: String,
    val bundles: List<ProductBundle>,
    val modifierGroups: List<ModifierGroup>
) : Parcelable {

    companion object {
        val EMPTY: Product
            get() = Product("", "", "", 0f, "", listOf(), listOf())
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
    val defaultSelection: ModifierInfo?,
    val options: List<ModifierInfo>,
    val min: Int,
    val max: Int
) : Parcelable {

    companion object {
        val EMPTY: ModifierGroup
            get() = ModifierGroup("", "", ModifierGroupAction.Optional, ModifierInfo.EMPTY, listOf(), 1, 1)
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
        val EMPTY: ModifierInfo
            get() = ModifierInfo("", "", 0f, "")
    }
}

@Parcelize
data class ProductGroup(
    val productGroupId: String,
    val productGroupName: String,
    val defaultProduct: Product,
    val options: List<Product>,
    val min: Int,
    val max: Int
) : Parcelable {

    companion object {
        val EMPTY: ProductGroup
            get() = ProductGroup("", "", Product.EMPTY, listOf(), 1, 1)
    }
}


sealed class ModifierGroupAction : Parcelable {

    @Parcelize
    object Required : ModifierGroupAction()

    @Parcelize
    object Optional : ModifierGroupAction()

}

