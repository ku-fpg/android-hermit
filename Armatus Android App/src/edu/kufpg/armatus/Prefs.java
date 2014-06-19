package edu.kufpg.armatus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public final class Prefs {

    /**
     * {@link CheckBoxPreference} key mapping to whether or
     * not {@link DeviceConstants#CACHE_DIR CACHE_DIR} should be used to save persistent data (if
     * the mapped value is {@code false}). If the mapped value is {@code true}, the String to which
     * {@link #HISTORY_DIR_KEY} maps is used instead.
     */
    static String IS_HISTORY_DIR_CUSTOM_KEY;

    /**
     * {@link Preference} key mapping to the String representation
     * of a directory where persistent data can be stored. The directory is only used if the value
     * to which {@link #IS_HISTORY_DIR_CUSTOM_KEY} maps is true.
     */
    static String HISTORY_DIR_KEY;

    /**
     * {@link ListPreference} key mapping to one of three String
     * values: "0" (for {@code READ} mode), "1" (for {@code WRITE} mode), or "2" (for {@code ARITHMETIC}
     * mode). The mapped String represent which {@code EditMode} is currently
     * being used.
     */
    static String EDIT_MODE_KEY;
    static String EDIT_MODE_READ;
    static String EDIT_MODE_WRITE;
    static String EDIT_MODE_ARITHMETIC;

    /**
     * {@link ListPreference ListPreference} key mapping to either {@link
     * #APP_THEME_DARK} or {@link #APP_THEME_LIGHT}, depending on which theme is currently
     * being used.
     */
    static String APP_THEME_KEY;

    /**
     * One of the possible values that the {@link Preference Preference} to
     * which {@link #APP_THEME_KEY} maps can be (the other being {@link #APP_THEME_LIGHT}).
     */
    static String APP_THEME_DARK;

    /**
     * One of the possible values that the {@link Preference Preference} to
     * which {@link #APP_THEME_KEY} maps can be (the other being {@link #APP_THEME_DARK}).
     */
    static String APP_THEME_LIGHT;

    /**
     * {@link ListPreference ListPreference} key mapping to either {@link
     * #NETWORK_SOURCE_WEB_SERVER} or {@link #NETWORK_SOURCE_BLUETOOTH_SERVER}, depending on
     * which network source is currently being used.
     */
    static String NETWORK_SOURCE_KEY;

    /**
     * One of the possible values that the {@link Preference Preference} to
     * which {@link #NETWORK_SOURCE_KEY} maps can be (the other being {@link
     * #NETWORK_SOURCE_BLUETOOTH_SERVER}).
     */
    static String NETWORK_SOURCE_WEB_SERVER;

    /**
     * One of the possible values that the {@link Preference Preference} to
     * which {@link #NETWORK_SOURCE_KEY} maps can be (the other being {@link
     * #NETWORK_SOURCE_WEB_SERVER}).
     */
    static String NETWORK_SOURCE_BLUETOOTH_SERVER;

    /**
     * {@link Preference Preference} key mapping to the friendly name of the
     * Bluetooth device being used (if enabled).
     */
    static String BLUETOOTH_DEVICE_NAME_KEY;

    /**
     * {@link Preference Preference} key mapping to the MAC address of the
     * Bluetooth device being used (if enabled).
     */
    static String BLUETOOTH_DEVICE_ADDRESS_KEY;

    static String SPECIAL_KEYS_VISIBLE_KEY;

    /**
     * Maps special {@link Preference Preference} keys to their default values
     * when the default values are impossible to know before runtime (e.g., the external cache
     * directory, which {@link #IS_HISTORY_DIR_CUSTOM_KEY} maps to by default).
     */
    static Map<String, ?> DYNAMIC_PREF_DEFAULTS_MAP;

    static String IS_FIRST_TIME_KEY;

    /**
     * {@link Preference} key used to choose the Bluetooth device if Bluetooth communications
     * are enabled.
     */
    static String CHOOSE_BLUETOOTH_DEVICE_KEY;

    static String SHOW_LINE_NUMBERS_KEY;

    /**
     * {@link Preference} key used for resetting preferences back to their default values.
     */
    static String RESTORE_DEFAULTS_KEY;

    private Prefs() {}

    static void initPrefs(@NonNull final Context context) {
        final Resources r = context.getResources();

        IS_HISTORY_DIR_CUSTOM_KEY = r.getString(R.string.pref_is_history_dir_custom);
        HISTORY_DIR_KEY = r.getString(R.string.pref_history_dir);
        EDIT_MODE_KEY = r.getString(R.string.pref_edit_mode);
        EDIT_MODE_READ = r.getString(R.string.pref_edit_mode_read);
        EDIT_MODE_WRITE = r.getString(R.string.pref_edit_mode_write);
        EDIT_MODE_ARITHMETIC = r.getString(R.string.pref_edit_mode_arithmetic);
        APP_THEME_KEY = r.getString(R.string.pref_app_theme);
        APP_THEME_DARK = r.getString(R.string.pref_app_theme_dark);
        APP_THEME_LIGHT = r.getString(R.string.pref_app_theme_light);
        NETWORK_SOURCE_KEY = r.getString(R.string.pref_network_source);
        NETWORK_SOURCE_WEB_SERVER = r.getString(R.string.pref_network_source_web);
        NETWORK_SOURCE_BLUETOOTH_SERVER = r.getString(R.string.pref_network_source_bluetooth);
        BLUETOOTH_DEVICE_NAME_KEY = r.getString(R.string.pref_bluetooth_device_name);
        BLUETOOTH_DEVICE_ADDRESS_KEY = r.getString(R.string.pref_bluetooth_device_address);
        SPECIAL_KEYS_VISIBLE_KEY = r.getString(R.string.pref_special_keys_visible);
        IS_FIRST_TIME_KEY = r.getString(R.string.pref_is_first_time);

        CHOOSE_BLUETOOTH_DEVICE_KEY = r.getString(R.string.pref_choose_bluetooth_device);
        SHOW_LINE_NUMBERS_KEY = r.getString(R.string.pref_show_line_numbers);
        RESTORE_DEFAULTS_KEY = r.getString(R.string.pref_restore_defaults);

        DYNAMIC_PREF_DEFAULTS_MAP = mapDynamicPrefDefaults();
        PreferenceManager.setDefaultValues(context, R.xml.preferences, true);
        getPrefsEditor(context).commit();

        if (isFirstTime(context)) {
            restoreDyanmicPrefDefaultValues(context);
            setSpecialKeysVisible(context, false);
            setIsFirstTime(context, false);
        }
    }

    /**
     * Convenience for retrieving the app's default {@link SharedPreferences}.
     *
     * @param context The {@link Context} to use.
     * @return The app's {@code SharedPreferences}.
     */
    @NonNull public static SharedPreferences getPrefs(@NonNull final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Convenience for retrieving the {@code Editor} that can change the app's
     * {@link SharedPreferences}.
     *
     * @param context The {@link Context} to use.
     * @return The app's {@code SharedPreferences.Editor}.
     */
    @NonNull public static SharedPreferences.Editor getPrefsEditor(@NonNull final Context context) {
        return getPrefs(context).edit();
    }

    /**
     * Initializes {@link #DYNAMIC_PREF_DEFAULTS_MAP} by mapping {@link
     * Preference Preference} keys to their default values when the default
     * values are impossible to know before runtime.
     *
     * @return a map of {@code Preference} keys to their dynamic default values.
     */
    @NonNull private static Map<String, ?> mapDynamicPrefDefaults() {
        return ImmutableMap.of(HISTORY_DIR_KEY, DeviceConstants.CACHE_DIR);
    }

    /**
     * Restores the preferences that are impossible to know before runtime to their
     * default values.
     */
    static void restoreDyanmicPrefDefaultValues(@NonNull final Context context) {
        final SharedPreferences.Editor editor = getPrefsEditor(context);
        for (final Entry<String, ?> entry : DYNAMIC_PREF_DEFAULTS_MAP.entrySet()) {
            if (entry.getValue() instanceof String) {
                editor.putString(entry.getKey(), (String) entry.getValue());
            } else if (entry.getValue() instanceof Boolean) {
                editor.putBoolean(entry.getKey(), (Boolean) entry.getValue());
            } else if (entry.getValue() instanceof Integer) {
                editor.putInt(entry.getKey(), (Integer) entry.getValue());
            } else if (entry.getValue() instanceof Float) {
                editor.putFloat(entry.getKey(), (Float) entry.getValue());
            } else if (entry.getValue() instanceof Long) {
                editor.putLong(entry.getKey(), (Long) entry.getValue());
            } else if (entry.getValue() instanceof Set) {
                @SuppressWarnings("unchecked") final
                Set<String> set = (Set<String>) entry.getValue();
                editor.putStringSet(entry.getKey(), set);
            }
        }
        editor.commit();
    }

    @Nullable public static String getBluetoothDeviceAddress(@NonNull final Context context) {
        return getPrefs(context).getString(BLUETOOTH_DEVICE_ADDRESS_KEY, null);
    }

    @Nullable public static String getBluetoothDeviceName(@NonNull final Context context) {
        return getPrefs(context).getString(BLUETOOTH_DEVICE_NAME_KEY, null);
    }

    @Nullable public static EditMode getEditMode(@NonNull final Context context) {
        final String editMode = getPrefs(context).getString(EDIT_MODE_KEY, null);
        if (Objects.equal(editMode, EDIT_MODE_READ)) {
            return EditMode.READ;
        } else if (Objects.equal(editMode, EDIT_MODE_WRITE)) {
            return EditMode.WRITE;
        } else if (Objects.equal(editMode, EDIT_MODE_ARITHMETIC)) {
            return EditMode.ARITHMETIC;
        } else {
            return null;
        }
    }

    @Nullable public static String getHistoryDir(@NonNull final Context context) {
        return getPrefs(context).getString(HISTORY_DIR_KEY, null);
    }

    @Nullable public static NetworkSource getNetworkSource(@NonNull final Context context) {
        final String source = getPrefs(context).getString(NETWORK_SOURCE_KEY, null);
        if (Objects.equal(source, NETWORK_SOURCE_WEB_SERVER)) {
            return NetworkSource.WEB_SERVER;
        } else if (Objects.equal(source, NETWORK_SOURCE_BLUETOOTH_SERVER)) {
            return NetworkSource.BLUETOOTH_SERVER;
        } else {
            return null;
        }
    }

    public static boolean getShowLineNumbers(@NonNull final Context context) {
        return getPrefs(context).getBoolean(SHOW_LINE_NUMBERS_KEY, false);
    }

    public static boolean getSpecialKeysVisible(@NonNull final Context context) {
        return getPrefs(context).getBoolean(SPECIAL_KEYS_VISIBLE_KEY, false);
    }

    /**
     * Returns the resource ID of the current app theme (either {@code ThemeLight} or
     * {@code ThemeDark}.
     *
     * @return the current app theme's resource ID.
     */
    @Nullable public static Theme getTheme(@NonNull final Context context) {
        final String theme = getPrefs(context).getString(APP_THEME_KEY, null);
        if (Objects.equal(theme, APP_THEME_LIGHT)) {
            return Theme.LIGHT;
        } else if (Objects.equal(theme, APP_THEME_DARK)) {
            return Theme.DARK;
        } else {
            return null;
        }
    }

    public static boolean isBluetoothSource(@NonNull final Context context) {
        return Objects.equal(getNetworkSource(context), NetworkSource.BLUETOOTH_SERVER);
    }

    private static boolean isFirstTime(@NonNull final Context context) {
        return getPrefs(context).getBoolean(IS_FIRST_TIME_KEY, true);
    }

    public static boolean isHistoryDirCustom(@NonNull final Context context) {
        return getPrefs(context).getBoolean(IS_HISTORY_DIR_CUSTOM_KEY, false);
    }

    public static boolean isWebSource(@NonNull final Context context) {
        return Objects.equal(getNetworkSource(context), NetworkSource.WEB_SERVER);
    }

    public static void refreshTheme(final Context context) {
        setTheme(context, getTheme(context));
    }

    public static void setBluetoothDeviceAddress(final Context context, final String address) {
        getPrefsEditor(context).putString(BLUETOOTH_DEVICE_ADDRESS_KEY, address).commit();
    }

    public static void setBluetoothDeviceName(final Context context, final String friendlyName) {
        getPrefsEditor(context).putString(BLUETOOTH_DEVICE_NAME_KEY, friendlyName).commit();
    }

    public static void setEditMode(@NonNull final Context context,
                                   @NonNull final EditMode editMode) {
        final SharedPreferences.Editor editor = getPrefsEditor(context);
        switch (editMode) {
            case READ:
                editor.putString(EDIT_MODE_KEY, EDIT_MODE_READ);
                break;
            case WRITE:
                editor.putString(EDIT_MODE_KEY, EDIT_MODE_WRITE);
                break;
            case ARITHMETIC:
                editor.putString(EDIT_MODE_KEY, EDIT_MODE_ARITHMETIC);
                break;
        }
        editor.commit();
    }

    public static void setHistoryDir(@NonNull final Context context,
                                     @Nullable final String dir) {
        getPrefsEditor(context).putString(HISTORY_DIR_KEY, dir).commit();
    }

    public static void setIsFirstTime(@NonNull final Context context, final boolean isFirstTime) {
        getPrefsEditor(context).putBoolean(IS_FIRST_TIME_KEY, isFirstTime).commit();
    }

    public static void setIsHistoryDirCustom(@NonNull final Context context, final boolean custom) {
        getPrefsEditor(context).putBoolean(IS_HISTORY_DIR_CUSTOM_KEY, custom).commit();
    }

    public static void setNetworkSource(@NonNull final Context context,
                                        @NonNull final NetworkSource source) {
        final SharedPreferences.Editor editor = getPrefsEditor(context);
        switch (source) {
            case WEB_SERVER:
                editor.putString(NETWORK_SOURCE_KEY, NETWORK_SOURCE_WEB_SERVER);
                break;
            case BLUETOOTH_SERVER:
                editor.putString(NETWORK_SOURCE_KEY, NETWORK_SOURCE_BLUETOOTH_SERVER);
                break;
        }
        editor.commit();
    }

    public static void setShowLineNumbers(@NonNull final Context context, final boolean newShowLineNumbers) {
        getPrefsEditor(context).putBoolean(SHOW_LINE_NUMBERS_KEY, newShowLineNumbers).commit();
    }

    public static void setSpecialKeysVisible(@NonNull final Context context, final boolean visible) {
        getPrefsEditor(context).putBoolean(SPECIAL_KEYS_VISIBLE_KEY, visible).commit();
    }

    public static void setTheme(@NonNull final Context context,
                                @NonNull final Theme theme) {
        final SharedPreferences.Editor editor = getPrefsEditor(context);
        switch (theme) {
            case LIGHT:
                editor.putString(APP_THEME_KEY, APP_THEME_LIGHT);
                context.setTheme(R.style.ThemeLight);
                break;
            case DARK:
                editor.putString(APP_THEME_KEY, APP_THEME_DARK);
                context.setTheme(R.style.ThemeDark);
                break;
        }
        editor.commit();
    }

    public enum EditMode {
        READ, WRITE, ARITHMETIC
    }

    public enum Theme {
        LIGHT, DARK
    }

    public enum NetworkSource {
        WEB_SERVER, BLUETOOTH_SERVER
    }
}