package com.ryanjames.swabergersmobilepos.database.realm

import io.reactivex.Single
import io.realm.Realm

class OrderRealmDao {

    fun getLineItems(): Single<LocalBagRealmEntity> {
        return Single.create { emitter ->
            val realm = Realm.getDefaultInstance()
            val result = realm.where(LocalBagRealmEntity::class.java).findFirst()
            if (result == null) {
                val newLocalBag = LocalBagRealmEntity()
                realm.beginTransaction()
                realm.insertOrUpdate(newLocalBag)
                realm.commitTransaction()
                emitter.onSuccess(newLocalBag)
            } else {
                emitter.onSuccess(result)
            }
            realm.close()
        }
    }

    fun insertLineItem(realm: Realm, lineItemRealmEntity: LineItemRealmEntity) {

        realm.insert(lineItemRealmEntity)
        realm.where(LocalBagRealmEntity::class.java).findFirst()?.apply {
            lineItems.add(lineItemRealmEntity)
        }

    }

    fun updateLineItem(realm: Realm, lineItemRealmEntity: LineItemRealmEntity) {
        realm.where(LineItemRealmEntity::class.java).equalTo("id", lineItemRealmEntity.id).findFirst()?.let {
            realm.insertOrUpdate(lineItemRealmEntity)
        }
    }

    fun deleteAllLineItems(realm: Realm) {
        realm.delete(LineItemRealmEntity::class.java)
    }


}