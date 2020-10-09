package com.ryanjames.swabergersmobilepos.core

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ryanjames.swabergersmobilepos.R

abstract class FullyExpandedBottomSheetFragment() : BottomSheetDialogFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fullyExpandBottomSheet()
    }

    private fun fullyExpandBottomSheet() {
        dialog?.setOnShowListener { dialog ->
            val bottomSheet = dialog as BottomSheetDialog
            bottomSheet.findViewById<FrameLayout>(R.id.design_bottom_sheet)?.also { frameLayout ->
                val bottomSheetBehavior = BottomSheetBehavior.from(frameLayout)
                frameLayout.height.let { bottomSheetBehavior.peekHeight = it }
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }

        }
    }
}