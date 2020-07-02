package com.ryanjames.swabergersmobilepos.database.realm

import io.realm.Realm
import java.util.*

class GlobalRealmDao {

    fun getLocalBagOrderId(): String {
        var id = NO_LOCAL_ORDER
        executeRealmTransaction { realm ->
            id = getGlobalRealmEntity(realm).localBagOrderId ?: NO_LOCAL_ORDER
        }
        return id
    }

    fun createLocalBagOrderId(realm: Realm): String {
        val id: String = UUID.randomUUID().toString()
        val globalRealm = getGlobalRealmEntity(realm)
        globalRealm.localBagOrderId = id
        realm.insertOrUpdate(globalRealm)
        return id
    }

    fun clearLocalBagOrderId(realm: Realm) {
        getGlobalRealmEntity(realm).localBagOrderId = null
    }

    private fun getGlobalRealmEntity(realm: Realm): GlobalRealmEntity {
        var result = realm.where(GlobalRealmEntity::class.java).findFirst()
        if (result == null) {
            result = GlobalRealmEntity()
        }
        return result
    }

    companion object {

        const val NO_LOCAL_ORDER = "no.local.order"

    }
}