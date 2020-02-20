package com.github.florent37.sample.singledateandtimepicker

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.github.florent37.singledateandtimepicker.dialog.DoubleDateAndTimePickerDialog
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class SingleDatePickerMainActivityWithDoublePicker : AppCompatActivity() {
    @JvmField
    @BindView(R.id.doubleText)
    var doubleText: TextView? = null
    @JvmField
    @BindView(R.id.singleText)
    var singleText: TextView? = null
    @JvmField
    @BindView(R.id.singleTimeText)
    var singleTimeText: TextView? = null
    @JvmField
    @BindView(R.id.singleDateText)
    var singleDateText: TextView? = null
    @JvmField
    @BindView(R.id.singleDateLocaleText)
    var singleDateLocaleText: TextView? = null
    var simpleDateFormat: SimpleDateFormat? = null
    var simpleTimeFormat: SimpleDateFormat? = null
    var simpleDateOnlyFormat: SimpleDateFormat? = null
    var simpleDateLocaleFormat: SimpleDateFormat? = null
    var singleBuilder: SingleDateAndTimePickerDialog.Builder? = null
    var doubleBuilder: DoubleDateAndTimePickerDialog.Builder? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.single_date_picker_activity_main_double_picker)
        ButterKnife.bind(this)
        simpleDateFormat = SimpleDateFormat("EEE d MMM HH:mm", Locale.getDefault())
        simpleTimeFormat = SimpleDateFormat("hh:mm aa", Locale.getDefault())
        simpleDateOnlyFormat = SimpleDateFormat("EEE d MMM", Locale.getDefault())
        simpleDateLocaleFormat = SimpleDateFormat("EEE d MMM", Locale.GERMAN)
    }

    override fun onPause() {
        super.onPause()
        if (singleBuilder != null) singleBuilder!!.dismiss()
        if (doubleBuilder != null) doubleBuilder!!.dismiss()
    }

    @OnClick(R.id.singleTimeText)
    fun simpleTimeClicked() {
        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = 21
        calendar[Calendar.MINUTE] = 50
        val defaultDate = calendar.time
        singleBuilder = SingleDateAndTimePickerDialog.Builder(this)
                .setTimeZone(TimeZone.getDefault())
                .bottomSheet()
                .curved()
                .defaultDate(defaultDate) //.titleTextColor(Color.GREEN)
//.backgroundColor(Color.BLACK)
//.mainColor(Color.GREEN)
                .displayMinutes(true)
                .displayHours(true)
                .displayDays(true) //.displayMonth(true)
//.displayYears(true)
                .displayListener { }
                .title("Simple Time")
                .listener { date -> singleTimeText!!.text = simpleTimeFormat!!.format(date) }
        singleBuilder?.display()
    }

    @OnClick(R.id.singleDateText)
    fun simpleDateClicked() {
        val calendar = Calendar.getInstance()
        val defaultDate = calendar.time
        singleBuilder = SingleDateAndTimePickerDialog.Builder(this)
                .setTimeZone(TimeZone.getDefault())
                .bottomSheet()
                .curved() //.titleTextColor(Color.GREEN)
//.backgroundColor(Color.BLACK)
//.mainColor(Color.GREEN)
                .displayHours(false)
                .displayMinutes(false)
                .displayDays(true)
                .displayListener { }
                .title("")
                .listener { date -> singleDateText!!.text = simpleDateOnlyFormat!!.format(date) }
        singleBuilder?.display()
    }

    @OnClick(R.id.singleLayout)
    fun simpleClicked() {
        val calendar = Calendar.getInstance()
        calendar[Calendar.DAY_OF_MONTH] = 4 // 4. Feb. 2018
        calendar[Calendar.MONTH] = 1
        calendar[Calendar.YEAR] = 2018
        calendar[Calendar.HOUR_OF_DAY] = 11
        calendar[Calendar.MINUTE] = 13
        val defaultDate = calendar.time
        singleBuilder = SingleDateAndTimePickerDialog.Builder(this)
                .setTimeZone(TimeZone.getDefault())
                .bottomSheet()
                .curved() //.backgroundColor(Color.BLACK)
//.mainColor(Color.GREEN)
                .displayHours(false)
                .displayMinutes(false)
                .displayDays(false)
                .displayMonth(true)
                .displayDaysOfMonth(true)
                .displayYears(true)
                .defaultDate(defaultDate)
                .displayMonthNumbers(true) //.mustBeOnFuture()
//.minutesStep(15)
//.mustBeOnFuture()
//.defaultDate(defaultDate)
// .minDateRange(minDate)
// .maxDateRange(maxDate)
                .displayListener { }
                .title("Simple")
                .listener { date -> singleText!!.text = simpleDateFormat!!.format(date) }
        singleBuilder?.display()
    }

    @OnClick(R.id.doubleLayout)
    fun doubleClicked() {
        val now = Date()
        val calendarMin = Calendar.getInstance()
        val calendarMax = Calendar.getInstance()
        calendarMin.time = now // Set min now
        calendarMax.time = Date(now.time + TimeUnit.DAYS.toMillis(150)) // Set max now + 150 days
        val minDate = calendarMin.time
        val maxDate = calendarMax.time
        doubleBuilder = DoubleDateAndTimePickerDialog.Builder(this)
                .setTimeZone(TimeZone.getDefault()) //.bottomSheet()
//.curved()
//                .backgroundColor(Color.BLACK)
//                .mainColor(Color.GREEN)
                .minutesStep(15)
                .mustBeOnFuture()
                .minDateRange(minDate)
                .maxDateRange(maxDate)
                .secondDateAfterFirst(true) //.defaultDate(now)
                .tab0Date(now)
                .tab1Date(Date(now.time + TimeUnit.HOURS.toMillis(1)))
                .title("Double")
                .tab0Text("Depart")
                .tab1Text("Return")
                .listener(object : DoubleDateAndTimePickerDialog.Listener {
                    override fun onDateSelected(dates: List<Date?>?) {
                        val dateList = dates ?: return
                        val stringBuilder = StringBuilder()
                        for (date in dateList) {
                            stringBuilder.append(simpleDateFormat?.format(date)).append("\n")
                        }
                        doubleText?.text = stringBuilder.toString()

                    }

                })
        doubleBuilder?.display()
    }

    @OnClick(R.id.singleDateLocaleLayout)
    fun singleDateLocaleClicked() {
        singleBuilder = SingleDateAndTimePickerDialog.Builder(this)
                .customLocale(Locale.GERMAN)
                .bottomSheet()
                .curved()
                .displayHours(false)
                .displayMinutes(false)
                .displayDays(true)
                .displayListener { }
                .title("")
                .listener { date -> singleDateLocaleText!!.text = simpleDateLocaleFormat!!.format(date) }
        singleBuilder?.display()
    }
}