package com.github.florent37.singledateandtimepicker.dialog

import android.content.Context
import android.support.annotation.ColorInt
import android.view.View
import android.widget.TextView
import com.github.florent37.singledateandtimepicker.DateHelper
import com.github.florent37.singledateandtimepicker.R
import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker
import com.github.florent37.singledateandtimepicker.widget.SingleDateAndTimeConstants
import java.text.SimpleDateFormat
import java.util.*

class SingleDateAndTimePickerDialog private constructor(context: Context, bottomSheet: Boolean = false) : BaseDialog() {
    private var listener: Listener? = null
    private val bottomSheetHelper: BottomSheetHelper
    private var picker: SingleDateAndTimePicker? = null
    private var title: String? = null
    private var titleTextSize: Int? = null
    private var bottomSheetHeight: Int? = null
    private var todayText: String? = null
    private var displayListener: DisplayListener? = null

    init {
        val layout = if (bottomSheet) R.layout.bottom_sheet_picker_bottom_sheet else R.layout.bottom_sheet_picker
        bottomSheetHelper = BottomSheetHelper(context, layout)
        bottomSheetHelper.setListener(object : BottomSheetHelper.Listener {
            override fun onOpen() {}
            override fun onLoaded(view: View) {
                initClass(view)
                displayListener?.onDisplayed(picker)
            }

            override fun onClose() {
                this@SingleDateAndTimePickerDialog.onClose()
            }
        })
    }

    private fun initClass(view: View) {
        picker = view.findViewById<View>(R.id.picker) as SingleDateAndTimePicker
        val pickerSetter = picker ?: return

        bottomSheetHeight?.let {
            val params = pickerSetter.layoutParams
            params.height = it
            pickerSetter.layoutParams = params
        }

        view.findViewById<TextView>(R.id.buttonOk).run {
            setOnClickListener {
                okClicked = true
                close()
            }
            mainColor?.let { setTextColor(it) }
            titleTextSize?.let { textSize = it.toFloat() }
        }

        view.findViewById<View>(R.id.sheetContentLayout).run {
            setOnClickListener { }
            backgroundColor?.let { setBackgroundColor(it) }
        }

        view.findViewById<TextView>(R.id.sheetTitle).run {
            text = title
            titleTextColor?.let { setTextColor(it) }
            titleTextSize?.let { textSize = it.toFloat() }
        }

        setTodayText(todayText)

        view.findViewById<View>(R.id.pickerTitleHeader).run {
            mainColor?.let { setBackgroundColor(it) }
        }

        with(pickerSetter) {
            if (curved) {
                setCurved(true)
                setVisibleItemCount(7)
            } else {
                setCurved(false)
                setVisibleItemCount(5)
            }
            setMustBeOnFuture(mustBeOnFuture)
            setStepMinutes(minutesStep)

            dayFormatter?.let { setDayFormatter(it) }
            customLocale?.let { setCustomLocale(it) }
            mainColor?.let { setSelectedTextColor(it) }

            minDate?.let { setMinDate(it) }
            maxDate?.let { setMaxDate(it) }

            defaultDate?.let { setDefaultDate(it) }
            isAmPm?.let { setIsAmPm(it) }

            setDisplayDays(displayDays)
            setDisplayYears(displayYears)
            setDisplayMonths(displayMonth)
            setDisplayDaysOfMonth(displayDaysOfMonth)
            setDisplayMinutes(displayMinutes)
            setDisplayHours(displayHours)
            setDisplayMonthNumbers(displayMonthNumbers)
        }
    }

    fun setListener(listener: Listener?): SingleDateAndTimePickerDialog {
        this.listener = listener
        return this
    }

    fun setCurved(curved: Boolean): SingleDateAndTimePickerDialog {
        this.curved = curved
        return this
    }

    fun setMinutesStep(minutesStep: Int): SingleDateAndTimePickerDialog {
        this.minutesStep = minutesStep
        return this
    }

    private fun setDisplayListener(displayListener: DisplayListener) {
        this.displayListener = displayListener
    }

    fun setTitle(title: String?): SingleDateAndTimePickerDialog {
        this.title = title
        return this
    }

    fun setTitleTextSize(titleTextSize: Int?): SingleDateAndTimePickerDialog {
        this.titleTextSize = titleTextSize
        return this
    }

    fun setBottomSheetHeight(bottomSheetHeight: Int?): SingleDateAndTimePickerDialog {
        this.bottomSheetHeight = bottomSheetHeight
        return this
    }

