package com.kaedea.mediastore.dualappcompat.utils;


import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reads CPU time of a specific core spent at various frequencies and provides a delta from the
 * last call to {@link #readDelta}. Each line in the proc file has the format:
 *
 * freq time
 *
 * where time is measured in jiffies.
 *
 * @see com.android.internal.os.KernelCpuSpeedReader
 */
@SuppressWarnings({"SpellCheckingInspection", "JavadocReference"})
public class KernelCpuSpeedReader {
    private static final String TAG = "KernelCpuSpeedReader";

    private final String mProcFile;

    public KernelCpuSpeedReader(int cpuNumber) {
        mProcFile = "/sys/devices/system/cpu/cpu" + cpuNumber + "/cpufreq/stats/time_in_state";
    }

    public long readTotoal() {
        long sum = 0;
        for (long item : readAbsolute()) {
            sum += item;
        }
        return sum;
    }

    /**
     * @return The time (in jiffies) spent at different cpu speeds. The values should be
     * monotonically increasing, unless the cpu was hotplugged.
     */
    public List<Long> readAbsolute() {
        List<Long> speedTimeMs = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(mProcFile))) {
            TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(' ');
            String line;
            while ((line = reader.readLine()) != null) {
                splitter.setString(line);
                splitter.next();
                speedTimeMs.add(Long.valueOf(splitter.next()));
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to read cpu-freq: " + e.getMessage());
        }
        return speedTimeMs;
    }
}