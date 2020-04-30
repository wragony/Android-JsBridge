package com.wragony.android.jsbridge.common;

import android.content.Context;

public interface IWebView {

    /**
     * @param script js脚本
     * @param mCallback js执行后的回调
     */
    void evaluateJavascript(String script, OnValueCallback mCallback);

    void loadUrl(String url);

    Context getContext();
}