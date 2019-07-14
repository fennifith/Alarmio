package me.jfenn.alarmio.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
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
    private static final int REQUEST_AUDIO = 285;
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
            if (data.hasExtra(EXTRA_PREFERENCE))
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
        int requestCode = type.equals("audio/*") ? REQUEST_AUDIO : REQUEST_IMAGE;
        Intent intent = new Intent();
        intent.setType(type);
        if (type.equals("audio/*")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            } else {
                intent.setAction(Intent.ACTION_GET_CONTENT);
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        }
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, requestCode);
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
                Cursor cursor = null;

                try {
                    cursor = getContentResolver().query(data.getData(), null, null, null, null);

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
                } finally {
                    if (cursor != null && !cursor.isClosed())
                        cursor.close();
                }
            }

            preference.setValue(this, path);
        } else if (requestCode == REQUEST_AUDIO && resultCode == RESULT_OK && data != null && type.equals("audio/*")) {
            String name = null;
            Cursor cursor = null;

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                    getContentResolver().takePersistableUriPermission(data.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION);

                cursor = getContentResolver().query(data.getData(), null, null, null, null);

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

                cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media._ID + " = ? ", new String[]{documentId}, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null && !cursor.isClosed())
                    cursor.close();
            }

            if (name != null && name.length() > 0)
                data.putExtra("name", name);
            setResult(RESULT_OK, data);
        }

        finish();
    }
}
