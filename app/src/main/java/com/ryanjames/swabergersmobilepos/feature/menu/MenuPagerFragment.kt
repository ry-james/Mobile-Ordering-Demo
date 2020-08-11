package com.ryanjames.swabergersmobilepos.feature.menu


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.BaseFragment
import com.ryanjames.swabergersmobilepos.core.SwabergersApplication
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.databinding.FragmentMenuListBinding
import com.ryanjames.swabergersmobilepos.databinding.RowMenuItemBinding
import com.ryanjames.swabergersmobilepos.domain.Product
import com.ryanjames.swabergersmobilepos.feature.menuitemdetail.MenuItemDetailActivity
import com.ryanjames.swabergersmobilepos.feature.menuitemdetail.REQUEST_LINE_ITEM
import com.ryanjames.swabergersmobilepos.feature.menuitemdetail.RESULT_ADD_OR_UPDATE
import javax.inject.Inject

const val EXTRA_CATEGORY_ID = "extra.category.id"

class MenuPagerFragment : BaseFragment<FragmentMenuListBinding>(R.layout.fragment_menu_list) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var fragmentCallback: MenuFragment.MenuFragmentCallback

    private val menuListAdapter: MenuListAdapter = MenuListAdapter { product ->
        startActivityForResult(MenuItemDetailActivity.createIntent(context, product), REQUEST_LINE_ITEM)
    }

    private lateinit var viewModel: MenuFragmentViewModel
    private var categoryId = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        SwabergersApplication.appComponent.inject(this)

        viewModel = ViewModelProviders.of(activity!!, viewModelFactory).get(MenuFragmentViewModel::class.java)
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
            layoutManager = LinearLayoutManager(this@MenuPagerFragment.context)
            adapter = menuListAdapter
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_LINE_ITEM && resultCode == RESULT_ADD_OR_UPDATE) {
            data?.let {
                val lineItem = MenuItemDetailActivity.getExtraLineItem(data)
                viewModel.addLineItem(lineItem)
                fragmentCallback.onAddLineItem()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            fragmentCallback = context as MenuFragment.MenuFragmentCallback
        } catch (e: ClassCastException) {
            throw ClassCastException("Host activity should implement ${MenuFragment.MenuFragmentCallback::class.java.simpleName}")
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
        fun newInstance(categoryId: String): MenuPagerFragment {
            val bundle = Bundle().apply { putString(EXTRA_CATEGORY_ID, categoryId) }
            return MenuPagerFragment().apply { arguments = bundle }
        }
    }
}

