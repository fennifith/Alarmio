package me.jfenn.alarmio.data.preference

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.ContextCompat
import com.afollestad.aesthetic.Aesthetic
import me.jfenn.alarmio.Alarmio
import me.jfenn.alarmio.R
import me.jfenn.alarmio.data.PreferenceData
import me.jfenn.alarmio.dialogs.AestheticTimeSheetPickerDialog
import me.jfenn.alarmio.utils.FormatUtils
import me.jfenn.sunrisesunsetview.SunriseSunsetView
import me.jfenn.timedatepickers.dialogs.PickerDialog
import me.jfenn.timedatepickers.views.LinearTimePickerView
import java.util.*

const val HOUR_LENGTH = 3600000L

/**
 * Allow the user to choose the theme of the
 * application.
 */
class ThemePreferenceData : BasePreferenceData<ThemePreferenceData.ViewHolder>() {

    override fun getViewHolder(inflater: LayoutInflater, parent: ViewGroup): BasePreferenceData.ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_preference_theme, parent, false))
    }

    @SuppressLint("CheckResult")
    override fun bindViewHolder(holder: ViewHolder) {
        holder.themeSpinner.adapter = ArrayAdapter.createFromResource(holder.itemView.context, R.array.array_themes, R.layout.support_simple_spinner_dropdown_item)

        val theme : Int = holder.alarmio?.activityTheme ?: Alarmio.THEME_DAY_NIGHT
        run {
            if (theme == Alarmio.THEME_DAY_NIGHT) View.VISIBLE else View.GONE
        }.let {
            holder.sunriseAutoSwitch.visibility = it
            holder.sunriseLayout.visibility = it
        }

        holder.themeSpinner.onItemSelectedListener = null
        holder.themeSpinner.setSelection(theme)
        holder.themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            var selection: Int? = null

            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                if (selection != null) {
                    run {
                        if (i == Alarmio.THEME_DAY_NIGHT) View.VISIBLE else View.GONE
                    }.let {
                        holder.sunriseAutoSwitch.visibility = it
                        holder.sunriseLayout.visibility = it
                    }

                    PreferenceData.THEME.setValue(adapterView.context, i)
                    holder.alarmio?.updateTheme()
                } else selection = i
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }

        val listener = object : SunriseSunsetView.SunriseListener {
            override fun onSunriseChanged(sunriseSunsetView: SunriseSunsetView, l: Long) {
                val hour = Math.round(l.toFloat() / HOUR_LENGTH)
                holder.sunriseTextView.text = getText(hour)
                sunriseSunsetView.setSunrise(hour * HOUR_LENGTH, true)
                PreferenceData.DAY_START.setValue(holder.context, hour)
                holder.alarmio?.updateTheme()
            }

            override fun onSunsetChanged(sunriseSunsetView: SunriseSunsetView, l: Long) {
                val hour = Math.round(l.toFloat() / HOUR_LENGTH)
                holder.sunsetTextView.text = getText(hour)
                sunriseSunsetView.setSunset(hour * HOUR_LENGTH, true)
                PreferenceData.DAY_END.setValue(holder.context, hour)
                holder.alarmio?.updateTheme()
            }

            private fun getText(hour: Int): String = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, 0)
            }.run {
                FormatUtils.formatShort(holder.context, Date(timeInMillis))
            }
        }

        holder.sunriseAutoSwitch.setOnCheckedChangeListener(null)
        holder.sunriseAutoSwitch.isChecked = holder.alarmio?.isDayAuto ?: false
        holder.sunriseAutoSwitch.setOnCheckedChangeListener { _, b ->
            PreferenceData.DAY_AUTO.setValue(holder.context, b)

            if (b && ContextCompat.checkSelfPermission(holder.context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                holder.alarmio?.requestPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
                holder.sunriseAutoSwitch.isChecked = false
            } else {
                holder.alarmio?.let { alarmio ->
                    listener.onSunriseChanged(holder.sunriseView, alarmio.dayStart * HOUR_LENGTH)
                    listener.onSunsetChanged(holder.sunriseView, alarmio.dayEnd * HOUR_LENGTH)
                }
            }
        }

        holder.alarmio?.let { alarmio ->
            listener.onSunriseChanged(holder.sunriseView, alarmio.dayStart * HOUR_LENGTH)
            listener.onSunsetChanged(holder.sunriseView, alarmio.dayEnd * HOUR_LENGTH)
        }

        holder.sunriseView.setListener(object : SunriseSunsetView.SunriseListener {
            override fun onSunriseChanged(sunriseSunsetView: SunriseSunsetView, l: Long) {
                holder.sunriseAutoSwitch.isChecked = false
                listener.onSunriseChanged(sunriseSunsetView, l)
            }

            override fun onSunsetChanged(sunriseSunsetView: SunriseSunsetView, l: Long) {
                holder.sunriseAutoSwitch.isChecked = false
                listener.onSunsetChanged(sunriseSunsetView, l)
            }
        })

        holder.sunriseTextView.setOnClickListener { view ->
            AestheticTimeSheetPickerDialog(view.context, holder.alarmio?.dayStart ?: 1, 0)
                    .setListener(object : PickerDialog.OnSelectedListener<LinearTimePickerView> {
                        override fun onSelect(dialog: PickerDialog<LinearTimePickerView>, view: LinearTimePickerView) {
                            holder.sunriseAutoSwitch.isChecked = false
                            holder.alarmio?.let { alarmio ->
                                if (view.hourOfDay < alarmio.dayEnd)
                                    listener.onSunriseChanged(holder.sunriseView, view.hourOfDay * HOUR_LENGTH)
                            }
                        }

                        override fun onCancel(dialog: PickerDialog<LinearTimePickerView>) {}
                    })
                    .show()
        }

        holder.sunsetTextView.setOnClickListener { view ->
            AestheticTimeSheetPickerDialog(view.context, holder.alarmio?.dayEnd ?: 23, 0)
                    .setListener(object : PickerDialog.OnSelectedListener<LinearTimePickerView> {
                        override fun onSelect(dialog: PickerDialog<LinearTimePickerView>, view: LinearTimePickerView) {
                            holder.sunriseAutoSwitch.isChecked = false
                            holder.alarmio?.let { alarmio ->
                                if (view.hourOfDay > alarmio.dayStart)
                                    listener.onSunsetChanged(holder.sunriseView, view.hourOfDay * HOUR_LENGTH)
                            }
                        }

                        override fun onCancel(dialog: PickerDialog<LinearTimePickerView>) {}
                    }).show()
        }

        Aesthetic.get()
                .textColorSecondary()
                .take(1)
                .subscribe { textColorSecondary ->
                    holder.themeSpinner.supportBackgroundTintList = ColorStateList.valueOf(textColorSecondary)
                }

        Aesthetic.get()
                .colorCardViewBackground()
                .take(1)
                .subscribe { colorForeground ->
                    holder.themeSpinner.setPopupBackgroundDrawable(ColorDrawable(colorForeground))
                }
    }

    /**
     * Holds child views of the current item.
     */
    class ViewHolder(v: View) : BasePreferenceData.ViewHolder(v) {
        val themeSpinner: AppCompatSpinner = v.findViewById(R.id.themeSpinner)
        val sunriseAutoSwitch: AppCompatCheckBox = v.findViewById(R.id.sunriseAutoSwitch)
        val sunriseLayout: View = v.findViewById(R.id.sunriseLayout)
        val sunriseView: SunriseSunsetView = v.findViewById(R.id.sunriseView)
        val sunriseTextView: TextView = v.findViewById(R.id.sunriseTextView)
        val sunsetTextView: TextView = v.findViewById(R.id.sunsetTextView)
    }

}
