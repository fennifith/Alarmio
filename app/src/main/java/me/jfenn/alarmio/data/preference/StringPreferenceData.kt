package me.jfenn.alarmio.data.preference

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.widget.SwitchCompat
import androidx.core.widget.CompoundButtonCompat
import com.afollestad.aesthetic.Aesthetic
import me.jfenn.alarmio.R
import me.jfenn.alarmio.data.PreferenceData

/**
 * Allow the user to choose from a simple boolean
 * using a switch item view.
 */
class StringPreferenceData(private val preference: PreferenceData, @StringRes private val title: Int, @StringRes private val description: Int) : BasePreferenceData<StringPreferenceData.ViewHolder>() {

    override fun getViewHolder(inflater: LayoutInflater, parent: ViewGroup): BasePreferenceData.ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_preference_string, parent, false))
    }

    @SuppressLint("CheckResult")
    override fun bindViewHolder(holder: ViewHolder) {
        holder.title.setText(title)
        holder.description.setText(description)
        holder.textChoice.setText(preference.getValue<String>(holder.context))
        holder.textChoice.afterTextChanged { editable -> preference.setValue(holder.context, editable.toString()) }

        // Scroll to it when focused

    }

    /**
     * Holds child views of the current item.
     */
    inner class ViewHolder(v: View) : BasePreferenceData.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.title)
        val description: TextView = v.findViewById(R.id.description)
        val textChoice: EditText = v.findViewById(R.id.textChoice)
    }

    fun EditText.afterTextChanged(afterTextChanged: (Editable?) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged.invoke(editable)
            }
        })
    }

}
