package com.ryanjames.swabergersmobilepos.repository

import com.ryanjames.swabergersmobilepos.database.realm.GlobalRealmDao
import com.ryanjames.swabergersmobilepos.database.realm.LineItemRealmEntity
import com.ryanjames.swabergersmobilepos.database.realm.OrderRealmDao
import com.ryanjames.swabergersmobilepos.database.realm.executeRealmTransaction
import com.ryanjames.swabergersmobilepos.domain.BagSummary
import com.ryanjames.swabergersmobilepos.domain.LineItem
import com.ryanjames.swabergersmobilepos.domain.Order
import com.ryanjames.swabergersmobilepos.domain.OrderStatus
import com.ryanjames.swabergersmobilepos.helper.replace
import com.ryanjames.swabergersmobilepos.mappers.toBagSummary
import com.ryanjames.swabergersmobilepos.mappers.toDomain
import com.ryanjames.swabergersmobilepos.mappers.toLineItemRequest
import com.ryanjames.swabergersmobilepos.mappers.toLocal
import com.ryanjames.swabergersmobilepos.network.responses.CreateUpdateOrderRequest
import com.ryanjames.swabergersmobilepos.network.retrofit.SwabergersService
import io.reactivex.Single
import javax.inject.Inject

class OrderRepository @Inject constructor(
    val swabergersService: SwabergersService,
    val orderRealmDao: OrderRealmDao,
    val globalRealmDao: GlobalRealmDao
) {

    private fun getLocalLineItems(): Single<List<LineItemRealmEntity>> {
        return orderRealmDao.getLocalBag().map { it.lineItems }
    }

    fun getOrderHistory(): Single<List<Order>> {
        return swabergersService.getOrderHistory().map { orderHistory -> orderHistory.toDomain() }
    }

    fun addOrUpdateLineItem(lineItem: LineItem): Single<BagSummary> {
        var orderId = globalRealmDao.getLocalBagOrderId()
        var newOrder = false

        if (orderId == GlobalRealmDao.NO_LOCAL_ORDER) {
            newOrder = true
            executeRealmTransaction { realm ->
                orderId = globalRealmDao.createLocalBagOrderId(realm)
            }
        }
        return getLocalLineItems()
            .flatMap { lineItemsEntities ->
                var lineItemListRequest = lineItemsEntities.map { it.toLineItemRequest() }
                val newLineItem = lineItem.toLineItemRequest()
                lineItemListRequest = if (lineItemListRequest.find { it.lineItemId == newLineItem.lineItemId } != null) {
                    lineItemListRequest.replace(newValue = newLineItem) { it.lineItemId == lineItem.lineItemId }
                } else {
                    lineItemListRequest.plus(newLineItem)
                }
                val request = CreateUpdateOrderRequest(orderId, lineItemListRequest, null)

                if (newOrder) {
                    swabergersService.postOrder(request)
                        .doOnSuccess { orderResponse ->
                            executeRealmTransaction { realm ->
                                orderResponse.lineItems.find { newLineItem.lineItemId == it.lineItemId }?.let {
                                    orderRealmDao.updateLocalBag(realm, orderResponse.lineItems.map { it.toLocal() })
                                }
                            }
                        }
                } else {
                    swabergersService.putOrder(request)
                        .doOnSuccess { orderResponse ->
                            executeRealmTransaction { realm ->
                                orderRealmDao.updateLocalBag(realm, orderResponse.lineItems.map { it.toLocal() })
                            }
                        }
                }
            }.map { it.toBagSummary() }
    }

    fun removeLineItem(lineItem: LineItem): Single<BagSummary> {
        return getLocalLineItems()
            .flatMap { lineItemsEntities ->
                val orderId = globalRealmDao.getLocalBagOrderId()
                var lineItemListRequest = lineItemsEntities.map { it.toLineItemRequest() }
                lineItemListRequest.find { it.lineItemId == lineItem.lineItemId }?.let {
                    lineItemListRequest = lineItemListRequest.minus(it)
                }

                val request = CreateUpdateOrderRequest(orderId, lineItemListRequest, null)
                swabergersService.putOrder(request)
                    .doOnSuccess { orderResponse ->
                        executeRealmTransaction { realm ->
                            orderRealmDao.updateLocalBag(realm, orderResponse.lineItems.map { it.toLocal() })
                        }
                    }
            }.map { it.toBagSummary() }
    }

    fun checkout(customerName: String): Single<BagSummary> {
        return getLocalLineItems().flatMap { lineItemsEntities ->
            val orderId = globalRealmDao.getLocalBagOrderId()
            val lineItemListRequest = lineItemsEntities.map { it.toLineItemRequest() }
            val request = CreateUpdateOrderRequest(orderId, lineItemListRequest, OrderStatus.CHECKOUT.toString())
            swabergersService.putOrder(request)
                .doOnSuccess {
                    executeRealmTransaction { realm ->
                        orderRealmDao.deleteAllLineItems(realm)
                        globalRealmDao.clearLocalBagOrderId(realm)
                    }
                }.map { it.toBagSummary() }
                .doOnError { error ->
                    error.printStackTrace()
                }
        }
    }

    fun getCurrentOrder(): Single<BagSummary> {
        val orderId = globalRealmDao.getLocalBagOrderId()
        return swabergersService.getOrderById(orderId)
            .doOnSuccess { orderResponse ->
                executeRealmTransaction { realm ->
                    orderRealmDao.updateLocalBag(realm, orderResponse.lineItems.map { it.toLocal() })
                }
            }.map {
                it.toBagSummary()
            }
    }

    fun getOrderById(orderId: String): Single<BagSummary> {
        return swabergersService.getOrderById(orderId).map {
            it.toBagSummary()
        }
    }

    fun clearLocalBag() {
        executeRealmTransaction { realm ->
            orderRealmDao.deleteAllLineItems(realm)
            globalRealmDao.clearLocalBagOrderId(realm)
        }
    }
}