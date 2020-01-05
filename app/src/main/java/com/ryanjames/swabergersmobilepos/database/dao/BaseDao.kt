package com.ryanjames.swabergersmobilepos.database.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import io.reactivex.*

interface BaseDao<T> {

    @Insert
    fun insert(vararg obj: T): Completable

    @Update
    fun update(vararg obj: T): Single<Int>

    @Delete
    fun delete(vararg obj: T): Single<Int>
}