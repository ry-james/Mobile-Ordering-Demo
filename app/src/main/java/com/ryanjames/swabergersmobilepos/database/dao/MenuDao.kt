package com.ryanjames.swabergersmobilepos.database.dao

import androidx.room.*
import com.ryanjames.swabergersmobilepos.database.entity.*
import io.reactivex.Observable
import io.reactivex.Single

@Dao
abstract class MenuDao : BaseDao<MenuEntity> {

    fun insertMenuAndCategories(menu: MenuEntity, categories: List<CategoryEntity>) {
        insert(menu)
        _insertAllCategories(categories)
    }



    @Query("SELECT * from menu_table WHERE menuId=:menuId")
    abstract fun _getMenu(menuId: String): Single<MenuEntity>

    @Query("SELECT * from category WHERE menuId=:menuId")
    abstract fun _getCategoryList(menuId: String): Single<List<CategoryEntity>>

    @Query("SELECT * from product WHERE categoryId=:categoryId")
    abstract fun getProductList(categoryId: String): Single<List<ProductEntity>>

    @Insert
    abstract fun _insertAllCategories(categories: List<CategoryEntity>): Single<List<Long>>

    @Transaction
    @Query("SELECT * from modifier_group")
    abstract fun getModifierGroupAndInfos(): Single<List<ModifierGroupAndInfos>>

    @Transaction
    @Insert
    abstract fun insertModifierGroupAndInfos(modifierGroupEntity: List<ModifierGroupEntity>): Single<List<Long>>
}