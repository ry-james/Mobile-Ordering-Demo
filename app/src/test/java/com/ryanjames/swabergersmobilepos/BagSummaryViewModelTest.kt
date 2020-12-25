package com.ryanjames.swabergersmobilepos

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.ryanjames.swabergersmobilepos.core.RxImmediateSchedulerRule
import com.ryanjames.swabergersmobilepos.core.getOrAwaitValue
import com.ryanjames.swabergersmobilepos.domain.BagLineItem
import com.ryanjames.swabergersmobilepos.domain.BagSummary
import com.ryanjames.swabergersmobilepos.feature.bagsummary.BagSummaryViewModel
import com.ryanjames.swabergersmobilepos.repository.OrderRepository
import io.reactivex.Single
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnit
import java.util.*

@RunWith(JUnit4::class)
class BagSummaryViewModelTest {

    @Rule
    @JvmField
    var testSchedulerRule = RxImmediateSchedulerRule()

    @get:Rule
    var instantExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @Rule
    @JvmField
    val rule = MockitoJUnit.rule()

    @Mock
    lateinit var orderRepository: OrderRepository

    @InjectMocks
    lateinit var viewModel: BagSummaryViewModel

    @Before
    @Throws(Exception::class)
    fun setUp() {
        Mockito.`when`(orderRepository.getCurrentOrder()).thenReturn(Single.just(NON_EMPTY_BAG))
    }

//    @Test
//    fun test_empty_bag() {
//        Mockito.`when`(orderRepository.getCurrentOrder()).thenReturn(Single.just(EMPTY_BAG))
//        viewModel.retrieveLocalBag()
//        assertEquals(View.VISIBLE, viewModel.emptyBagVisibility.value)
//        assertEquals(View.GONE, viewModel.nonEmptyBagVisibility.value)
//        assertEquals(View.GONE, viewModel.serverIssueVisibility.value)
//    }
//
//    @Test
//    fun test_non_empty_bag() {
//        viewModel.retrieveLocalBag()
//        assertEquals(View.GONE, viewModel.emptyBagVisibility.value)
//        assertEquals(View.VISIBLE, viewModel.nonEmptyBagVisibility.value)
//        assertEquals(View.GONE, viewModel.serverIssueVisibility.value)
//    }

    @Test
    fun test_price() {
        viewModel.retrieveLocalBag()
        assertEquals("20.00", viewModel.total.getOrAwaitValue())
        assertEquals("17.86", viewModel.subtotal.getOrAwaitValue())
        assertEquals("2.14", viewModel.tax.getOrAwaitValue())
    }


//    @Test
//    fun test_loading_view() {
//        val observer: Observer<LoadingDialogBinding> = mock()
//        viewModel.loadingViewBinding.observeForever(observer)
//        Mockito.`when`(orderRepository.getCurrentOrder()).thenReturn(Single.just(EMPTY_BAG))
//        viewModel.retrieveLocalBag()
//
//        val captor = ArgumentCaptor.forClass(LoadingDialogBinding::class.java)
//        captor.run {
//            verify(observer, times(2)).onChanged(capture())
//            assertEquals(LoadingDialogBinding(View.VISIBLE, "Fetching bag...", R.color.colorWhite), allValues.getOrNull(0))
//            assertEquals(LoadingDialogBinding(View.GONE, "Fetching bag...", R.color.colorWhite), allValues.getOrNull(1))
//        }
//
//    }

    companion object {

        val EMPTY_BAG: BagSummary
            get() = BagSummary.emptyBag

        val NON_EMPTY_BAG: BagSummary
            get() = EMPTY_BAG.copy(lineItems = listOf(BAG_LINE_ITEM_1), price = 20f)

        val BAG_LINE_ITEM_1 = BagLineItem.EMPTY.copy(lineItemId = UUID.randomUUID().toString())
    }

}