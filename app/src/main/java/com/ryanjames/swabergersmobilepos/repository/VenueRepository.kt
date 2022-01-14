package com.ryanjames.swabergersmobilepos.repository

import com.ryanjames.swabergersmobilepos.database.realm.GlobalRealmDao
import com.ryanjames.swabergersmobilepos.database.realm.VenueRealmDao
import com.ryanjames.swabergersmobilepos.database.realm.executeRealmTransaction
import com.ryanjames.swabergersmobilepos.domain.HomeVenues
import com.ryanjames.swabergersmobilepos.domain.Venue
import com.ryanjames.swabergersmobilepos.mappers.toDomain
import com.ryanjames.swabergersmobilepos.mappers.toLocal
import com.ryanjames.swabergersmobilepos.network.retrofit.ApiService
import io.reactivex.Maybe
import io.reactivex.Observable
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


    fun getHomeVenues(): Single<HomeVenues> {
        return apiService.getFeaturedStores()
            .doOnSuccess { homeResponse ->
                executeRealmTransaction { realm ->
                    venueRealmDao.addOrUpdateVenues(realm, homeResponse.featuredStores.mapNotNull { it.toLocal() })
                    venueRealmDao.addOrUpdateVenues(realm, homeResponse.restaurants.mapNotNull { it.toLocal() })
                }
            }
            .map { homeResponse ->
                val featuredVenues = homeResponse.featuredStores.mapNotNull { it.toDomain() }
                val restaurants = homeResponse.restaurants.mapNotNull { it.toDomain() }
                HomeVenues(featuredVenues, restaurants)
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

    fun getVenueById(storeId: String): Observable<Venue> {
        return Observable.concat(venueByIdDatabaseObservable(storeId).toObservable(), venueByIdApiObservable(storeId).toObservable())
            .firstElement().toObservable()
    }

    private fun venueByIdDatabaseObservable(storeId: String): Maybe<Venue> {
        return venueRealmDao.getStoreById(storeId).map { it.toDomain() }
    }

    private fun venueByIdApiObservable(storeId: String): Single<Venue> {
        return apiService.getStoreById(storeId)
            .doOnSuccess { venueResponse ->
                executeRealmTransaction { realm ->
                    venueRealmDao.addOrUpdateVenues(realm, listOfNotNull(venueResponse.toLocal()))
                }
            }
            .map { venueResponse ->
                venueResponse.toDomain()
            }
    }

}