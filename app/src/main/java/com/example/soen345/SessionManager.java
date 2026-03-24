package com.example.soen345;

import android.content.Context;

public interface SessionManager {
    void saveCurrentUserId(String id);
    String getCurrentUserId();
    void clearCurrentUserId();

    class Prefs implements SessionManager {
        private final Context ctx;
        private static final String PREFS = "SOEN345_PREFS";
        private static final String KEY_CURRENT_USER_ID = "CURRENT_USER_ID";

        public Prefs(Context ctx) { this.ctx = ctx.getApplicationContext(); }

        @Override
        public void saveCurrentUserId(String id) {
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_CURRENT_USER_ID, id).apply();
        }

        @Override
        public String getCurrentUserId() {
            return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_CURRENT_USER_ID, null);
        }

        @Override
        public void clearCurrentUserId() {
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY_CURRENT_USER_ID).apply();
        }
    }
}

