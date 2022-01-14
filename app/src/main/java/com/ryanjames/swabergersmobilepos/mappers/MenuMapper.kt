package com.ryanjames.swabergersmobilepos.mappers

import com.ryanjames.swabergersmobilepos.database.realm.*
import com.ryanjames.swabergersmobilepos.domain.*
import com.ryanjames.swabergersmobilepos.network.responses.*
import io.realm.RealmList
import java.util.*

class BasicMenuMapper : DataMapper<BasicMenuRealmEntity, BasicMenuResponse, Menu> {

    override fun mapRemoteToLocalDb(input: BasicMenuResponse): BasicMenuRealmEntity {
        val categoryList = RealmList<BasicCategoryRealmEntity>()

        for (categoryResponse in input.categories) {
            if (categoryResponse.categoryId != null && categoryResponse.categoryName != null) {
                val categoryRealm = BasicCategoryRealmEntity(categoryResponse.categoryId, categoryResponse.categoryName, RealmList<BasicProductRealmEntity>())

                categoryResponse.products?.forEach { productResponse ->
                    if (productResponse.productId != null && productResponse.productName != null) {
                        val productRealm = BasicProductRealmEntity(productResponse.productId, productResponse.productName, productResponse.price ?: 0f, productResponse.imageUrl)
                        categoryRealm.products.add(productRealm)
                    }
                }
                categoryList.add(categoryRealm)
            }
        }

        return BasicMenuRealmEntity(categoryList, Date(), "")
    }

    fun mapRemoteToLocalDbWithStoreId(input: BasicMenuResponse, storeId: String): BasicMenuRealmEntity {
        val basicMenuRealm = mapRemoteToLocalDb(input)
        basicMenuRealm.storeId = storeId
        return basicMenuRealm
    }

    override fun mapLocalDbToDomain(input: BasicMenuRealmEntity): Menu {
        val categoryEntities = input.categories
        val categoryList = mutableListOf<Category>()
        categoryEntities.forEach { categoryEntity ->
            val productList = mutableListOf<Product>()
            val category = Category(categoryEntity.categoryId, categoryEntity.categoryName, productList)
            categoryEntity.products.forEach { productEntity ->
                val product = Product.EMPTY.copy(productId = productEntity.productId, productName = productEntity.productName, price = productEntity.price, imageUrl = productEntity.imageUrl)
                productList.add(product)
            }
            categoryList.add(category)
        }

        return Menu(categoryList)
    }

    override fun mapDomainToLocalDb(input: Menu): BasicMenuRealmEntity {
        TODO("Not yet implemented")
    }
}

class ProductMapper : DataMapper<ProductRealmEntity, ProductResponse, Product> {

    private val modifierGroupMapper = ModifierGroupMapper()
    private val bundleMapper = ProductBundleMapper()

    override fun mapRemoteToLocalDb(input: ProductResponse): ProductRealmEntity {
        if (input.productId == null || input.productName == null) return ProductRealmEntity()
        return ProductRealmEntity(
            input.productId,
            input.productName,
            input.productDescription ?: "",
            input.price ?: 0f,
            input.receiptText ?: "",
            RealmList(),
            RealmList(),
            null
        )
    }

    override fun mapLocalDbToDomain(input: ProductRealmEntity): Product {
        return Product(
            input.productId,
            input.productName,
            input.productDescription,
            input.price,
            input.receiptText,
            bundleMapper.mapLocalDbToDomain(input.bundles),
            modifierGroupMapper.mapLocalDbToDomain(input.modifierGroups),
            input.imageUrl
        )
    }

    override fun mapDomainToLocalDb(input: Product): ProductRealmEntity {
        return ProductRealmEntity(
            input.productId,
            input.productName,
            input.productDescription,
            input.price,
            input.receiptText,
            modifierGroupMapper.mapDomainToLocalDb(input.modifierGroups),
            bundleMapper.mapDomainToLocalDb(input.bundles),
            input.imageUrl
        )
    }
}

class ProductBundleMapper : DataMapper<ProductBundleRealmEntity, BundleResponse, ProductBundle> {

    private val productGroupMapper = ProductGroupMapper()

    override fun mapRemoteToLocalDb(input: BundleResponse): ProductBundleRealmEntity {
        if (input.bundleId == null || input.bundleName == null) return ProductBundleRealmEntity()
        return ProductBundleRealmEntity(
            input.bundleId,
            input.bundleName,
            input.price ?: 0f,
            input.receiptText ?: "",
            RealmList()
        )
    }


    override fun mapLocalDbToDomain(input: ProductBundleRealmEntity): ProductBundle {
        return ProductBundle(
            input.bundleId,
            input.bundleName,
            input.price,
            input.receiptText,
            productGroupMapper.mapLocalDbToDomain(input.productGroups)
        )
    }

    override fun mapDomainToLocalDb(input: ProductBundle): ProductBundleRealmEntity {

        return ProductBundleRealmEntity(
            input.bundleId,
            input.bundleName,
            input.price,
            input.receiptText,
            productGroupMapper.mapDomainToLocalDb(input.productGroups)
        )
    }
}

class ProductGroupMapper : DataMapper<ProductGroupRealmEntity, ProductGroupResponse, ProductGroup> {

    private val modifierGroupMapper = ModifierGroupMapper()

    override fun mapRemoteToLocalDb(input: ProductGroupResponse): ProductGroupRealmEntity {
        if (input.productGroupId == null || input.productGroupName == null) return ProductGroupRealmEntity()
        return ProductGroupRealmEntity(
            input.productGroupId,
            input.productGroupName,
            RealmList(),
            null,
            input.min ?: 1,
            input.max ?: 1
        )
    }

