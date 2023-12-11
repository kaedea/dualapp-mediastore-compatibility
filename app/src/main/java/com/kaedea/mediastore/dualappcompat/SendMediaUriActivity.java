package com.kaedea.mediastore.dualappcompat;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import com.kaedea.mediastore.dualappcompat.utils.FileProviderOps;
import com.kaedea.mediastore.dualappcompat.utils.MediaStoreOps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION;

public class SendMediaUriActivity extends AppCompatActivity {


    StringBuilder sb = new StringBuilder();
    TextView mTextView;

    Uri mExternalImagesUri;
    Uri mExternalDownloadsImageUri;
    Uri mExternalDownloadsDocumentUri;
    Uri mExternalFilesImageUri;
    Uri mExternalFilesDocumentUri;

    File mInnerImageToSend;
    File mInnerDocumentToSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_media_uri);
        mTextView = findViewById(R.id.text);
        mTextView.setMovementMethod(new ScrollingMovementMethod());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mExternalImagesUri = queryLastMediaUri(this, MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL), null);
            mExternalDownloadsImageUri = queryLastMediaUri(this, MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL), "image/jpeg");
            mExternalDownloadsDocumentUri = queryLastMediaUri(this, MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL), "application/rar");
            mExternalFilesImageUri = queryLastMediaUri(this, MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL), "image/jpeg");
            mExternalFilesDocumentUri = queryLastMediaUri(this, MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL), "application/rar");

            if (mExternalImagesUri != null) {
                mInnerImageToSend = new File(getFilesDir(), "shared/" + DocumentFile.fromSingleUri(this, mExternalImagesUri).getName());
                File file = mInnerImageToSend;
                try {
                    file.delete();
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                    try (InputStream in = getContentResolver().openInputStream(mExternalImagesUri)) {
                        try (OutputStream out = new FileOutputStream(file)) {
                            FileUtils.copy(in, out);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            if (mExternalFilesDocumentUri != null) {
                mInnerDocumentToSend = new File(getFilesDir(), "shared/" + DocumentFile.fromSingleUri(this, mExternalFilesDocumentUri).getName());
                File file = mInnerDocumentToSend;
                try {
                    file.delete();
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                    try (InputStream in = getContentResolver().openInputStream(mExternalFilesDocumentUri)) {
                        try (OutputStream out = new FileOutputStream(file)) {
                            FileUtils.copy(in, out);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        println("Images-Uri: " + mExternalImagesUri
                + "\nDownloads-ImgUri:" + mExternalDownloadsImageUri
                + "\nDownloads-DocUri:" + mExternalDownloadsDocumentUri
                + "\nFiles-ImgUri:" + mExternalFilesImageUri
                + "\nFiles-DocUri:" + mExternalFilesDocumentUri
                + "\nInnerImage:" + mInnerImageToSend + "("  + (mInnerImageToSend != null && mInnerImageToSend.exists()) + ")"
                + "\nInnerFile:" + mInnerDocumentToSend + "("  + (mInnerDocumentToSend != null && mInnerDocumentToSend.exists()) + ")"
                + "\n"
                + "\nFileProviders:" + FileProviderOps.getAllFileProviders(this).size()
                + "\nFileProvidersR2:" + FileProviderOps.getAllFileProvidersR2(this).size()
        );


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                this.startActivity(new Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private static Uri queryLastMediaUri(@NonNull Context context, Uri contentUri, String mimeType) {
        try (Cursor cursor = context.getContentResolver().query(
                contentUri,
                new String[]{MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.MIME_TYPE},
                null,
                null
        )) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (TextUtils.isEmpty(mimeType)) {
                        return Uri.withAppendedPath(contentUri, String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))));
                    }
                    int columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE);
                    if (columnIndex >= 0) {
                        String type = cursor.getString(columnIndex);
                        if (mimeType.equals(type)) {
                            return Uri.withAppendedPath(contentUri, String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))));
                        }
                    }
                }
            }
        } catch (Throwable e) {
            Log.e("SendMediaUriActivity", "queryLastMediaUri fail", e);
        }
        return null;
    }

    public void onSendImagesUri(View view) {
        if (mExternalImagesUri != null) {
            onSendMediaUri(mExternalImagesUri);
        }
    }

    public void onSendDownloadsImageUri(View view) {
        if (mExternalDownloadsImageUri != null) {
            onSendMediaUri(mExternalDownloadsImageUri);
        }
    }

    public void onSendDownloadsDocumentUri(View view) {
        if (mExternalDownloadsDocumentUri != null) {
            onSendMediaUri(mExternalDownloadsDocumentUri);
        }
    }

    public void onSendFilesImageUri(View view) {
        if (mExternalFilesImageUri != null) {
            onSendMediaUri(mExternalFilesImageUri);
        }
    }

    public void onSendFilesDocumentUri(View view) {
        if (mExternalFilesDocumentUri != null) {
            onSendMediaUri(mExternalFilesDocumentUri);
        }
    }

    private void onSendMediaUri(Uri uri) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setDataAndType(uri, getContentResolver().getType(uri));
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Send MediaUri"));
    }

    public void onSendFileProviderImageUri(View view) {
        if (mInnerImageToSend != null) {
            Uri fileUri = FileProvider.getUriForFile(this, this.getPackageName() + ".fileprovider", mInnerImageToSend);
            Toast.makeText(this, "uri2path:" + MediaStoreOps.uriToPath(this, fileUri), Toast.LENGTH_SHORT).show();
            onSendFileProviderUri(fileUri);
        }
    }

    public void onSendFileProviderDocumentUri(View view) {
        if (mInnerDocumentToSend != null) {
            Uri fileUri = FileProvider.getUriForFile(this, this.getPackageName() + ".fileprovider", mInnerDocumentToSend);
            Toast.makeText(this, "uri2path:" + MediaStoreOps.uriToPath(this, fileUri), Toast.LENGTH_SHORT).show();
            onSendFileProviderUri(fileUri);
        }
    }

    private void onSendFileProviderUri(Uri uri) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setDataAndType(uri, getContentResolver().getType(uri));
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Send FileProviderUri"));
    }

    private void println(String msg) {
        sb.append(msg).append("\n\n");
        mTextView.setText(sb.toString());
    }
}
