package com.ryanjames.swabergersmobilepos.mappers

import com.ryanjames.swabergersmobilepos.database.realm.*
import com.ryanjames.swabergersmobilepos.domain.*
import com.ryanjames.swabergersmobilepos.network.responses.*
import io.realm.RealmList
import java.util.*

class MenuMapper : DataMapper<MenuRealmEntity, MenuResponse, Menu> {

    private val categoryMapper = CategoryMapper()
    private val modifierGroupMapper = ModifierGroupMapper()
    private val productMapper = ProductMapper()
    private val productBundleMapper = ProductBundleMapper()
    private val productGroupMapper = ProductGroupMapper()

    override fun mapRemoteToLocal(input: MenuResponse): MenuRealmEntity {
        val categoryRealmList = RealmList<CategoryRealmEntity>()
        val modifierGroupRealmList = RealmList<ModifierGroupRealmEntity>()
        val productRealmList = RealmList<ProductRealmEntity>()
        val bundleRealmList = RealmList<ProductBundleRealmEntity>()
        val productGroupRealmList = RealmList<ProductGroupRealmEntity>()

        input.modifierGroups?.let {
            val modifierGroupList = modifierGroupMapper.mapRemoteToLocal(it)
            modifierGroupRealmList.addAll(modifierGroupList)
        }

        input.bundles?.forEach { bundleResponse ->
            val bundleEntity = productBundleMapper.mapRemoteToLocal(bundleResponse)
            bundleRealmList.add(bundleEntity)
        }

        input.products?.forEach { productResponse ->
            val productEntity = productMapper.mapRemoteToLocal(productResponse)

            productResponse.modifierGroups?.forEach { modifierGroupId ->
                modifierGroupRealmList.find { it.modifierGroupId == modifierGroupId }?.let { productEntity.modifierGroups.add(it) }
            }

            productResponse.bundles?.forEach { bundleId ->
                bundleRealmList.find { it.bundleId == bundleId }?.let { productEntity.bundles.add(it) }
            }

            productRealmList.add(productEntity)
        }

        input.productGroups?.forEach { productGroupResponse ->
            val productGroupEntity = productGroupMapper.mapRemoteToLocal(productGroupResponse)

            productGroupResponse.defaultProduct?.let { productId ->
                productRealmList.find { it.productId == productId }?.let { productGroupEntity.defaultProduct = it }
            }

            productGroupResponse.options?.forEach { productId ->
                productRealmList.find { it.productId == productId }?.let { productGroupEntity.options.add(it) }
            }

            productGroupRealmList.add(productGroupEntity)
        }

        // Adding product groups to bundles
        input.bundles?.forEach { bundleResponse ->
            val bundleRealm = bundleRealmList.find { it.bundleId == bundleResponse.bundleId }
            bundleRealm?.let {
                bundleResponse.productGroups?.forEach { productGroupId ->
                    productGroupRealmList.find { it.productGroupId == productGroupId }?.let { bundleRealm.productGroups.add(it) }
                }
            }
        }

        input.categories?.forEach { categoryResponse ->
            val categoryEntity = categoryMapper.mapRemoteToLocal(categoryResponse)
            categoryResponse.products?.forEach { productId ->
                productRealmList.find { it.productId == productId }?.let { categoryEntity.products.add(it) }
            }
            categoryRealmList.add(categoryEntity)
        }

        return MenuRealmEntity(categoryRealmList, Date())
    }

    override fun mapLocalToDomain(input: MenuRealmEntity): Menu {
        return Menu(categoryMapper.mapLocalToDomain(input.categories))
    }

