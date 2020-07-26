package com.ryanjames.swabergersmobilepos.feature.menu

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.SwabergersApplication
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.databinding.FragmentMenuBinding
import com.ryanjames.swabergersmobilepos.domain.Category
import javax.inject.Inject

class MenuFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private var binding: FragmentMenuBinding? = null
    private lateinit var viewModel: MenuFragmentViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        SwabergersApplication.appComponent.inject(this)

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_menu, container, false)

        binding?.lifecycleOwner = this

        viewModel = ViewModelProviders.of(activity!!, viewModelFactory).get(MenuFragmentViewModel::class.java)

        binding?.viewModel = viewModel
        viewModel.retrieveMenu()
        addSubscriptions()
        return binding?.root
    }

    private fun addSubscriptions() {
        viewModel.menuObservable.observe(this, Observer { menu ->
            if (menu.categories.isEmpty()) {
                showMenuErrorLoading()
                return@Observer
            }
            setupViewPager(menu.categories)
        })

        viewModel.errorLoadingMenuObservable.observe(this, Observer { event ->
            if (event.getContentIfNotHandled() == true) {
                showMenuErrorLoading()
            }
        })
    }

    private fun showMenuErrorLoading() {
        AlertDialog.Builder(context)
            .setMessage("We can't load the menu at the moment. Please try again later.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun setupViewPager(categories: List<Category>) {
        activity?.let {
            binding?.tabLayout?.setupWithViewPager(binding?.viewPager)
            binding?.viewPager?.adapter = ProgramDetailPagerAdapter(it.supportFragmentManager, categories)
            binding?.viewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {}

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

                override fun onPageSelected(position: Int) {
                    viewModel.selectedCategoryPosition = position
                }
            })
            binding?.viewPager?.currentItem = viewModel.selectedCategoryPosition
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Setting this to null to prevent memory leak
        binding?.viewPager?.adapter = null
        binding = null

    }

    private class ProgramDetailPagerAdapter(fm: FragmentManager, private val tabs: List<Category>) :
        FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return MenuPagerFragment.newInstance(tabs[position].categoryId)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return tabs[position].categoryName
        }

        override fun getCount(): Int = tabs.size

    }

    interface MenuFragmentCallback {
        fun onAddLineItem()
    }

}
