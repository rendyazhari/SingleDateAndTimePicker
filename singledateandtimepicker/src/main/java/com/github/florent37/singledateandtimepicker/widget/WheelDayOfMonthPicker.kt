package com.github.florent37.singledateandtimepicker.widget

import android.content.Context
import android.util.AttributeSet
import com.github.florent37.singledateandtimepicker.DateHelper
import java.util.*

class WheelDayOfMonthPicker @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null) : WheelPicker<String?>(context, attrs) {
    var daysInMonth = 0
    private var selectedListener: DayOfMonthSelectedListener? = null
    private var finishedLoopListener: FinishedLoopListener? = null
    override fun initClass() { // no-op here
    }

    override fun generateAdapterValues(): List<String> {
        val dayList: MutableList<String> = ArrayList()
        for (i in 1..daysInMonth) {
            dayList.add(String.format("%02d", i))
        }
        return dayList
    }

    override fun initDefault(): String =  DateHelper.getDay(DateHelper.today()).toString()

    fun setOnFinishedLoopListener(finishedLoopListener: FinishedLoopListener?) {
        this.finishedLoopListener = finishedLoopListener
    }

    override fun onFinishedLoop() {
        super.onFinishedLoop()
        finishedLoopListener?.onFinishedLoop(this)
    }

    fun setDayOfMonthSelectedListener(listener: DayOfMonthSelectedListener) {
        this.selectedListener = listener
    }

    override fun onItemSelected(position: Int, item: String?) {
        selectedListener?.onDayOfMonthSelected(this, position)
    }

    val currentDay: Int
        get() = currentItemPosition

    interface FinishedLoopListener {
        fun onFinishedLoop(picker: WheelDayOfMonthPicker?)
    }

    interface DayOfMonthSelectedListener {
        fun onDayOfMonthSelected(picker: WheelDayOfMonthPicker?, dayIndex: Int)
    }
}