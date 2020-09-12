package com.ryanjames.swabergersmobilepos

import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import com.ryanjames.swabergersmobilepos.data.LineItemTestData
import com.ryanjames.swabergersmobilepos.data.LineItemTestData.basicProduct
import com.ryanjames.swabergersmobilepos.data.toBagLineItem
import com.ryanjames.swabergersmobilepos.domain.*
import com.ryanjames.swabergersmobilepos.feature.menuitemdetail.MenuItemDetailViewModel
import com.ryanjames.swabergersmobilepos.helper.toTwoDigitString
import com.ryanjames.swabergersmobilepos.repository.MenuRepository
import com.ryanjames.swabergersmobilepos.repository.OrderRepository
import io.reactivex.Single
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnit
import org.hamcrest.CoreMatchers.`is` as Is


@RunWith(JUnit4::class)
class MenuItemDetailViewModelTest {

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

    @Mock
    lateinit var menuRepository: MenuRepository

    @InjectMocks
    lateinit var viewModel: MenuItemDetailViewModel


    @Before
    @Throws(Exception::class)
    fun setUp() {
        Mockito.`when`(menuRepository.getProductDetails(PRODUCT_CHEESE_BURGER.productId)).thenReturn(Single.just(PRODUCT_CHEESE_BURGER))
        Mockito.`when`(menuRepository.getProductDetails(basicProduct.productId)).thenReturn(Single.just(basicProduct))
    }

    @Test
    fun test_add_to_bag_cta() {
        viewModel.setupWithProductId(PRODUCT_CHEESE_BURGER.productId)
        val formattedPrice = PRODUCT_CHEESE_BURGER.price.toTwoDigitString()
        assertEquals(R.string.add_to_bag, viewModel.strAddToBag.value?.id)
        assertEquals(1, viewModel.strAddToBag.value?.formatArgs?.size)
        assertEquals(formattedPrice, viewModel.strAddToBag.value?.formatArgs?.get(0))
        assertEquals(View.GONE, viewModel.btnRemoveVisibility.value)
    }

    @Test
    fun test_update_bag_cta() {
        val lineItem = LineItemTestData.lineItemProductNoModifier()
        viewModel.setupWithBagLineItem(lineItem.toBagLineItem())
        val formattedPrice = lineItem.price.toTwoDigitString()
        assertEquals(R.string.update_item, viewModel.strAddToBag.value?.id)
        assertEquals(1, viewModel.strAddToBag.value?.formatArgs?.size)
        assertEquals(formattedPrice, viewModel.strAddToBag.value?.formatArgs?.get(0))
        assertEquals(View.VISIBLE, viewModel.btnRemoveVisibility.value)
    }

    @Test
    fun test_product_name_and_description() {
        viewModel.setupWithProductId(PRODUCT_CHEESE_BURGER.productId)
        assertThat(viewModel.strProductName.value, Is(PRODUCT_CHEESE_BURGER.productName))
        assertThat(viewModel.strProductDescription.value, Is(PRODUCT_CHEESE_BURGER.productDescription))
    }

    @Test
    fun test_line_item_name_and_description() {
        viewModel.setupWithBagLineItem(LINE_ITEM_MEAL.toBagLineItem())
        assertEquals(PRODUCT_CHEESE_BURGER.productName, viewModel.strProductName.value)
        assertEquals(PRODUCT_CHEESE_BURGER.productDescription, viewModel.strProductDescription.value)
    }

    @Test
    fun test_product_default_modifiers() {
        viewModel.setupWithProductId(PRODUCT_CHEESE_BURGER.productId)
        val defaultModifiers = hashMapOf(KEY_CHEESE to listOf(AMERICAN_CHEESE))
        assertEquals(defaultModifiers, viewModel.lineItemObservable.value().modifiers)
    }

    @Test
    fun test_make_a_meal() {
        viewModel.setupWithProductId(PRODUCT_CHEESE_BURGER.productId)
        viewModel.setProductBundle(CHEESE_BURGER_MEAL)
        val modifiers = hashMapOf(KEY_CHEESE to listOf(AMERICAN_CHEESE), KEY_FRIES to listOf(SMALL_FRIES))
        assertEquals(modifiers, viewModel.lineItemObservable.value().modifiers)
        assertEquals(DEFAULT_MEAL_SELECTION, viewModel.lineItemObservable.value().productsInBundle)
    }

