package com.example.cal.calshop.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;

/**
 * Utility class
 */
public class Utils {
    /**
     * Format the date with SimpleDateFormat
     */
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private Context mContext = null;


    /**
     * Public constructor that takes mContext for later use
     */
    public Utils(Context con) {
        mContext = con;
    }

    public static String encodeEmail(String email) {
        return email.replaceAll("\\.", ",");
    }
    public static String decodeEmail(String encodedEmail) {
        return encodedEmail.replaceAll(",", ".");
    }

    public static String getCurrentUserName(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return decodeEmail(prefs.getString(Constants.KEY_PREF_USERNAME, Constants.DEFAULT_OWNER));
    }

    public static String getCurrentUserEmail(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return decodeEmail(prefs.getString(Constants.KEY_PREF_ENCODED_EMAIL, Constants.DEFAULT_OWNER));
    }

    public static String getCurrentUserProvider(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return prefs.getString(Constants.KEY_PREF_PROVIDER, Constants.PREF_PROVIDER_PASSWORD);
    }

    public static void clearUserPrefs(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(Constants.KEY_PREF_USERNAME);
        editor.remove(Constants.KEY_PREF_ENCODED_EMAIL);
        editor.remove(Constants.KEY_PREF_PROVIDER);
        editor.commit();
    }
}
