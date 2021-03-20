package com.ryanjames.swabergersmobilepos.database.realm

import io.reactivex.Single
import io.realm.Realm

class OrderRealmDao {

    init {
        executeRealmTransaction { realm ->
            val result2 = realm.where(LocalBagRealmEntity::class.java).findFirst()
            if (result2 == null) {
                val newLocalBag = LocalBagRealmEntity()
                realm.insertOrUpdate(newLocalBag)
            }
        }
    }

    fun getLocalBag(): Single<LocalBagRealmEntity> {
        return Single.create { emitter ->
            val realm = Realm.getDefaultInstance()
            realm.beginTransaction()
            val result = realm.where(LocalBagRealmEntity::class.java).findFirst() ?: LocalBagRealmEntity()
            emitter.onSuccess(result)
            realm.close()
        }
    }

    fun hasItemsInBag(realm: Realm): Boolean {
        return realm.where(LocalBagRealmEntity::class.java).findFirst()?.lineItems?.count() ?: 0 > 0
    }

    fun updateLocalBag(realm: Realm, lineItemEntities: List<LineItemRealmEntity>) {
        realm.where(LocalBagRealmEntity::class.java).findFirst()?.apply {
            lineItems.map { it.deleteChildrenFromRealm() }
            lineItems.deleteAllFromRealm()
            lineItems.addAll(lineItemEntities)
        }
    }

    fun deleteAllLineItems(realm: Realm) {
        realm.where(LocalBagRealmEntity::class.java).findFirst()?.apply {
            lineItems.map { it.deleteChildrenFromRealm() }
            lineItems.deleteAllFromRealm()
        }
    }
}