    @Test
    fun test_line_item_current_selections() {
        viewModel.setupWithProductId(PRODUCT_CHEESE_BURGER.productId)
        viewModel.setProductBundle(CHEESE_BURGER_MEAL)
        val modifiers = hashMapOf(KEY_CHEESE to listOf(AMERICAN_CHEESE), KEY_FRIES to listOf(SMALL_FRIES))
        assertEquals(modifiers, viewModel.lineItemObservable.value().modifiers)
        assertEquals(DEFAULT_MEAL_SELECTION, viewModel.lineItemObservable.value().productsInBundle)
    }

    @Test
    fun test_meal_to_ala_carte() {
        viewModel.setupWithBagLineItem(LINE_ITEM_MEAL.toBagLineItem())
        assertEquals(MODIFIER_SELECTIONS, viewModel.lineItemObservable.value().modifiers)
        viewModel.setProductBundle(null)
        assertEquals(hashMapOf<ProductGroup, List<Product>>(), viewModel.lineItemObservable.value().productsInBundle)
        assertEquals(hashMapOf(KEY_CHEESE to listOf(NO_CHEESE), KEY_TOPPING to listOf(LETTUCE, BACON)), viewModel.lineItemObservable.value().modifiers)
    }

    @Test
    fun test_discard_changes() {
        viewModel.setupWithBagLineItem(LINE_ITEM_MEAL.toBagLineItem())
        assertFalse(viewModel.shouldShowDiscardChanges())

        viewModel.setProductBundle(null)
        assertTrue(viewModel.shouldShowDiscardChanges())

        viewModel.setProductBundle(CHEESE_BURGER_MEAL)
        assertTrue(viewModel.shouldShowDiscardChanges())

        viewModel.setDrink(PRODUCT_PEPSI)
        viewModel.setCheese(NO_CHEESE)
        viewModel.setToppings(LETTUCE, BACON)
        viewModel.setFriesSize(LARGE_FRIES)
        assertFalse(viewModel.shouldShowDiscardChanges())

        viewModel.setQuantity(2)
        assertTrue(viewModel.shouldShowDiscardChanges())
        viewModel.setQuantity(1)
        assertFalse(viewModel.shouldShowDiscardChanges())

    }

    @Test
    fun test_adding_modifiers() {
        viewModel.setupWithProductId(PRODUCT_CHEESE_BURGER.productId)
        viewModel.setCheese(NO_CHEESE)
        viewModel.setToppings(BACON, MUSHROOM, LETTUCE)
        assertEquals(hashMapOf(KEY_CHEESE to listOf(NO_CHEESE), KEY_TOPPING to listOf(BACON, MUSHROOM, LETTUCE)), viewModel.lineItemObservable.value().modifiers)
    }

    @Test
    fun test_adding_unknown_modifier() {
        viewModel.setupWithProductId(PRODUCT_CHEESE_BURGER.productId)
        viewModel.setCheese(UNKNOWN_MODIFIER)
        assertEquals(hashMapOf(KEY_CHEESE to listOf<ModifierInfo>()), viewModel.lineItemObservable.value().modifiers)
    }

    @Test
    fun test_adding_unknown_product_selection_in_product_group() {
        viewModel.setupWithProductId(PRODUCT_CHEESE_BURGER.productId)
        viewModel.setProductBundle(CHEESE_BURGER_MEAL)
        viewModel.setDrink(PRODUCT_UNKNOWN_DRINK)
        assertTrue(viewModel.lineItemObservable.value().productsInBundle[PRODUCT_GROUP_DRINKS]?.contains(PRODUCT_COKE) == true)
        assertTrue(viewModel.lineItemObservable.value().productsInBundle[PRODUCT_GROUP_DRINKS]?.contains(PRODUCT_UNKNOWN_DRINK) == false)
    }

