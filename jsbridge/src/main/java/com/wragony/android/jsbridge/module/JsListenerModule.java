package com.wragony.android.jsbridge.module;

import android.text.TextUtils;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wragony.android.jsbridge.JBUtils;
import com.wragony.android.jsbridge.common.IWebView;

public abstract class JsListenerModule extends JsModule {

    /**
     * 执行 JS 回调方法
     *
     * @param callPath
     * @param webView
     * @param args
     */
    protected static final void callJsListener(@NonNull String callPath, @NonNull WebView webView,
                                               @Nullable Object... args) {
        if (TextUtils.isEmpty(callPath) || webView == null) {
            return;
        }
        JBUtils.callJsMethod(callPath, webView, args);
    }

    /**
     * 执行 JS 回调方法
     *
     * @param callPath
     * @param webView
     * @param args
     */
    protected static final void callJsListener(@NonNull String callPath, @NonNull IWebView webView,
            @Nullable Object... args) {
        if (TextUtils.isEmpty(callPath) || webView == null) {
            return;
        }
        JBUtils.callJsMethod(callPath, webView, args);
    }
}
