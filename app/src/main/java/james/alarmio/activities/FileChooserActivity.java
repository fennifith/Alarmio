package james.alarmio.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import james.alarmio.data.PreferenceData;

public class FileChooserActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE = 284;
    private static final int REQUEST_STORAGE_PERMISSION = 727;

    public static final String EXTRA_TYPE = "james.alarmio.FileChooserActivity.EXTRA_TYPE";
    public static final String EXTRA_PREFERENCE = "james.alarmio.FileChooserActivity.EXTRA_PREFERENCE";

    private PreferenceData preference;
    private String type = "image/*";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent data = getIntent();
        if (data != null) {
            preference = (PreferenceData) data.getSerializableExtra(EXTRA_PREFERENCE);
            if (data.hasExtra(EXTRA_TYPE))
                type = data.getStringExtra(EXTRA_TYPE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            startIntent();
        else
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
    }

    private void startIntent() {
        Intent intent = new Intent();
        intent.setType(type);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            startIntent();
        else finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK && data != null) {
            String path = data.getDataString();
            if (path != null)
                preference.setValue(this, path);
        }

        finish();
    }
}
