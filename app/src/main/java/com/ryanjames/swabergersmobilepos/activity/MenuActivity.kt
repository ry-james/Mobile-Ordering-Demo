package com.ryanjames.swabergersmobilepos.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.BaseActivity
import com.ryanjames.swabergersmobilepos.core.SwabergersApplication
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.databinding.ActivityMenuBinding
import com.ryanjames.swabergersmobilepos.domain.Category
import com.ryanjames.swabergersmobilepos.feature.bagsummary.BagSummaryActivity
import com.ryanjames.swabergersmobilepos.fragments.MenuListFragment
import com.ryanjames.swabergersmobilepos.viewmodels.MenuActivityViewModel
import javax.inject.Inject

class MenuActivity : BaseActivity() {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var binding: ActivityMenuBinding
    private lateinit var viewModel: MenuActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SwabergersApplication.appComponent.inject(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_menu)
        binding.lifecycleOwner = this

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MenuActivityViewModel::class.java)
        binding.viewModel = viewModel
        viewModel.retrieveMenu()
        setToolbarTitle(getString(R.string.menu_toolbar_title))
        addSubscriptions()
    }

    override fun onResume() {
        super.onResume()
        viewModel.retrieveLocalBag()
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
        AlertDialog.Builder(this)
            .setMessage("We can't load the menu at the moment. Please try again later.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                finish()
            }.show()
    }

    private fun setupViewPager(categories: List<Category>) {
        binding.tabLayout.setupWithViewPager(binding.viewPager)
        binding.viewPager.adapter = ProgramDetailPagerAdapter(supportFragmentManager, categories)
    }


    private class ProgramDetailPagerAdapter(fm: FragmentManager, private val tabs: List<Category>) :
        FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return MenuListFragment.newInstance(tabs[position].categoryId)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return tabs[position].categoryName
        }

        override fun getCount(): Int = tabs.size

    }

    fun onClickBag(view: View) {
        startActivity(BagSummaryActivity.createIntent(this, viewModel.orderDetails))
    }


    companion object {

        fun createIntent(context: Context): Intent {
            return Intent(context, MenuActivity::class.java)
        }

    }


}
