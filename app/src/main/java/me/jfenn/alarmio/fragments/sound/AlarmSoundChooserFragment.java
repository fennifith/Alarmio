package me.jfenn.alarmio.fragments.sound;

import android.content.Context;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.jfenn.alarmio.R;
import me.jfenn.alarmio.adapters.SoundsAdapter;
import me.jfenn.alarmio.data.SoundData;
import me.jfenn.alarmio.fragments.BasePagerFragment;
import me.jfenn.alarmio.interfaces.SoundChooserListener;

public class AlarmSoundChooserFragment extends BaseSoundChooserFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sound_chooser_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<SoundData> sounds = new ArrayList<>();
        RingtoneManager manager = new RingtoneManager(getContext());
        manager.setType(RingtoneManager.TYPE_ALARM);
        Cursor cursor = manager.getCursor();
        int count = cursor.getCount();
        if (count > 0 && cursor.moveToFirst()) {
            do {
                sounds.add(new SoundData(cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX), SoundData.TYPE_RINGTONE, cursor.getString(RingtoneManager.URI_COLUMN_INDEX) + "/" + cursor.getString(RingtoneManager.ID_COLUMN_INDEX)));
            } while (cursor.moveToNext());
        }

        SoundsAdapter adapter = new SoundsAdapter(getAlarmio(), sounds);
        adapter.setListener(this);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.title_alarms);
    }

    public static class Instantiator extends BaseSoundChooserFragment.Instantiator {

        public Instantiator(Context context, SoundChooserListener listener) {
            super(context, listener);
        }

        @Override
        BasePagerFragment newInstance(int position, SoundChooserListener listener) {
            BaseSoundChooserFragment fragment = new AlarmSoundChooserFragment();
            fragment.setListener(listener);
            return fragment;
        }

        @Override
        public String getTitle(Context context, int position) {
            return context.getString(R.string.title_alarms);
        }
    }

}
