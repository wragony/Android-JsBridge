package com.wragony.android.jsbridge;

import android.util.Log;

class JBLog {

    // DEBUG
    private static final boolean DEBUG = JsBridgeConfigImpl.getInstance().isDebug();

    public static void d(String msg) {
        if (DEBUG) {
            Log.d(JsBridge.TAG, msg);
        }
    }

    public static void e(String msg, Throwable throwable) {
        if (DEBUG) {
            Log.e(JsBridge.TAG, msg, throwable);
        }
    }
}
