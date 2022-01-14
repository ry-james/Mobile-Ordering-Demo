package com.ryanjames.swabergersmobilepos.database.realm

import io.reactivex.Maybe
import io.realm.Realm

class VenueRealmDao {

    fun addOrUpdateVenues(realm: Realm, venueRealmEntities: List<VenueRealmEntity>) {
        val savedVenues = realm.where(VenueRealmEntity::class.java).findAll()

        venueRealmEntities.forEach { venue ->
            savedVenues.find { it.venueId == venue.venueId }?.deleteFromRealm()
            realm.copyToRealm(venue)
        }
    }

    fun getStoreById(venueId: String): Maybe<VenueRealmEntity> {
        return Maybe.create { emitter ->
            val realm = Realm.getDefaultInstance()
            val result = realm.where(VenueRealmEntity::class.java).equalTo("venueId", venueId).findFirst()
            if (result != null) {
                emitter.onSuccess(result)
            }
            realm.close()
        }
    }
}