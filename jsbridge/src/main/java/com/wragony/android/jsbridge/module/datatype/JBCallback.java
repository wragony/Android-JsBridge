package com.wragony.android.jsbridge.module.datatype;

import androidx.annotation.UiThread;

public interface JBCallback {

    @UiThread
    void apply(Object... args);
}
