package com.wragony.android.jsbridge.sample.view;

import android.Manifest;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.wragony.android.jsbridge.JsBridge;
import com.wragony.android.jsbridge.common.OnValueCallback;
import com.wragony.android.jsbridge.sample.R;
import com.wragony.android.jsbridge.sample.module.ListenerModule;
import com.wragony.android.jsbridge.sample.module.NativeModule;
import com.wragony.android.jsbridge.sample.module.ServiceModule;
import com.wragony.android.jsbridge.sample.module.Static2Module;
import com.wragony.android.jsbridge.sample.util.TakePhotoResult;
import com.wragony.android.jsbridge.sample.util.WebEvent;
import com.wragony.android.jsbridge.sample.widget.X5WebView;
import com.wragony.android.jsbridge.sample.widget.X5WebView.PromptResultCallback;
import com.wragony.android.jsbridge.sample.widget.X5WebView.PromptResultImpl;
import io.reactivex.functions.Consumer;

/**
 * @author wragony
 */
public class X5WebViewActivity extends BaseActivity implements WebEvent {

    private JsBridge jsBridge;
    private X5WebView mX5WebView;
    private Button nativeButton;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_x5webview);

        setTitle("X5 WebView");

        jsBridge = JsBridge.loadModule("window", "onJsBridgeReady", new ServiceModule(), new Static2Module(), new ListenerModule(), new NativeModule());

        WebView.setWebContentsDebuggingEnabled(true);

        mX5WebView = findViewById(R.id.x5webview);
        nativeButton = findViewById(R.id.button_native);

        mX5WebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
                super.onPageStarted(webView, s, bitmap);
            }

            @Override
            public void onPageFinished(WebView webView, String s) {
                super.onPageFinished(webView, s);
                jsBridge.injectJs(mX5WebView);
            }
        });
        mX5WebView.setPromptResult(new PromptResultCallback() {
            @Override
            public void onResult(String args, PromptResultImpl promptResult) {
                jsBridge.callJsPrompt(args, promptResult);
            }
        });
        nativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jsBridge.callJs("test", "", new OnValueCallback() {
                    @Override
                    public void onCallBack(String data) {
                        Log.d(JsBridge.TAG, "onCallBack:data=" + data);
                        Toast.makeText(X5WebViewActivity.this, data, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        new RxPermissions(this).request(Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                if (aBoolean) {
                    mX5WebView.loadUrl("file:///android_asset/x5.html");
                }
            }
        });

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
        ListenerModule.onResume(mX5WebView, "hello");
    }

    @Override
    protected void onPause() {
        super.onPause();
        ListenerModule.onPause(mX5WebView, 1.234);
    }


}
