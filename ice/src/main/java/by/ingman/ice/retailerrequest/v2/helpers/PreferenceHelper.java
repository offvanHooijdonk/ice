package by.ingman.ice.retailerrequest.v2.helpers;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by yahor on 23.03.16.
 */
public class PreferenceHelper {
    private static final String PREF_LAST_UPDATE_DATE = "PREF_LAST_UPDATE_DATE";
    private static final String PREF_UPDATE_IN_PROGRESS = "PREF_UPDATE_IN_PROGRESS";

    private static final String PREF_MANAGER_NAME = "username";
    private static final String PREF_DEFAULT_STOREHOUSE_CODE = "storehouseDefaultCode";
    private static final String PREF_NOTIFICATIONS_ENABLED = "updateNotificationsEnabled";
    private static final String PREF_DATA_UPDATE_INTERVAL = "exchangeFrequency";
    private static final String PREF_ORDER_DAYS_AHEAD = "orderDaysAhead";

    private static final String PREF_REMOTE_DB_HOST = "host";
    private static final String PREF_REMOTE_DB_PORT = "port";
    private static final String PREF_REMOTE_DB_NAME = "baseName";
    private static final String PREF_REMOTE_DB_USERNAME = "usernameDB";
    private static final String PREF_REMOTE_DB_PWD = "password";

    public static class Runtime {
        public static long getLastUpdateDate(Context ctx) {
            return PreferenceManager.getDefaultSharedPreferences(ctx).getLong(PREF_LAST_UPDATE_DATE, 0);
        }

        public static void setLastUpdateDate(Context ctx, long date) {
            PreferenceManager.getDefaultSharedPreferences(ctx).edit().putLong(PREF_LAST_UPDATE_DATE, date).apply();
        }

        public static boolean getUpdateInProgress(Context ctx) {
            return PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean(PREF_UPDATE_IN_PROGRESS, false);
        }

        public static void setUpdateInProgress(Context ctx, boolean inProgress) {
            PreferenceManager.getDefaultSharedPreferences(ctx).edit().putBoolean(PREF_UPDATE_IN_PROGRESS, inProgress).apply();
        }
    }

    public static class Settings {
        public static String getManagerName(Context ctx) {
            return PreferenceManager.getDefaultSharedPreferences(ctx).getString(PREF_MANAGER_NAME, "");
        }

        public static String getDefaultStoreHouseCode(Context ctx) {
            return PreferenceManager.getDefaultSharedPreferences(ctx).getString(PREF_DEFAULT_STOREHOUSE_CODE, "");
        }

        public static boolean getNotificationsEnabled(Context ctx) {
            return PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean(PREF_NOTIFICATIONS_ENABLED, true);
        }

        public static int getDataUpdateInterval(Context ctx) {
            return Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(ctx).getString(PREF_DATA_UPDATE_INTERVAL, "30"));
        }

        public static int getOrderDaysAhead(Context ctx) {
            return Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(ctx).getString(PREF_ORDER_DAYS_AHEAD, "20"));
        }
    }

    public static class RemoteDB {
        public static String getHost(Context ctx) {
            return PreferenceManager.getDefaultSharedPreferences(ctx).getString(PREF_REMOTE_DB_HOST, "");
        }

        public static String getPort(Context ctx) {
            return PreferenceManager.getDefaultSharedPreferences(ctx).getString(PREF_REMOTE_DB_PORT, "");
        }

        public static String getDBName(Context ctx) {
            return PreferenceManager.getDefaultSharedPreferences(ctx).getString(PREF_REMOTE_DB_NAME, "");
        }

        public static String getUserName(Context ctx) {
            return PreferenceManager.getDefaultSharedPreferences(ctx).getString(PREF_REMOTE_DB_USERNAME, "");
        }

        public static String getPassword(Context ctx) {
            return PreferenceManager.getDefaultSharedPreferences(ctx).getString(PREF_REMOTE_DB_PWD, "");
        }
    }

}
