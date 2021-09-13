package com.kaedea.mediastore.dualappcompat.utils;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kaede
 * @since 2021/10/20
 */
public final class WorkProfiles {
    public static final String PROFILE_HOST = "host";
    public static final String PROFILE_WORK = "work";

    public static boolean isRunningInDualApp(Context context) {
        if (getAppUserId() != 0) {
            List<Integer> appUserIds = getAppUserIds(context);
            if (appUserIds.size() > 1) {
                return PROFILE_HOST.equals(getAppProfile(context));
            }
        }
        return false;
    }

    public static int getAppUserId() {
        return android.os.Process.myUserHandle().hashCode();
    }

    static List<Integer> getAppUserIds(Context context) {
        List<Integer> userIds = new ArrayList<>();
        try {
            UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
            if (userManager != null) {
                List<UserHandle> userProfiles = userManager.getUserProfiles();
                for (UserHandle item : userProfiles) {
                    userIds.add(item.hashCode());
                }
            }
        } catch (Throwable ignored) {
        }
        return userIds;
    }

    public static String getAppUserInfo(Context context) {
        int myUserId = getAppUserId();
        int isSysUser = -1;
        List<Integer> userIds = new ArrayList<>();
        userIds.add(myUserId);

        try {
            UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
            if (userManager != null) {
                for (UserHandle item : userManager.getUserProfiles()) {
                    if (!userIds.contains(item.hashCode())) {
                        userIds.add(item.hashCode());
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isSysUser = userManager.isSystemUser() ? 1 : 0;
                }
            }
        } catch (Throwable ignored) {
        }

        String tips = "";
        for (Integer item : userIds) {
            if (!TextUtils.isEmpty(tips)) {
                tips += "|";
            }
            tips += item;
        }

        tips += " sys(" + isSysUser + ")";
        return tips;
    }

    public static String getAppProfile(Context context) {
        try {
            DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            if (devicePolicyManager != null) {
                List<ComponentName> activeAdmins = devicePolicyManager.getActiveAdmins();
                if (activeAdmins != null) {
                    for (ComponentName admin : activeAdmins) {
                        String packageName = admin.getPackageName();
                        if (devicePolicyManager.isProfileOwnerApp(packageName)) {
                            return PROFILE_WORK;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return PROFILE_HOST;
    }
}
