package com.kaedea.hiddenapi;

import android.os.BatteryStatsManager;

import dev.rikka.tools.refine.RefineAs;

@RefineAs(BatteryStatsManager.class)
public class BatteryStatsManagerManagerHidden {
    public long getStatsStartTimestamp() {
        throw new RuntimeException("Stub!");
    }
}