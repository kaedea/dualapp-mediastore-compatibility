package com.kaedea.mediastore.dualappcompat;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.BatteryConsumer;
import android.os.BatteryStatsManager;
import android.os.BatteryUsageStats;
import android.os.BatteryUsageStatsQuery;
import android.os.Build;
import android.os.Bundle;
import android.os.UidBatteryConsumer;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.RangeSlider;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SystemApiCallActivity extends AppCompatActivity {

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    StringBuilder sb = new StringBuilder();
    TextView mTextView;
    RangeSlider mSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_system_api);
        mTextView = findViewById(R.id.text);
        mTextView.setMovementMethod(new ScrollingMovementMethod());
        mSlider = findViewById(R.id.slider);

        mSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                StringBuilder text = new StringBuilder("Range: \n")
                        .append(dateFormat.format(new Date((long) (System.currentTimeMillis() - ((24 - slider.getValues().get(0)) * 60 * 60 * 1000L)))))
                        .append(" ~ ")
                        .append(dateFormat.format(new Date((long) (System.currentTimeMillis() - ((24 - slider.getValues().get(1)) * 60 * 60 * 1000L)))))
                        .append("\n");
                println(text.toString(), true);
            }
        });
    }


    /**
     * adb shell pm grant com.kaedea.mediastore.dualappcompat.test android.permission.BATTERY_STATS
     */
    public void onCallBatteryStats(View view) {
        sb.setLength(0);
        // long statsStartTimestamp = Refine.<BatteryStatsManagerManagerHidden>unsafeCast(getSystemService("batterystats")).getStatsStartTimestamp();
        // Object batterystats = Refine.unsafeCast(getSystemService("batterystats"));
        // if (batterystats != null) {
        //
        // }
        BatteryStatsManager manager = (BatteryStatsManager) getSystemService("batterystats");
        if (manager != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                BatteryUsageStatsQuery statsQuery = buildQuery((int) (24 - mSlider.getValues().get(0)), (int) (24 - mSlider.getValues().get(1)));
                BatteryUsageStats batteryUsageStats = (BatteryUsageStats) HiddenApiBypass.invoke(BatteryStatsManager.class, manager, "getBatteryUsageStats", statsQuery);
                if (batteryUsageStats != null) {
                    dumpBatteryUsageStats(batteryUsageStats);
                    // noinspection unchecked
                    List<UidBatteryConsumer> consumers = (List<UidBatteryConsumer>) HiddenApiBypass.invoke(BatteryUsageStats.class, batteryUsageStats, "getUidBatteryConsumers");
                    if (!consumers.isEmpty()) {
                        dumpTopApps(consumers);
                        dumpForApp(consumers, "com.tencent.mm"); // WeChat as test
                    }
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void dumpBatteryUsageStats(BatteryUsageStats batteryUsageStats) {
        StringBuilder sb = new StringBuilder("BatteryUsageStats:\n");
        // StringWriter sw = new StringWriter();
        // PrintWriter pw = new PrintWriter(sw);
        // HiddenApiBypass.invoke(BatteryUsageStats.class, batteryUsageStats, "dump", pw, "foo");
        // pw.flush();
        // sb.append(sw);

        long startTimestamp = (long) HiddenApiBypass.invoke(BatteryUsageStats.class, batteryUsageStats, "getStatsStartTimestamp");
        long endTimestamp = (long) HiddenApiBypass.invoke(BatteryUsageStats.class, batteryUsageStats, "getStatsEndTimestamp");
        long durationMs = (long) HiddenApiBypass.invoke(BatteryUsageStats.class, batteryUsageStats, "getStatsDuration");
        long batteryTimeRemainingMs = (long) HiddenApiBypass.invoke(BatteryUsageStats.class, batteryUsageStats, "getBatteryTimeRemainingMs");
        double drainPower = (double) HiddenApiBypass.invoke(BatteryUsageStats.class, batteryUsageStats, "getConsumedPower");
        int fromHourAgo = (int) (24 - mSlider.getValues().get(0));
        int toHourAgo = (int) (24 - mSlider.getValues().get(1));
        if (fromHourAgo == toHourAgo) {
            sb.append("Query: ").append("INVALID").append("\n");
        } else {
            sb.append("Query: ").append(dateFormat.format(new Date(System.currentTimeMillis() - (fromHourAgo * 60 * 60 * 1000L)))).append(" ~ ").append(dateFormat.format(new Date(System.currentTimeMillis() - (toHourAgo * 60 * 60 * 1000L)))).append("\n");
        }
        sb.append("StatsRange: ").append(dateFormat.format(new Date(startTimestamp))).append(" ~ ").append(dateFormat.format(new Date(endTimestamp))).append("\n");
        sb.append("StatsDuration: ").append(durationMs/(60000L)).append("min\n");
        sb.append("Drain: ").append(Math.round(drainPower * 100)/100f).append("mAh\n");
        sb.append("BatteryRemain: ").append(batteryTimeRemainingMs > 0 ? (batteryTimeRemainingMs/(60000L)) : "N/A").append("min\n");
        println(sb.toString());
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void dumpTopApps(List<UidBatteryConsumer> consumers) {
        int limit = 5;
        StringBuilder sb = new StringBuilder("TopApps(" + limit + "): \n");
        consumers.sort((left, right) -> {
            Double leftPower = (Double) HiddenApiBypass.invoke(BatteryConsumer.class, left, "getConsumedPower");
            Double rightPower = (Double) HiddenApiBypass.invoke(BatteryConsumer.class, right, "getConsumedPower");
            return -leftPower.compareTo(rightPower);
        });
        for (int i = 0; i < Math.min(limit, consumers.size()); i++) {
            UidBatteryConsumer item = consumers.get(i);
            int uid = (int) HiddenApiBypass.invoke(UidBatteryConsumer.class, item, "getUid");
            String highestDrain = String.valueOf(HiddenApiBypass.invoke(UidBatteryConsumer.class, item, "getPackageWithHighestDrain"));
            double consumedPower = (double) HiddenApiBypass.invoke(BatteryConsumer.class, item, "getConsumedPower");
            sb.append(fixedWith(Math.round(consumedPower * 100) / 100f + "mAh", 10))
                    .append("\t").append(findAppPackageName(this, uid)).append("(").append(uid).append("|").append(highestDrain).append(")")
                    .append("\n");
        }
        println(sb.toString());
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void dumpForApp(List<UidBatteryConsumer> consumers, String pkgName) {
        StringBuilder sb = new StringBuilder("Power for app: " + pkgName + "\n");
        int appUid = findAppUid(this, pkgName);
        UidBatteryConsumer myBatteryConsumer = null;
        if (!consumers.isEmpty()) {
            for (UidBatteryConsumer item : consumers) {
                int uid = (int) HiddenApiBypass.invoke(UidBatteryConsumer.class, item, "getUid");
                if (uid == appUid) {
                    myBatteryConsumer = item;
                    break;
                }
            }
        }

        if (myBatteryConsumer != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            HiddenApiBypass.invoke(UidBatteryConsumer.class, myBatteryConsumer, "dump", pw, false);
            pw.flush();
            // UID u0a587: 0.725 ( screen=0 cpu=0.667 (25s 757ms) bluetooth=0 camera=0 audio=0 video=0 flashlight=0 system_services=0.0483 mobile_radio=0 sensors=0 gnss=0 wifi=0.0100 (260ms) wakelock=0.0000690 (46ms) memory=0 phone=0 ambient_display=0 idle=0 reattributed=0 )
            String dump = sw.toString();
            String symbolBgn = " ( ";
            String symbolEnd = " ) ";
            int idxBgn = dump.indexOf(symbolBgn);
            int idxEnd = dump.indexOf(symbolEnd);
            if (idxBgn >= 0 && idxEnd > (idxBgn + symbolBgn.length())) {
                String prefix = dump.substring(0, idxBgn);
                String suffix = dump.substring(idxBgn + symbolBgn.length(), idxEnd);
                String[] split = suffix.split(" ");
                String msg = "";
                for (String curr : split) {
                    if (!TextUtils.isEmpty(msg)) {
                        if (curr.contains("=")) {
                            msg += "\n";
                        }
                    }
                    msg += curr;
                }
                sb.append(prefix).append("\n").append(msg);
            } else {
                sb.append(sw);
            }
        }

        println(sb.toString());
    }

    private static BatteryUsageStatsQuery buildQuery(int fromHourAgo, int toHourAgo) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            BatteryUsageStatsQuery.Builder builder = (BatteryUsageStatsQuery.Builder) HiddenApiBypass.newInstance(BatteryUsageStatsQuery.Builder.class);
            HiddenApiBypass.invoke(BatteryUsageStatsQuery.Builder.class, builder, "includeBatteryHistory");
            HiddenApiBypass.invoke(BatteryUsageStatsQuery.Builder.class, builder, "includeProcessStateData");
            HiddenApiBypass.invoke(BatteryUsageStatsQuery.Builder.class, builder, "includePowerModels");
            HiddenApiBypass.invoke(BatteryUsageStatsQuery.Builder.class, builder, "includeVirtualUids");
            if (fromHourAgo > toHourAgo) {
                HiddenApiBypass.invoke(BatteryUsageStatsQuery.Builder.class, builder, "aggregateSnapshots", System.currentTimeMillis() - (fromHourAgo * 60 * 60 * 1000L), System.currentTimeMillis() - (toHourAgo * 60 * 60 * 1000L));
            }
            HiddenApiBypass.invoke(BatteryUsageStatsQuery.Builder.class, builder, "setMaxStatsAgeMs", 0L);
            return (BatteryUsageStatsQuery) HiddenApiBypass.invoke(BatteryUsageStatsQuery.Builder.class, builder, "build");
        }
        return null;
    }

    private static int findAppUid(Context context, String pkgName) {
        try {
            return context.getPackageManager().getApplicationInfo(pkgName, 0).uid;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }

    private static String findAppPackageName(Context context, int uid) {
        try {
            List<PackageInfo> installedPackages = context.getPackageManager().getInstalledPackages(0);
            for (PackageInfo item : installedPackages) {
                if (findAppUid(context, item.packageName) == uid) {
                    return item.packageName;
                }
            }
        } catch (Throwable e) {
            return null;
        }
        return null;
    }

    private void println(String msg) {
        println(msg, false);
    }

    private void println(String msg, boolean clear) {
        if (clear) {
            sb.setLength(0);
        }
        sb.append(msg).append("\n\n");
        mTextView.setText(sb.toString());
    }

    private String fixedWith(String input, int fixedLength) {
        if (input.length() >= fixedLength) {
            return input;
        }
        return input + new String(new char[(fixedLength - input.length())]).replace("\0", " ");
    }

}
