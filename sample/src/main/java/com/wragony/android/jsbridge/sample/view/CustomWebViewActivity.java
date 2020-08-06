package com.wragony.android.jsbridge.sample.view;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.wragony.android.jsbridge.JsBridge;
import com.wragony.android.jsbridge.common.OnValueCallback;
import com.wragony.android.jsbridge.sample.module.ListenerModule;
import com.wragony.android.jsbridge.sample.module.NativeModule;
import com.wragony.android.jsbridge.sample.module.ServiceModule;
import com.wragony.android.jsbridge.sample.module.StaticModule;
import com.wragony.android.jsbridge.sample.util.TakePhotoResult;
import com.wragony.android.jsbridge.sample.util.WebEvent;
import com.wragony.android.jsbridge.sample.widget.CustomWebView;
import com.wragony.android.jsbridge.sample.widget.CustomWebView.PromptResultCallback;
import com.wragony.android.jsbridge.sample.widget.CustomWebView.PromptResultImpl;

public class CustomWebViewActivity extends BaseActivity implements WebEvent {

    private JsBridge jsBridge;
    private CustomWebView customWebView;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Custom WebView");

        jsBridge = JsBridge.loadModule("window", "ready", new ServiceModule(), new StaticModule(), new ListenerModule(), new NativeModule());

        WebView.setWebContentsDebuggingEnabled(true);
        customWebView = new CustomWebView(this);
        setContentView(customWebView);
        customWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                jsBridge.injectJs(customWebView);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

            }
        });
        customWebView.setPromptResult(new PromptResultCallback() {
            @Override
            public void onResult(String args, PromptResultImpl promptResult) {
                jsBridge.callJsPrompt(args, promptResult);
            }
        });
        customWebView.setOnclick(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                jsBridge.callJs("test", "", new OnValueCallback() {
                    @Override
                    public void onCallBack(String data) {
                        Log.d(JsBridge.TAG, "onPageFinished:data=" + data);
                        Toast.makeText(CustomWebViewActivity.this, data, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        customWebView.loadUrl("file:///android_asset/sample.html");

    }

    @Override
    public void takePhoto(TakePhotoResult result) {

    }

    @Override
    protected void onDestroy() {
        jsBridge.release();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ListenerModule.onResume(customWebView, "hello");
    }

    @Override
    protected void onPause() {
        super.onPause();
        ListenerModule.onPause(customWebView, 1.234);
    }
}
