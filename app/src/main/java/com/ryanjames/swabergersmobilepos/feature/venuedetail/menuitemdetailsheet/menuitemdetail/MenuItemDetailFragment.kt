package com.ryanjames.swabergersmobilepos.feature.venuedetail.menuitemdetailsheet.menuitemdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.BaseFragment
import com.ryanjames.swabergersmobilepos.core.MobilePosDemoApplication
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.databinding.FragmentMenuItemDetailsBinding
import com.ryanjames.swabergersmobilepos.domain.ModifierGroup
import com.ryanjames.swabergersmobilepos.domain.Product
import com.ryanjames.swabergersmobilepos.domain.ProductGroup
import com.ryanjames.swabergersmobilepos.domain.Resource
import com.ryanjames.swabergersmobilepos.feature.venuedetail.menuitemdetailsheet.parent.MenuItemBottomSheetViewModel
import javax.inject.Inject


class MenuItemDetailFragment : BaseFragment<FragmentMenuItemDetailsBinding>(R.layout.fragment_menu_item_details) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: MenuItemBottomSheetViewModel by viewModels(ownerProducer = { requireParentFragment() }, factoryProducer = { viewModelFactory })
    private lateinit var adapter: MenuItemDetailAdapter2

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        MobilePosDemoApplication.appComponent.inject(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        setupRecyclerView()
        subscribe()
    }

    private fun subscribe() {
        viewModel.lineItemObservable.observe(viewLifecycleOwner, Observer {
            if (it is Resource.Success) {
                adapter.update(it.data)
            }
        })
    }

    private fun setupRecyclerView() {

        adapter = MenuItemDetailAdapter2(object :
            MenuItemDetailAdapter2.OnClickRowListener {

            override fun onClickRowMealOptions() {
                viewModel.selectMealOptionRow()
            }

            override fun onClickRowProductGroup(productGroup: ProductGroup) {
                viewModel.selectProductGroupRow(productGroup)
            }

            override fun onClickRowProductGroupModifier(product: Product, modifierGroup: ModifierGroup) {
                viewModel.selectProductModifierGroupRow(product, modifierGroup)
            }

        })

        binding.rvModifiers.apply {
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            layoutManager = LinearLayoutManager(activity)
            adapter = this@MenuItemDetailFragment.adapter
        }

    }


    companion object {
        fun createInstance(): MenuItemDetailFragment {
            return MenuItemDetailFragment()
        }
    }

}