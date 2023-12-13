package android.os;

import android.util.Range;

import java.util.List;

/**
 * @author Kaede
 * @since 12/12/2023
 */
public class BatteryUsageStats {
    public List<UidBatteryConsumer> getUidBatteryConsumers() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Timestamp (as returned by System.currentTimeMillis()) of the latest battery stats reset, in
     * milliseconds.
     */
    public long getStatsStartTimestamp() {
        throw new RuntimeException("Stub!");    }

    /**
     * Timestamp (as returned by System.currentTimeMillis()) of when the stats snapshot was taken,
     * in milliseconds.
     */
    public long getStatsEndTimestamp() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Returns the duration of the stats session captured by this BatteryUsageStats.
     * In rare cases, statsDuration != statsEndTimestamp - statsStartTimestamp.  This may
     * happen when BatteryUsageStats represents an accumulation of data across multiple
     * non-contiguous sessions.
     */
    public long getStatsDuration() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Total amount of battery charge drained since BatteryStats reset (e.g. due to being fully
     * charged), in mAh
     */
    public double getConsumedPower() {
        throw new RuntimeException("Stub!");

    }

    /**
     * Returns battery capacity in milli-amp-hours.
     */
    public double getBatteryCapacity() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Portion of battery charge drained since BatteryStats reset (e.g. due to being fully
     * charged), as percentage of the full charge in the range [0:100]. May exceed 100 if
     * the device repeatedly charged and discharged prior to the reset.
     */
    public int getDischargePercentage() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Returns the discharged power since BatteryStats were last reset, in mAh as an estimated
     * range.
     */
    public Range<Double> getDischargedPowerRange() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Returns the total amount of time the battery was discharging.
     */
    public long getDischargeDurationMs() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Returns an approximation for how much run time (in milliseconds) is remaining on
     * the battery.  Returns -1 if no time can be computed: either there is not
     * enough current data to make a decision, or the battery is currently
     * charging.
     */
    public long getBatteryTimeRemainingMs() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Returns an approximation for how much time (in milliseconds) remains until the battery
     * is fully charged.  Returns -1 if no time can be computed: either there is not
     * enough current data to make a decision, or the battery is currently discharging.
     */
    public long getChargeTimeRemainingMs() {
        throw new RuntimeException("Stub!");
    }
}
