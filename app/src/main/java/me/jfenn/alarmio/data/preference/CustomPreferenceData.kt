package me.jfenn.alarmio.data.preference

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import me.jfenn.alarmio.R

/**
 * A simple preference item to bind a title
 * and text value of a preference to a basic
 * item view.
 */
abstract class CustomPreferenceData(private val name: Int) : BasePreferenceData<CustomPreferenceData.ViewHolder>() {

    /**
     * Get the name of the current value of the preference.
     */
    abstract fun getValueName(holder: ViewHolder): String?

    /**
     * Called when the preference is clicked.
     */
    abstract fun onClick(holder: ViewHolder)

    override fun getViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder = ViewHolder(inflater.inflate(R.layout.item_preference_custom, parent, false))

    override fun bindViewHolder(holder: ViewHolder) {
        holder.nameView.setText(name)
        holder.valueNameView.text = getValueName(holder) ?: run {
            holder.valueNameView.visibility = View.GONE; null
        }

        holder.itemView.setOnClickListener { onClick(holder) }
    }

    /**
     * Holds child views of the current item.
     */
    class ViewHolder(v: View) : BasePreferenceData.ViewHolder(v) {
        val nameView: TextView = v.findViewById(R.id.name)
        val valueNameView: TextView = v.findViewById(R.id.value)
    }

}
