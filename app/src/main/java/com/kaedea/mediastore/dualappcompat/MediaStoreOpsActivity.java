package com.kaedea.mediastore.dualappcompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

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

public class MediaStoreOpsActivity extends AppCompatActivity {

    final static String EXTERNAL_PATH = "Pictures/Weixin/test_img.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_store);

        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    112
            );
        } else {
            File externalFile = getExternalFile();
            if (!externalFile.exists()) {
                try {
                    IOUtils.copy(new FileInputStream(getInternalFile()), new FileOutputStream(externalFile));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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


    public void onPath2Uri(View view) {
        Uri uri = MediaStoreOps.pathToUri(this, getExternalFile().getAbsolutePath());
        Toast.makeText(
                view.getContext(),
                String.valueOf(uri),
                Toast.LENGTH_LONG
        ).show();
    }

    public void onUri2Path(View view) {
        Uri uri = MediaStoreOps.pathToUri(this, getExternalFile().getAbsolutePath());
        Toast.makeText(
                view.getContext(),
                uri == null ? "null URI" : MediaStoreOps.uriToPath(this, uri),
                Toast.LENGTH_LONG
        ).show();
    }

    public void onGetMediaStoreOutputPath(View view) {
        String outputPath = MediaStoreOps.configureOutputPath(this, getExternalFile().getAbsolutePath());
        Toast.makeText(view.getContext(), "outputPath: " + outputPath, Toast.LENGTH_LONG).show();
    }
}
