package me.jfenn.alarmio.fragments.sound

import android.content.Context
import android.media.RingtoneManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.jfenn.alarmio.R
import me.jfenn.alarmio.adapters.SoundsAdapter
import me.jfenn.alarmio.data.SoundData
import me.jfenn.alarmio.fragments.BasePagerFragment
import me.jfenn.alarmio.interfaces.SoundChooserListener
import me.jfenn.alarmio.interfaces.SoundPlayer
import org.koin.android.ext.android.inject
import java.util.*

class RingtoneSoundChooserFragment : BaseSoundChooserFragment() {

    val player: SoundPlayer by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sound_chooser_list, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val sounds: MutableList<SoundData> = ArrayList()

        val manager = RingtoneManager(context)
        manager.setType(RingtoneManager.TYPE_RINGTONE)

        val cursor = manager.cursor
        val count = cursor.count
        if (count > 0 && cursor.moveToFirst()) {
            do {
                sounds.add(SoundData(cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX), SoundData.TYPE_RINGTONE, cursor.getString(RingtoneManager.URI_COLUMN_INDEX) + "/" + cursor.getString(RingtoneManager.ID_COLUMN_INDEX)))
            } while (cursor.moveToNext())
        }

        val adapter = SoundsAdapter(alarmio, sounds, player)
        adapter.setListener(this)
        recyclerView.adapter = adapter

        return view
    }

    override fun getTitle(context: Context): String {
        return context.getString(R.string.title_ringtones)
    }

    class Instantiator(context: Context?, listener: SoundChooserListener?) : BaseSoundChooserFragment.Instantiator(context, listener) {
        public override fun newInstance(position: Int, listener: SoundChooserListener): BasePagerFragment {
            val fragment: BaseSoundChooserFragment = RingtoneSoundChooserFragment()
            fragment.setListener(listener)
            return fragment
        }

        override fun getTitle(context: Context, position: Int): String {
            return context.getString(R.string.title_ringtones)
        }
    }
}