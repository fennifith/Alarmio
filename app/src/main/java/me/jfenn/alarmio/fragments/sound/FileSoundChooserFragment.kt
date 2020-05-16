package me.jfenn.alarmio.fragments.sound

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.jfenn.alarmio.R
import me.jfenn.alarmio.activities.FileChooserActivity
import me.jfenn.alarmio.adapters.SoundsAdapter
import me.jfenn.alarmio.data.SoundData
import me.jfenn.alarmio.fragments.BasePagerFragment
import me.jfenn.alarmio.interfaces.SoundChooserListener
import me.jfenn.alarmio.interfaces.SoundPlayer
import org.koin.android.ext.android.inject
import java.util.*

class FileSoundChooserFragment : BaseSoundChooserFragment() {

    companion object {
        private const val REQUEST_AUDIO = 285
        private const val SEPARATOR = ":AlarmioFileSound:"
        private const val PREF_FILES = "previousFiles"
    }

    private val prefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    private val player: SoundPlayer by inject()
    private var sounds: ArrayList<SoundData>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sound_chooser_file, container, false)

        view.findViewById<View>(R.id.addAudioFile).setOnClickListener {
            val intent = Intent(context, FileChooserActivity::class.java)
            intent.putExtra(FileChooserActivity.EXTRA_TYPE, "audio/*")
            startActivityForResult(intent, REQUEST_AUDIO)
        }

        val recycler: RecyclerView = view.findViewById(R.id.recycler)
        val previousFiles = ArrayList(prefs.getStringSet(PREF_FILES, HashSet())!!)
        previousFiles.sortWith(Comparator { o1: String, o2: String ->
            try {
                o1.split(SEPARATOR)[0].toInt() - o2.split(SEPARATOR)[0].toInt()
            } catch (e: NumberFormatException) {
                0
            }
        })

        sounds = ArrayList<SoundData>()
        for (string in previousFiles) {
            val parts = string.split(SEPARATOR)
            sounds?.add(SoundData(parts[1], SoundData.TYPE_RINGTONE, parts[2]))
        }

        recycler.layoutManager = LinearLayoutManager(context)

        val adapter = SoundsAdapter(alarmio, sounds, player)
        adapter.setListener(this)
        recycler.adapter = adapter

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_AUDIO && resultCode == Activity.RESULT_OK && data != null) {
            var name: String? = "Audio File"
            if (data.hasExtra("name")) name = data.getStringExtra("name")
            onSoundChosen(SoundData(name, SoundData.TYPE_RINGTONE, data.dataString))
        }
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
                "${index}${SEPARATOR}${sound.name}${SEPARATOR}${sound.url}"
            }.let { files ->
                prefs.edit().putStringSet(PREF_FILES, files.toHashSet()).apply()
            }
        }
    }

    override fun getTitle(context: Context): String {
        return context.getString(R.string.title_files)
    }

    class Instantiator(context: Context?, listener: SoundChooserListener?) : BaseSoundChooserFragment.Instantiator(context, listener) {
        override fun newInstance(position: Int, listener: SoundChooserListener): BasePagerFragment {
            val fragment: BaseSoundChooserFragment = FileSoundChooserFragment()
            fragment.setListener(listener)
            return fragment
        }

        override fun getTitle(context: Context, position: Int): String {
            return context.getString(R.string.title_files)
        }
    }
}