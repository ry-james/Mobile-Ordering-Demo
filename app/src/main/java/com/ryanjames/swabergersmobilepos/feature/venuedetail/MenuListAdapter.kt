package com.ryanjames.swabergersmobilepos.feature.venuedetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ryanjames.swabergersmobilepos.databinding.ViewMenuCategoryHeaderBinding
import com.ryanjames.swabergersmobilepos.databinding.ViewMenuItemCardBinding
import com.ryanjames.swabergersmobilepos.domain.Category
import com.ryanjames.swabergersmobilepos.domain.Product
import com.ryanjames.swabergersmobilepos.feature.menu.MenuListItemViewModel

private const val PRODUCT_VIEW_TYPE = 0
private const val CATEGORY_VIEW_TYPE = 1
class MenuListAdapter(val onClickMenuItem: (product: Product) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var items = listOf<ItemType>()
    private val mapCategoryPositions = mutableMapOf<Int, Int>()

    fun getCategoryIndexByPosition(visibleItemPosition: Int): Int {
        return mapCategoryPositions.filter { (_, categoryPosition) -> categoryPosition <= visibleItemPosition }.keys.max() ?: 0
    }

    fun getCategoryPosition(categoryIndex: Int): Int {
        return mapCategoryPositions.getOrDefault(categoryIndex, 0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == CATEGORY_VIEW_TYPE) {
            val binding = ViewMenuCategoryHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return MenuCategoryViewHolder(binding)
        }
        val binding = ViewMenuItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MenuItemViewHolder) {
            (items[position] as? ItemType.ProductItem)?.also {
                holder.bind(it.viewModel)
            }
        } else if (holder is MenuCategoryViewHolder) {
            (items[position] as? ItemType.CategoryItem)?.also {
                holder.bind(it.category.categoryName)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            items[position] is ItemType.ProductItem -> PRODUCT_VIEW_TYPE
            items[position] is ItemType.CategoryItem -> CATEGORY_VIEW_TYPE
            else -> -1
        }
    }

    fun setCategories(categories: List<Category>) {
        val itemList = mutableListOf<ItemType>()
        categories.forEachIndexed { index, category ->
            mapCategoryPositions[index] = itemList.size
            itemList.add(ItemType.CategoryItem(category))
            category.products.forEach { product ->
                val productItem = ItemType.ProductItem(product, MenuListItemViewModel(product, onClickMenuItem))
                itemList.add(productItem)
            }
        }
        items = itemList
        notifyDataSetChanged()
    }


    class MenuCategoryViewHolder(val binding: ViewMenuCategoryHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(categoryName: String) {
            binding.tvHeader.text = categoryName
        }
    }

    class MenuItemViewHolder(val binding: ViewMenuItemCardBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(viewModel: MenuListItemViewModel) {
            viewModel.setupViewModel()
            binding.viewModel = viewModel
        }
    }

}

sealed class ItemType {
    class ProductItem(val product: Product, val viewModel: MenuListItemViewModel) : ItemType()
    class CategoryItem(val category: Category) : ItemType()
}