    fun setTodayText(todayText: String?): SingleDateAndTimePickerDialog {
        this.todayText = todayText
        return this
    }

    fun setMustBeOnFuture(mustBeOnFuture: Boolean): SingleDateAndTimePickerDialog {
        this.mustBeOnFuture = mustBeOnFuture
        return this
    }

    fun setMinDateRange(minDate: Date?): SingleDateAndTimePickerDialog {
        this.minDate = minDate
        return this
    }

    fun setMaxDateRange(maxDate: Date?): SingleDateAndTimePickerDialog {
        this.maxDate = maxDate
        return this
    }

    fun setDefaultDate(defaultDate: Date?): SingleDateAndTimePickerDialog {
        this.defaultDate = defaultDate
        return this
    }

    fun setDisplayDays(displayDays: Boolean): SingleDateAndTimePickerDialog {
        this.displayDays = displayDays
        return this
    }

    fun setDisplayMinutes(displayMinutes: Boolean): SingleDateAndTimePickerDialog {
        this.displayMinutes = displayMinutes
        return this
    }

    fun setDisplayMonthNumbers(displayMonthNumbers: Boolean): SingleDateAndTimePickerDialog {
        this.displayMonthNumbers = displayMonthNumbers
        return this
    }

    fun setDisplayHours(displayHours: Boolean): SingleDateAndTimePickerDialog {
        this.displayHours = displayHours
        return this
    }

    fun setDisplayDaysOfMonth(displayDaysOfMonth: Boolean): SingleDateAndTimePickerDialog {
        this.displayDaysOfMonth = displayDaysOfMonth
        return this
    }

    private fun setDisplayMonth(displayMonth: Boolean): SingleDateAndTimePickerDialog {
        this.displayMonth = displayMonth
        return this
    }

    private fun setDisplayYears(displayYears: Boolean): SingleDateAndTimePickerDialog {
        this.displayYears = displayYears
        return this
    }

    fun setDayFormatter(dayFormatter: SimpleDateFormat?): SingleDateAndTimePickerDialog {
        this.dayFormatter = dayFormatter
        return this
    }

    fun setCustomLocale(locale: Locale?): SingleDateAndTimePickerDialog {
        customLocale = locale
        return this
    }

    fun setIsAmPm(isAmPm: Boolean): SingleDateAndTimePickerDialog {
        this.isAmPm = java.lang.Boolean.valueOf(isAmPm)
        return this
    }

    override fun display() {
        super.display()
        bottomSheetHelper.display()
    }

    override fun close() {
        super.close()
        bottomSheetHelper.hide()
        if (okClicked) {
            picker?.date?.let { listener?.onDateSelected(it) }
        }
    }

    override fun dismiss() {
        super.dismiss()
        bottomSheetHelper.dismiss()
    }

    interface Listener {
        fun onDateSelected(date: Date?)
    }

    interface DisplayListener {
        fun onDisplayed(picker: SingleDateAndTimePicker?)
    }

    class Builder(private val context: Context) {
        private var dialog: SingleDateAndTimePickerDialog? = null
        private var listener: Listener? = null
        private var displayListener: DisplayListener? = null
        private var title: String? = null
        private var titleTextSize: Int? = null
        private var bottomSheetHeight: Int? = null
        private var todayText: String? = null
        private var bottomSheet = false
        private var curved = false
        private var mustBeOnFuture = false
        private var minutesStep: Int = SingleDateAndTimeConstants.STEP_MINUTES_DEFAULT
        private var displayDays = true
        private var displayMinutes = true
        private var displayHours = true
        private var displayMonth = false
        private var displayDaysOfMonth = false
        private var displayYears = false
        private var displayMonthNumbers = false
        private var isAmPm: Boolean? = null
        @ColorInt
        private var backgroundColor: Int? = null
        @ColorInt
        private var mainColor: Int? = null
        @ColorInt
        private var titleTextColor: Int? = null
        private var minDate: Date? = null
        private var maxDate: Date? = null
        private var defaultDate: Date? = null
        private var dayFormatter: SimpleDateFormat? = null
        private var customLocale: Locale? = null

        fun title(title: String?): Builder {
            this.title = title
            return this
        }

        fun titleTextSize(titleTextSize: Int?): Builder {
            this.titleTextSize = titleTextSize
            return this
        }

