package com.kaedea.mediastore.dualappcompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.UserHandle;
import android.os.UserManager;
import android.view.View;
import android.widget.Toast;

import com.kaedea.mediastore.dualappcompat.utils.DoubleReflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

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

    public void onCheckNotificationPermission(View view) {
        if (android.os.Build.VERSION.SDK_INT >= 33 || Build.VERSION.PREVIEW_SDK_INT >= 33 || Build.VERSION.CODENAME.equals("Tiramisu")) {
            boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED);
            if (!hasPermission) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 112
                );
            }
            Toast.makeText(view.getContext(), "hasPermission: " + hasPermission, Toast.LENGTH_LONG).show();
        }


    }

    public void onCheckUidCapacity(View view) {
        try {
            IBinder binder = getCurrentBinder(Context.ACTIVITY_SERVICE);
            if (binder != null) {
                Object manager = createServiceManager("android.app.IActivityManager", binder);
                if (manager != null) {
                    Class clazz = Class.forName("android.app.IActivityManager");
                    Method method = DoubleReflect.findMethod(clazz, "isUidActive", new Class[]{int.class, String.class});
                    if (method != null) {
                        try {
                            Object ret = method.invoke(manager, view.getContext().getApplicationInfo().uid, view.getContext().getPackageName());
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                            e.printStackTrace();
                            Toast.makeText(view.getContext(), "Error: " + e.getCause().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    method = DoubleReflect.findMethod(clazz, "getUidProcessState", new Class[]{int.class, String.class});
                    if (method != null) {
                        try {
                            Object ret = method.invoke(manager, view.getContext().getApplicationInfo().uid, view.getContext().getPackageName());
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                            e.printStackTrace();
                            Toast.makeText(view.getContext(), "Error: " + e.getCause().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    method = DoubleReflect.findMethod(clazz, "getUidProcessCapabilities", new Class[]{int.class, String.class});
                    if (method != null) {
                        try {
                            Object ret = method.invoke(manager, view.getContext().getApplicationInfo().uid, view.getContext().getPackageName());
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                            e.printStackTrace();
                            Toast.makeText(view.getContext(), "Error: " + e.getCause().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static IBinder getCurrentBinder(String serviceName) throws Exception {
        Class<?> serviceManagerCls = Class.forName("android.os.ServiceManager");
        Method getService = serviceManagerCls.getDeclaredMethod("getService", String.class);
        return  (IBinder) getService.invoke(null, serviceName);
    }

    private static Object createServiceManager(String serviceClassName, IBinder originBinder) throws Exception  {
        Class<?> serviceManagerCls = Class.forName(serviceClassName);
        Class<?> serviceManagerStubCls = Class.forName(serviceClassName + "$Stub");
        ClassLoader classLoader = serviceManagerStubCls.getClassLoader();
        if (classLoader == null) {
            throw new IllegalStateException("get service manager ClassLoader fail!");
        }
        Method asInterfaceMethod = serviceManagerStubCls.getDeclaredMethod("asInterface", IBinder.class);
        return asInterfaceMethod.invoke(null, originBinder);
    }
}
