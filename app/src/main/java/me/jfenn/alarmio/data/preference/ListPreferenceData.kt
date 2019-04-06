package me.jfenn.alarmio.data.preference

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.aesthetic.Aesthetic
import me.jfenn.alarmio.R
import me.jfenn.alarmio.data.PreferenceData

abstract class ListPreferenceData(private val preference: PreferenceData, private val title: Int) : BasePreferenceData<ListPreferenceData.ViewHolder>() {

    internal abstract fun getAdapter(context: Context, items: Array<String>): RecyclerView.Adapter<*>

    internal abstract fun requestAddItem(holder: ViewHolder)

    override fun getViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_preference_list, parent, false))
    }

    @SuppressLint("CheckResult")
    override fun bindViewHolder(holder: ViewHolder) {
        val items = getItems(holder.context)

        holder.title.setText(title)
        holder.recycler.layoutManager = LinearLayoutManager(holder.context)
        holder.recycler.adapter = getAdapter(holder.context, items)

        holder.add.setOnClickListener { requestAddItem(holder) }

        holder.remove.visibility = if (items.size > 1) View.VISIBLE else View.GONE
        holder.remove.setOnClickListener { removeItem(holder) }

        Aesthetic.get()
                .textColorPrimary()
                .take(1)
                .subscribe { colorPrimary ->
                    holder.add.colorFilter = PorterDuffColorFilter(colorPrimary, PorterDuff.Mode.SRC_IN)
                    holder.remove.colorFilter = PorterDuffColorFilter(colorPrimary, PorterDuff.Mode.SRC_IN)
                }
    }

    fun getItems(context: Context): Array<String> {
        return preference.getValue(context)
    }

    /**
     * adds an item to the end of the list
     *
     * @param holder the ViewHolder containing the RecyclerView
     * @param item   the item to add
     */
    fun addItem(holder: ViewHolder, item: String) {
        preference.setValue(holder.context, getItems(holder.context).plusElement(item))
        bindViewHolder(holder)
    }

    /**
     * Removes the last item in the list
     *
     * @param holder the ViewHolder containing the RecyclerView
     */
    fun removeItem(holder: ViewHolder) {
        val items = getItems(holder.context).dropLast(1)
        if (items.size > 1) {
            preference.setValue(holder.context, items.dropLast(1))
            bindViewHolder(holder)
        }
    }

    /**
     * Holds child views of the current item.
     */
    class ViewHolder(v: View) : BasePreferenceData.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.title)
        val recycler: RecyclerView = v.findViewById(R.id.recycler)
        val add: ImageView = v.findViewById(R.id.add)
        val remove: ImageView = v.findViewById(R.id.remove)
    }


}
