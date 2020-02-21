package com.github.florent37.singledateandtimepicker.widget

import android.content.Context
import android.util.AttributeSet
import com.github.florent37.singledateandtimepicker.DateHelper
import com.github.florent37.singledateandtimepicker.R
import java.util.*

class WheelAmPmPicker : WheelPicker<String?> {
    private var amPmListener: AmPmListener? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun initClass() {}
    override fun initDefault(): String =
            if (DateHelper.getHour(DateHelper.today(), true) >= SingleDateAndTimeConstants.MAX_HOUR_AM_PM) {
                getLocalizedString(R.string.picker_pm)
            } else {
                getLocalizedString(R.string.picker_am)
            }

    override fun generateAdapterValues(): List<String> = listOf(
            getLocalizedString(R.string.picker_am),
            getLocalizedString(R.string.picker_pm))

    override fun findIndexOfDate(date: Date): Int {
        val calendar = Calendar.getInstance()
        calendar.timeZone = DateHelper.timeZone
        calendar.time = date
        val hours = calendar[Calendar.HOUR_OF_DAY]
        return if (hours >= SingleDateAndTimeConstants.MAX_HOUR_AM_PM) {
            1
        } else {
            0
        }
    }

    fun setAmPmListener(amPmListener: AmPmListener?) {
        this.amPmListener = amPmListener
    }

    override fun onItemSelected(position: Int, item: String?) {
        super.onItemSelected(position, item)
        amPmListener?.run { onAmPmChanged(this@WheelAmPmPicker, isAm) }
    }

    override var isCyclic: Boolean
        get() = super.isCyclic
        set(value) {
            super.isCyclic = false
        }

    fun isAmPosition(position: Int): Boolean = position == INDEX_AM

    override fun getFormattedValue(value: Any): String {
        if (value is Date) {
            val instance = Calendar.getInstance()
            instance.timeZone = DateHelper.timeZone
            instance.time = value
            return getLocalizedString(if (instance[Calendar.AM_PM] == Calendar.PM) R.string.picker_pm else R.string.picker_am)
        }
        return value.toString()
    }

    val isAm: Boolean
        get() = currentItemPosition == INDEX_AM

    val isPm: Boolean
        get() = currentItemPosition == INDEX_PM

    interface AmPmListener {
        fun onAmPmChanged(pmPicker: WheelAmPmPicker?, isAm: Boolean)
    }

    companion object {
        const val INDEX_AM = 0
        const val INDEX_PM = 1
    }
}