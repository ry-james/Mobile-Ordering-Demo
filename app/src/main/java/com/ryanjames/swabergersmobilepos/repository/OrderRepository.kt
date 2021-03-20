package com.ryanjames.swabergersmobilepos.repository

import com.ryanjames.swabergersmobilepos.database.realm.GlobalRealmDao
import com.ryanjames.swabergersmobilepos.database.realm.LineItemRealmEntity
import com.ryanjames.swabergersmobilepos.database.realm.OrderRealmDao
import com.ryanjames.swabergersmobilepos.database.realm.executeRealmTransaction
import com.ryanjames.swabergersmobilepos.domain.*
import com.ryanjames.swabergersmobilepos.feature.checkout.ServiceOption
import com.ryanjames.swabergersmobilepos.helper.replace
import com.ryanjames.swabergersmobilepos.mappers.toBagSummary
import com.ryanjames.swabergersmobilepos.mappers.toDomain
import com.ryanjames.swabergersmobilepos.mappers.toLineItemRequest
import com.ryanjames.swabergersmobilepos.mappers.toLocal
import com.ryanjames.swabergersmobilepos.network.responses.CreateUpdateOrderRequest
import com.ryanjames.swabergersmobilepos.network.retrofit.ApiService
import io.reactivex.Single
import javax.inject.Inject

class OrderRepository @Inject constructor(
    val apiService: ApiService,
    val orderRealmDao: OrderRealmDao,
    val globalRealmDao: GlobalRealmDao
) {

    private fun getLocalLineItems(): Single<List<LineItemRealmEntity>> {
        return orderRealmDao.getLocalBag().map { it.lineItems }
    }

    fun getOrderHistory(): Single<List<Order>> {
        return apiService.getOrderHistory().map { orderHistory -> orderHistory.toDomain() }
    }

    fun hasItemsInBag(): Boolean {
        var hasItems = false
        executeRealmTransaction { realm ->
            hasItems = orderRealmDao.hasItemsInBag(realm)
        }
        return hasItems
    }

    fun addOrUpdateLineItem(lineItem: LineItem, venueId: String): Single<BagSummary> {
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
                val request = CreateUpdateOrderRequest(orderId, lineItemListRequest, status = null, customerName = null, storeId = venueId)

                if (newOrder) {
                    apiService.postOrder(request)
                        .doOnSuccess { orderResponse ->
                            executeRealmTransaction { realm ->
                                orderResponse.lineItems.find { newLineItem.lineItemId == it.lineItemId }?.let {
                                    orderRealmDao.updateLocalBag(realm, orderResponse.lineItems.map { it.toLocal() })
                                }
                            }
                        }
                } else {
                    apiService.putOrder(request)
                        .doOnSuccess { orderResponse ->
                            executeRealmTransaction { realm ->
                                orderRealmDao.updateLocalBag(realm, orderResponse.lineItems.map { it.toLocal() })
                            }
                        }
                }
            }.map { it.toBagSummary() }
    }

    fun removeBagLineItems(lineItems: List<BagLineItem>, venueId: String): Single<BagSummary> {
        return removeLineItems(lineItems.map { it.lineItemId }, venueId)
    }

    fun removeLineItem(lineItem: LineItem, venueId: String): Single<BagSummary> {
        return removeLineItems(listOf(lineItem.lineItemId), venueId)
    }

    private fun removeLineItems(lineItemIds: List<String>, venueId: String): Single<BagSummary> {
        return getLocalLineItems()
            .flatMap { lineItemsEntities ->
                val orderId = globalRealmDao.getLocalBagOrderId()
                var lineItemListRequest = lineItemsEntities.map { it.toLineItemRequest() }

                for (lineItemId in lineItemIds) {
                    lineItemListRequest.find { it.lineItemId == lineItemId }?.let {
                        lineItemListRequest = lineItemListRequest.minus(it)
                    }
                }

                val request = CreateUpdateOrderRequest(orderId, lineItemListRequest, null, null, storeId = venueId)
                apiService.putOrder(request)
                    .doOnSuccess { orderResponse ->
                        executeRealmTransaction { realm ->
                            orderRealmDao.updateLocalBag(realm, orderResponse.lineItems.map { it.toLocal() })
                        }
                    }
            }.map { it.toBagSummary() }
    }

    fun checkout(customerName: String, serviceOption: ServiceOption, venueId: String): Single<BagSummary> {
        return getLocalLineItems().flatMap { lineItemsEntities ->
            val orderId = globalRealmDao.getLocalBagOrderId()
            val lineItemListRequest = lineItemsEntities.map { it.toLineItemRequest() }
            var request = CreateUpdateOrderRequest(orderId, lineItemListRequest, OrderStatus.CHECKOUT.toString(), customerName, storeId = venueId)

            // Set pick up or delivery
            if (serviceOption is ServiceOption.Delivery) {
                request = request.copy(deliveryAddress = serviceOption.deliveryAddress, pickup = null)
            } else if (serviceOption is ServiceOption.Pickup) {
                request = request.copy(pickup = true, deliveryAddress = null)
            }

            apiService.putOrder(request)
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
        if (orderId == GlobalRealmDao.NO_LOCAL_ORDER) {
            return Single.just(BagSummary.emptyBag)
        }
        return apiService.getOrderById(orderId)
            .doOnSuccess { orderResponse ->
                executeRealmTransaction { realm ->
                    orderRealmDao.updateLocalBag(realm, orderResponse.lineItems.map { it.toLocal() })
                }
            }.map {
                it.toBagSummary()
            }
    }

    fun getOrderById(orderId: String): Single<BagSummary> {
        return apiService.getOrderById(orderId).map {
            it.toBagSummary()
        }
    }

    fun cancelOrder(orderId: String): Single<Any> {
        return apiService.cancelOrder(orderId)
    }

    fun clearLocalBag() {
        executeRealmTransaction { realm ->
            orderRealmDao.deleteAllLineItems(realm)
            globalRealmDao.clearLocalBagOrderId(realm)
        }
    }
}