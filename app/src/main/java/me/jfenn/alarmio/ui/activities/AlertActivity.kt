package me.jfenn.alarmio.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import me.jfenn.alarmio.viewmodel.AlarmioViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class AlertActivity: AppCompatActivity() {

    companion object {
        val EXTRA_ALERT = "${AlertActivity::class.java.name}.EXTRA_ALERT"
        val EXTRA_ALERT_ID = "${AlertActivity::class.java.name}.EXTRA_ALERT_ID"
    }

    val alarmio: AlarmioViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

}