        fun bottomSheetHeight(bottomSheetHeight: Int?): Builder {
            this.bottomSheetHeight = bottomSheetHeight
            return this
        }

        fun todayText(todayText: String?): Builder {
            this.todayText = todayText
            return this
        }

        fun bottomSheet(): Builder {
            bottomSheet = true
            return this
        }

        fun curved(): Builder {
            curved = true
            return this
        }

        fun mustBeOnFuture(): Builder {
            mustBeOnFuture = true
            return this
        }

        fun minutesStep(minutesStep: Int): Builder {
            this.minutesStep = minutesStep
            return this
        }

        fun displayDays(displayDays: Boolean): Builder {
            this.displayDays = displayDays
            return this
        }

        fun displayAmPm(isAmPm: Boolean): Builder {
            this.isAmPm = isAmPm
            return this
        }

        fun displayMinutes(displayMinutes: Boolean): Builder {
            this.displayMinutes = displayMinutes
            return this
        }

        fun displayHours(displayHours: Boolean): Builder {
            this.displayHours = displayHours
            return this
        }

        fun displayDaysOfMonth(displayDaysOfMonth: Boolean): Builder {
            this.displayDaysOfMonth = displayDaysOfMonth
            return this
        }

        fun displayMonth(displayMonth: Boolean): Builder {
            this.displayMonth = displayMonth
            return this
        }

        fun displayYears(displayYears: Boolean): Builder {
            this.displayYears = displayYears
            return this
        }

        fun listener(listener: Listener?): Builder {
            this.listener = listener
            return this
        }

        fun displayListener(displayListener: DisplayListener?): Builder {
            this.displayListener = displayListener
            return this
        }

        fun titleTextColor(@ColorInt titleTextColor: Int): Builder {
            this.titleTextColor = titleTextColor
            return this
        }

        fun backgroundColor(@ColorInt backgroundColor: Int): Builder {
            this.backgroundColor = backgroundColor
            return this
        }

        fun mainColor(@ColorInt mainColor: Int): Builder {
            this.mainColor = mainColor
            return this
        }

        fun minDateRange(minDate: Date?): Builder {
            this.minDate = minDate
            return this
        }

        fun maxDateRange(maxDate: Date?): Builder {
            this.maxDate = maxDate
            return this
        }

        fun displayMonthNumbers(displayMonthNumbers: Boolean): Builder {
            this.displayMonthNumbers = displayMonthNumbers
            return this
        }

        fun defaultDate(defaultDate: Date?): Builder {
            this.defaultDate = defaultDate
            return this
        }

        fun setDayFormatter(dayFormatter: SimpleDateFormat?): Builder {
            this.dayFormatter = dayFormatter
            return this
        }

        fun customLocale(locale: Locale?): Builder {
            customLocale = locale
            return this
        }

        fun setTimeZone(timeZone: TimeZone?): Builder {
            DateHelper.timeZone = timeZone
            return this
        }

        fun build(): SingleDateAndTimePickerDialog {
            val dialog = SingleDateAndTimePickerDialog(context, bottomSheet)
                    .setTitle(title)
                    .setTitleTextSize(titleTextSize)
                    .setBottomSheetHeight(bottomSheetHeight)
                    .setTodayText(todayText)
                    .setListener(listener)
                    .setCurved(curved)
                    .setMinutesStep(minutesStep)
                    .setMaxDateRange(maxDate)
                    .setMinDateRange(minDate)
                    .setDefaultDate(defaultDate)
                    .setDisplayHours(displayHours)
                    .setDisplayMonth(displayMonth)
                    .setDisplayYears(displayYears)
                    .setDisplayDaysOfMonth(displayDaysOfMonth)
                    .setDisplayMinutes(displayMinutes)
                    .setDisplayMonthNumbers(displayMonthNumbers)
                    .setDisplayDays(displayDays)
                    .setDayFormatter(dayFormatter)
                    .setCustomLocale(customLocale)
                    .setMustBeOnFuture(mustBeOnFuture)

            mainColor?.let { dialog.mainColor = it }
            backgroundColor?.let { dialog.backgroundColor = it }
            titleTextColor?.let { dialog.titleTextColor = it }
            displayListener?.let { dialog.setDisplayListener(it) }
            isAmPm?.let { dialog.setIsAmPm(it) }

            return dialog
        }

        fun display() {
            dialog = build()
            dialog?.display()
        }

        fun close() {
            dialog?.close()
        }

        fun dismiss() {
            dialog?.dismiss()
        }

    }
}