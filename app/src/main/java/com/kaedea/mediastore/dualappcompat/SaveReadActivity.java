package com.kaedea.mediastore.dualappcompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.kaedea.mediastore.dualappcompat.utils.MediaStoreOps;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SaveReadActivity extends AppCompatActivity {

    public static final String EXTERNAL_PATH_PREFIX = "Pictures/Weixin/test_img_";
    public static final AtomicInteger INC = new AtomicInteger();

    StringBuilder sb = new StringBuilder();
    TextView mTextView;
    Uri mExternalFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_read_file);

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
    }

    public void onWriteImg(View view) {
        INC.incrementAndGet();
        File internalFile = getInternalFile();
        File externalFile = getExternalFile();

        println("write externalFile to: " + externalFile.getAbsolutePath());
        if (externalFile.exists()) {
            externalFile.delete();
        } else {
            externalFile.getParentFile().mkdirs();
        }

        Uri uri = MediaStoreOps.saveWithMediaStore(this, internalFile.getAbsolutePath(), externalFile.getAbsolutePath());
        if (uri != null) {
            println("uri: " + uri);
        } else {
            println("saveWithMediaStore fail!");
        }

        println("-------------------");
    }

    public void onReadImg(View view) {
        String filePath = getExternalFile().getAbsolutePath();
        println("try query uri of saved file: " + filePath);
        // String fileName = new File(filePath).getName();
        // Uri uri = MediaStoreOps.queryUriByName(view.getContext(), fileName);
        Uri uri = MediaStoreOps.queryUriByRelativePath(view.getContext(), filePath);
        if (uri != null) {
            println("uri: " + uri);
        } else {
            println("try query saved file fail! ");
        }

        println("-------------------");
    }

    private File getExternalFile() {
        return new File(Environment.getExternalStorageDirectory(), EXTERNAL_PATH_PREFIX + INC.get() + ".jpg");
    }

    private File getInternalFile() {
        File file = new File(this.getFilesDir(), "test_img.jpg");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                InputStream is = getAssets().open("test_img.jpg");
                IOUtils.copy(is, new FileOutputStream(file));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return file;
    }

    private void println(String msg) {
        sb.append(msg).append("\n\n");
        mTextView.setText(sb.toString());
        Log.i("DualAppMediaStoreTest", sb.toString());
    }


}
