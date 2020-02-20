package com.github.florent37.sample.singledateandtimepicker

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker
import java.util.*

class SingleDatePickerMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.single_date_picker_activity_main)
        val singleDateAndTimePicker = findViewById<View>(R.id.single_day_picker) as SingleDateAndTimePicker
        singleDateAndTimePicker.addOnDateChangedListener(object : SingleDateAndTimePicker.OnDateChangedListener {
            override fun onDateChanged(displayed: String?, date: Date?) {
                displayed?.let { display(it) }
            }

        })

        findViewById<View>(R.id.toggleEnabled).setOnClickListener { singleDateAndTimePicker.isEnabled = !singleDateAndTimePicker.isEnabled }
    }

    private fun display(toDisplay: String) {
        Toast.makeText(this, toDisplay, Toast.LENGTH_SHORT).show()
    }
}