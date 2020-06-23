package com.wragony.android.jsbridge;

import android.support.annotation.NonNull;
import android.webkit.JsPromptResult;
import android.webkit.WebView;
import com.wragony.android.jsbridge.common.IPromptResult;
import com.wragony.android.jsbridge.common.IWebView;
import com.wragony.android.jsbridge.common.OnValueCallback;
import com.wragony.android.jsbridge.module.JsModule;

public abstract class JsBridge {

    public static final String TAG = "JsBridgeDebug";

    public abstract void injectJs(@NonNull WebView webView);

    public abstract void injectJs(@NonNull IWebView webView);

    public abstract boolean callJsPrompt(@NonNull String methodArgs, @NonNull JsPromptResult result);

    public abstract boolean callJsPrompt(@NonNull String methodArgs, @NonNull IPromptResult result);

    public abstract boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result);

    public abstract void evaluateJavascript(@NonNull String jsCode);

    public abstract void clean();

    public abstract void release();

    public abstract void callJs(String functionName, String params, OnValueCallback mCallback);

    public static JsBridge loadModule(JsModule... modules) {
        return new JsBridgeImpl(modules);
    }

    public static JsBridge loadModule() {
        return new JsBridgeImpl();
    }

    public static JsBridge loadModule(@NonNull String protocol, @NonNull String readyMethod, JsModule... modules) {
        return new JsBridgeImpl(protocol, readyMethod, modules);
    }
}
