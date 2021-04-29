package com.kaedea.mediastore.dualappcompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    public static final String EXTERNAL_PATH = "Pictures/Weixin/test_img.jpg";

    StringBuilder sb = new StringBuilder();
    TextView mTextView;
    Uri mExternalFileUri;
    String mExternalFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        File internalFile = getInternalFile();
        File externalFile = getExternalFile();

        println("write externalFile to: " + externalFile.getAbsolutePath());
        if (externalFile.exists()) {
            externalFile.delete();
        } else {
            externalFile.getParentFile().mkdirs();
        }

        Uri uri = MediaStoreOps.saveWithMediaStore(this, internalFile.getAbsolutePath(), externalFile.getAbsolutePath());
        if (uri == null) {
            println("saveWithMediaStore fail!");
            try {
                println("saveWithFile instead...");
                IOUtils.copy(new FileInputStream(internalFile), new FileOutputStream(externalFile));
                mExternalFilePath = externalFile.getAbsolutePath();
                println("saveWithFile success");
            } catch (IOException e) {
                println("fail: " + e.getMessage());
            }
        } else {
            println("saveWithMediaStore success: " + uri.toString());
            mExternalFileUri = uri;
        }

        println("-------------------");
    }

    public void onReadImgByUri(View view) {
        if (mExternalFileUri == null) {
            println("pls saveImg or path2Uri first");

        } else {
            println("uriToPath: " + mExternalFileUri.toString());
            String uriToPath = MediaStoreOps.uriToPath(this, mExternalFileUri);
            if (!TextUtils.isEmpty(uriToPath)) {
                println("uriToPath success: " + uriToPath);
                mExternalFilePath = uriToPath;
            } else {
                println("uriToPath fail");
            }
        }

        println("-------------------");
    }

    public void onReadImgByPath(View view) {
        if (mExternalFilePath == null) {
            println("pls saveImg or uri2Path first");
        } else {
            Uri pathToUri = MediaStoreOps.pathToUri(this, mExternalFilePath);
            if (pathToUri != null) {
                println("pathToUri success: " + pathToUri.toString());
                mExternalFileUri = pathToUri;
            } else {
                println("pathToUri fail");
            }
        }

        println("-------------------");
    }

    private File getExternalFile() {
        return new File(Environment.getExternalStorageDirectory(), EXTERNAL_PATH);
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
    }
}
