package me.jfenn.alarmio.data.preference

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.RecyclerView
import me.jfenn.alarmio.Alarmio

abstract class BasePreferenceData<V : BasePreferenceData.ViewHolder> {

    abstract fun getViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder

    abstract fun bindViewHolder(holder: V)

    open class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        val context: Context
            get() = itemView.context

        val alarmio: Alarmio
            get() = context.applicationContext as Alarmio

    }

}
