package com.android.internal.app;

import android.os.BatteryUsageStats;
import android.os.BatteryUsageStatsQuery;
import android.os.Binder;
import android.os.IBinder;

import java.util.List;

/**
 * See {@link "https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/com/android/internal/app/IBatteryStats.aidl"}
 *
 * @author Kaede
 * @since 13/12/2023
 */
public interface IBatteryStats {
    List<BatteryUsageStats> getBatteryUsageStats(List<BatteryUsageStatsQuery> queries);

    abstract class Stub extends Binder implements IBatteryStats {

        public static IBatteryStats asInterface(IBinder obj) {
            throw new RuntimeException("STUB");
        }
    }
}
