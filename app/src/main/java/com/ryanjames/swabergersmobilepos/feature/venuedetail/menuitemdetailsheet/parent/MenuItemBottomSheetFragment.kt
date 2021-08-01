package com.ryanjames.swabergersmobilepos.feature.venuedetail.menuitemdetailsheet.parent

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.MobilePosDemoApplication
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.databinding.FragmentMenuItemParentBinding
import com.ryanjames.swabergersmobilepos.domain.BagLineItem
import com.ryanjames.swabergersmobilepos.domain.BagSummary
import com.ryanjames.swabergersmobilepos.domain.Resource
import com.ryanjames.swabergersmobilepos.domain.Venue
import com.ryanjames.swabergersmobilepos.feature.venuedetail.menuitemdetailsheet.menuitemdetail.MenuItemDetailFragment
import com.ryanjames.swabergersmobilepos.feature.venuedetail.menuitemdetailsheet.menuitemmodifier.MenuItemModifierFragment
import com.ryanjames.swabergersmobilepos.helper.DialogManager
import java.lang.ref.WeakReference
import javax.inject.Inject

private const val EXTRA_PRODUCT_ID = "extra.product.id"
private const val EXTRA_VENUE_ID = "extra.venue.id"
private const val EXTRA_LINE_ITEM = "extra.line.item"

class MenuItemBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentMenuItemParentBinding

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val dialogManager by lazy { DialogManager(viewLifecycleOwner.lifecycle, requireContext()) }

    private val viewModel: MenuItemBottomSheetViewModel by viewModels { viewModelFactory }

    private var listener: Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.RoundedBottomSheetDialogTheme)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : BottomSheetDialog(requireContext(), theme) {
            override fun onBackPressed() {
                if (binding.viewpager.currentItem == 1) {
                    viewModel.setViewPagerPosition(0)
                } else {
                    dismiss()
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            this.listener = context as Listener
        } catch (e: ClassCastException) {
            this.listener = object : Listener {
                override fun onAddLineItem(bagSummary: BagSummary) {}
                override fun onUpdateLineItem(bagSummary: BagSummary) {}
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        MobilePosDemoApplication.appComponent.inject(this)
        binding = FragmentMenuItemParentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewpager.layoutParams = binding.viewpager.layoutParams.apply {
            this.height = (resources.displayMetrics.heightPixels * .9f).toInt()
        }

        dialog?.setOnShowListener { dialog ->
            val bottomSheet: WeakReference<BottomSheetDialog> = WeakReference(dialog as BottomSheetDialog)

            bottomSheet.get()?.findViewById<FrameLayout>(R.id.design_bottom_sheet)?.also { frameLayout ->
                val bottomSheetBehavior = BottomSheetBehavior.from(frameLayout)
                bottomSheetBehavior.setPeekHeight(resources.displayMetrics.heightPixels, true)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        val venue = arguments?.getParcelable<Venue>(EXTRA_VENUE_ID)
        val productId = arguments?.getString(EXTRA_PRODUCT_ID)
        val lineItem = arguments?.getParcelable<BagLineItem>(EXTRA_LINE_ITEM)
        if (venue != null && productId != null) {
            viewModel.initializeWithProduct(productId, venue)
        } else if (lineItem != null && venue != null) {
            viewModel.initializeWithLineItem(lineItem, venue)
        }
        subscribe()
    }

    private fun subscribe() {
        viewModel.addItemEvent.observe(viewLifecycleOwner, Observer { event ->
            event.handleEvent {
                when (it) {
                    is Resource.InProgress -> {
                        dialogManager.showLoadingDialog(getString(R.string.adding_to_bag))
                    }
                    is Resource.Success -> {
                        dialogManager.hideLoadingDialog()
                        listener?.onAddLineItem(it.data)
                        dismiss()
                    }
                    is Resource.Error -> {
                        dialogManager.hideLoadingDialog()
                        dialogManager.showDismissableDialog(getString(R.string.error_add_item))
                    }
                }

            }
        })

        viewModel.updateItemEvent.observe(viewLifecycleOwner, Observer { event ->
            event.handleEvent {
                when (it) {
                    is Resource.InProgress -> {
                        dialogManager.showLoadingDialog(getString(R.string.updating_item))
                    }
                    is Resource.Success -> {
                        dialogManager.hideLoadingDialog()
                        dismiss()
                        listener?.onUpdateLineItem(it.data)
                    }
                    is Resource.Error -> {
                        dialogManager.hideLoadingDialog()
                        dialogManager.showDismissableDialog(getString(R.string.error_modify_item))
                    }
                }
            }
        })

        viewModel.onShowStartNewOrderDialog.observe(viewLifecycleOwner, Observer { event ->
            event.handleEvent { oldVenue ->
                AlertDialog.Builder(requireContext())
                    .setCancelable(false)
                    .setMessage(getString(R.string.clear_cart, oldVenue.name))
                    .setPositiveButton("YES") { dialog, _ ->
                        dialog.dismiss()
                        viewModel.clearBagAndChangeVenue()
                    }.setNegativeButton("NO") { dialog, _ ->
                        dialog.dismiss()
                    }.show()
            }
        })
    }

    private fun setupViewPager() {
        binding.viewpager.isUserInputEnabled = false
        binding.viewpager.adapter = MenuItemPagerAdapter(this)

        viewModel.onViewPagerPositionChange.observe(viewLifecycleOwner, Observer { position ->
            binding.viewpager.currentItem = position.coerceIn(0, 1)
        })
    }


    private class MenuItemPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(position: Int): Fragment {
            return if (position == 1) {
                MenuItemModifierFragment.createInstance()
            } else {
                MenuItemDetailFragment.createInstance()
            }
        }
    }

    interface Listener {
        fun onAddLineItem(bagSummary: BagSummary)
        fun onUpdateLineItem(bagSummary: BagSummary)
    }

    companion object {
        fun createInstance(productId: String, venue: Venue): MenuItemBottomSheetFragment {
            return MenuItemBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(EXTRA_PRODUCT_ID, productId)
                    putParcelable(EXTRA_VENUE_ID, venue)
                }
            }
        }

        fun createInstance(lineItem: BagLineItem, venue: Venue): MenuItemBottomSheetFragment {
            return MenuItemBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(EXTRA_LINE_ITEM, lineItem)
                    putParcelable(EXTRA_VENUE_ID, venue)
                }
            }
        }
    }

}