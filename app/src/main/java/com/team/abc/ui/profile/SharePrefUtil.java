package com.team.abc.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;

import com.team.abc.model.User;
import com.google.gson.Gson;

public final class SharePrefUtil {
    private static final String TAG = "SharePrefUtil";
    private static final String LOGIN = "Login";

    public static User getUserLogged(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        String str = sharedPreferences.getString(LOGIN, "");
        if ("".equals(str)) {
            return null;
        } else {
            Gson gson = new Gson();
            return gson.fromJson(str, User.class);
        }
    }

    public static void setUser(Context context, User user) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        sharedPreferences.edit().putString(LOGIN, gson.toJson(user)).apply();
    }

    public static void clearUser(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(LOGIN, "").apply();
    }
}