    @Test
    fun test_adding_multiple_products_in_product_group() {
        viewModel.setupWithProductId(PRODUCT_CHEESE_BURGER.productId)
        viewModel.setProductBundle(CHEESE_BURGER_MEAL)
        viewModel.setDrinks(listOf(PRODUCT_UNKNOWN_DRINK, PRODUCT_PEPSI, PRODUCT_COKE))
        assertEquals(listOf(PRODUCT_PEPSI, PRODUCT_COKE), viewModel.lineItemObservable.value().productsInBundle[PRODUCT_GROUP_DRINKS])
    }

    private fun MenuItemDetailViewModel.setDrink(drink: Product) {
        this.setProductSelectionsForProductGroupByIds(PRODUCT_GROUP_DRINKS, listOf(drink.productId))
    }

    private fun MenuItemDetailViewModel.setDrinks(drinks: List<Product>) {
        this.setProductSelectionsForProductGroupByIds(PRODUCT_GROUP_DRINKS, drinks.map { it.productId })
    }

    private fun MenuItemDetailViewModel.setCheese(cheese: ModifierInfo) {
        this.setProductModifiersByIds(PRODUCT_CHEESE_BURGER, MODIFIER_GROUP_CHEESE, listOf(cheese.modifierId))
    }

    private fun MenuItemDetailViewModel.setFriesSize(size: ModifierInfo) {
        this.setProductModifiersByIds(PRODUCT_FRIES, MODIFIER_GROUP_FRIES, listOf(size.modifierId))
    }

    private fun MenuItemDetailViewModel.setToppings(vararg toppings: ModifierInfo) {
        this.setProductModifiersByIds(PRODUCT_CHEESE_BURGER, MODIFIER_GROUP_TOPPING, toppings.map { it.modifierId })
    }

    private fun LiveData<Resource<LineItem>>.value(): LineItem {
        return (viewModel.lineItemObservable.value as Resource.Success).data.peekContent()
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
            get() = Product("D4000", "Coke", "", 0f, "CKE", listOf(), listOf())
        private val PRODUCT_PEPSI
            get() = Product("D4001", "Pepsi", "", 0f, "PEP", listOf(), listOf())
        private val PRODUCT_DR_PEPPER
            get() = Product("D4002", "Dr. Pepper", "", 0f, "DRP", listOf(), listOf())
        private val MODIFIER_GROUP_FRIES
            get() = ModifierGroup("MG3000", "Size", ModifierGroupAction.Required, SMALL_FRIES, listOf(SMALL_FRIES, LARGE_FRIES), 1, 1)
        private val PRODUCT_FRIES
            get() = Product("F1000", "Fries", "Description", 3f, "", listOf(), listOf(MODIFIER_GROUP_FRIES))
        private val PRODUCTS_TOTS
            get() = Product("T1000", "Tots", "Description", 3f, "", listOf(), listOf())
        private val PRODUCT_GROUP_DRINKS
            get() = ProductGroup("PG1000", "Drinks", PRODUCT_COKE, listOf(PRODUCT_COKE, PRODUCT_PEPSI, PRODUCT_DR_PEPPER), 1, 1)
        private val PRODUCT_GROUP_SIDES
            get() = ProductGroup("PG1001", "Sides", PRODUCT_FRIES, listOf(PRODUCT_FRIES, PRODUCTS_TOTS), 1, 1)
        private val CHEESE_BURGER_MEAL
            get() = ProductBundle("B1000", "Cheese Burger Meal", 12f, "CBM", listOf(PRODUCT_GROUP_DRINKS, PRODUCT_GROUP_SIDES))
        private val PRODUCT_CHEESE_BURGER
            get() = Product("C1000", "Cheese Burger", "Description", 6.5f, "CHB", listOf(CHEESE_BURGER_MEAL), listOf(MODIFIER_GROUP_CHEESE, MODIFIER_GROUP_TOPPING))


        private val UNKNOWN_MODIFIER
            get() = ModifierInfo("UNKNOWN", "Unknown Modifier", 100f, "")

        private val PRODUCT_UNKNOWN_DRINK
            get() = Product("D11000", "Unknown", "", 0f, "CKE", listOf(), listOf())

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