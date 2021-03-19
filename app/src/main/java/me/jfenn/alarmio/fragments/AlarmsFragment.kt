package me.jfenn.alarmio.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.aesthetic.Aesthetic.Companion.get
import io.reactivex.disposables.Disposable
import me.jfenn.alarmio.R
import me.jfenn.alarmio.adapters.AlarmsAdapter
import me.jfenn.alarmio.adapters.AlertAdapter
import me.jfenn.alarmio.interfaces.ContextFragmentInstantiator
import me.jfenn.alarmio.viewmodels.HomeViewModel
import me.jfenn.androidutils.bind
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class AlarmsFragment : BasePagerFragment() {

    private val viewModel: HomeViewModel by sharedViewModel()

    private val recyclerView: RecyclerView? by bind(R.id.recycler)
    private val empty: View? by bind(R.id.empty)
    private val emptyText: TextView? by bind(R.id.emptyText)

    private var alarmsAdapter: AlertAdapter? = null

    private var colorAccentSubscription: Disposable? = null
    private var colorForegroundSubscription: Disposable? = null
    private var textColorPrimarySubscription: Disposable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recycler, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emptyText?.setText(R.string.msg_alarms_empty)

        recyclerView?.apply {
            layoutManager = GridLayoutManager(context, 1)
            alarmsAdapter = AlertAdapter(this, fragmentManager!!, ArrayList())
            adapter = alarmsAdapter
        }

        viewModel.alerts.observe(this) {
            empty?.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
            alarmsAdapter?.swapList(it)
        }

        colorAccentSubscription = get()
                .colorAccent()
                .subscribe { integer: Int? -> alarmsAdapter!!.colorAccent = integer!! }
        colorForegroundSubscription = get()
                .colorCardViewBackground()
                .subscribe { integer: Int? -> alarmsAdapter!!.colorForeground = integer!! }
        textColorPrimarySubscription = get()
                .textColorPrimary()
                .subscribe { integer: Int? -> alarmsAdapter!!.textColorPrimary = integer!! }

        onChanged()
    }

    override fun onDestroyView() {
        colorAccentSubscription!!.dispose()
        colorForegroundSubscription!!.dispose()
        textColorPrimarySubscription!!.dispose()
        super.onDestroyView()
    }

    override fun getTitle(context: Context): String {
        return context.getString(R.string.title_alarms)
    }

    override fun onAlarmsChanged() {
        if (recyclerView != null && alarmsAdapter != null) {
            recyclerView!!.post { alarmsAdapter!!.notifyDataSetChanged() }
            onChanged()
        }
    }

    override fun onTimersChanged() {
        if (recyclerView != null && alarmsAdapter != null) {
            recyclerView!!.post { alarmsAdapter!!.notifyDataSetChanged() }
            onChanged()
        }
    }

    private fun onChanged() {
        if (empty != null && alarmsAdapter != null) empty!!.visibility = if (alarmsAdapter!!.itemCount > 0) View.GONE else View.VISIBLE
    }

    class Instantiator(context: Context?) : ContextFragmentInstantiator(context) {
        override fun getTitle(context: Context, position: Int): String {
            return context.getString(R.string.title_alarms)
        }

        override fun newInstance(position: Int): BasePagerFragment? {
            return AlarmsFragment()
        }
    }
}