package com.kaedea.mediastore.dualappcompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.kaedea.mediastore.dualappcompat.utils.ClipBoardUtil;
import com.kaedea.mediastore.dualappcompat.utils.MediaStoreOps;
import com.kaedea.mediastore.dualappcompat.utils.WorkProfiles;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SmokeActivity extends AppCompatActivity {

    StringBuilder sb = new StringBuilder();
    TextView mTextView;
    String publicMediaFilePath;
    String publicMediaFilePathHide;
    String sdRootFilePath;
    String sdUnspecificFilePath;
    String otherAppPrivateFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smoke);

        mTextView = findViewById(R.id.text);
        mTextView.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                prepareFilePath();
                dumpTestStat();
            }
        });
    }

    private void println(String msg) {
        sb.append(msg).append("\n");
        mTextView.setText(sb.toString());
    }
    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 112 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            dumpTestStat();
        }
    }

    private void prepareFilePath() {
        File file = new File(Environment.getExternalStorageDirectory() + "/Pictures/test_img.jpg");
        if (file.exists()) {
            publicMediaFilePath = file.getAbsolutePath();
        } else {
            try {
                file.getParentFile().mkdirs();
                IOUtils.copy(new FileInputStream(getInternalFile()), new FileOutputStream(file));
                publicMediaFilePath = file.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        file = new File(Environment.getExternalStorageDirectory() + "/Pictures/.temp/test_img.jpg");
        if (file.exists()) {
            publicMediaFilePathHide = file.getAbsolutePath();
        } else {
            try {
                file.getParentFile().mkdirs();
                IOUtils.copy(new FileInputStream(getInternalFile()), new FileOutputStream(file));
                File nomedia = new File(file.getParentFile(), ".nomedia");
                if (!nomedia.exists()) {
                    nomedia.createNewFile();
                }
                publicMediaFilePathHide = file.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        file = new File(Environment.getExternalStorageDirectory() + "/test_img.jpg");
        if (file.exists()) {
            sdRootFilePath = file.getAbsolutePath();
        } else {
            try {
                file.getParentFile().mkdirs();
                IOUtils.copy(new FileInputStream(getInternalFile()), new FileOutputStream(file));
                sdRootFilePath = file.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        file = new File(Environment.getExternalStorageDirectory() + "/MyApp/test_img.jpg");
        if (file.exists()) {
            sdUnspecificFilePath = file.getAbsolutePath();
        } else {
            try {
                file.getParentFile().mkdirs();
                IOUtils.copy(new FileInputStream(getInternalFile()), new FileOutputStream(file));
                sdUnspecificFilePath = file.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String text = ClipBoardUtil.paste(this);
        if (!TextUtils.isEmpty(text)) {
            if (text.startsWith("/")) {
                otherAppPrivateFilePath = text;
            }
        }
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

    private void dumpTestStat() {
        int sdkVersion = Build.VERSION.SDK_INT;
        int targetSdkVersion = getApplicationContext().getApplicationInfo().targetSdkVersion;
        boolean hasPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        String userInfo = WorkProfiles.getAppUserInfo(this);
        boolean dualApp = WorkProfiles.isRunningInDualApp(this);
        String userProfile = WorkProfiles.getAppProfile(this);
        println("android-sdk: " + sdkVersion);
        println("target-version: " + targetSdkVersion);
        println("sdcard permission: " + hasPermission);
        println("user_info: " + userInfo);
        println("user_profile: " + userProfile);
        println("dual_app: " + dualApp);
        println("\n");
        println((!TextUtils.isEmpty(publicMediaFilePath) ? "🌝" : "🌚") + "public_media_file: " + (TextUtils.isEmpty(publicMediaFilePath) ? "NULL" : publicMediaFilePath));
        println((!TextUtils.isEmpty(publicMediaFilePathHide) ? "🌝" : "🌚") + "public_media_file(hide): " + (TextUtils.isEmpty(publicMediaFilePathHide) ? "NULL" : publicMediaFilePathHide));
        println((!TextUtils.isEmpty(sdRootFilePath) ? "🌝" : "🌚") + "sd_root_file: " + (TextUtils.isEmpty(sdRootFilePath) ? "NULL" : sdRootFilePath));
        println((!TextUtils.isEmpty(sdUnspecificFilePath) ? "🌝" : "🌚") + "sd_unspecific_file: " + (TextUtils.isEmpty(sdUnspecificFilePath) ? "NULL" : sdUnspecificFilePath));
        println((!TextUtils.isEmpty(otherAppPrivateFilePath) ? "🌝" : "🌚") + "3rd_app_file_path: " + (TextUtils.isEmpty(otherAppPrivateFilePath) ? "NULL" : otherAppPrivateFilePath));
        println("----------\n");
    }

    public void requestSdCardPermission(View view) {
        boolean hasPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (hasPermission) {
            toast("Granted");
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    112
            );
        }
    }

    public void smoking(View view) {
        println("Run smoking ...");
        println("\n");
        smoking();
    }

    public void smokingBg(View view) {
        toast("Run smoke in 5s, pls switch to bg");
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                println("Run smoking bg ...");
                println("\n");
                smoking();
                toast("Run smoke done");
            }
        }, 5000L);
    }

    private void smoking() {
        println("1. Test File: public medias");
        if (TextUtils.isEmpty(publicMediaFilePath)) {
            println("🌚: public media path missing");
        } else {
            println(publicMediaFilePath);
            if (new File(publicMediaFilePath).exists()) {
                println("🌝: file exists");
            } else {
                println("🌚: file exists");
            }

            try {
                new FileInputStream(new File(publicMediaFilePath));
                println("🌝: file open");
            } catch (FileNotFoundException e) {
                println("🌚: file open, " + e.getMessage());
            }
            try {
                IOUtils.copy(new FileInputStream(publicMediaFilePath), new FileOutputStream(publicMediaFilePath + "_temp"));
                println("🌝: file copy");
            } catch (IOException e) {
                println("🌚: file copy, " + e.getMessage());
            }

            Uri uri = MediaStoreOps.pathToUri(this, publicMediaFilePath);
            if (uri != null) {
                println("🌝: path2Uri = " + uri);
                String path = MediaStoreOps.uriToPath(this, uri);
                if (!TextUtils.isEmpty(path)) {
                    println("🌝: uri2Path = " + path);
                } else {
                    println("🌚: uri2Path = NULL");
                }
            } else {
                println("🌚: path2Uri = NULL");
            }
        }

        println("\n");
        println("2. Test File: public medias (hide)");
        if (TextUtils.isEmpty(publicMediaFilePathHide)) {
            println("🌚: public media path (hide) missing");
        } else {
            println(publicMediaFilePathHide);
            if (new File(publicMediaFilePathHide).exists()) {
                println("🌝: file exists");
            } else {
                println("🌚: file exists");
            }

            try {
                new FileInputStream(new File(publicMediaFilePathHide));
                println("🌝: file open");
            } catch (FileNotFoundException e) {
                println("🌚: file open, " + e.getMessage());
            }
            try {
                IOUtils.copy(new FileInputStream(publicMediaFilePathHide), new FileOutputStream(publicMediaFilePathHide + "_temp"));
                println("🌝: file copy");
            } catch (IOException e) {
                println("🌚: file copy, " + e.getMessage());
            }

            Uri uri = MediaStoreOps.pathToUri(this, publicMediaFilePathHide);
            if (uri != null) {
                println("🌝: path2Uri = " + uri);
                String path = MediaStoreOps.uriToPath(this, uri);
                if (!TextUtils.isEmpty(path)) {
                    println("🌝: uri2Path = " + path);
                } else {
                    println("🌚: uri2Path = NULL");
                }
            } else {
                println("🌚: path2Uri = NULL");
            }
        }

        println("\n");
        println("3. Test File: sd root path");
        if (TextUtils.isEmpty(sdRootFilePath)) {
            println("🌚: sdcard root path missing");
        } else {
            println(sdRootFilePath);
            if (new File(sdRootFilePath).exists()) {
                println("🌝: file exists");
            } else {
                println("🌚: file exists");
            }

            try {
                new FileInputStream(new File(sdRootFilePath));
                println("🌝: file open");
            } catch (FileNotFoundException e) {
                println("🌚: file open, " + e.getMessage());
            }
            try {
                IOUtils.copy(new FileInputStream(sdRootFilePath), new FileOutputStream(sdRootFilePath + "_temp"));
                println("🌝: file copy");
            } catch (IOException e) {
                println("🌚: file copy, " + e.getMessage());
            }
        }

        println("\n");
        println("4. Test File: sd unspecific path");
        if (TextUtils.isEmpty(sdUnspecificFilePath)) {
            println("🌚: sdcard root path missing");
        } else {
            println(sdUnspecificFilePath);
            if (new File(sdUnspecificFilePath).exists()) {
                println("🌝: file exists");
            } else {
                println("🌚: file exists");
            }

            try {
                new FileInputStream(new File(sdUnspecificFilePath));
                println("🌝: file open");
            } catch (FileNotFoundException e) {
                println("🌚: file open, " + e.getMessage());
            }
            try {
                IOUtils.copy(new FileInputStream(sdUnspecificFilePath), new FileOutputStream(sdUnspecificFilePath + "_temp"));
                println("🌝: file copy");
            } catch (IOException e) {
                println("🌚: file copy, " + e.getMessage());
            }
        }

        println("\n");
        println("5. Test File: 3rd app private path");
        if (TextUtils.isEmpty(otherAppPrivateFilePath)) {
            println("🌚: 3rd app private path missing, please copy it into CLIPBOARD!");
        } else {
            println(otherAppPrivateFilePath);
            if (new File(otherAppPrivateFilePath).exists()) {
                println("🌝: file exists");
            } else {
                println("🌚: file exists");
            }

            try {
                new FileInputStream(new File(otherAppPrivateFilePath));
                println("🌝: file open");
            } catch (FileNotFoundException e) {
                println("🌚: file open, " + e.getMessage());
            }
            try {
                IOUtils.copy(new FileInputStream(otherAppPrivateFilePath), new FileOutputStream(otherAppPrivateFilePath + "_temp"));
                println("🌝: file copy");
            } catch (IOException e) {
                println("🌚: file copy, " + e.getMessage());
            }
        }

        println("----------\n");
    }
}
