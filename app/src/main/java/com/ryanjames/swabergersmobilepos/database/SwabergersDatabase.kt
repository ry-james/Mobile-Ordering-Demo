package com.ryanjames.swabergersmobilepos.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ryanjames.swabergersmobilepos.database.dao.MenuDao
import com.ryanjames.swabergersmobilepos.database.dao.ModifierInfoDao
import com.ryanjames.swabergersmobilepos.database.entity.*

private const val DATABASE_NAME = "swabergers"

@Database(
    entities = [
        CategoryEntity::class,
        ProductEntity::class,
        ModifierGroupEntity::class,
        ModifierInfoEntity::class,
        MenuEntity::class,
        MenuToModifierGroupCrossRef::class], version = 1
)
abstract class SwabergersDatabase : RoomDatabase() {

    companion object {

        private var swabeDb: SwabergersDatabase? = null

        @Synchronized
        fun getInstance(context: Context): SwabergersDatabase {
            if (swabeDb == null) {
                swabeDb = Room.databaseBuilder(
                    context.applicationContext,
                    SwabergersDatabase::class.java,
                    DATABASE_NAME
                ).build()
            }
            return swabeDb as SwabergersDatabase
        }

    }

    abstract fun modifierInfoDao(): ModifierInfoDao

    abstract fun menuDao(): MenuDao

}