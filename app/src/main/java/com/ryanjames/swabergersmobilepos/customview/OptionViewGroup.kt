package com.ryanjames.swabergersmobilepos.customview

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import com.ryanjames.swabergersmobilepos.R
import kotlinx.android.synthetic.main.row_item_modifier_option.view.*


class OptionViewGroup(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val tvSectionHeader: TextView
    private val llOptions: LinearLayout
    private val optionsRadioGroup = mutableListOf<RadioButton>()
    private val subOptionsRadioGroup = mutableListOf<RadioButton>()
    private var groupId: String = ""
    private var onClickListener: OnClickListener? = null


    init {
        val view = View.inflate(context, R.layout.row_item_modifier_header, this)
        tvSectionHeader = view.findViewById(R.id.tvTitle)
        llOptions = view.findViewById(R.id.llOptions)
        orientation = VERTICAL
    }

    fun setSectionHeader(text: String) {
        tvSectionHeader.text = text
    }

    fun setOptions(options: List<OptionViewItem>) {
        options.forEachIndexed { index, option ->
            val optionView = View.inflate(context, R.layout.row_item_modifier_option, null)
            optionView.tvPriceDelta.text = option.rightText
            optionView.tvName.text = option.name

            if (index == options.size - 1) {
                optionView.divider.visibility = View.GONE
            }

            optionView.radioButton.apply {
                tag = option.id
                setOnClickListener(::onClickRbRow)
                optionsRadioGroup.add(this)
            }

            optionView.apply {
                tag = option.id
                setOnClickListener(::onClickRbRow)
                llOptions.addView(this)

                option.subOptions.map {
                    llOptions.addView(createSubOptionView(it))
                }

            }

        }
    }

    fun getSelectedOption(): String? {
        return optionsRadioGroup.find { it.isChecked }?.tag?.toString()
    }

    private fun createSubOptionView(optionViewItem: OptionViewItem): View {
        val optionView = View.inflate(context, R.layout.row_item_modifier_option, null)

        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40f, resources.displayMetrics).toInt()

        optionView.apply {
            setPadding(paddingStart + px, paddingTop, paddingEnd, paddingBottom)
            divider.visibility = View.GONE
            tvPriceDelta.text = optionViewItem.rightText
            tvName.text = optionViewItem.name
        }

        optionView.radioButton.apply {
            tag = optionViewItem.id
            subOptionsRadioGroup.add(this)
            setOnClickListener(::onClickSubRbRow)
        }

        optionView.apply {
            tag = optionViewItem.id
            setOnClickListener(::onClickSubRbRow)
        }

        return optionView
    }

    private fun onClickRbRow(view: View) {
        optionsRadioGroup.map {
            it.isChecked = (view.tag == it.tag)

            if (it.isChecked) {
                onClickListener?.onSelectOption(groupId, it.tag as String)
            }

        }
    }

    private fun onClickSubRbRow(view: View) {
        subOptionsRadioGroup.map {
            it.isChecked = (view.tag == it.tag)

            if (it.isChecked) {
                onClickListener?.onSelectSubOption()
            }

        }
    }

    fun select(viewId: String) {
        optionsRadioGroup.find { it.tag == viewId }?.let { onClickRbRow(it) }
    }

    fun setId(id: String) {
        this.groupId = id
    }

    interface OnClickListener {
        fun onSelectOption(groupId: String, optionId: String)
        fun onSelectSubOption()
    }


    data class OptionViewItem(
        val id: String,
        val name: String,
        val rightText: String = "",
        val subOptions: List<OptionViewItem> = listOf()
    )

}