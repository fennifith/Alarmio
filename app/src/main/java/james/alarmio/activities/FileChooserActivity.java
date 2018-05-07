package james.alarmio.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import james.alarmio.data.PreferenceData;

public class FileChooserActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE = 284;

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

        Intent intent = new Intent();
        intent.setType(type);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_IMAGE);
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
