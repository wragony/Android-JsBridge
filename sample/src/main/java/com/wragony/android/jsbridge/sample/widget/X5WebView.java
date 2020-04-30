package com.wragony.android.jsbridge.sample.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import com.tencent.smtt.export.external.interfaces.ConsoleMessage;
import com.tencent.smtt.export.external.interfaces.JsPromptResult;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebSettings.LayoutAlgorithm;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.wragony.android.jsbridge.JsBridge;
import com.wragony.android.jsbridge.common.IPromptResult;
import com.wragony.android.jsbridge.common.IWebView;
import com.wragony.android.jsbridge.common.OnValueCallback;

/**
 * @author wragony
 */
public class X5WebView extends WebView implements IWebView {

    private PromptResultCallback callback;

    @SuppressLint("SetJavaScriptEnabled")
    public X5WebView(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
        this.setWebViewClient(client);
        this.setWebChromeClient(mWebChromeClient);
        initWebViewSettings();
        this.getView().setClickable(true);
    }

    private void initWebViewSettings() {
        WebSettings webSetting = this.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting.setAllowFileAccess(true);
        webSetting.setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportZoom(false);
        webSetting.setBuiltInZoomControls(false);
        webSetting.setUseWideViewPort(false);
        webSetting.setSupportMultipleWindows(true);
        // webSetting.setLoadWithOverviewMode(true);
        webSetting.setAppCacheEnabled(true);
        // webSetting.setDatabaseEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        // webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);

        //extension settings 的设计
        // this.getSettingsExtension().setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
    }

    WebChromeClient mWebChromeClient = new WebChromeClient() {

        @Override
        public boolean onJsPrompt(WebView webView, String url, String message, String defaultValue, JsPromptResult result) {
            if (callback != null) {
                callback.onResult(message, new PromptResultImpl(result));
            }
            return true;
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            Log.d(JsBridge.TAG, consoleMessage.message());
            return true;
        }
    };

    public static class PromptResultImpl implements IPromptResult {

        private JsPromptResult jsPromptResult;

        public PromptResultImpl(JsPromptResult jsPromptResult) {
            this.jsPromptResult = jsPromptResult;
        }

        @Override
        public void confirm(String result) {
            this.jsPromptResult.confirm(result);
        }
    }

    public interface PromptResultCallback {

        void onResult(String args, PromptResultImpl promptResult);
    }

    private WebViewClient client = new WebViewClient() {

        /**
         * prevent system browser from launching when web page loads
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView webView, String s) {
            super.onPageFinished(webView, s);
        }

    };

    @Override
    public void evaluateJavascript(String script, final OnValueCallback mCallback) {
        if (mCallback == null) {
            super.evaluateJavascript(script, null);
            return;
        }
        super.evaluateJavascript(script, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                if (null != mCallback) {
                    mCallback.onCallBack(value);
                }
            }
        });
    }

    public void setPromptResult(final PromptResultCallback callback) {
        this.callback = callback;
    }
}
