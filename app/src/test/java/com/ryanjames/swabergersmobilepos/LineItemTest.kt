package com.ryanjames.swabergersmobilepos

import com.ryanjames.swabergersmobilepos.data.LineItemTestData.lineItemBundleNoModifiers
import com.ryanjames.swabergersmobilepos.data.LineItemTestData.lineItemBundleWithModifiers
import com.ryanjames.swabergersmobilepos.data.LineItemTestData.lineItemProductNoModifier
import com.ryanjames.swabergersmobilepos.data.LineItemTestData.lineItemProductWithModifier
import junit.framework.Assert.assertEquals
import org.junit.Test


class LineItemTest {

    @Test
    fun test_unitPrice() {
        assertEquals(lineItemProductNoModifier().unitPrice, 12.5f)
        assertEquals(lineItemProductNoModifier().copy(quantity = 2).unitPrice, 12.5f)
        assertEquals(lineItemProductWithModifier().unitPrice, 12.5f)
        assertEquals(lineItemBundleNoModifiers().unitPrice, 20f)
        assertEquals(lineItemBundleNoModifiers().copy(quantity = 2).unitPrice, 20f)
        assertEquals(lineItemBundleWithModifiers().unitPrice, 20f)
    }

    @Test
    fun test_price() {
        assertEquals(lineItemProductNoModifier().price, 12.5f)
        assertEquals(lineItemProductNoModifier().copy(quantity = 2).price, 25f)
        assertEquals(lineItemProductWithModifier().price, 16.5f)
        assertEquals(lineItemProductWithModifier().copy(quantity = 5).price, 82.5f)
        assertEquals(lineItemBundleNoModifiers().price, 20f)
        assertEquals(lineItemBundleNoModifiers().copy(quantity = 3).price, 60f)
        assertEquals(lineItemBundleWithModifiers().price, 24f)
        assertEquals(lineItemBundleWithModifiers().copy(quantity = 4).price, 96f)
        assertEquals(lineItemProductNoModifier().copy(quantity = 0).price, 12.5f)
        assertEquals(lineItemProductNoModifier().copy(quantity = -1).price, 12.5f)
    }

    @Test
    fun test_line_item_name() {
        assertEquals(lineItemProductNoModifier().lineItemName, "Sample Product")
        assertEquals(lineItemBundleNoModifiers().lineItemName, "Sample Bundle")
    }


}