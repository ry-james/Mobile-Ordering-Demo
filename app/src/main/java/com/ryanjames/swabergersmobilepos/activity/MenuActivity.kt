package com.ryanjames.swabergersmobilepos.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.base.BaseActivity
import com.ryanjames.swabergersmobilepos.database.realm.CategoryRealmEntity
import com.ryanjames.swabergersmobilepos.databinding.ActivityMenuBinding
import com.ryanjames.swabergersmobilepos.domain.Category
import com.ryanjames.swabergersmobilepos.feature.bagsummary.BagSummaryActivity
import com.ryanjames.swabergersmobilepos.fragments.MenuListFragment
import com.ryanjames.swabergersmobilepos.viewmodels.MenuActivityViewModel
import io.reactivex.disposables.CompositeDisposable

class MenuActivity : BaseActivity() {

    private lateinit var binding: ActivityMenuBinding
    private lateinit var viewModel: MenuActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_menu)
        binding.lifecycleOwner = this


        viewModel = ViewModelProviders.of(this).get(MenuActivityViewModel::class.java)
        viewModel.retrieveMenu()
        addSubscriptions()
    }

    private fun addSubscriptions() {
        viewModel.menuObservable.observe(this, Observer { menu ->
            setupViewPager(menu.categories)
        })
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
