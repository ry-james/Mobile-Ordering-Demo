package com.ryanjames.swabergersmobilepos.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.ryanjames.swabergersmobilepos.database.entity.ModifierInfoEntity
import com.ryanjames.swabergersmobilepos.network.Resource
import io.reactivex.Flowable
import io.reactivex.Observable

@Dao
interface ModifierInfoDao : BaseDao<ModifierInfoEntity> {

    @Query("DELETE FROM modifier_info")
    fun deleteAllModifierInfos()

    @Query("SELECT * FROM modifier_info")
    fun getAllModifierInfos(): Observable<List<ModifierInfoEntity>>
}