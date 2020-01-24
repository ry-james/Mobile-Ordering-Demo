package com.ryanjames.swabergersmobilepos.repository

import com.ryanjames.swabergersmobilepos.database.realm.OrderRealmDao
import com.ryanjames.swabergersmobilepos.domain.LineItem
import com.ryanjames.swabergersmobilepos.mappers.toDomain
import com.ryanjames.swabergersmobilepos.mappers.toEntity
import io.reactivex.Single
import io.realm.Realm

object OrderRepository {

    private val orderRealmDao = OrderRealmDao()

    fun getLocalBag(): Single<List<LineItem>> {
        return orderRealmDao.getLineItems().map { it.lineItems.map { item -> item.toDomain() } }
    }

    fun executeRealmTransaction(action: (realm: Realm) -> Unit) {
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        action.invoke(realm)
        realm.commitTransaction()
        realm.close()
    }

    fun insertLineItem(lineItem: LineItem) {
        executeRealmTransaction { realm -> orderRealmDao.insertLineItem(realm, lineItem.toEntity(realm)) }
    }

    fun updateLineItem(lineItem: LineItem) {
        executeRealmTransaction { realm -> orderRealmDao.updateLineItem(realm, lineItem.toEntity(realm)) }
    }
}