    override fun mapDomainToLocal(input: Menu): MenuRealmEntity {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class CategoryMapper : DataMapper<CategoryRealmEntity, CategoryResponse, Category> {

    private val productMapper = ProductMapper()

    override fun mapRemoteToLocal(input: CategoryResponse): CategoryRealmEntity {
        if (input.categoryId != null && input.categoryName != null) {
            return CategoryRealmEntity(input.categoryId, input.categoryName, RealmList())
        }
        return CategoryRealmEntity()
    }

    override fun mapLocalToDomain(input: CategoryRealmEntity): Category {
        return Category(input.categoryId, input.categoryName, productMapper.mapLocalToDomain(input.products))
    }

    override fun mapDomainToLocal(input: Category): CategoryRealmEntity {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}


class ProductMapper : DataMapper<ProductRealmEntity, ProductResponse, Product> {

    private val modifierGroupMapper = ModifierGroupMapper()
    private val bundleMapper = ProductBundleMapper()

    override fun mapRemoteToLocal(input: ProductResponse): ProductRealmEntity {
        if (input.productId == null || input.productName == null) return ProductRealmEntity()
        return ProductRealmEntity(
            input.productId,
            input.productName,
            input.productDescription ?: "",
            input.price ?: 0f,
            input.receiptText ?: "",
            RealmList(),
            RealmList()
        )
    }

    override fun mapLocalToDomain(input: ProductRealmEntity): Product {
        return Product(
            input.productId,
            input.productName,
            input.productDescription,
            input.price,
            input.receiptText,
            bundleMapper.mapLocalToDomain(input.bundles),
            modifierGroupMapper.mapLocalToDomain(input.modifierGroups)
        )
    }

    override fun mapDomainToLocal(input: Product): ProductRealmEntity {
        return ProductRealmEntity(
            input.productId,
            input.productName,
            input.productDescription,
            input.price,
            input.receiptText,
            modifierGroupMapper.mapDomainToLocal(input.modifierGroups),
            bundleMapper.mapDomainToLocal(input.bundles)
        )
    }
}

class ProductBundleMapper : DataMapper<ProductBundleRealmEntity, BundleResponse, ProductBundle> {

    private val productGroupMapper = ProductGroupMapper()

    override fun mapRemoteToLocal(input: BundleResponse): ProductBundleRealmEntity {
        if (input.bundleId == null || input.bundleName == null) return ProductBundleRealmEntity()
        return ProductBundleRealmEntity(
            input.bundleId,
            input.bundleName,
            input.price ?: 0f,
            input.receiptText ?: "",
            RealmList()
        )
    }


    override fun mapLocalToDomain(input: ProductBundleRealmEntity): ProductBundle {
        return ProductBundle(
            input.bundleId,
            input.bundleName,
            input.price,
            input.receiptText,
            productGroupMapper.mapLocalToDomain(input.productGroups)
        )
    }

    override fun mapDomainToLocal(input: ProductBundle): ProductBundleRealmEntity {

        return ProductBundleRealmEntity(
            input.bundleId,
            input.bundleName,
            input.price,
            input.receiptText,
            productGroupMapper.mapDomainToLocal(input.productGroups)
        )
    }
}

class ProductGroupMapper : DataMapper<ProductGroupRealmEntity, ProductGroupResponse, ProductGroup> {

    private val modifierGroupMapper = ModifierGroupMapper()

    override fun mapRemoteToLocal(input: ProductGroupResponse): ProductGroupRealmEntity {
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

    override fun mapLocalToDomain(input: ProductGroupRealmEntity): ProductGroup {
        return ProductGroup(
            input.productGroupId,
            input.productGroupName,
            mapToEmptyBundleDomain(input.defaultProduct ?: ProductRealmEntity()),
            mapToEmptyBundleDomain(input.options),
            input.min,
            input.max
        )
    }


    private fun mapToEmptyBundleDomain(input: ProductRealmEntity): Product {
        return Product(
            input.productId,
            input.productName,
            input.productDescription,
            input.price,
            input.receiptText,
            listOf(),
            modifierGroupMapper.mapLocalToDomain(input.modifierGroups)
        )
    }

    private fun mapToEmptyBundleDomain(input: List<ProductRealmEntity>): List<Product> {
        return input.map { mapToEmptyBundleDomain(it) }
    }

    private fun mapDomainToEmptyBundleEntity(input: Product): ProductRealmEntity {
        return ProductRealmEntity(
            input.productId,
            input.productName,
            input.productDescription,
            input.price,
            input.receiptText,
            modifierGroupMapper.mapDomainToLocal(input.modifierGroups),
            RealmList()
        )
    }

    private fun mapDomainToEmptyBundleEntity(input: List<Product>): RealmList<ProductRealmEntity> {
        return RealmList<ProductRealmEntity>().apply {
            addAll(input.map { mapDomainToEmptyBundleEntity(it) })
        }
    }


    override fun mapDomainToLocal(input: ProductGroup): ProductGroupRealmEntity {

        return ProductGroupRealmEntity(
            input.productGroupId,
            input.productGroupName,
            mapDomainToEmptyBundleEntity(input.options),
            null,
            input.min,
            input.max
        )
    }
}

class ModifierInfoMapper : DataMapper<ModifierInfoRealmEntity, ModifierInfoResponse, ModifierInfo> {

    override fun mapRemoteToLocal(input: ModifierInfoResponse): ModifierInfoRealmEntity {
        if (input.modifierId == null || input.modifierName == null) return ModifierInfoRealmEntity()
        return ModifierInfoRealmEntity(input.modifierId, input.modifierName, input.priceDelta ?: 0f, input.receiptText ?: "")
    }

    override fun mapLocalToDomain(input: ModifierInfoRealmEntity): ModifierInfo {
        return ModifierInfo(input.modifierId, input.modifierName, input.priceDelta, input.receiptText)
    }

    override fun mapDomainToLocal(input: ModifierInfo): ModifierInfoRealmEntity {
        return ModifierInfoRealmEntity(input.modifierId, input.modifierName, input.priceDelta, input.receiptText)
    }
}

class ModifierGroupMapper : DataMapper<ModifierGroupRealmEntity, ModifierGroupResponse, ModifierGroup> {

    private val modifierInfoMapper = ModifierInfoMapper()

    override fun mapRemoteToLocal(input: ModifierGroupResponse): ModifierGroupRealmEntity {
        if (input.modifierGroupId == null || input.modifierGroupName == null) return ModifierGroupRealmEntity()

        val modifierInfoRealmList = RealmList<ModifierInfoRealmEntity>()
        input.options?.let {
            modifierInfoRealmList.addAll(modifierInfoMapper.mapRemoteToLocal(it))
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

    override fun mapLocalToDomain(input: ModifierGroupRealmEntity): ModifierGroup {
        return ModifierGroup(
            input.modifierGroupId,
            input.modifierGroupName,
            input.action.toModifierGroupAction(),
            modifierInfoMapper.mapLocalToDomain(input.getDefaultSelection() ?: ModifierInfoRealmEntity()),
            modifierInfoMapper.mapLocalToDomain(input.options),
            input.min,
            input.max
        )
    }

    override fun mapDomainToLocal(input: ModifierGroup): ModifierGroupRealmEntity {

        val modifierInfoRealmList = RealmList<ModifierInfoRealmEntity>()
        input.options.let { modifierInfoRealmList.addAll(modifierInfoMapper.mapDomainToLocal(it)) }

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


