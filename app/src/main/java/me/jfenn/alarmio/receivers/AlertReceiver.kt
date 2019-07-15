package me.jfenn.alarmio.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import me.jfenn.alarmio.common.interfaces.AlertScheduler
import me.jfenn.alarmio.ui.activities.AlertActivity
import me.jfenn.alarmio.viewmodel.AlarmioViewModel
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.Serializable

class AlertReceiver: BroadcastReceiver(), KoinComponent {

    companion object {
        val EXTRA_ALERT_ID = "${AlertReceiver::class.java.name}.ALERT_ID"
    }

    val alarmio: AlarmioViewModel by inject()
    val scheduler: AlertScheduler by inject()

    override fun onReceive(context: Context?, intent: Intent?) {
        // this might horribly fail, idk, don't trust AndroidViewModel here
        // I have no clue what I'm doing

        val id = intent?.getStringExtra(EXTRA_ALERT_ID)
        val alert = id?.let { alarmio.getAlert(it) }

        // re-schedule the alert if it should repeat
        alert?.let { scheduler.reschedule(alert) }

        // start the alert activity to actually notify the user
        context?.startActivity(Intent(context, AlertActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            (alert as? Serializable)?.let {
                putExtra(AlertActivity.EXTRA_ALERT, it)
            } ?: run {
                putExtra(AlertActivity.EXTRA_ALERT_ID, id)
            }
        })
    }

}