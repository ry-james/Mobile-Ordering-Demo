package com.ryanjames.swabergersmobilepos.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.ryanjames.swabergersmobilepos.R

class KeypadDialogFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppTheme_FullScreenDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_keypad, container)
        return view
    }

    companion object {

        private const val FULL_SCREEN_DIALOG_TAG = "Full Screen Dialog"

        fun show(supportFragmentManager: FragmentManager): DialogFragment {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            val previous = supportFragmentManager.findFragmentByTag(FULL_SCREEN_DIALOG_TAG)
            if (previous != null) {
                fragmentTransaction.remove(previous)
            }
            fragmentTransaction.addToBackStack(null)

            val fullScreenDialog = KeypadDialogFragment()
            fullScreenDialog.show(supportFragmentManager, FULL_SCREEN_DIALOG_TAG)
            return fullScreenDialog
        }

    }

}