    override fun mapLocalDbToDomain(input: ProductGroupRealmEntity): ProductGroup {
        return ProductGroup(
            productGroupId = input.productGroupId,
            productGroupName = input.productGroupName,
            defaultProduct = mapToEmptyBundleDomain(input.defaultProduct ?: ProductGroupOptionRealmEntity()),
            options = mapToEmptyBundleDomain(input.options),
            min = input.min,
            max = input.max
        )
    }


    private fun mapToEmptyBundleDomain(input: ProductGroupOptionRealmEntity): Product {
        return Product(
            productId = input.productId,
            productName = input.productName,
            productDescription = "",
            price = input.price,
            receiptText = "",
            bundles = listOf(),
            modifierGroups = modifierGroupMapper.mapLocalDbToDomain(input.modifierGroups),
            imageUrl = null
        )
    }

    private fun mapToEmptyBundleDomain(input: List<ProductGroupOptionRealmEntity>): List<Product> {
        return input.map { mapToEmptyBundleDomain(it) }
    }

    private fun mapDomainToEmptyBundleEntity(input: Product): ProductGroupOptionRealmEntity {
        return ProductGroupOptionRealmEntity(
            productId = input.productId,
            productName = input.productName,
            price = input.price,
            modifierGroups = modifierGroupMapper.mapDomainToLocalDb(input.modifierGroups)
        )
    }

    private fun mapDomainToEmptyBundleEntity(input: List<Product>): RealmList<ProductGroupOptionRealmEntity> {
        return RealmList<ProductGroupOptionRealmEntity>().apply {
            addAll(input.map { mapDomainToEmptyBundleEntity(it) })
        }
    }


    override fun mapDomainToLocalDb(input: ProductGroup): ProductGroupRealmEntity {

        val options = mapDomainToEmptyBundleEntity(input.options)
        val defaultProduct = options.find { it.productId == input.defaultProduct.productId }

        return ProductGroupRealmEntity(
            productGroupId = input.productGroupId,
            productGroupName = input.productGroupName,
            options = options,
            defaultProduct = defaultProduct,
            min = input.min,
            max = input.max
        )
    }
}

class ModifierInfoMapper : DataMapper<ModifierInfoRealmEntity, ModifierInfoResponse, ModifierInfo> {

    override fun mapRemoteToLocalDb(input: ModifierInfoResponse): ModifierInfoRealmEntity {
        if (input.modifierId == null || input.modifierName == null) return ModifierInfoRealmEntity()
        return ModifierInfoRealmEntity(input.modifierId, input.modifierName, input.priceDelta ?: 0f, input.receiptText ?: "")
    }

    override fun mapLocalDbToDomain(input: ModifierInfoRealmEntity): ModifierInfo {
        return ModifierInfo(input.modifierId, input.modifierName, input.priceDelta, input.receiptText)
    }

    override fun mapDomainToLocalDb(input: ModifierInfo): ModifierInfoRealmEntity {
        return ModifierInfoRealmEntity(input.modifierId, input.modifierName, input.priceDelta, input.receiptText)
    }
}

class ModifierGroupMapper : DataMapper<ModifierGroupRealmEntity, ModifierGroupResponse, ModifierGroup> {

    private val modifierInfoMapper = ModifierInfoMapper()

    override fun mapRemoteToLocalDb(input: ModifierGroupResponse): ModifierGroupRealmEntity {
        if (input.modifierGroupId == null || input.modifierGroupName == null) return ModifierGroupRealmEntity()

        val modifierInfoRealmList = RealmList<ModifierInfoRealmEntity>()
        input.options?.let {
            modifierInfoRealmList.addAll(modifierInfoMapper.mapRemoteToLocalDb(it))
        }

        return ModifierGroupRealmEntity(
            input.modifierGroupId,
            input.modifierGroupName,
            input.action ?: "",
            modifierInfoRealmList,
            input.defaultSelection,
            input.min ?: 1,
            input.max ?: 1
        )
    }

    override fun mapLocalDbToDomain(input: ModifierGroupRealmEntity): ModifierGroup {
        return ModifierGroup(
            input.modifierGroupId,
            input.modifierGroupName,
            input.action.toModifierGroupAction(),
            input.getDefaultSelection()?.let {modifierInfoMapper.mapLocalDbToDomain(it)},
            modifierInfoMapper.mapLocalDbToDomain(input.options),
            input.min,
            input.max
        )
    }

    override fun mapDomainToLocalDb(input: ModifierGroup): ModifierGroupRealmEntity {

        val modifierInfoRealmList = RealmList<ModifierInfoRealmEntity>()
        input.options.let { modifierInfoRealmList.addAll(modifierInfoMapper.mapDomainToLocalDb(it)) }

        return ModifierGroupRealmEntity(
            input.modifierGroupId,
            input.modifierGroupName,
            input.action.toString(),
            modifierInfoRealmList,
            input.defaultSelection?.modifierId,
            input.min,
            input.max
        )
    }
}

private fun String?.toModifierGroupAction(): ModifierGroupAction {
    return when (this) {
        "on" -> ModifierGroupAction.Required
        "add" -> ModifierGroupAction.Optional
        else -> ModifierGroupAction.Required
    }
}

private fun ModifierGroupAction?.toString(): String {
    return when (this) {
        ModifierGroupAction.Required -> "on"
        ModifierGroupAction.Optional -> "add"
        else -> ""
    }
}


