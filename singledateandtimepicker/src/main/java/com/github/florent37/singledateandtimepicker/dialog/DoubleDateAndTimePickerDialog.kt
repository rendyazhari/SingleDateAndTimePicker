package com.github.florent37.singledateandtimepicker.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.StateListDrawable
import android.support.annotation.ColorInt
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import com.github.florent37.singledateandtimepicker.DateHelper
import com.github.florent37.singledateandtimepicker.R
import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker
import com.github.florent37.singledateandtimepicker.widget.SingleDateAndTimeConstants
import java.text.SimpleDateFormat
import java.util.*


class DoubleDateAndTimePickerDialog(context: Context, bottomSheet: Boolean = false) : BaseDialog() {
    private var listener: Listener? = null
    private val bottomSheetHelper: BottomSheetHelper
    private var buttonTab0: TextView? = null
    private var buttonTab1: TextView? = null
    private var pickerTab0: SingleDateAndTimePicker? = null
    private var pickerTab1: SingleDateAndTimePicker? = null
    private var tab0: View? = null
    private var tab1: View? = null
    private var tab0Text: String? = null
    private var tab1Text: String? = null
    private var title: String? = null
    private var titleTextSize: Int? = null
    private var bottomSheetHeight: Int? = null
    private var todayText: String? = null
    private var buttonOkText: String? = null
    private var tab0Date: Date? = null
    private var tab1Date: Date? = null
    private var secondDateAfterFirst = false
    private var tab0Days = false
    private var tab0Hours = false
    private var tab0Minutes = false
    private var tab1Days = false
    private var tab1Hours = false
    private var tab1Minutes = false

    interface Listener {
        fun onDateSelected(dates: List<Date>)
    }

    init {
        val layout: Int = if (bottomSheet) R.layout.bottom_sheet_double_picker_bottom_sheet
        else R.layout.bottom_sheet_double_picker
        bottomSheetHelper = BottomSheetHelper(context, layout).apply {
            setListener(object : BottomSheetHelper.Listener {
                override fun onOpen() {}
                override fun onLoaded(view: View) {
                    initClass(view)
                }

                override fun onClose() {
                    this@DoubleDateAndTimePickerDialog.onClose()
                }
            })
        }
    }

