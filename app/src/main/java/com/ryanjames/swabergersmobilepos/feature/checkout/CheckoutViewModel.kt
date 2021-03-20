package com.ryanjames.swabergersmobilepos.feature.checkout

import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.domain.BagSummary
import com.ryanjames.swabergersmobilepos.domain.Resource
import com.ryanjames.swabergersmobilepos.repository.OrderRepository
import com.ryanjames.swabergersmobilepos.repository.VenueRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class CheckoutViewModel @Inject constructor(
    val orderRepository: OrderRepository,
    val venueRepository: VenueRepository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private var selectedServiceOption: ServiceOption = ServiceOption.Pickup()

    private val _pickupServiceOption = MutableLiveData(ServiceOptionBindingModel(R.string.pickup, R.drawable.ic_store, R.drawable.bg_service_option, this::onClickPickup))
    val pickupServiceOption: LiveData<ServiceOptionBindingModel>
        get() = _pickupServiceOption

    private val _deliveryServiceOption = MutableLiveData(ServiceOptionBindingModel(R.string.delivery, R.drawable.ic_delivery, R.drawable.bg_service_option_unselected, this::onClickDelivery))
    val deliveryServiceOption: LiveData<ServiceOptionBindingModel>
        get() = _deliveryServiceOption

    private val _deliveryAddressVisibility = MutableLiveData<Int>(View.GONE)
    val deliveryAddressVisibility: LiveData<Int>
        get() = _deliveryAddressVisibility

    val customerName = MutableLiveData<String>()
    val deliveryAddress = MutableLiveData<String>()

    private val _checkoutObservable = MutableLiveData<Resource<BagSummary>>()
    val checkoutObservable: LiveData<Resource<BagSummary>>
        get() = _checkoutObservable

    private val _deliveryAddressError = MutableLiveData<Int?>()
    val deliveryAddressError: LiveData<Int?>
        get() = _deliveryAddressError

    private val _customerNameError = MutableLiveData<Int?>()
    val customerNameError: LiveData<Int?>
        get() = _customerNameError

    private fun selectOption(serviceOption: ServiceOption) {
        selectedServiceOption = serviceOption
        if (serviceOption is ServiceOption.Delivery) {
            _deliveryServiceOption.value = deliveryServiceOption.value?.copy(background = R.drawable.bg_service_option)
            _deliveryAddressVisibility.value = View.VISIBLE
            _pickupServiceOption.value = pickupServiceOption.value?.copy(background = R.drawable.bg_service_option_unselected)
        } else if (serviceOption is ServiceOption.Pickup) {
            _deliveryServiceOption.value = deliveryServiceOption.value?.copy(background = R.drawable.bg_service_option_unselected)
            _deliveryAddressVisibility.value = View.GONE
            _pickupServiceOption.value = pickupServiceOption.value?.copy(background = R.drawable.bg_service_option)
        }
    }

    private fun onClickPickup() {
        selectOption(ServiceOption.Pickup())
    }

    private fun onClickDelivery() {
        selectOption(ServiceOption.Delivery())
    }

    fun onDeliveryAddressTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        validateAddress(s.toString())
    }

    fun onCustomerNameTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        validateName(s.toString())
    }

    private fun validateAddress(address: String?): Boolean {
        return if (address.isNullOrBlank()) {
            _deliveryAddressError.value = R.string.delivery_address_error
            false
        } else {
            _deliveryAddressError.value = null
            true
        }
    }

    private fun validateName(name: String?): Boolean {
        return if (name.isNullOrBlank()) {
            _customerNameError.value = R.string.customer_name_error
            false
        } else {
            _customerNameError.value = null
            true
        }
    }


    fun onClickCheckout() {
        val isNameValid = validateName(customerName.value)
        val isAddressValid = validateAddress(deliveryAddress.value)

        if (!isNameValid || (selectedServiceOption is ServiceOption.Delivery && !isAddressValid)) {
            return
        }

        // Set Delivery Address
        (selectedServiceOption as? ServiceOption.Delivery)?.copy(deliveryAddress = deliveryAddress.value ?: "")?.let { selectedServiceOption = it }

        compositeDisposable.add(orderRepository.checkout(customerName.value ?: "", selectedServiceOption, venueRepository.getSelectedVenue()?.id ?: "")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                _checkoutObservable.value = Resource.InProgress
            }
            .subscribe({ bagSummary ->
                _checkoutObservable.value = Resource.Success(bagSummary)
            }, { error ->
                _checkoutObservable.value = Resource.Error(error)
            })
        )
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

}

sealed class ServiceOption {

    data class Delivery(val deliveryAddress: String = "") : ServiceOption()
    class Pickup() : ServiceOption()

}

data class ServiceOptionBindingModel(
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
    @DrawableRes val background: Int,
    val onClick: () -> Unit
) {
    fun click() {
        onClick.invoke()
    }
}