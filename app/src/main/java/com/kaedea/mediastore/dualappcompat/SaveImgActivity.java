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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SaveImgActivity extends AppCompatActivity {

    public static final String EXTERNAL_PATH = "Pictures/Weixin/test_img.jpg";

    StringBuilder sb = new StringBuilder();
    TextView mTextView;
    Uri mExternalFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_file);

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
            String filePath = getExternalFile().getAbsolutePath();
            println("tryQueryUriByPath: " + filePath);
            Uri uri = MediaStoreOps.pathToUri(view.getContext(), filePath);
            if (uri == null) {
                println("tryQueryUriByPath fail!" );
                println("pls saveImg or path2Uri first");
                return;
            }
            mExternalFileUri = uri;
        }

        println("readImgByUri: " + mExternalFileUri.toString());
        try (InputStream inputStream = view.getContext().getContentResolver().openInputStream(mExternalFileUri)) {
            File tempFile = new File(view.getContext().getCacheDir(), System.currentTimeMillis() + ".jpg");
            tempFile.getParentFile().mkdir();
            tempFile.createNewFile();
            println("copy2File: " + tempFile.getAbsolutePath());
            IOUtils.copy(inputStream, new FileOutputStream(tempFile));
            println("copy2File success: " + tempFile.exists());

        } catch (Exception e) {
            println("readImgByUri fail: " +  e.getMessage());
        }

        println("-------------------");
    }

    public void onReadImgByPath(View view) {
        File externalFile = getExternalFile();
        println("readImgByPath: " + externalFile.getAbsolutePath());
        if (!externalFile.exists()) {
            println("readImgByPath fail: file not found");
            println("pls saveImg or path2Uri first");
            return;
        }

        try (InputStream inputStream = new FileInputStream(externalFile)) {
            File tempFile = new File(view.getContext().getCacheDir(), System.currentTimeMillis() + ".jpg");
            tempFile.getParentFile().mkdir();
            tempFile.createNewFile();
            println("copy2File: " + tempFile.getAbsolutePath());
            IOUtils.copy(inputStream, new FileOutputStream(tempFile));
            println("copy2File success: " + tempFile.exists());

        } catch (Exception e) {
            println("readImgByPath fail: " +  e.getMessage());
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
        Log.i("DualAppMediaStoreTest", sb.toString());
    }


}
