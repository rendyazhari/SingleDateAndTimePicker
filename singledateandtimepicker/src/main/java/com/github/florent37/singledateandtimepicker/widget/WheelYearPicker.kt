package com.github.florent37.singledateandtimepicker.widget

import android.content.Context
import android.util.AttributeSet
import com.github.florent37.singledateandtimepicker.DateHelper
import com.github.florent37.singledateandtimepicker.R
import java.text.SimpleDateFormat
import java.util.*

class WheelYearPicker : WheelPicker<String?> {
    private var simpleDateFormat: SimpleDateFormat? = null
    @JvmField
    var minYear = 0
    protected var maximumYear = 0
    private var onYearSelectedListener: OnYearSelectedListener? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun initClass() {
        simpleDateFormat = SimpleDateFormat("yyyy", currentLocale)
        val instance = Calendar.getInstance()
        instance.timeZone = DateHelper.timeZone
        val currentYear = instance[Calendar.YEAR]
        minYear = currentYear - SingleDateAndTimeConstants.MIN_YEAR_DIFF
        maximumYear = currentYear + SingleDateAndTimeConstants.MAX_YEAR_DIFF
    }

    override fun initDefault(): String {
        return todayText
    }

    private val todayText: String
        get() = getLocalizedString(R.string.picker_today)

    override fun onItemSelected(position: Int, item: String?) {
        val year = convertItemToYear(position)
        onYearSelectedListener?.onYearSelected(this, position, year)
    }

    fun setMaxYear(maxYear: Int) {
        this.maximumYear = maxYear
        notifyDatasetChanged()
    }

    fun setMinYear(minYear: Int) {
        this.minYear = minYear
        notifyDatasetChanged()
    }

    override fun generateAdapterValues(): List<String> {
        val years: MutableList<String> = ArrayList()
        val instance = Calendar.getInstance()
        instance.timeZone = DateHelper.timeZone
        instance[Calendar.YEAR] = minYear - 1
        for (i in minYear..maximumYear) {
            instance.add(Calendar.YEAR, 1)
            years.add(getFormattedValue(instance.time))
        }
        return years
    }

    override fun getFormattedValue(value: Any): String = simpleDateFormat?.format(value).orEmpty()

    fun setOnYearSelectedListener(onYearSelectedListener: OnYearSelectedListener?) {
        this.onYearSelectedListener = onYearSelectedListener
    }

    val currentYear: Int
        get() = convertItemToYear(super.currentItemPosition)

    private fun convertItemToYear(itemPosition: Int): Int = minYear + itemPosition

    interface OnYearSelectedListener {
        fun onYearSelected(picker: WheelYearPicker?, position: Int, year: Int)
    }
}