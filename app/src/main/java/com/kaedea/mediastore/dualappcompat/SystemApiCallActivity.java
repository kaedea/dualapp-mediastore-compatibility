package com.kaedea.mediastore.dualappcompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.BatteryConsumer;
import android.os.BatteryManager;
import android.os.BatteryStatsManager;
import android.os.BatteryUsageStats;
import android.os.BatteryUsageStatsQuery;
import android.os.Build;
import android.os.Bundle;
import android.os.UidBatteryConsumer;
import android.os.health.HealthStats;
import android.os.health.SystemHealthManager;
import android.os.health.UidHealthStats;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import com.android.internal.app.IBatteryStats;
import com.google.android.material.slider.RangeSlider;
import com.kaedea.mediastore.dualappcompat.utils.Singleton;

import org.apache.commons.io.FileUtils;
import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuBinderWrapper;
import rikka.shizuku.SystemServiceHelper;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class SystemApiCallActivity extends AppCompatActivity {
    public static final String BATTERY_STATS_SERVICE = "batterystats";
    public static final String APP_WECHAT = "com.tencent.mm"; // WeChat as demo app

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    StringBuilder sb = new StringBuilder();
    TextView mTextView;
    RangeSlider mSlider;
    CheckBox mChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_system_api);
        mTextView = findViewById(R.id.text);
        mTextView.setMovementMethod(new ScrollingMovementMethod());
        mSlider = findViewById(R.id.slider);
        mChecker = findViewById(R.id.check_inTime);

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
        mChecker.setOnCheckedChangeListener((buttonView, isChecked) -> mSlider.setEnabled(!isChecked));
        mChecker.setChecked(true);

        Shizuku.addBinderReceivedListenerSticky(BINDER_RECEIVED_LISTENER);
        Shizuku.addBinderDeadListener(BINDER_DEAD_LISTENER);
        Shizuku.addRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Shizuku.removeBinderReceivedListener(BINDER_RECEIVED_LISTENER);
        Shizuku.removeBinderDeadListener(BINDER_DEAD_LISTENER);
        Shizuku.removeRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);
    }

    public void onSendAdbCommand(View view) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "adb shell pm grant " + getPackageName() + " android.permission.BATTERY_STATS\n\n" + "FAQ: https://shizuku.rikka.app/guide/setup/#faq" );
        sendIntent.setType("text/plain");
        Intent shareIntent = Intent.createChooser(sendIntent, "Enable BATTERY_STATS Permission");
        startActivity(shareIntent);
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
        BatteryStatsManager manager = (BatteryStatsManager) getSystemService(BATTERY_STATS_SERVICE);
        if (manager != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                BatteryUsageStatsQuery statsQuery = buildQuery((int) (24 - mSlider.getValues().get(0)), (int) (24 - mSlider.getValues().get(1)), mChecker.isChecked());
                BatteryUsageStats batteryUsageStats = (BatteryUsageStats) HiddenApiBypass.invoke(BatteryStatsManager.class, manager, "getBatteryUsageStats", statsQuery);
                if (batteryUsageStats != null) {
                    dumpBatteryUsageStats(batteryUsageStats);
                    // noinspection unchecked
                    List<UidBatteryConsumer> consumers = (List<UidBatteryConsumer>) HiddenApiBypass.invoke(BatteryUsageStats.class, batteryUsageStats, "getUidBatteryConsumers");
                    if (!consumers.isEmpty()) {
                        dumpTopApps(consumers);
                        dumpForApp(consumers, APP_WECHAT);
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
        if (mChecker.isChecked() || fromHourAgo == toHourAgo) {
            sb.append("Query: ").append("实时数据").append("\n");
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
            // HiddenApiBypass.invoke(BatteryConsumer.class, myBatteryConsumer, "getKeys", 0); // 0-17
            
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

            // HealthStats
            sb.append("\n-----");
            if (mChecker.isChecked()) {
                long procStatTopMs = 0, fgActivityMs = 0;
                long mobileRadioActiveMs = 0, mobileIdleMs = 0, mobileRxMs = 0, mobileTxMs = 0;
                SystemHealthManager manager = getSystemService(SystemHealthManager.class);
                if (manager != null) {
                    HealthStats healthStats = manager.takeUidSnapshot(appUid);
                    if ("UidHealthStats".equals(healthStats.getDataType())) {
                        if (healthStats.hasTimer(UidHealthStats.TIMER_PROCESS_STATE_TOP_MS)) {
                            procStatTopMs = healthStats.getTimerTime(UidHealthStats.TIMER_PROCESS_STATE_TOP_MS);
                        }
                        if (healthStats.hasTimer(UidHealthStats.TIMER_FOREGROUND_ACTIVITY)) {
                            fgActivityMs = healthStats.getTimerTime(UidHealthStats.TIMER_FOREGROUND_ACTIVITY);
                        }
                        if (healthStats.hasTimer(UidHealthStats.TIMER_MOBILE_RADIO_ACTIVE)) {
                            mobileRadioActiveMs = healthStats.getTimerTime(UidHealthStats.TIMER_MOBILE_RADIO_ACTIVE);
                        }
                        if (healthStats.hasMeasurement(UidHealthStats.MEASUREMENT_MOBILE_IDLE_MS)) {
                            mobileIdleMs = healthStats.getMeasurement(UidHealthStats.MEASUREMENT_MOBILE_IDLE_MS);
                        }
                        if (healthStats.hasMeasurement(UidHealthStats.MEASUREMENT_MOBILE_RX_MS)) {
                            mobileRxMs = healthStats.getMeasurement(UidHealthStats.MEASUREMENT_MOBILE_RX_MS);
                        }
                        if (healthStats.hasMeasurement(UidHealthStats.MEASUREMENT_MOBILE_TX_MS)) {
                            mobileTxMs = healthStats.getMeasurement(UidHealthStats.MEASUREMENT_MOBILE_TX_MS);
                        }
                    }
                }
                sb.append("\nHealthStats:").append("\nprocess_state_top=");
                formatTimeMs(sb, procStatTopMs);
                sb.append("\nforeground_activity=");
                formatTimeMs(sb, fgActivityMs);

                sb.append("\nMobileRadio:").append("\nmobile_radio_active=");
                formatTimeMs(sb, mobileRadioActiveMs);
                sb.append("\nmobile_idle_ms=");
                formatTimeMs(sb, mobileIdleMs);
                sb.append("\nmobile_rx_ms=");
                formatTimeMs(sb, mobileRxMs);
                sb.append("\nmobile_tx_ms=");
                formatTimeMs(sb, mobileTxMs);

                sb.append("\n");
            } else {
                sb.append("\nMetaData:").append("\nOnly work for in-time mode");
                sb.append("\n");
            }
        }

        println(sb.toString());
    }

    private static BatteryUsageStatsQuery buildQuery(int fromHourAgo, int toHourAgo, boolean justInTime) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            BatteryUsageStatsQuery.Builder builder = (BatteryUsageStatsQuery.Builder) HiddenApiBypass.newInstance(BatteryUsageStatsQuery.Builder.class);
            HiddenApiBypass.invoke(BatteryUsageStatsQuery.Builder.class, builder, "includeBatteryHistory");
            HiddenApiBypass.invoke(BatteryUsageStatsQuery.Builder.class, builder, "includeProcessStateData");
            HiddenApiBypass.invoke(BatteryUsageStatsQuery.Builder.class, builder, "includePowerModels");
            HiddenApiBypass.invoke(BatteryUsageStatsQuery.Builder.class, builder, "includeVirtualUids");
            if (!justInTime && fromHourAgo > toHourAgo) {
                long deltaMs = 10 * 60 * 1000L;
                HiddenApiBypass.invoke(BatteryUsageStatsQuery.Builder.class, builder, "aggregateSnapshots", System.currentTimeMillis() - deltaMs - (fromHourAgo * 60 * 60 * 1000L), System.currentTimeMillis() + deltaMs - (toHourAgo * 60 * 60 * 1000L));
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

    private static void formatTimeRaw(StringBuilder out, long seconds) {
        long days = seconds / (60 * 60 * 24);
        if (days != 0) {
            out.append(days);
            out.append("d ");
        }
        long used = days * 60 * 60 * 24;

        long hours = (seconds - used) / (60 * 60);
        if (hours != 0 || used != 0) {
            out.append(hours);
            out.append("h ");
        }
        used += hours * 60 * 60;

        long mins = (seconds-used) / 60;
        if (mins != 0 || used != 0) {
            out.append(mins);
            out.append("m ");
        }
        used += mins * 60;

        if (seconds != 0 || used != 0) {
            out.append(seconds-used);
            out.append("s ");
        }
    }

    public static void formatTimeMs(StringBuilder sb, long time) {
        long sec = time / 1000;
        formatTimeRaw(sb, sec);
        sb.append(time - (sec * 1000));
        sb.append("ms ");
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

    private void handleCallBatteryStatsViaShizuku() {
        sb.setLength(0);
        println("handleCallBatteryStatsViaShizuku");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            BatteryUsageStatsQuery statsQuery = buildQuery((int) (24 - mSlider.getValues().get(0)), (int) (24 - mSlider.getValues().get(1)), mChecker.isChecked());
            IBatteryStats batteryStats = BATTERY_STATS_MANAGER.get();
            List<BatteryUsageStats> batteryUsageStatsList = (List<BatteryUsageStats>) HiddenApiBypass.invoke(batteryStats.getClass(), batteryStats, "getBatteryUsageStats", Collections.singletonList(statsQuery));
            if (batteryUsageStatsList != null && !batteryUsageStatsList.isEmpty()) {
                BatteryUsageStats batteryUsageStats = batteryUsageStatsList.get(0);
                dumpBatteryUsageStats(batteryUsageStats);
                // noinspection unchecked
                List<UidBatteryConsumer> consumers = (List<UidBatteryConsumer>) HiddenApiBypass.invoke(BatteryUsageStats.class, batteryUsageStats, "getUidBatteryConsumers");
                if (!consumers.isEmpty()) {
                    dumpTopApps(consumers);
                    dumpForApp(consumers, APP_WECHAT);
                }
            }
        }
    }

    /**
     * adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh
     */
    public void onCallBatteryStatsViaShizuku(View view) {
        if (checkShizukuPermission(REQUEST_CODE_BATTERY_STATS)) {
            handleCallBatteryStatsViaShizuku();
        }
    }

    private boolean checkShizukuPermission(int code) {
        if (Shizuku.isPreV11()) {
            return false;
        }
        try {
            if (Shizuku.checkSelfPermission() == PERMISSION_GRANTED) {
                return true;
            } else if (Shizuku.shouldShowRequestPermissionRationale()) {
                println("User denied permission (shouldShowRequestPermissionRationale=true)");
                return false;
            } else {
                Shizuku.requestPermission(code);
                return false;
            }
        } catch (Throwable e) {
            println(Log.getStackTraceString(e));
        }

        return false;
    }

    private static final int REQUEST_CODE_BATTERY_STATS = 1;
    private final Shizuku.OnBinderReceivedListener BINDER_RECEIVED_LISTENER = () -> {
        if (Shizuku.isPreV11()) {
            println("Shizuku pre-v11 is not supported");
        } else {
            println("Binder received");
        }
    };
    private final Shizuku.OnBinderDeadListener BINDER_DEAD_LISTENER = () -> println("Binder dead");
    private final Shizuku.OnRequestPermissionResultListener REQUEST_PERMISSION_RESULT_LISTENER = this::onRequestPermissionsResult;

    private void onRequestPermissionsResult(int requestCode, int grantResult) {
        if (grantResult == PERMISSION_GRANTED) {
            switch (requestCode) {
                case REQUEST_CODE_BATTERY_STATS:
                    handleCallBatteryStatsViaShizuku();
                    break;
                default:
                    println("Unknown Code: " + requestCode);
                    break;
            }
        } else {
            println("User denied permission");
        }
    }

    private static final Singleton<IBatteryStats> BATTERY_STATS_MANAGER = new Singleton<IBatteryStats>() {
        @Override
        protected IBatteryStats create() {
            return IBatteryStats.Stub.asInterface(new ShizukuBinderWrapper(SystemServiceHelper.getSystemService(BATTERY_STATS_SERVICE)));
        }
    };


    public void onOpenBatteryUsageStats(View view) {
        BatteryStatsManager manager = (BatteryStatsManager) getSystemService(BATTERY_STATS_SERVICE);
        if (manager != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                BatteryUsageStatsQuery statsQuery = buildQuery((int) (24 - mSlider.getValues().get(0)), (int) (24 - mSlider.getValues().get(1)), mChecker.isChecked());
                BatteryUsageStats batteryUsageStats = (BatteryUsageStats) HiddenApiBypass.invoke(BatteryStatsManager.class, manager, "getBatteryUsageStats", statsQuery);
                if (batteryUsageStats != null) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    HiddenApiBypass.invoke(BatteryUsageStats.class, batteryUsageStats, "dump", pw, "");
                    pw.flush();
                    onOpenPainText(sw.toString());
                }
            }
        }
    }

    private void onOpenPainText(String text) {
        File file = new File(getFilesDir(), "shared/battery-stats.txt");
        try {
            file.delete();
            file.getParentFile().mkdirs();
            FileUtils.write(file, text, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Uri fileUri = FileProvider.getUriForFile(this, this.getPackageName() + ".fileprovider", file);
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, getContentResolver().getType(fileUri));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "BatteryUsageStats"));
    }
}
