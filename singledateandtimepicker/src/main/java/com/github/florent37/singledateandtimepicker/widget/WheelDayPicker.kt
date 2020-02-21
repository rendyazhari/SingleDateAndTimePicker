package com.github.florent37.singledateandtimepicker.widget

import android.content.Context
import android.util.AttributeSet
import com.github.florent37.singledateandtimepicker.DateHelper
import com.github.florent37.singledateandtimepicker.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import com.github.florent37.singledateandtimepicker.widget.SingleDateAndTimeConstants.DAYS_PADDING

class WheelDayPicker : WheelPicker<String?> {
    private var simpleDateFormat: SimpleDateFormat? = null
    private var customDateFormat: SimpleDateFormat? = null
    private var onDaySelectedListener: OnDaySelectedListener? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun initClass() {
        simpleDateFormat = SimpleDateFormat(DAY_FORMAT_PATTERN, currentLocale)
        simpleDateFormat?.timeZone = DateHelper.timeZone
    }

    override fun setCustomLocale(customLocale: Locale) {
        super.setCustomLocale(customLocale)
        simpleDateFormat = SimpleDateFormat(DAY_FORMAT_PATTERN, currentLocale)
        simpleDateFormat?.timeZone = DateHelper.timeZone
    }

    override fun initDefault(): String = todayText.orEmpty()

    var todayText: String?
        get() = getLocalizedString(R.string.picker_today)
        set(todayText) {
            val index = adapter.data.indexOf(todayText)
            if (index != -1) {
                adapter.data[index] = todayText
                notifyDatasetChanged()
            }
        }

    override fun onItemSelected(position: Int, item: String?) {
        onDaySelectedListener?.onDaySelected(this, position, item, convertItemToDate(position))
    }

    override fun generateAdapterValues(): List<String> {
        val days: MutableList<String> = ArrayList()
        var instance = Calendar.getInstance()
        instance.timeZone = DateHelper.timeZone
        instance.add(Calendar.DATE, -1 * DAYS_PADDING - 1)
        for (i in -1 * DAYS_PADDING..-1) {
            instance.add(Calendar.DAY_OF_MONTH, 1)
            days.add(getFormattedValue(instance.time))
        }
        //today
        days.add(todayText.orEmpty())
        instance = Calendar.getInstance()
        instance.timeZone = DateHelper.timeZone
        for (i in 0 until DAYS_PADDING) {
            instance.add(Calendar.DATE, 1)
            days.add(getFormattedValue(instance.time))
        }
        return days
    }

    override fun getFormattedValue(value: Any): String = dateFormat?.format(value).orEmpty()

    fun setDayFormatter(simpleDateFormat: SimpleDateFormat): WheelDayPicker {
        simpleDateFormat.timeZone = DateHelper.timeZone
        customDateFormat = simpleDateFormat
        updateAdapter()
        return this
    }

    fun setOnDaySelectedListener(onDaySelectedListener: OnDaySelectedListener?) {
        this.onDaySelectedListener = onDaySelectedListener
    }

    val currentDate: Date?
        get() = convertItemToDate(super.currentItemPosition)

    private val dateFormat: SimpleDateFormat?
        private get() = if (customDateFormat != null) {
            customDateFormat
        } else simpleDateFormat

    private fun convertItemToDate(itemPosition: Int): Date? {
        var date: Date? = null
        val itemText = adapter.getItemText(itemPosition)
        val todayCalendar = Calendar.getInstance()
        todayCalendar.timeZone = DateHelper.timeZone
        val todayPosition = adapter.data.indexOf(todayText)
        if (todayText == itemText) {
            date = todayCalendar.time
        } else {
            try {
                date = dateFormat?.parse(itemText)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
        if (date != null) { //try to know the year
            val dateCalendar = DateHelper.getCalendarOfDate(date)
            todayCalendar.add(Calendar.DATE, itemPosition - todayPosition)
            dateCalendar[Calendar.YEAR] = todayCalendar[Calendar.YEAR]
            date = dateCalendar.time
        }
        return date
    }

    interface OnDaySelectedListener {
        fun onDaySelected(picker: WheelDayPicker?, position: Int, name: String?, date: Date?)
    }

    companion object {
        private const val DAY_FORMAT_PATTERN = "EEE d MMM"
    }
}