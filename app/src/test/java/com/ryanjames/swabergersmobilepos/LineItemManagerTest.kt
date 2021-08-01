package com.ryanjames.swabergersmobilepos

import com.ryanjames.swabergersmobilepos.data.toBagLineItem
import com.ryanjames.swabergersmobilepos.domain.*
import com.ryanjames.swabergersmobilepos.manager.LineItemManager
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class LineItemManagerTest {

    @Test
    fun test_default_modifiers() {
        val lineItemManager = LineItemManager(PRODUCT_CHEESE_BURGER)
        assertEquals(lineItemManager.getLineItem().modifiers, mapOf(ProductModifierGroupKey(PRODUCT_CHEESE_BURGER, MODIFIER_GROUP_CHEESE) to listOf(AMERICAN_CHEESE)))
    }

    @Test
    fun test_meal_default_selections() {
        val lineItemManager = LineItemManager(PRODUCT_CHEESE_BURGER)
        lineItemManager.setProductBundle(CHEESE_BURGER_MEAL)
        assertEquals(lineItemManager.getLineItem().modifiers, hashMapOf(KEY_CHEESE to listOf(AMERICAN_CHEESE), KEY_FRIES to listOf(SMALL_FRIES)))
        assertEquals(lineItemManager.getLineItem().productsInBundle, DEFAULT_MEAL_SELECTION)
    }

    @Test
    fun test_meal_to_ala_carte() {
        val lineItemManager = LineItemManager(PRODUCT_CHEESE_BURGER, LINE_ITEM_MEAL.toBagLineItem())
        assertEquals(MODIFIER_SELECTIONS, lineItemManager.getLineItem().modifiers)
        lineItemManager.setProductBundle(null)
        assertEquals(hashMapOf<ProductGroup, List<Product>>(), lineItemManager.getLineItem().productsInBundle)
        assertEquals(hashMapOf(KEY_CHEESE to listOf(NO_CHEESE), KEY_TOPPING to listOf(LETTUCE, BACON)), lineItemManager.getLineItem().modifiers)
    }

    @Test
    fun test_adding_modifiers() {
        val lineItemManager = LineItemManager(PRODUCT_CHEESE_BURGER)
        lineItemManager.setCheese(NO_CHEESE)
        lineItemManager.setToppings(BACON, MUSHROOM, LETTUCE)
        assertEquals(hashMapOf(KEY_CHEESE to listOf(NO_CHEESE), KEY_TOPPING to listOf(BACON, MUSHROOM, LETTUCE)), lineItemManager.getLineItem().modifiers)
    }

    @Test
    fun test_adding_unknown_modifier() {
        val lineItemManager = LineItemManager(PRODUCT_CHEESE_BURGER)
        lineItemManager.setCheese(UNKNOWN_MODIFIER)
        assertEquals(hashMapOf(KEY_CHEESE to listOf<ModifierInfo>()), lineItemManager.getLineItem().modifiers)
    }

    @Test
    fun test_adding_unknown_product_selection_in_product_group() {
        val lineItemManager = LineItemManager(PRODUCT_CHEESE_BURGER)
        lineItemManager.setProductBundle(CHEESE_BURGER_MEAL)
        lineItemManager.setDrink(PRODUCT_UNKNOWN_DRINK)
        assertTrue(lineItemManager.getLineItem().productsInBundle[PRODUCT_GROUP_DRINKS]?.contains(PRODUCT_COKE) == true)
        assertTrue(lineItemManager.getLineItem().productsInBundle[PRODUCT_GROUP_DRINKS]?.contains(PRODUCT_UNKNOWN_DRINK) == false)
    }

    @Test
    fun test_adding_multiple_products_in_product_group() {
        val lineItemManager = LineItemManager(PRODUCT_CHEESE_BURGER)
        lineItemManager.setProductBundle(CHEESE_BURGER_MEAL)
        lineItemManager.setDrinks(listOf(PRODUCT_UNKNOWN_DRINK, PRODUCT_PEPSI, PRODUCT_COKE))
        assertEquals(listOf(PRODUCT_PEPSI, PRODUCT_COKE), lineItemManager.getLineItem().productsInBundle[PRODUCT_GROUP_DRINKS])
    }

    private fun LineItemManager.setDrink(drink: Product) {
        this.setProductSelectionsForProductGroupByIds(PRODUCT_GROUP_DRINKS, listOf(drink.productId))
    }

    private fun LineItemManager.setDrinks(drinks: List<Product>) {
        this.setProductSelectionsForProductGroupByIds(PRODUCT_GROUP_DRINKS, drinks.map { it.productId })
    }

    private fun LineItemManager.setCheese(cheese: ModifierInfo) {
        this.setProductModifiersByIds(PRODUCT_CHEESE_BURGER, MODIFIER_GROUP_CHEESE, listOf(cheese.modifierId))
    }

    private fun LineItemManager.setFriesSize(size: ModifierInfo) {
        this.setProductModifiersByIds(PRODUCT_FRIES, MODIFIER_GROUP_FRIES, listOf(size.modifierId))
    }

    private fun LineItemManager.setToppings(vararg toppings: ModifierInfo) {
        this.setProductModifiersByIds(PRODUCT_CHEESE_BURGER, MODIFIER_GROUP_TOPPING, toppings.map { it.modifierId })
    }


    companion object {

        private val NO_CHEESE
            get() = ModifierInfo("M1000", "No Cheese", 0f, "NCH")
        private val AMERICAN_CHEESE
            get() = ModifierInfo("M1001", "American Cheese", 0f, "ACH")
        private val LETTUCE
            get() = ModifierInfo("M2000", "Lettuce", 1f, "LTC")
        private val MUSHROOM
            get() = ModifierInfo("M2001", "Mushroom", 2f, "MSH")
        private val BACON
            get() = ModifierInfo("M2002", "Bacon", 3f, "BCN")
        private val MODIFIER_GROUP_CHEESE
            get() = ModifierGroup("MG1000", "Cheese", ModifierGroupAction.Required, AMERICAN_CHEESE, listOf(NO_CHEESE, AMERICAN_CHEESE), 1, 1)
        private val MODIFIER_GROUP_TOPPING
            get() = ModifierGroup("MG2000", "Topping", ModifierGroupAction.Optional, null, listOf(LETTUCE, MUSHROOM, BACON), 0, 5)
        private val SMALL_FRIES
            get() = ModifierInfo("M3000", "Small Fries", 0f, "SMF")
        private val LARGE_FRIES
            get() = ModifierInfo("M3001", "Large Fries", 0f, "LRF")
        private val PRODUCT_COKE
            get() = Product("D4000", "Coke", "", 0f, "CKE", listOf(), listOf(), null)
        private val PRODUCT_PEPSI
            get() = Product("D4001", "Pepsi", "", 0f, "PEP", listOf(), listOf(), null)
        private val PRODUCT_DR_PEPPER
            get() = Product("D4002", "Dr. Pepper", "", 0f, "DRP", listOf(), listOf(), null)
        private val MODIFIER_GROUP_FRIES
            get() = ModifierGroup("MG3000", "Size", ModifierGroupAction.Required, SMALL_FRIES, listOf(SMALL_FRIES, LARGE_FRIES), 1, 1)
        private val PRODUCT_FRIES
            get() = Product("F1000", "Fries", "Description", 3f, "", listOf(), listOf(MODIFIER_GROUP_FRIES), null)
        private val PRODUCTS_TOTS
            get() = Product("T1000", "Tots", "Description", 3f, "", listOf(), listOf(), null)
        private val PRODUCT_GROUP_DRINKS
            get() = ProductGroup("PG1000", "Drinks", PRODUCT_COKE, listOf(PRODUCT_COKE, PRODUCT_PEPSI, PRODUCT_DR_PEPPER), 1, 1)
        private val PRODUCT_GROUP_SIDES
            get() = ProductGroup("PG1001", "Sides", PRODUCT_FRIES, listOf(PRODUCT_FRIES, PRODUCTS_TOTS), 1, 1)
        private val CHEESE_BURGER_MEAL
            get() = ProductBundle("B1000", "Cheese Burger Meal", 12f, "CBM", listOf(PRODUCT_GROUP_DRINKS, PRODUCT_GROUP_SIDES))
        private val PRODUCT_CHEESE_BURGER
            get() = Product("C1000", "Cheese Burger", "Description", 6.5f, "CHB", listOf(CHEESE_BURGER_MEAL), listOf(MODIFIER_GROUP_CHEESE, MODIFIER_GROUP_TOPPING), null)


        private val UNKNOWN_MODIFIER
            get() = ModifierInfo("UNKNOWN", "Unknown Modifier", 100f, "")

        private val PRODUCT_UNKNOWN_DRINK
            get() = Product("D11000", "Unknown", "", 0f, "CKE", listOf(), listOf(), null)

        private val KEY_CHEESE
            get() = ProductModifierGroupKey(PRODUCT_CHEESE_BURGER, MODIFIER_GROUP_CHEESE)

        private val KEY_TOPPING
            get() = ProductModifierGroupKey(PRODUCT_CHEESE_BURGER, MODIFIER_GROUP_TOPPING)

        private val KEY_FRIES
            get() = ProductModifierGroupKey(PRODUCT_FRIES, MODIFIER_GROUP_FRIES)

        private val MEAL_SELECTIONS
            get() = hashMapOf(PRODUCT_GROUP_SIDES to listOf(PRODUCT_FRIES), PRODUCT_GROUP_DRINKS to listOf(PRODUCT_PEPSI))

        private val DEFAULT_MEAL_SELECTION
            get() = hashMapOf(PRODUCT_GROUP_SIDES to listOf(PRODUCT_FRIES), PRODUCT_GROUP_DRINKS to listOf(PRODUCT_COKE))

        private val MODIFIER_SELECTIONS
            get() = hashMapOf(KEY_CHEESE to listOf(NO_CHEESE), KEY_TOPPING to listOf(LETTUCE, BACON), KEY_FRIES to listOf(LARGE_FRIES))

        private val LINE_ITEM_MEAL: LineItem
            get() = LineItem("aaa", PRODUCT_CHEESE_BURGER, CHEESE_BURGER_MEAL, MEAL_SELECTIONS, MODIFIER_SELECTIONS, 1)

    }
}