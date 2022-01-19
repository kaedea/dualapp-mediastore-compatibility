package com.kaedea.mediastore.dualappcompat;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.atomic.AtomicBoolean;

import androidx.appcompat.app.AppCompatActivity;

public class ToastCompatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toast);
    }

    public void onToast(View view) {
        Toast toast = Toast.makeText(view.getContext(), "This is a normal toast", Toast.LENGTH_LONG);
        toast.show();
    }

    public void onToastSafely(View view) {
        safeToast(view.getContext(), "This is a safe toast");
    }

    public void onCheckToastCompat(View view) {
        safeToast(view.getContext(), "Compat: " + checkToastCompatibility(view.getContext()));
    }

    private void safeToast(Context context, String msg) {
        Toast toast = new Toast(context);
        final TextView toastTV = new TextView(context);
        toastTV.setText(msg);
        toast.setView(toastTV);
        toast.show();
    }

    static AtomicBoolean sToastCompat = null;
    static boolean checkToastCompatibility(Context context) {
        if (sToastCompat != null) {
            return sToastCompat.get();
        }
        if (Build.VERSION.SDK_INT >= 30 && context.getApplicationInfo().targetSdkVersion >= 30) {
            String manufacture = String.valueOf(Build.MANUFACTURER);
            if (manufacture.equalsIgnoreCase("vivo")
                    || manufacture.equalsIgnoreCase("huawei")
                    || manufacture.equalsIgnoreCase("honor")) {
                sToastCompat = new AtomicBoolean(false);
                return sToastCompat.get();
            }
        }
        sToastCompat = new AtomicBoolean(true);
        return sToastCompat.get();
    }
}
