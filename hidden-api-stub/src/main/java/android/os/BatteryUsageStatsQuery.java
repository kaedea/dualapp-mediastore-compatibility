package android.os;

/**
 * @author Kaede
 * @since 12/12/2023
 */
public class BatteryUsageStatsQuery {
    public static final BatteryUsageStatsQuery DEFAULT = new BatteryUsageStatsQuery.Builder().build();

    public static final int FLAG_BATTERY_USAGE_STATS_POWER_PROFILE_MODEL = 0x0001;
    public static final int FLAG_BATTERY_USAGE_STATS_INCLUDE_HISTORY = 0x0002;
    public static final int FLAG_BATTERY_USAGE_STATS_INCLUDE_POWER_MODELS = 0x0004;
    public static final int FLAG_BATTERY_USAGE_STATS_INCLUDE_PROCESS_STATE_DATA = 0x0008;
    public static final int FLAG_BATTERY_USAGE_STATS_INCLUDE_VIRTUAL_UIDS = 0x0010;
    private static final long DEFAULT_MAX_STATS_AGE_MS = 5 * 60 * 1000;

    public int getFlags() {
        throw new RuntimeException("Stub!");
    }

    public int[] getUserIds() {
        throw new RuntimeException("Stub!");
    }


    public boolean shouldForceUsePowerProfileModel() {
        throw new RuntimeException("Stub!");
    }

    public boolean isProcessStateDataNeeded() {
        throw new RuntimeException("Stub!");
    }


    public int[] getPowerComponents() {
        throw new RuntimeException("Stub!");
    }


    public long getMaxStatsAge() {
        throw new RuntimeException("Stub!");
    }


    public long getFromTimestamp() {
        throw new RuntimeException("Stub!");
    }


    public long getToTimestamp() {
        throw new RuntimeException("Stub!");
    }


    public static final class Builder {

        public BatteryUsageStatsQuery build() {
            throw new RuntimeException("Stub!");
        }

        public Builder addUser(UserHandle userHandle) {

            return this;
        }


        public Builder includeBatteryHistory() {
            throw new RuntimeException("Stub!");
        }


        public Builder includeProcessStateData() {
            throw new RuntimeException("Stub!");
        }


        public Builder powerProfileModeledOnly() {
            throw new RuntimeException("Stub!");
        }


        public Builder includePowerModels() {
            throw new RuntimeException("Stub!");
        }


        public Builder includePowerComponents(int[] powerComponents) {
            throw new RuntimeException("Stub!");
        }


        public Builder includeVirtualUids() {
            throw new RuntimeException("Stub!");
        }


        public Builder aggregateSnapshots(long fromTimestamp, long toTimestamp) {
            throw new RuntimeException("Stub!");
        }


        public Builder setMaxStatsAgeMs(long maxStatsAgeMs) {
            throw new RuntimeException("Stub!");
        }
    }
}
