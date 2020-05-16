package me.jfenn.alarmio.fragments.sound

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
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

class RadioSoundChooserFragment : BaseSoundChooserFragment() {

    companion object {
        private const val SEPARATOR = ":AlarmioRadioSound:"
        private const val PREF_RADIOS = "previousRadios"
    }

    private var radioUrlEditText: EditText? = null
    private var testRadio: AppCompatButton? = null

    private val prefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    private var sounds: MutableList<SoundData>? = null

    private val player: SoundPlayer by inject()
    private var isPlaying: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sound_chooser_radio, container, false)
        radioUrlEditText = view.findViewById(R.id.radioUrl)
        val errorTextView = view.findViewById<TextView>(R.id.errorText)
        val recycler: RecyclerView = view.findViewById(R.id.recycler)
        testRadio = view.findViewById(R.id.testRadio)

        val previousRadios = ArrayList(prefs.getStringSet(PREF_RADIOS, HashSet())!!)

        previousRadios.sortWith(Comparator { o1: String, o2: String ->
            try {
                o1.split(SEPARATOR)[0].toInt() - o2.split(SEPARATOR)[0].toInt()
            } catch (e: NumberFormatException) {
                0
            }
        })

        sounds = ArrayList()
        for (string in previousRadios) {
            val url = string.split(SEPARATOR).toTypedArray()[1]
            sounds?.add(SoundData(url, SoundData.TYPE_RADIO, url))
        }

        recycler.layoutManager = LinearLayoutManager(context)
        val adapter = SoundsAdapter(alarmio, sounds, player)
        adapter.setListener(this)
        recycler.adapter = adapter

        testRadio?.setOnClickListener {
            val currentSound = SoundData("", SoundData.TYPE_RADIO, radioUrlEditText?.text?.toString() ?: "")
            player.stop()

            if (isPlaying) {
                testRadio?.setText(R.string.title_radio_test)
                isPlaying = false
            } else {
                try {
                    player.play(currentSound)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }

                testRadio?.setText(R.string.title_radio_stop)
                isPlaying = true
            }
        }

        view.findViewById<View>(R.id.createRadio).setOnClickListener {
            radioUrlEditText?.text?.toString()?.let { url ->
                if (URLUtil.isValidUrl(url))
                    onSoundChosen(SoundData(getString(R.string.title_radio), SoundData.TYPE_RADIO, url))
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        radioUrlEditText = null
        testRadio = null
    }

    override fun onSoundChosen(sound: SoundData?) {
        super.onSoundChosen(sound)

        sounds?.let { sounds ->
            sound?.let { // add sound (it) to top of list
                if (sounds.contains(it))
                    sounds.remove(it)

                sounds.add(0, it)
            }

            // store sounds in SharedPreferences
            sounds.mapIndexed { index, sound ->
                "${index}${SEPARATOR}${sound.url}"
            }.let { files ->
                prefs.edit().putStringSet(PREF_RADIOS, files.toHashSet()).apply()
            }
        }
    }

    override fun getTitle(context: Context): String {
        return context.getString(R.string.title_radio)
    }

    class Instantiator(context: Context?, listener: SoundChooserListener?) : BaseSoundChooserFragment.Instantiator(context, listener) {
        public override fun newInstance(position: Int, listener: SoundChooserListener): BasePagerFragment {
            val fragment: BaseSoundChooserFragment = RadioSoundChooserFragment()
            fragment.setListener(listener)
            return fragment
        }

        override fun getTitle(context: Context, position: Int): String {
            return context.getString(R.string.title_radio)
        }
    }
}