    private fun initClass(view: View) {
        buttonTab0 = view.findViewById(R.id.buttonTab0)
        buttonTab1 = view.findViewById(R.id.buttonTab1)
        pickerTab0 = view.findViewById(R.id.picker_tab_0)
        pickerTab1 = view.findViewById(R.id.picker_tab_1)
        tab0 = view.findViewById(R.id.tab0)
        tab1 = view.findViewById(R.id.tab1)

        if (pickerTab0 != null) {
            bottomSheetHeight?.let {
                val params = pickerTab0?.layoutParams
                params?.height = bottomSheetHeight
                pickerTab0?.setLayoutParams(params)
            }
        }

        if (pickerTab1 != null) {
            bottomSheetHeight?.let {
                val params = pickerTab1?.layoutParams
                params?.height = it
                pickerTab1?.setLayoutParams(params)
            }
        }

        val titleLayout = view.findViewById<View>(R.id.sheetTitleLayout)
        val titleTextView = view.findViewById<TextView>(R.id.sheetTitle)
        if (title != null) {
            titleTextView?.run {
                titleTextView.text = title
                titleTextColor?.let { color -> setTextColor(color) }
                titleTextSize?.let { size -> textSize = size.toFloat() }
            }

            mainColor?.let {
                titleLayout?.setBackgroundColor(it)
            }
        } else {
            titleLayout.visibility = View.GONE
        }

        pickerTab0?.setTodayText(todayText)
        pickerTab1?.setTodayText(todayText)

        val sheetContentLayout = view.findViewById<View>(R.id.sheetContentLayout)
        sheetContentLayout?.run {
            setOnClickListener { }
            backgroundColor?.let { setBackgroundColor(it) }
        }

        tab1?.let { tab ->
            tab.viewTreeObserver?.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    tab.viewTreeObserver.removeOnPreDrawListener(this)
                    tab.translationX = tab.width.toFloat()
                    return false
                }
            })
        }

        buttonTab0?.isSelected = true

        tab0Text?.let { buttonTab0?.text = it }

        buttonTab0?.setOnClickListener { displayTab0() }

        tab1Text?.let { buttonTab1?.setText(it) }

        buttonTab1?.setOnClickListener { displayTab1() }

        //noinspection deprecation
        buttonTab0?.setBackgroundDrawable(getTabsListDrawable())
        //noinspection deprecation
        buttonTab1?.setBackgroundDrawable(getTabsListDrawable())

        view.findViewById<TextView>(R.id.buttonOk)?.run {
            text = buttonOkText.orEmpty()
            mainColor?.let { setTextColor(it) }
            titleTextSize?.let { textSize = it.toFloat() }

            setOnClickListener {
                if (isTab0Visible()) {
                    displayTab1()
                } else {
                    okClicked = true
                    close()
                }
            }
        }

        pickerTab0?.setCurved(curved)
        pickerTab1?.setCurved(curved)
        pickerTab0?.setVisibleItemCount(if (curved) DEFAULT_ITEM_COUNT_MODE_CURVED else DEFAULT_ITEM_COUNT_MODE_NORMAL)
        pickerTab1?.setVisibleItemCount(if (curved) DEFAULT_ITEM_COUNT_MODE_CURVED else DEFAULT_ITEM_COUNT_MODE_NORMAL)

        pickerTab0?.setDisplayDays(tab0Days)
        pickerTab0?.setDisplayHours(tab0Hours)
        pickerTab0?.setDisplayMinutes(tab0Minutes)
        pickerTab1?.setDisplayDays(tab1Days)
        pickerTab1?.setDisplayHours(tab1Hours)
        pickerTab1?.setDisplayMinutes(tab1Minutes)

        pickerTab0?.setMustBeOnFuture(mustBeOnFuture)
        pickerTab1?.setMustBeOnFuture(mustBeOnFuture)

        pickerTab0?.setStepMinutes(minutesStep)
        pickerTab1?.setStepMinutes(minutesStep)

        mainColor?.let {
            pickerTab0?.setSelectedTextColor(it)
            pickerTab1?.setSelectedTextColor(it)
        }

        minDate?.let {
            pickerTab0?.setMinDate(it)
            pickerTab1?.setMinDate(it)
        }

        maxDate?.let {
            pickerTab0?.setMaxDate(it)
            pickerTab1?.setMaxDate(it)
        }

        defaultDate?.let {
            val calendar = Calendar.getInstance().apply { time = it }
            pickerTab0?.selectDate(calendar)
            pickerTab1?.selectDate(calendar)
        }

        tab0Date?.let {
            val calendar = Calendar.getInstance().apply { time = it }
            pickerTab0?.selectDate(calendar)
        }

        tab1Date?.let {
            val calendar = Calendar.getInstance().apply { time = it }
            pickerTab1?.selectDate(calendar)
        }

        dayFormatter?.let {
            pickerTab0?.setDayFormatter(it)
            pickerTab1?.setDayFormatter(it)
        }

        customLocale?.let {
            pickerTab0?.setCustomLocale(it)
            pickerTab1?.setCustomLocale(it)
        }

        if (secondDateAfterFirst) {
            val pickerTab = pickerTab0
            pickerTab?.addOnDateChangedListener(object : SingleDateAndTimePicker.OnDateChangedListener {
                override fun onDateChanged(displayed: String?, date: Date?) {
                    pickerTab.setMinDate(date)
                    pickerTab.checkPickersMinMax()
                }
            })
        }
    }

    private fun getTabsListDrawable(): StateListDrawable {
        val colorState0 = StateListDrawable()
        colorState0.addState(intArrayOf(android.R.attr.state_selected), ColorDrawable(mainColor!!))
        colorState0.addState(intArrayOf(-android.R.attr.state_selected), ColorDrawable(backgroundColor!!))
        return colorState0
    }

    fun setTab0Text(tab0Text: String?): DoubleDateAndTimePickerDialog {
        this.tab0Text = tab0Text
        return this
    }

    fun setTab1Text(tab1Text: String?): DoubleDateAndTimePickerDialog {
        this.tab1Text = tab1Text
        return this
    }

    fun setButtonOkText(buttonOkText: String?): DoubleDateAndTimePickerDialog {
        this.buttonOkText = buttonOkText
        return this
    }

    fun setTitle(title: String?): DoubleDateAndTimePickerDialog {
        this.title = title
        return this
    }

    fun setTitleTextSize(titleTextSize: Int?): DoubleDateAndTimePickerDialog {
        this.titleTextSize = titleTextSize
        return this
    }

    fun setBottomSheetHeight(bottomSheetHeight: Int?): DoubleDateAndTimePickerDialog {
        this.bottomSheetHeight = bottomSheetHeight
        return this
    }

    fun setTodayText(todayText: String?): DoubleDateAndTimePickerDialog {
        this.todayText = todayText
        return this
    }

    fun setListener(listener: Listener?): DoubleDateAndTimePickerDialog {
        this.listener = listener
        return this
    }

    fun setCurved(curved: Boolean): DoubleDateAndTimePickerDialog {
        this.curved = curved
        return this
    }

    fun setMinutesStep(minutesStep: Int): DoubleDateAndTimePickerDialog {
        this.minutesStep = minutesStep
        return this
    }

    fun setMustBeOnFuture(mustBeOnFuture: Boolean): DoubleDateAndTimePickerDialog {
        this.mustBeOnFuture = mustBeOnFuture
        return this
    }

    fun setMinDateRange(minDate: Date?): DoubleDateAndTimePickerDialog {
        this.minDate = minDate
        return this
    }

    fun setMaxDateRange(maxDate: Date?): DoubleDateAndTimePickerDialog {
        this.maxDate = maxDate
        return this
    }

    fun setDefaultDate(defaultDate: Date?): DoubleDateAndTimePickerDialog {
        this.defaultDate = defaultDate
        return this
    }

    fun setDayFormatter(dayFormatter: SimpleDateFormat?): DoubleDateAndTimePickerDialog {
        this.dayFormatter = dayFormatter
        return this
    }

    fun setCustomLocale(locale: Locale?): DoubleDateAndTimePickerDialog {
        customLocale = locale
        return this
    }

    fun setTab0Date(tab0Date: Date?): DoubleDateAndTimePickerDialog {
        this.tab0Date = tab0Date
        return this
    }

    fun setTab1Date(tab1Date: Date?): DoubleDateAndTimePickerDialog {
        this.tab1Date = tab1Date
        return this
    }

    fun setSecondDateAfterFirst(secondDateAfterFirst: Boolean): DoubleDateAndTimePickerDialog {
        this.secondDateAfterFirst = secondDateAfterFirst
        return this
    }

    fun setTab0DisplayDays(tab0Days: Boolean): DoubleDateAndTimePickerDialog {
        this.tab0Days = tab0Days
        return this
    }

    fun setTab0DisplayHours(tab0Hours: Boolean): DoubleDateAndTimePickerDialog {
        this.tab0Hours = tab0Hours
        return this
    }

    fun setTab0DisplayMinutes(tab0Minutes: Boolean): DoubleDateAndTimePickerDialog {
        this.tab0Minutes = tab0Minutes
        return this
    }

    fun setTab1DisplayDays(tab1Days: Boolean): DoubleDateAndTimePickerDialog {
        this.tab1Days = tab1Days
        return this
    }

    fun setTab1DisplayHours(tab1Hours: Boolean): DoubleDateAndTimePickerDialog {
        this.tab1Hours = tab1Hours
        return this
    }

    fun setTab1DisplayMinutes(tab1Minutes: Boolean): DoubleDateAndTimePickerDialog {
        this.tab1Minutes = tab1Minutes
        return this
    }

    override fun display() {
        super.display()
        bottomSheetHelper.display()
    }

    override fun dismiss() {
        super.dismiss()
        bottomSheetHelper.dismiss()
    }

    override fun close() {
        super.close()
        bottomSheetHelper.hide()
    }

    override fun onClose() {
        super.onClose()
        if (okClicked) {
            val pickerDate0 = pickerTab0?.date ?: return
            val pickerDate1 = pickerTab1?.date ?: return

            listener?.onDateSelected(listOf(pickerDate0, pickerDate1))
        }
    }

    private fun displayTab0() {
        if (isTab0Visible()) return
        buttonTab0?.isSelected = true
        buttonTab1?.isSelected = false
        tab0?.animate()?.translationX(0f)
        tab1?.width?.toFloat()?.let {
            tab1?.animate()?.translationX(it)
        }
    }

    private fun displayTab1() {
        if (!isTab0Visible()) return
        buttonTab0?.isSelected = false
        buttonTab1?.isSelected = true

        tab0?.width?.toFloat()?.let {
            tab0!!.animate().translationX(-it)
        }
    }

    private fun isTab0Visible(): Boolean = tab0?.translationX == 0f

    class Builder(private val context: Context) {

        private var listener: Listener? = null
        private var bottomSheet = false
        private var dialog: DoubleDateAndTimePickerDialog? = null
        private var tab0Text: String? = null
        private var tab1Text: String? = null
        private var title: String? = null
        private var titleTextSize: Int? = null
        private var bottomSheetHeight: Int? = null
        private var buttonOkText: String? = null
        private var todayText: String? = null
        private var curved = false
        private var secondDateAfterFirst = false
        private var mustBeOnFuture = false
        private var minutesStep: Int = SingleDateAndTimeConstants.STEP_MINUTES_DEFAULT
        private var dayFormatter: SimpleDateFormat? = null
        private var customLocale: Locale? = null
        @ColorInt

        private var backgroundColor: Int? = null
        @ColorInt

        private var mainColor: Int? = null
        @ColorInt

        private var titleTextColor: Int? = null

        private var minDate: Date? = null

        private var maxDate: Date? = null

        private var defaultDate: Date? = null

        private var tab0Date: Date? = null

        private var tab1Date: Date? = null
        private var tab0Days = true
        private var tab0Hours = true
        private var tab0Minutes = true
        private var tab1Days = true
        private var tab1Hours = true
        private var tab1Minutes = true
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

        fun dayFormatter(dayFormatter: SimpleDateFormat?): Builder {
            this.dayFormatter = dayFormatter
            return this
        }

        fun customLocale(locale: Locale?): Builder {
            customLocale = locale
            return this
        }

        fun minutesStep(minutesStep: Int): Builder {
            this.minutesStep = minutesStep
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

        fun defaultDate(defaultDate: Date?): Builder {
            this.defaultDate = defaultDate
            return this
        }

        fun tab0Date(tab0Date: Date?): Builder {
            this.tab0Date = tab0Date
            return this
        }

        fun tab1Date(tab1Date: Date?): Builder {
            this.tab1Date = tab1Date
            return this
        }

        fun listener(listener: Listener?): Builder {
            this.listener = listener
            return this
        }

        fun tab1Text(tab1Text: String?): Builder {
            this.tab1Text = tab1Text
            return this
        }

        fun tab0Text(tab0Text: String?): Builder {
            this.tab0Text = tab0Text
            return this
        }

        fun buttonOkText(buttonOkText: String?): Builder {
            this.buttonOkText = buttonOkText
            return this
        }

        fun secondDateAfterFirst(secondDateAfterFirst: Boolean): Builder {
            this.secondDateAfterFirst = secondDateAfterFirst
            return this
        }

        fun setTab0DisplayDays(tab0Days: Boolean): Builder {
            this.tab0Days = tab0Days
            return this
        }

        fun setTab0DisplayHours(tab0Hours: Boolean): Builder {
            this.tab0Hours = tab0Hours
            return this
        }

        fun setTab0DisplayMinutes(tab0Minutes: Boolean): Builder {
            this.tab0Minutes = tab0Minutes
            return this
        }

        fun setTab1DisplayDays(tab1Days: Boolean): Builder {
            this.tab1Days = tab1Days
            return this
        }

        fun setTab1DisplayHours(tab1Hours: Boolean): Builder {
            this.tab1Hours = tab1Hours
            return this
        }

        fun setTab1DisplayMinutes(tab1Minutes: Boolean): Builder {
            this.tab1Minutes = tab1Minutes
            return this
        }

        fun setTimeZone(timeZone: TimeZone?): Builder {
            DateHelper.timeZone = timeZone
            return this
        }

        fun build(): DoubleDateAndTimePickerDialog? {
            val dialog = DoubleDateAndTimePickerDialog(context, bottomSheet)
                    .setTitle(title)
                    .setTitleTextSize(titleTextSize)
                    .setBottomSheetHeight(bottomSheetHeight)
                    .setTodayText(todayText)
                    .setListener(listener)
                    .setCurved(curved)
                    .setButtonOkText(buttonOkText)
                    .setTab0Text(tab0Text)
                    .setTab1Text(tab1Text)
                    .setMinutesStep(minutesStep)
                    .setMaxDateRange(maxDate)
                    .setMinDateRange(minDate)
                    .setDefaultDate(defaultDate)
                    .setTab0DisplayDays(tab0Days)
                    .setTab0DisplayHours(tab0Hours)
                    .setTab0DisplayMinutes(tab0Minutes)
                    .setTab1DisplayDays(tab1Days)
                    .setTab1DisplayHours(tab1Hours)
                    .setTab1DisplayMinutes(tab1Minutes)
                    .setTab0Date(tab0Date)
                    .setTab1Date(tab1Date)
                    .setDayFormatter(dayFormatter)
                    .setCustomLocale(customLocale)
                    .setMustBeOnFuture(mustBeOnFuture)
                    .setSecondDateAfterFirst(secondDateAfterFirst)

            mainColor?.let { dialog.mainColor = it }
            backgroundColor?.let { dialog.backgroundColor = it }
            titleTextColor?.let { dialog.titleTextColor = it }

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