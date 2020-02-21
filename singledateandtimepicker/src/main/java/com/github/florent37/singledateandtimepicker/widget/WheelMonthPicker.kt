package com.github.florent37.singledateandtimepicker.widget

import android.content.Context
import android.util.AttributeSet
import com.github.florent37.singledateandtimepicker.DateHelper
import java.text.SimpleDateFormat
import java.util.*

class WheelMonthPicker @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : WheelPicker<String?>(context, attrs) {
    private var monthLastScrollPosition = 0
    private var selectedListener: MonthSelectedListener? = null
    private var displayMonthNumbers = false
    override fun initClass() {}
    override fun generateAdapterValues(): List<String> {
        val monthList: MutableList<String> = ArrayList()
        val month_date = SimpleDateFormat("MMMM", currentLocale)
        val cal = Calendar.getInstance(currentLocale)
        cal.timeZone = DateHelper.timeZone
        cal[Calendar.DAY_OF_MONTH] = 1
        for (i in 0..11) {
            cal[Calendar.MONTH] = i
            if (displayMonthNumbers) {
                monthList.add(String.format("%02d", i + 1))
            } else {
                monthList.add(month_date.format(cal.time))
            }
        }
        return monthList
    }

    override fun initDefault(): String = DateHelper.getMonth(DateHelper.today()).toString()

    fun setOnMonthSelectedListener(listener: MonthSelectedListener) {
        this.selectedListener = listener
    }

    override fun onItemSelected(position: Int, item: String?) {
        selectedListener?.onMonthSelected(this, position, item)
    }

    override fun onItemCurrentScroll(position: Int, item: String?) {
        if (monthLastScrollPosition != position) {
            onItemSelected(position, item)
            monthLastScrollPosition = position
        }
    }

    fun displayMonthNumbers(): Boolean = displayMonthNumbers

    fun setDisplayMonthNumbers(displayMonthNumbers: Boolean) {
        this.displayMonthNumbers = displayMonthNumbers
    }

    val currentMonth: Int
        get() = currentItemPosition

    interface MonthSelectedListener {
        fun onMonthSelected(picker: WheelMonthPicker?, monthIndex: Int, monthName: String?)
    }
}