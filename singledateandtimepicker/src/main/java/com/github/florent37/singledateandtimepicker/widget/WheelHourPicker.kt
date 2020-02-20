package com.github.florent37.singledateandtimepicker.widget

import android.content.Context
import android.util.AttributeSet
import com.github.florent37.singledateandtimepicker.DateHelper
import java.util.*

class WheelHourPicker : WheelPicker<String?> {
    private var minHour = 0
    private var maxHour = 0
    private var hoursStep = 0
    @JvmField
    var isAmPm = false
    private var finishedLoopListener: FinishedLoopListener? = null
    private var hourChangedListener: OnHourChangedListener? = null

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    override fun init() {
        isAmPm = false
        minHour = SingleDateAndTimeConstants.MIN_HOUR_DEFAULT
        maxHour = SingleDateAndTimeConstants.MAX_HOUR_DEFAULT
        hoursStep = SingleDateAndTimeConstants.STEP_HOURS_DEFAULT
    }

    override fun initDefault(): String {
        return DateHelper.getHour(DateHelper.today(), isAmPm).toString()
    }

    override fun generateAdapterValues(): List<String> {
        val hours: MutableList<String> = ArrayList()
        if (isAmPm) {
            hours.add(getFormattedValue(12))
            var hour = hoursStep
            while (hour < maxHour) {
                hours.add(getFormattedValue(hour))
                hour += hoursStep
            }
        } else {
            var hour = minHour
            while (hour <= maxHour) {
                hours.add(getFormattedValue(hour))
                hour += hoursStep
            }
        }
        return hours
    }

    override fun findIndexOfDate(date: Date): Int {
        if (isAmPm) {
            val hours = date.hours
            if (hours >= SingleDateAndTimeConstants.MAX_HOUR_AM_PM) {
                val copy = Date(date.time)
                copy.hours = hours % SingleDateAndTimeConstants.MAX_HOUR_AM_PM
                return super.findIndexOfDate(copy)
            }
        }
        return super.findIndexOfDate(date)
    }

    override fun getFormattedValue(value: Any): String {
        var valueItem = value
        if (value is Date) {
            val instance = Calendar.getInstance()
            instance.timeZone = DateHelper.timeZone
            instance.time = value
            valueItem = instance[Calendar.HOUR_OF_DAY]
        }
        return String.format(currentLocale, FORMAT, valueItem)
    }

    override fun setDefault(defaultValue: String?) {
        try {
            var hour = defaultValue?.toInt() ?: 0
            if (isAmPm && hour >= SingleDateAndTimeConstants.MAX_HOUR_AM_PM) {
                hour -= SingleDateAndTimeConstants.MAX_HOUR_AM_PM
            }
            super.setDefault(getFormattedValue(hour))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setIsAmPm(isAmPm: Boolean) {
        this.isAmPm = isAmPm
        if (isAmPm) {
            setMaxHour(SingleDateAndTimeConstants.MAX_HOUR_AM_PM)
        } else {
            setMaxHour(SingleDateAndTimeConstants.MAX_HOUR_DEFAULT)
        }
        updateAdapter()
    }

    fun setMaxHour(maxHour: Int) {
        if (maxHour >= SingleDateAndTimeConstants.MIN_HOUR_DEFAULT && maxHour <= SingleDateAndTimeConstants.MAX_HOUR_DEFAULT) {
            this.maxHour = maxHour
        }
        notifyDatasetChanged()
    }

    fun setMinHour(minHour: Int) {
        if (minHour >= SingleDateAndTimeConstants.MIN_HOUR_DEFAULT && minHour <= SingleDateAndTimeConstants.MAX_HOUR_DEFAULT) {
            this.minHour = minHour
        }
        notifyDatasetChanged()
    }

    fun setHoursStep(hoursStep: Int) {
        if (hoursStep >= SingleDateAndTimeConstants.MIN_HOUR_DEFAULT && hoursStep <= SingleDateAndTimeConstants.MAX_HOUR_DEFAULT) {
            this.hoursStep = hoursStep
        }
        notifyDatasetChanged()
    }

    private fun convertItemToHour(item: Any): Int {
        var hour = Integer.valueOf(item.toString())
        if (!isAmPm) {
            return hour
        }
        if (hour == 12) {
            hour = 0
        }
        return hour
    }

    val currentHour: Int
        get() = adapter.getItem(currentItemPosition)?.let { convertItemToHour(it) } ?: 0

    override fun onItemSelected(position: Int, item: String?) {
        super.onItemSelected(position, item)
        hourChangedListener?.onHourChanged(this, convertItemToHour(item.orEmpty()))
    }

    fun setOnFinishedLoopListener(finishedLoopListener: FinishedLoopListener?): WheelHourPicker {
        this.finishedLoopListener = finishedLoopListener
        return this
    }

    fun setHourChangedListener(hourChangedListener: OnHourChangedListener?): WheelHourPicker {
        this.hourChangedListener = hourChangedListener
        return this
    }

    override fun onFinishedLoop() {
        super.onFinishedLoop()
        if (finishedLoopListener != null) {
            finishedLoopListener?.onFinishedLoop(this)
        }
    }

    interface FinishedLoopListener {
        fun onFinishedLoop(picker: WheelHourPicker?)
    }

    interface OnHourChangedListener {
        fun onHourChanged(picker: WheelHourPicker?, hour: Int)
    }
}