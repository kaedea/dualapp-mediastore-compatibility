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
import java.io.OutputStream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SmokeActivity extends AppCompatActivity {

    byte[] fileBytes;
    StringBuilder sb = new StringBuilder();
    TextView mTextView;

    String publicMediaFilePath;
    String publicMediaFilePathHide;
    String sdAppSpecificPath;
    String sdRootFilePath;
    String sdUnspecificFilePath;
    String otherAppPrivateFilePath;

    String publicMediaUri;
    String publicMediaUriHide;

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
        // File
        {
            {
                File file = new File(Environment.getExternalStorageDirectory() + "/Pictures/test_img.jpg");
                if (!file.exists()) {
                    try {
                        file.getParentFile().mkdirs();
                        try (FileOutputStream outputStream = new FileOutputStream(file)) {
                            outputStream.write(getInternalFileBytes());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (file.exists()) {
                    publicMediaFilePath = file.getAbsolutePath();
                }
            }

            {
                File file = new File(Environment.getExternalStorageDirectory() + "/Pictures/.temp/test_img.jpg");
                if (!file.exists()) {
                    try {
                        file.getParentFile().mkdirs();
                        try (FileOutputStream outputStream = new FileOutputStream(file)) {
                            outputStream.write(getInternalFileBytes());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (file.exists()) {
                    publicMediaFilePathHide = file.getAbsolutePath();
                }
            }

            {
                File file = new File(getApplicationContext().getExternalCacheDir(), "test_img.jpg");
                if (!file.exists()) {
                    try {
                        file.getParentFile().mkdirs();
                        try (FileOutputStream outputStream = new FileOutputStream(file)) {
                            outputStream.write(getInternalFileBytes());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (file.exists()) {
                    sdAppSpecificPath = file.getAbsolutePath();
                }
            }

            {
                File file = new File(Environment.getExternalStorageDirectory() + "/test_img.jpg");
                if (!file.exists()) {
                    try {
                        file.getParentFile().mkdirs();
                        try (FileOutputStream outputStream = new FileOutputStream(file)) {
                            outputStream.write(getInternalFileBytes());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (file.exists()) {
                    sdRootFilePath = file.getAbsolutePath();
                }
            }

            {
                File file = new File(Environment.getExternalStorageDirectory() + "/MyApp/test_img.jpg");
                if (!file.exists()) {
                    try {
                        file.getParentFile().mkdirs();
                        try (FileOutputStream outputStream = new FileOutputStream(file)) {
                            outputStream.write(getInternalFileBytes());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (file.exists()) {
                    sdUnspecificFilePath = file.getAbsolutePath();
                }
            }

            {
                String text = ClipBoardUtil.paste(this);
                if (!TextUtils.isEmpty(text)) {
                    if (text.startsWith("/")) {
                        otherAppPrivateFilePath = text;
                    }
                }
            }
        }

        // Uri
        {
            {
                File file = new File(Environment.getExternalStorageDirectory() + "/Pictures/test_img_uri.jpg");
                Uri uri = MediaStoreOps.pathToUri(getApplicationContext(), file.getAbsolutePath());
                if (uri == null) {
                    uri = MediaStoreOps.saveWithMediaStore(getApplicationContext(), getInternalFile().getAbsolutePath(), file.getAbsolutePath());
                }
                if (uri != null) {
                    publicMediaUri = uri.toString();
                }
            }
            {
                File file = new File(Environment.getExternalStorageDirectory() + "/Pictures/.temp/test_img_uri.jpg");
                Uri uri = MediaStoreOps.pathToUri(getApplicationContext(), file.getAbsolutePath());
                if (uri == null) {
                    try {
                        file.getParentFile().mkdirs();
                        File nomedia = new File(file.getParentFile(), ".nomedia");
                        if (!nomedia.exists()) {
                            nomedia.createNewFile();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    uri = MediaStoreOps.saveWithMediaStore(getApplicationContext(), getInternalFile().getAbsolutePath(), file.getAbsolutePath());
                }
                if (uri != null) {
                    publicMediaUriHide = uri.toString();
                }
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

    private byte[] getInternalFileBytes() {
        if (fileBytes == null) {
            try {
                InputStream is = getAssets().open("test_img.jpg");
                fileBytes = new byte[is.available()];
                //noinspection ResultOfMethodCallIgnored
                is.read(fileBytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return fileBytes;
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
        println((!TextUtils.isEmpty(sdAppSpecificPath) ? "🌝" : "🌚") + "sd_app_specific_file: " + (TextUtils.isEmpty(sdAppSpecificPath) ? "NULL" : sdAppSpecificPath));
        println((!TextUtils.isEmpty(sdRootFilePath) ? "🌝" : "🌚") + "sd_root_file: " + (TextUtils.isEmpty(sdRootFilePath) ? "NULL" : sdRootFilePath));
        println((!TextUtils.isEmpty(sdUnspecificFilePath) ? "🌝" : "🌚") + "sd_unspecific_file: " + (TextUtils.isEmpty(sdUnspecificFilePath) ? "NULL" : sdUnspecificFilePath));
        println((!TextUtils.isEmpty(otherAppPrivateFilePath) ? "🌝" : "🌚") + "3rd_app_file_path: " + (TextUtils.isEmpty(otherAppPrivateFilePath) ? "NULL" : otherAppPrivateFilePath));
        println("\n");
        println((!TextUtils.isEmpty(publicMediaUri) ? "🌝" : "🌚") + "public_media_uri: " + (TextUtils.isEmpty(publicMediaUri) ? "NULL" : publicMediaUri));
        println((!TextUtils.isEmpty(publicMediaUriHide) ? "🌝" : "🌚") + "public_media_uri(hide): " + (TextUtils.isEmpty(publicMediaUriHide) ? "NULL" : publicMediaUriHide));
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
        {
            String path = this.publicMediaFilePath;
            if (TextUtils.isEmpty(path)) {
                println("🌚: public media path missing");
            } else {
                testFileOps(path);
            }
        }

        println("\n");
        println("2. Test File: public medias (hide)");
        {
            String path = publicMediaFilePathHide;
            if (TextUtils.isEmpty(path)) {
                println("🌚: public media path (hide) missing");
            } else {
                testFileOps(path);
            }
        }

        println("\n");
        println("3. Test File: app-specific path");
        {
            String path = sdAppSpecificPath;
            if (TextUtils.isEmpty(path)) {
                println("🌚: sdcard app-specific path missing");
            } else {
                testFileOps(path);
            }
        }

        println("\n");
        println("4. Test File: sd root path");
        {
            String path = sdRootFilePath;
            if (TextUtils.isEmpty(path)) {
                println("🌚: sdcard root path missing");
            } else {
                testFileOps(path);
            }
        }

        println("\n");
        println("5. Test File: sd unspecific path");
        {
            String path = sdUnspecificFilePath;
            if (TextUtils.isEmpty(path)) {
                println("🌚: sdcard root path missing");
            } else {
                testFileOps(path);
            }
        }

        println("\n");
        println("6. Test File: 3rd app private path");
        {
            String path = otherAppPrivateFilePath;
            if (TextUtils.isEmpty(path)) {
                println("🌚: 3rd app private path missing, please copy it into CLIPBOARD!");
            } else {
                testFileOps(path);
            }
        }

        println("\n");
        println("A. Test Uri: public media uri");
        {
            String mediaUri = publicMediaUri;
            if (TextUtils.isEmpty(mediaUri)) {
                println("🌚: public media uri is missing");
            } else {
                testUriOpes(mediaUri);
            }
        }

        println("\n");
        println("B. Test Uri: public media uri(hide)");
        {
            String mediaUri = publicMediaUriHide;
            if (TextUtils.isEmpty(mediaUri)) {
                println("🌚: public media uri(hide) is missing");
            } else {
                testUriOpes(mediaUri);
            }
        }

        println("\n");
        println("C. Test Uri: from sd root file");
        {
            Uri mediaUri = MediaStoreOps.pathToUri(getApplicationContext(), new File(Environment.getExternalStorageDirectory() + "/test_img.jpg").getAbsolutePath());
            if (mediaUri == null) {
                println("🌚: sd root uri is missing");
            } else {
                testUriOpes(mediaUri.toString());
            }
        }

        println("\n");
        println("D. Test Uri: from sd unspecific file");
        {
            Uri mediaUri = MediaStoreOps.pathToUri(getApplicationContext(), new File(Environment.getExternalStorageDirectory() + "/MyApp/test_img.jpg").getAbsolutePath());
            if (mediaUri == null) {
                println("🌚: sd unspecific uri is missing");
            } else {
                testUriOpes(mediaUri.toString());
            }
        }

        println("----------\n");
    }

    private void testFileOps(String path) {
        println(path);

        // test file exists
        if (new File(path).exists()) {
            println("🌝: file exists");
        } else {
            println("🌚: file exists");
        }

        // test read file
        try (InputStream inputStream = new FileInputStream(path)) {
            println("🌝: file read: size=" + inputStream.available());
        } catch (IOException e) {
            println("🌚: file read, " + e.getMessage());
        }

        // test write file
        try (FileOutputStream outputStream = new FileOutputStream(path)) {
            outputStream.write(getInternalFileBytes());
            println("🌝: file write");
        } catch (IOException e) {
            println("🌚: file write, " + e.getMessage());
        }

        // test delete file
        boolean delete = false;
        try {
            delete = new File(path).delete();
            println((delete ? "🌝" : "🌚") + ": file delete = " + delete);
        } catch (Exception e) {
            println("🌚: file delete, " + e.getMessage());
        }

        // test recover(rewrite) file
        if (delete) {
            try (FileOutputStream outputStream = new FileOutputStream(path)) {
                outputStream.write(getInternalFileBytes());
                println("🌝: file recover(rewrite)");
            } catch (IOException e) {
                println("🌚: file recover(rewrite), " + e.getMessage());
            }
        }

        // test path-uri convert
        Uri uri = MediaStoreOps.pathToUri(this, path);
        if (uri != null) {
            println("🌝: path2Uri = " + uri);
            String uriToPath = MediaStoreOps.uriToPath(this, uri);
            if (!TextUtils.isEmpty(uriToPath)) {
                println("🌝: uri2Path = " + uriToPath);
            } else {
                println("🌚: uri2Path = NULL");
            }
        } else {
            println("🌚: path2Uri = NULL");
        }
    }

    private void testUriOpes(String mediaUri) {
        println(mediaUri);
        Uri uri = Uri.parse(mediaUri);
        // test read media
        try (InputStream inputStream = MediaStoreOps.readWithMediaStore(getApplicationContext(), uri)) {
            if (inputStream != null) {
                try {
                    byte[] buffer = new byte[inputStream.available()];
                    //noinspection ResultOfMethodCallIgnored
                    inputStream.read(buffer);
                    println("🌝: uri read");
                } catch (IOException e) {
                    println("🌚: uri read, " + e.getMessage());
                }
            } else {
                println("🌚: uri read inputStream, null");
            }
        } catch (IOException e) {
            println("🌚: uri read inputStream, " + e.getMessage());
        }

        // test write media
        try (OutputStream outputStream = MediaStoreOps.writeWithMediaStore(getApplicationContext(), uri)) {
            if (outputStream != null) {
                try {
                    outputStream.write(getInternalFileBytes());
                    println("🌝: uri write");
                } catch (IOException e) {
                    println("🌚: uri write, " + e.getMessage());
                }
            } else {
                println("🌚: uri write outputStream, null");
            }
        } catch (IOException e) {
            println("🌚: uri write outputStream, " + e.getMessage());
        }

        // If we delete media by uri, the uri can never be open/reused again!
        // Therefore the delete test is skipped here.
        //
        // // test delete media
        // boolean delete = MediaStoreOps.deleteWithMediaStore(getApplicationContext(), uri);
        // if (delete) {
        //     println("🌝: uri delete");
        // } else {
        //     println("🌚: uri delete");
        // }
        //
        // // test recover(rewrite) media
        // if (delete) {
        //     try (OutputStream outputStream = MediaStoreOps.writeWithMediaStore(getApplicationContext(), uri)) {
        //         if (outputStream != null) {
        //             try {
        //                 outputStream.write(getInternalFileBytes());
        //                 println("🌝: uri recover(rewrite)");
        //             } catch (IOException e) {
        //                 println("🌚: uri recover(rewrite), " + e.getMessage());
        //             }
        //         } else {
        //             println("🌚: uri recover(rewrite) outputStream, null");
        //         }
        //     } catch (IOException e) {
        //         println("🌚: uri recover(rewrite) outputStream, " + e.getMessage());
        //     }
        // }

        // test path-uri convert
        String uriToPath = MediaStoreOps.uriToPath(this, uri);
        if (!TextUtils.isEmpty(uriToPath)) {
            println("🌝: uri2Path = " + uriToPath);
            Uri pathToUri = MediaStoreOps.pathToUri(this, uriToPath);
            if (pathToUri != null) {
                println("🌝: path2Uri = " + pathToUri);
            } else {
                println("🌚: path2Uri = NULL");
            }
        } else {
            println("🌚: uri2Path = NULL");
        }
    }
}
