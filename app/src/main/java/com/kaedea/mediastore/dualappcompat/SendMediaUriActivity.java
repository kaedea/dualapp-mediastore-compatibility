package com.kaedea.mediastore.dualappcompat;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import static android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION;

public class SendMediaUriActivity extends AppCompatActivity {


    StringBuilder sb = new StringBuilder();
    TextView mTextView;
    Uri mExternalFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_media_uri);
        mTextView = findViewById(R.id.text);
        mTextView.setMovementMethod(new ScrollingMovementMethod());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mExternalFileUri = queryLastDownloads(this);
        }
        println("MediaUri: " + mExternalFileUri);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                this.startActivity(new Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private static Uri queryLastDownloads(@NonNull Context context) {
        Uri contentUri = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL);
        try (Cursor cursor = context.getContentResolver().query(
                contentUri,
                new String[]{MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME, /*MediaStore.MediaColumns.RELATIVE_PATH,*/ MediaStore.MediaColumns.DATA},
                null,
                null
        )) {
            if (cursor != null) {
                if (cursor.moveToLast()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
                    // String dn = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
                    // String rp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH));
                    // String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                    return Uri.withAppendedPath(contentUri, String.valueOf(id));
                }
            }
        } catch (Throwable e) {
            Log.e("SendMediaUriActivity", "convert pathToUri fail", e);
        }
        return null;
    }

    public void onSendUri(View view) {
        if (mExternalFileUri != null) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, mExternalFileUri);
            shareIntent.setType(getContentResolver().getType(mExternalFileUri));
            startActivity(Intent.createChooser(shareIntent, "Send MediaUri"));
        }
    }

    private void println(String msg) {
        sb.append(msg).append("\n\n");
        mTextView.setText(sb.toString());
    }
}
