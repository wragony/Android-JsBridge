package com.wragony.android.jsbridge.sample.module;

import com.wragony.android.jsbridge.common.IWebView;
import com.wragony.android.jsbridge.module.JsListenerModule;

public class ListenerModule extends JsListenerModule {

    @Override
    public String getModuleName() {
        return "page";
    }

    public static void onResume(IWebView webView, Object... args) {
        callJsListener("window.page.onResume", webView, args);
    }

    public static void onPause(IWebView webView, Object... args) {
        callJsListener("window.page.onPause", webView, args);
    }
}
