package com.kaedea.mediastore.dualappcompat;

import androidx.appcompat.app.AppCompatActivity;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
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

    public void onCheckUserId(View view) {
        String tips = "UserId: " + android.os.Process.myUserHandle().hashCode();
        UserManager userManager = (UserManager) view.getContext().getSystemService(Context.USER_SERVICE);
        List<UserHandle> userProfiles = userManager.getUserProfiles();
        List<Integer> userIds = new ArrayList<>();
        for (UserHandle item : userProfiles) {
            userIds.add(item.hashCode());
        }
        tips += " in " + userIds;
        tips += "\n" + "SystemUser: " + userManager.isSystemUser();
        Toast.makeText(view.getContext(), tips, Toast.LENGTH_LONG).show();
    }

    public void onCheckUserPath(View view) {
        String userPath = "unknown";
        String dataPath = view.getContext().getDataDir().getAbsolutePath();
        String packageName = view.getContext().getPackageName();
        if (dataPath.contains(packageName)) {
            userPath = dataPath.substring(0, dataPath.indexOf(packageName));
        }
        Toast.makeText(view.getContext(), "UserPath: " + userPath, Toast.LENGTH_LONG).show();
    }
}
