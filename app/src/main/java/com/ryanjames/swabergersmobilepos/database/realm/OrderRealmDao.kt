package com.ryanjames.swabergersmobilepos.database.realm

import io.reactivex.Single
import io.realm.Realm

class OrderRealmDao {

    init {
        executeRealmTransaction { realm ->
            val result = realm.where(LocalBagRealmEntity::class.java).findFirst()
            if (result == null) {
                val newLocalBag = LocalBagRealmEntity()
                realm.insertOrUpdate(newLocalBag)
            }
        }
    }

    fun getLineItems(): Single<LocalBagRealmEntity> {
        return Single.create { emitter ->
            val realm = Realm.getDefaultInstance()
            realm.beginTransaction()
            val result = realm.where(LocalBagRealmEntity::class.java).findFirst() ?: LocalBagRealmEntity()
            emitter.onSuccess(result)
            realm.close()
        }
    }


    fun lineItemsCount(realm: Realm): Int {
        val result = realm.where(LocalBagRealmEntity::class.java).findFirst()
        return result?.lineItems?.size ?: 0
    }

    fun insertLineItem(realm: Realm, lineItemRealmEntity: LineItemRealmEntity) {
        realm.insert(lineItemRealmEntity)
        realm.where(LocalBagRealmEntity::class.java).findFirst()?.apply {
            lineItems.add(lineItemRealmEntity)
        }
    }

    fun updateLineItem(realm: Realm, lineItemRealmEntity: LineItemRealmEntity) {
        realm.where(LineItemRealmEntity::class.java).equalTo("id", lineItemRealmEntity.id).findFirst()?.let {
            it.modifiers.deleteAllFromRealm()
            it.productsInBundle.deleteAllFromRealm()
            realm.insertOrUpdate(lineItemRealmEntity)
        }
    }

    fun removeLineItem(realm: Realm, lineItemRealmEntity: LineItemRealmEntity) {
        realm.where(LineItemRealmEntity::class.java).equalTo("id", lineItemRealmEntity.id).findFirst()?.deleteFromRealm()

        if (realm.where(LocalBagRealmEntity::class.java).findFirst()?.lineItems.isNullOrEmpty()) {
            deleteAllLineItems(realm)
        }
    }

    fun deleteAllLineItems(realm: Realm) {
        realm.delete(ProductsInBundleRealmEntity::class.java)
        realm.delete(ModifiersInProductRealmEntity::class.java)
        realm.delete(LineItemRealmEntity::class.java)
    }


}