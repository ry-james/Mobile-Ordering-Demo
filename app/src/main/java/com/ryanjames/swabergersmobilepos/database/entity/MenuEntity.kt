package com.ryanjames.swabergersmobilepos.database.entity

import androidx.room.*
import androidx.room.ForeignKey.CASCADE

@Entity(
    tableName = "menu_table"
)
data class MenuEntity(
    @PrimaryKey
    var menuId: String
) {
    @Ignore
    var categories: List<CategoryEntity> = listOf()
}

@Entity(tableName = "category")
data class CategoryEntity(
    @PrimaryKey
    val categoryId: String,
    val categoryName: String,
    val menuId: String
)

@Entity(tableName = "product")
data class ProductEntity(
    @PrimaryKey
    val productId: String,
    val productName: String,
    val price: Float,
    val receiptText: String,
    val categoryId: String
)

@Entity(
    tableName = "modifier_group"
)
data class ModifierGroupEntity(
    @PrimaryKey
    val modifierGroupId: String,
    val modifierGroupName: String,
    val action: String
)

@Entity(
    tableName = "modifier_info"
)
data class ModifierInfoEntity(
    @PrimaryKey
    val modifierId: String,
    val name: String,
    val priceDelta: Float,
    val receiptText: String,
    val modifierGroupId: String
)

data class ModifierGroupAndInfos(
    @Embedded val modifierGroupEntity: ModifierGroupEntity,
    @Relation(parentColumn = "modifierGroupId", entityColumn = "modifierGroupId", entity = ModifierInfoEntity::class)
    val modifierInfosEntity: List<ModifierInfoEntity>
)


@Entity(primaryKeys = ["menuId", "modifierGroupId"])
data class MenuToModifierGroupCrossRef(
    val menuId: String,
    val modifierGroupId: String
)



