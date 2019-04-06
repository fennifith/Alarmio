package me.jfenn.alarmio.data.preference

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.RecyclerView
import me.jfenn.alarmio.Alarmio

/**
 * A preference item to display and bind values
 * of preferences as a settings item.
 */
abstract class BasePreferenceData<V : BasePreferenceData.ViewHolder> {

    /**
     * Create a ViewHolder instance for the item to use.
     *
     * @param inflater The LayoutInflater to inflate the layout from.
     * @param parent The parent view to be inflated into.
     * @return A non-null instance of a ViewHolder.
     */
    abstract fun getViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder

    /**
     * Bind the item data to its viewholder.
     *
     * @param holder The ViewHolder to bind the item's data to.
     */
    abstract fun bindViewHolder(holder: V)

    /**
     * Holds child views of the current item.
     */
    open class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        val context: Context
            get() = itemView.context

        val alarmio: Alarmio?
            get() = context.applicationContext as? Alarmio?

    }

}
