package james.alarmio.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import james.alarmio.R;
import james.alarmio.data.SoundData;

public class RadioSoundChooserFragment extends SoundChooserFragment {

    private EditText radioUrlEditText;
    private AppCompatButton testRadio;
    private SoundData currentSound;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sound_chooser_radio, container, false);
        radioUrlEditText = view.findViewById(R.id.radioUrl);
        TextView errorTextView = view.findViewById(R.id.errorText);
        RecyclerView recycler = view.findViewById(R.id.recycler);
        testRadio = view.findViewById(R.id.testRadio);

        testRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentSound == null) {
                    currentSound = new SoundData("", radioUrlEditText.getText().toString());
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
            }
        });

        view.findViewById(R.id.createRadio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (URLUtil.isValidUrl(radioUrlEditText.getText().toString())) {
                    onSoundChosen(new SoundData(getString(R.string.title_radio), radioUrlEditText.getText().toString()));
                }
            }
        });

        return view;
    }

    @Override
    public String getTitle() {
        return "Radio";
    }

}
