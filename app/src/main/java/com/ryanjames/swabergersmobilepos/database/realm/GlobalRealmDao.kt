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
        return id
    }

    fun clearLocalBagOrderId(realm: Realm) {
        getGlobalRealmEntity(realm).localBagOrderId = null
    }

    fun getCurrentSelectedVenueId(realm: Realm): VenueRealmEntity? {
        getGlobalRealmEntity(realm).currentVenue?.let { venueId ->
            return realm.where(VenueRealmEntity::class.java).equalTo("venueId", venueId).findFirst()
        }
        return null
    }

    fun setSelectedVenue(venueRealmEntity: VenueRealmEntity) {
        executeRealmTransaction { realm ->
            realm.where(VenueRealmEntity::class.java).equalTo("venueId", venueRealmEntity.venueId).findFirst()?.also {
                getGlobalRealmEntity(realm).currentVenue = it.venueId
            }
        }
    }

    fun clearSelectedVenue() {
        executeRealmTransaction { realm ->
            getGlobalRealmEntity(realm).currentVenue = null
        }
    }

    private fun getGlobalRealmEntity(realm: Realm): GlobalRealmEntity {
        var result = realm.where(GlobalRealmEntity::class.java).findFirst()
        if (result == null) {
            result = GlobalRealmEntity()
            realm.insertOrUpdate(result)
        }
        return result
    }

    companion object {

        const val NO_LOCAL_ORDER = "no.local.order"

    }
}