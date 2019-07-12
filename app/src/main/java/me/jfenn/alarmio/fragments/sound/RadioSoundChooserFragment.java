package me.jfenn.alarmio.fragments.sound;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.jfenn.alarmio.R;
import me.jfenn.alarmio.adapters.SoundsAdapter;
import me.jfenn.alarmio.data.SoundData;
import me.jfenn.alarmio.fragments.BasePagerFragment;
import me.jfenn.alarmio.interfaces.SoundChooserListener;

public class RadioSoundChooserFragment extends BaseSoundChooserFragment {

    private static final String SEPARATOR = ":AlarmioRadioSound:";
    private static final String PREF_RADIOS = "previousRadios";

    private EditText radioUrlEditText;
    private AppCompatButton testRadio;
    private SoundData currentSound;

    private SharedPreferences prefs;
    private List<SoundData> sounds;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sound_chooser_radio, container, false);

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        radioUrlEditText = view.findViewById(R.id.radioUrl);
        TextView errorTextView = view.findViewById(R.id.errorText);
        RecyclerView recycler = view.findViewById(R.id.recycler);
        testRadio = view.findViewById(R.id.testRadio);

        List<String> previousRadios = new ArrayList<>(prefs.getStringSet(PREF_RADIOS, new HashSet<String>()));
        Collections.sort(previousRadios, (o1, o2) -> {
            try {
                return Integer.parseInt(o1.split(SEPARATOR)[0]) - Integer.parseInt(o2.split(SEPARATOR)[0]);
            } catch (NumberFormatException e) {
                return 0;
            }
        });

        sounds = new ArrayList<>();
        for (String string : previousRadios) {
            String url = string.split(SEPARATOR)[1];
            sounds.add(new SoundData(url, SoundData.TYPE_RADIO, url));
        }

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        SoundsAdapter adapter = new SoundsAdapter(getAlarmio(), sounds);
        adapter.setListener(this);
        recycler.setAdapter(adapter);

        testRadio.setOnClickListener(v -> {
            if (currentSound == null) {
                currentSound = new SoundData("", SoundData.TYPE_RADIO, radioUrlEditText.getText().toString());
                try {
                    currentSound.preview(getAlarmio());
                    testRadio.setText(R.string.title_radio_stop);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                if (currentSound.isPlaying(getAlarmio()))
                    currentSound.stop(getAlarmio());

                currentSound = null;
                testRadio.setText(R.string.title_radio_test);
            }
        });

        view.findViewById(R.id.createRadio).setOnClickListener(v -> {
            if (URLUtil.isValidUrl(radioUrlEditText.getText().toString())) {
                onSoundChosen(new SoundData(getString(R.string.title_radio), SoundData.TYPE_RADIO, radioUrlEditText.getText().toString()));
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        radioUrlEditText = null;
        testRadio = null;
    }

    @Override
    public void onSoundChosen(SoundData sound) {
        super.onSoundChosen(sound);

        if (sound != null) {
            if (sounds.contains(sound))
                sounds.remove(sound);

            sounds.add(0, sound);

            Set<String> radios = new HashSet<>();
            for (int i = 0; i < sounds.size(); i++) {
                radios.add(i + SEPARATOR + sounds.get(i).getUrl());
            }

            prefs.edit().putStringSet(PREF_RADIOS, radios).apply();
        }
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.title_radio);
    }

    public static class Instantiator extends BaseSoundChooserFragment.Instantiator {

        public Instantiator(Context context, SoundChooserListener listener) {
            super(context, listener);
        }

        @Override
        BasePagerFragment newInstance(int position, SoundChooserListener listener) {
            BaseSoundChooserFragment fragment = new RadioSoundChooserFragment();
            fragment.setListener(listener);
            return fragment;
        }

        @Override
        public String getTitle(Context context, int position) {
            return context.getString(R.string.title_radio);
        }
    }

}
