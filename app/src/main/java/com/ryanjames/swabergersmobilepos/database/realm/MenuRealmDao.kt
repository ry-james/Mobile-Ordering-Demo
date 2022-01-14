package com.ryanjames.swabergersmobilepos.database.realm

import io.reactivex.Maybe
import io.reactivex.Single
import io.realm.Realm

class MenuRealmDao {

    fun getBasicMenuById(storeId: String): Single<BasicMenuRealmEntity> {
        return Single.create { emitter ->
            val realm = Realm.getDefaultInstance()
            val result = realm.where(BasicMenuRealmEntity::class.java).equalTo("storeId", storeId).findFirst()
            if (result == null) {
                emitter.onSuccess(BasicMenuRealmEntity())
            } else {
                emitter.onSuccess(result)
            }
            realm.close()
        }
    }

    fun deleteMenu() {
        executeRealmTransaction { realm ->
            realm.delete(BasicMenuRealmEntity::class.java)
            realm.delete(BasicProductRealmEntity::class.java)
            realm.delete(BasicCategoryRealmEntity::class.java)
        }
    }

    fun getProductDetailsById(id: String): Maybe<ProductRealmEntity> {
        return Maybe.create { emitter ->
            val realm = Realm.getDefaultInstance()
            val result = realm.where(ProductRealmEntity::class.java).equalTo("productId", id).findFirst()
            if (result == null) {
                emitter.onComplete()
            } else {
                emitter.onSuccess(result)
            }
            realm.close()
        }
    }

    fun saveProductDetail(productRealmEntity: ProductRealmEntity) {
        executeRealmTransaction { realm ->
            realm.copyToRealm(productRealmEntity)
        }
    }

    fun saveBasicMenu(basicMenuRealmEntity: BasicMenuRealmEntity) {
        executeRealmTransaction { realm ->
            val storeRealm = realm.where(BasicMenuRealmEntity::class.java).equalTo("storeId", basicMenuRealmEntity.storeId).findFirst()
            storeRealm?.deleteFromRealm()
            realm.insertOrUpdate(basicMenuRealmEntity)
        }
    }
}