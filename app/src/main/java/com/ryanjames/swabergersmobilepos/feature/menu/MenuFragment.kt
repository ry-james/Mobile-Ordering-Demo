package com.ryanjames.swabergersmobilepos.feature.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.BaseFragment
import com.ryanjames.swabergersmobilepos.core.MobilePosDemoApplication
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.databinding.FragmentMenuBinding
import com.ryanjames.swabergersmobilepos.domain.Category
import com.ryanjames.swabergersmobilepos.domain.Resource
import javax.inject.Inject

class MenuFragment : BaseFragment<FragmentMenuBinding>(R.layout.fragment_menu) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: MenuFragmentViewModel by activityViewModels { viewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        MobilePosDemoApplication.appComponent.inject(this)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addSubscriptions()
        viewModel.retrieveMenu()
    }

    private fun addSubscriptions() {
        viewModel.menuObservable.observe(viewLifecycleOwner, Observer { menuResource ->
            when (menuResource) {
                is Resource.Success -> {
                    if (menuResource.data.categories.isEmpty()) {
                        return@Observer
                    }
                    setupViewPager2(menuResource.data.categories)
                }
            }
        })
    }

    private fun setupViewPager2(categories: List<Category>) {
        binding.viewPager2.adapter = MenuPagerAdapter(this, categories)
        TabLayoutMediator(binding.tabLayout2, binding.viewPager2) { tab, position ->
            tab.text = categories[position].categoryName
        }.attach()

        binding.viewPager2.setCurrentItem(viewModel.selectedCategoryPosition, false)
        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.selectedCategoryPosition = position
            }
        })
    }

    private class MenuPagerAdapter(fragment: Fragment, private val categories: List<Category>) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int {
            return categories.size
        }

        override fun createFragment(position: Int): Fragment {
            return MenuPagerFragment.newInstance(categories[position].categoryId)
        }
    }

    interface MenuFragmentCallback {
        fun onAddLineItem()
    }

}
