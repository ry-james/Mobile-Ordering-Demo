package com.ryanjames.swabergersmobilepos.database.realm

import io.reactivex.Single
import io.realm.Realm

class MenuRealmDao {

    fun getMenu(): Single<MenuRealmEntity> {
        return Single.create { emitter ->
            val realm = Realm.getDefaultInstance()
            val result = realm.where(MenuRealmEntity::class.java).findFirst()
            if (result == null) {
                emitter.onSuccess(MenuRealmEntity())
            } else {
                emitter.onSuccess(result)
            }
            realm.close()
        }
    }

    fun saveMenu(menuRealmEntity: MenuRealmEntity) {
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        realm.insertOrUpdate(menuRealmEntity)
        realm.commitTransaction()
        realm.close()
    }

}