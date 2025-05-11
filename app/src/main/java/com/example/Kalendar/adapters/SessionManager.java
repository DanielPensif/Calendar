package com.example.Kalendar.adapters;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.Kalendar.utils.BiometricHelper;
import com.example.Kalendar.R;
import com.example.Kalendar.fragments.LoginFragment;

public class SessionManager {
    private static final String PREF = "user_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";
    private static final String KEY_BIO_ASKED = "biometric_asked";

    public static void saveUser(Context ctx, int userId) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit().putInt(KEY_USER_ID, userId).apply();
    }

    public static int getLoggedInUserId(Context ctx) {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getInt(KEY_USER_ID, -1);
    }

    public static void clear(Context ctx) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit().clear().apply();
    }
    public static void promptBiometricLogin(AppCompatActivity activity, Runnable onSuccess) {
        new BiometricHelper(activity).authenticate(onSuccess);
    }

    public static void showLoginOrRegister(AppCompatActivity activity) {
        FragmentTransaction ft = activity
                .getSupportFragmentManager()
                .beginTransaction();
        ft.replace(R.id.auth_container, new LoginFragment());
        ft.commit();
    }
    public static boolean isBiometricAsked(Context ctx) {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getBoolean(KEY_BIO_ASKED, false);
    }

    public static void setBiometricAsked(Context ctx, boolean asked) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_BIO_ASKED, asked)
                .apply();
    }

    public static boolean isBiometricEnabled(Context ctx) {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getBoolean(KEY_BIOMETRIC_ENABLED, false);
    }

    public static void setBiometricEnabled(Context ctx, boolean enabled) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_BIOMETRIC_ENABLED, enabled)
                .apply();
    }
}
