package com.ryanjames.swabergersmobilepos.database.realm

import io.realm.Realm

class VenueRealmDao {

    fun addOrUpdateVenues(realm: Realm, venueRealmEntities: List<VenueRealmEntity>) {
        val savedVenues = realm.where(VenueRealmEntity::class.java).findAll()

        venueRealmEntities.forEach { venue ->
            savedVenues.find { it.venueId == venue.venueId }?.deleteFromRealm()
            realm.copyToRealm(venue)
        }
    }

    fun getStoreById(realm: Realm, venueId: String): VenueRealmEntity? {
        return realm.where(VenueRealmEntity::class.java).equalTo("venueId", venueId).findFirst()
    }

}