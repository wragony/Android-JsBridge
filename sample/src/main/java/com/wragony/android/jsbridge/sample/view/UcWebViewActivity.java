package com.wragony.android.jsbridge.sample.view;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.uc.webview.export.WebView;
import com.uc.webview.export.WebViewClient;
import com.wragony.android.jsbridge.JsBridge;
import com.wragony.android.jsbridge.common.OnValueCallback;
import com.wragony.android.jsbridge.sample.R;
import com.wragony.android.jsbridge.sample.module.ListenerModule;
import com.wragony.android.jsbridge.sample.module.NativeModule;
import com.wragony.android.jsbridge.sample.module.ServiceModule;
import com.wragony.android.jsbridge.sample.module.StaticModule;
import com.wragony.android.jsbridge.sample.util.TakePhotoResult;
import com.wragony.android.jsbridge.sample.util.WebEvent;
import com.wragony.android.jsbridge.sample.widget.UCWebView;
import com.wragony.android.jsbridge.sample.widget.UCWebView.PromptResultCallback;
import com.wragony.android.jsbridge.sample.widget.UCWebView.PromptResultImpl;

/**
 * @author wragony
 */
public class UcWebViewActivity extends BaseActivity implements WebEvent {

    private JsBridge jsBridge;
    private UCWebView ucWebview;
    private Button nativeButton;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ucwebview);

        setTitle("UC WebView");

        jsBridge = JsBridge.loadModule("window", "ready", new ServiceModule(), new StaticModule(), new ListenerModule(), new NativeModule());

        WebView.setWebContentsDebuggingEnabled(true);

        ucWebview = findViewById(R.id.ucwebview);
        nativeButton = findViewById(R.id.button_native);

        ucWebview.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
                super.onPageStarted(webView, s, bitmap);
                jsBridge.injectJs(ucWebview);
            }

            @Override
            public void onPageFinished(WebView webView, String s) {
                super.onPageFinished(webView, s);
            }
        });
        ucWebview.setPromptResult(new PromptResultCallback() {
            @Override
            public void onResult(String args, PromptResultImpl promptResult) {
                jsBridge.callJsPrompt(args, promptResult);
            }
        });
        nativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jsBridge.callJs("test", null, new OnValueCallback() {
                    @Override
                    public void onCallBack(String data) {
                        Log.d(JsBridge.TAG, "onCallBack:data=" + data);
                        Toast.makeText(UcWebViewActivity.this, data, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        ucWebview.loadUrl("file:///android_asset/sample.html");

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
        ListenerModule.onResume(ucWebview, "hello");
    }

    @Override
    protected void onPause() {
        super.onPause();
        ListenerModule.onPause(ucWebview, 1.234);
    }


}
