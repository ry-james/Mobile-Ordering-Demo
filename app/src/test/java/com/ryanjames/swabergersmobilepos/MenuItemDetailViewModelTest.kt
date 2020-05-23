package com.ryanjames.swabergersmobilepos

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.ryanjames.swabergersmobilepos.data.LineItemTestData
import com.ryanjames.swabergersmobilepos.domain.Product
import com.ryanjames.swabergersmobilepos.feature.menuitemdetail.MenuItemDetailViewModel
import com.ryanjames.swabergersmobilepos.helper.toTwoDigitString
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MenuItemDetailViewModelTest {

    @get:Rule
    var instantExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    @Throws(Exception::class)
    fun setUp() {

    }

    @Test
    fun test_add_to_bag_cta() {
        val viewModel = MenuItemDetailViewModel()
        viewModel.strAddToBag.observeForever { }
        val product = Product.EMPTY.copy(productName = "Mock", price = 5.5f)
        viewModel.setupWithProduct(product)
        val formattedPrice = product.price.toTwoDigitString()
        assertEquals(viewModel.strAddToBag.value?.id, R.string.add_to_bag)
        assertEquals(viewModel.strAddToBag.value?.formatArgs?.size, 1)
        assertEquals(viewModel.strAddToBag.value?.formatArgs?.get(0), formattedPrice)
    }

    @Test
    fun test_update_bag_cta() {
        val viewModel = MenuItemDetailViewModel()
        viewModel.strAddToBag.observeForever { }
        val lineItem = LineItemTestData.lineItemProductNoModifier()
        viewModel.setupWithLineItem(lineItem)
        val formattedPrice = lineItem.price.toTwoDigitString()
        assertEquals(viewModel.strAddToBag.value?.id, R.string.update_item)
        assertEquals(viewModel.strAddToBag.value?.formatArgs?.size, 1)
        assertEquals(viewModel.strAddToBag.value?.formatArgs?.get(0), formattedPrice)
    }
}