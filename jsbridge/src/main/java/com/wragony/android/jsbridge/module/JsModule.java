package com.wragony.android.jsbridge.module;

import android.content.Context;
import android.webkit.WebView;
import com.wragony.android.jsbridge.common.IWebView;

public abstract class JsModule {

    public Context mContext;
    public Object mWebView;

    protected final Context getContext() {
        return mContext;
    }

    protected IWebView getIWebView() {
        return (IWebView) mWebView;
    }

    protected WebView getWebView() {
        return (WebView) mWebView;
    }

    protected Object getWebViewObject() {
        return mWebView;
    }

    public abstract String getModuleName();
}

