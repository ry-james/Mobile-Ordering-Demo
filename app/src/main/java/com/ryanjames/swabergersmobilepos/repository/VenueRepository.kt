package com.ryanjames.swabergersmobilepos.repository

import com.ryanjames.swabergersmobilepos.database.realm.GlobalRealmDao
import com.ryanjames.swabergersmobilepos.database.realm.VenueRealmDao
import com.ryanjames.swabergersmobilepos.database.realm.executeRealmTransaction
import com.ryanjames.swabergersmobilepos.domain.Venue
import com.ryanjames.swabergersmobilepos.mappers.toDomain
import com.ryanjames.swabergersmobilepos.mappers.toLocal
import com.ryanjames.swabergersmobilepos.network.retrofit.ApiService
import io.reactivex.Single
import javax.inject.Inject

class VenueRepository @Inject constructor(
    val apiService: ApiService,
    val globalRealmDao: GlobalRealmDao,
    val venueRealmDao: VenueRealmDao
) {

    fun getVenues(): Single<List<Venue>> {
        return apiService.getStores()
            .doOnSuccess { venueListResponse ->
                executeRealmTransaction { realm ->
                    venueRealmDao.addOrUpdateVenues(realm, venueListResponse.stores.mapNotNull { it.toLocal() })
                }
            }
            .map { storeListResponse ->
                storeListResponse.stores.mapNotNull { it.toDomain() }
            }
    }

    fun getSelectedVenue(): Venue? {
        var venue: Venue? = null
        executeRealmTransaction { realm ->
            globalRealmDao.getCurrentSelectedVenueId(realm)?.let { venueRealmEntity ->
                venue = venueRealmEntity.toDomain()
            }
        }
        return venue
    }

    fun setSelectedVenue(venue: Venue) {
        globalRealmDao.setSelectedVenue(venue.toLocal())
    }

    fun clearSelectedVenue() {
        globalRealmDao.clearSelectedVenue()
    }


}