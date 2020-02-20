package com.github.florent37.singledateandtimepicker.widget

import android.content.Context
import android.util.AttributeSet
import com.github.florent37.singledateandtimepicker.DateHelper
import java.util.*

class WheelMinutePicker : WheelPicker<String?> {
    private var stepMinutes = 0
    private var onMinuteChangedListener: OnMinuteChangedListener? = null
    private var onFinishedLoopListener: OnFinishedLoopListener? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    override fun init() {
        stepMinutes = SingleDateAndTimeConstants.STEP_MINUTES_DEFAULT
    }

    override fun initDefault(): String {
        val now = Calendar.getInstance()
        now.timeZone = DateHelper.timeZone
        return getFormattedValue(now[Calendar.MINUTE])
    }

    override fun generateAdapterValues(): List<String> {
        val minutes: MutableList<String> = ArrayList()
        var min: Int = SingleDateAndTimeConstants.MIN_MINUTES
        while (min <= SingleDateAndTimeConstants.MAX_MINUTES) {
            minutes.add(getFormattedValue(min))
            min += stepMinutes
        }
        return minutes
    }

    private fun findIndexOfMinute(minute: Int): Int {
        val itemCount = adapter.itemCount
        for (i in 0 until itemCount) {
            val `object` = adapter.getItemText(i)
            val value = Integer.valueOf(`object`)
            if (minute == value) {
                return i
            }
            if (minute < value) {
                return i - 1
            }
        }
        return itemCount - 1
    }

    override fun findIndexOfDate(date: Date): Int {
        return findIndexOfMinute(DateHelper.getMinuteOf(date))
    }

    override fun getFormattedValue(value: Any): String {
        var valueItem = value
        if (value is Date) {
            val instance = Calendar.getInstance()
            instance.timeZone = DateHelper.timeZone
            instance.time = value
            valueItem = instance[Calendar.MINUTE]
        }
        return String.format(currentLocale, FORMAT, valueItem)
    }

    fun setStepMinutes(stepMinutes: Int) {
        if (stepMinutes in 1..59) {
            this.stepMinutes = stepMinutes
            updateAdapter()
        }
    }

    private fun convertItemToMinute(item: Any): Int {
        return Integer.valueOf(item.toString())
    }

    val currentMinute: Int
        get() = adapter.getItem(currentItemPosition)?.let { convertItemToMinute(it) } ?: 0

    fun setOnMinuteChangedListener(onMinuteChangedListener: OnMinuteChangedListener?): WheelMinutePicker {
        this.onMinuteChangedListener = onMinuteChangedListener
        return this
    }

    fun setOnFinishedLoopListener(onFinishedLoopListener: OnFinishedLoopListener?): WheelMinutePicker {
        this.onFinishedLoopListener = onFinishedLoopListener
        return this
    }

    override fun onItemSelected(position: Int, item: String?) {
        super.onItemSelected(position, item)
        onMinuteChangedListener?.onMinuteChanged(this, convertItemToMinute(item.orEmpty()))
    }

    override fun onFinishedLoop() {
        super.onFinishedLoop()
        onFinishedLoopListener?.onFinishedLoop(this)
    }

    interface OnMinuteChangedListener {
        fun onMinuteChanged(picker: WheelMinutePicker?, minutes: Int)
    }

    interface OnFinishedLoopListener {
        fun onFinishedLoop(picker: WheelMinutePicker?)
    }
}