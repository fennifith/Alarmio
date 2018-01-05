package james.alarmio.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import james.alarmio.R;

public class RadioSoundChooserFragment extends SoundChooserFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sound_chooser_radio, container, false);
        EditText radioUrlEditText = view.findViewById(R.id.radioUrl);
        TextView errorTextView = view.findViewById(R.id.errorText);
        RecyclerView recycler = view.findViewById(R.id.recycler);

        //TODO: implement radio stuff

        return view;
    }

    @Override
    public String getTitle() {
        return "Radio";
    }

}
