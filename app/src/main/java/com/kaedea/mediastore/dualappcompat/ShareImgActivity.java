package com.kaedea.mediastore.dualappcompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.kaedea.mediastore.dualappcompat.utils.MediaStoreOps;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class ShareImgActivity extends AppCompatActivity {

    StringBuilder sb = new StringBuilder();
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        mTextView = findViewById(R.id.text);
        mTextView.setMovementMethod(new ScrollingMovementMethod());

        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    112
            );
        }

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);

            println("Receive ACTION_SEND Uri: " + imageUri);
            if (imageUri != null) {
                println("readImgByUri: " + imageUri.toString());
                try (InputStream inputStream = this.getContentResolver().openInputStream(imageUri)) {
                    File tempFile = new File(this.getCacheDir(), System.currentTimeMillis() + ".jpg");
                    tempFile.getParentFile().mkdir();
                    tempFile.createNewFile();
                    println("copy2File: " + tempFile.getAbsolutePath());
                    IOUtils.copy(inputStream, new FileOutputStream(tempFile));
                    println("copy2File success: " + tempFile.exists());

                } catch (Exception e) {
                    println("readImgByUri fail: " +  e.getMessage());
                }

                println("uriToPath: " + imageUri);
                String uriToPath = MediaStoreOps.uriToPath(this, imageUri);
                if (uriToPath != null) {
                    println("uriToPath success: " + uriToPath);
                    println("readImgByPath: " + uriToPath);
                    File file = new File(uriToPath);
                    println("fileExits: " + file.exists());

                    try (InputStream inputStream = new FileInputStream(file)) {
                        File tempFile = new File(this.getCacheDir(), System.currentTimeMillis() + ".jpg");
                        tempFile.getParentFile().mkdir();
                        tempFile.createNewFile();
                        println("copy2File: " + tempFile.getAbsolutePath());
                        IOUtils.copy(inputStream, new FileOutputStream(tempFile));
                        println("copy2File success: " + tempFile.exists());

                    } catch (Exception e) {
                        println("readImgByPath fail: " +  e.getMessage());
                    }
                } else {
                    println("uriToPath fail!");
                }

                println("-------------------");
            }
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            println("Receive ACTION_SEND Uri: " + uri);
        }
    }

    private void println(String msg) {
        sb.append(msg).append("\n\n");
        mTextView.setText(sb.toString());
    }
}
