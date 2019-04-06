package me.jfenn.alarmio.data.preference

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.widget.AppCompatSpinner
import com.afollestad.aesthetic.Aesthetic
import me.jfenn.alarmio.R
import me.jfenn.alarmio.data.PreferenceData

/**
 * Allow the user to choose from one of a range
 * of items.
 */
class SpinnerPreferenceData(private val preference: PreferenceData, private val title: Int, private val options: Int) : BasePreferenceData<SpinnerPreferenceData.ViewHolder>() {

    override fun getViewHolder(inflater: LayoutInflater, parent: ViewGroup): BasePreferenceData.ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_preference_spinner, parent, false))
    }

    @SuppressLint("CheckResult")
    override fun bindViewHolder(holder: ViewHolder) {
        holder.title.setText(title)
        holder.spinner.adapter = ArrayAdapter.createFromResource(holder.itemView.context, options, R.layout.support_simple_spinner_dropdown_item)

        preference.getValue<Int>(holder.itemView.context)?.let { value ->
            holder.spinner.setSelection(value)
        }

        holder.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                preference.setValue(adapterView.context, i)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }

        Aesthetic.get()
                .textColorSecondary()
                .take(1)
                .subscribe { textColorSecondary ->
                    holder.spinner.supportBackgroundTintList = ColorStateList.valueOf(textColorSecondary)
                }

        Aesthetic.get()
                .colorCardViewBackground()
                .take(1)
                .subscribe { colorForeground ->
                    holder.spinner.setPopupBackgroundDrawable(ColorDrawable(colorForeground))
                }
    }

    /**
     * Holds child views of the current item.
     */
    inner class ViewHolder(v: View) : BasePreferenceData.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.title)
        val spinner: AppCompatSpinner = v.findViewById(R.id.spinner)
    }

}
