package me.jfenn.alarmio.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import me.jfenn.alarmio.data.PreferenceData;

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
            if (type.equals("image/*")) {
                try {
                    Cursor cursor = getContentResolver().query(data.getData(), null, null, null, null);

                    String documentId;
                    if (cursor != null) {
                        cursor.moveToFirst();
                        documentId = cursor.getString(0);
                        documentId = documentId.substring(documentId.lastIndexOf(":") + 1);
                        cursor.close();
                    } else {
                        finish();
                        return;
                    }

                    cursor = getContentResolver().query(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media._ID + " = ? ", new String[]{documentId}, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                        cursor.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            preference.setValue(this, path);
        }

        finish();
    }
}
