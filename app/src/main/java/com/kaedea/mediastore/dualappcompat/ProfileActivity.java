package com.kaedea.mediastore.dualappcompat;

import androidx.appcompat.app.AppCompatActivity;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
    }

    public void onCheckProfile(View view) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) view.getContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (devicePolicyManager != null) {
            List<ComponentName> activeAdmins = devicePolicyManager.getActiveAdmins();
            if (activeAdmins != null) {
                for (ComponentName admin : activeAdmins) {
                    String packageName = admin.getPackageName();
                    if (devicePolicyManager.isProfileOwnerApp(packageName)) {
                        Toast.makeText(view.getContext(), "App is running as WorkProfile", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
        Toast.makeText(view.getContext(), "App is running as HostProfile", Toast.LENGTH_LONG).show();
    }
}