package me.jfenn.alarmio.fragments.sound;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.jfenn.alarmio.R;
import me.jfenn.alarmio.activities.FileChooserActivity;
import me.jfenn.alarmio.adapters.SoundsAdapter;
import me.jfenn.alarmio.data.SoundData;
import me.jfenn.alarmio.fragments.BasePagerFragment;
import me.jfenn.alarmio.interfaces.SoundChooserListener;

import static android.app.Activity.RESULT_OK;

public class FileSoundChooserFragment extends BaseSoundChooserFragment {

    private static final int REQUEST_AUDIO = 285;

    private static final String TYPE_AUDIO = "audio/*";

    private static final String SEPARATOR = ":AlarmioFileSound:";
    private static final String PREF_FILES = "previousFiles";

    private SharedPreferences prefs;
    private List<SoundData> sounds;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sound_chooser_file, container, false);

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        view.findViewById(R.id.addAudioFile).setOnClickListener(v -> {
            launchFileChooser(TYPE_AUDIO);
        });

        RecyclerView recycler = view.findViewById(R.id.recycler);

        List<String> previousFiles = new ArrayList<>(prefs.getStringSet(PREF_FILES, new HashSet<String>()));
        Collections.sort(previousFiles, (o1, o2) -> {
            try {
                return Integer.parseInt(o1.split(SEPARATOR)[0]) - Integer.parseInt(o2.split(SEPARATOR)[0]);
            } catch (NumberFormatException e) {
                return 0;
            }
        });

        sounds = new ArrayList<>();
        for (String string : previousFiles) {
            String[] parts = string.split(SEPARATOR);
            sounds.add(new SoundData(parts[1], SoundData.TYPE_RINGTONE, parts[2]));
        }

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        SoundsAdapter adapter = new SoundsAdapter(getAlarmio(), sounds);
        adapter.setListener(this);
        recycler.setAdapter(adapter);

        return view;
    }

    private void launchFileChooser(String type) {
        Intent intent = new Intent(getContext(), FileChooserActivity.class);
        intent.putExtra(FileChooserActivity.EXTRA_TYPE, type);
        startActivityForResult(intent, REQUEST_AUDIO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_AUDIO && resultCode == RESULT_OK && data != null) {
            String name = "Audio File";
            if (data.hasExtra("name"))
                name = data.getStringExtra("name");

            onSoundChosen(new SoundData(name, SoundData.TYPE_RINGTONE, data.getDataString()));
        }
    }

    @Override
    public void onSoundChosen(SoundData sound) {
        super.onSoundChosen(sound);

        if (sound != null) {
            if (sounds.contains(sound))
                sounds.remove(sound);

            sounds.add(0, sound);

            Set<String> files = new HashSet<>();
            for (int i = 0; i < sounds.size(); i++) {
                files.add(i + SEPARATOR + sounds.get(i).getName() + SEPARATOR + sounds.get(i).getUrl());
            }

            prefs.edit().putStringSet(PREF_FILES, files).apply();
        }
    }



    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.title_files);
    }

    public static class Instantiator extends BaseSoundChooserFragment.Instantiator {

        public Instantiator(Context context, SoundChooserListener listener) {
            super(context, listener);
        }

        @Override
        BasePagerFragment newInstance(int position, SoundChooserListener listener) {
            BaseSoundChooserFragment fragment = new FileSoundChooserFragment();
            fragment.setListener(listener);
            return fragment;
        }

        @Override
        public String getTitle(Context context, int position) {
            return context.getString(R.string.title_files);
        }
    }
}
