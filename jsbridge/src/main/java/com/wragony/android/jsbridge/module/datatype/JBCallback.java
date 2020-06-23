package com.wragony.android.jsbridge.module.datatype;

import android.support.annotation.UiThread;

public interface JBCallback {

    @UiThread
    void apply(Object... args);
}
