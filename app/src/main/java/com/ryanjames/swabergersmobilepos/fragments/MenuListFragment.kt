package com.ryanjames.swabergersmobilepos.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.feature.menuitemdetail.MenuItemDetailActivity
import com.ryanjames.swabergersmobilepos.databinding.FragmentMenuListBinding
import com.ryanjames.swabergersmobilepos.databinding.RowMenuItemBinding
import com.ryanjames.swabergersmobilepos.domain.Product
import com.ryanjames.swabergersmobilepos.viewmodels.MenuActivityViewModel
import com.ryanjames.swabergersmobilepos.viewmodels.MenuListItemViewModel

const val EXTRA_CATEGORY_ID = "extra.category.id"

class MenuListFragment : Fragment() {

    private val menuListAdapter: MenuListAdapter = MenuListAdapter { product ->
        startActivity(MenuItemDetailActivity.createIntent(context, product))
    }

    private lateinit var viewModel: MenuActivityViewModel
    private lateinit var binding: FragmentMenuListBinding
    private var categoryId = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_menu_list, container, false)
        viewModel = activity?.run {
            ViewModelProviders.of(this)[MenuActivityViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        binding.lifecycleOwner = this

        categoryId = arguments?.getString(EXTRA_CATEGORY_ID) ?: ""

        addSubscriptions()

        return binding.root
    }

    private fun addSubscriptions() {
        viewModel.menuObservable.observe(this, Observer { menu ->
            val products = menu.categories.find { it.categoryId == categoryId }?.products ?: listOf()
            menuListAdapter.setProducts(products)
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvMenuList.apply {
            layoutManager = LinearLayoutManager(this@MenuListFragment.context)
            adapter = menuListAdapter
        }
    }

    private class MenuListAdapter(val onClickMenuItem: (product: Product) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var items = listOf<Product>()

        private val itemViewModels: List<MenuListItemViewModel>
            get() = items.map { MenuListItemViewModel(it, onClickMenuItem) }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val binding = RowMenuItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return MenuItemViewHolder(binding)
        }

        override fun getItemCount(): Int {
            return itemViewModels.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is MenuItemViewHolder) {
                holder.bind(itemViewModels[position])
            }
        }

        fun setProducts(products: List<Product>) {
            items = products
            notifyDataSetChanged()
        }

        class MenuItemViewHolder(val binding: RowMenuItemBinding) : RecyclerView.ViewHolder(binding.root) {

            fun bind(viewModel: MenuListItemViewModel) {
                binding.viewModel = viewModel
                viewModel.setupViewModel()
                binding.executePendingBindings()
            }
        }

    }


    companion object {
        @JvmStatic
        fun newInstance(categoryId: String): MenuListFragment {
            val bundle = Bundle().apply { putString(EXTRA_CATEGORY_ID, categoryId) }
            return MenuListFragment().apply { arguments = bundle }
        }
    }
}

