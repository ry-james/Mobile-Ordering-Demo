package com.ryanjames.swabergersmobilepos.database.realm

import io.reactivex.Single
import io.realm.Realm

class MenuRealmDao {

    fun getBasicMenu(): Single<BasicMenuRealmEntity> {
        return Single.create { emitter ->
            val realm = Realm.getDefaultInstance()
            val result = realm.where(BasicMenuRealmEntity::class.java).findFirst()
            if (result == null) {
                emitter.onSuccess(BasicMenuRealmEntity())
            } else {
                emitter.onSuccess(result)
            }
            realm.close()
        }
    }

    fun saveBasicMenu(basicMenuRealmEntity: BasicMenuRealmEntity) {
        executeRealmTransaction { realm ->
            realm.delete(BasicMenuRealmEntity::class.java)
            realm.insertOrUpdate(basicMenuRealmEntity)
        }
    }
}