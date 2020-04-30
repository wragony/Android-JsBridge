package com.wragony.android.jsbridge.sample.widget;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.JsPromptResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import com.wragony.android.jsbridge.JsBridge;
import com.wragony.android.jsbridge.common.IPromptResult;
import com.wragony.android.jsbridge.common.IWebView;
import com.wragony.android.jsbridge.common.OnValueCallback;

/**
 * @author wragony
 */
public class CustomWebView extends FrameLayout implements IWebView {

    private WebView webView;
    private PromptResultCallback callback;
    private Button button;
    private OnClickListener mListener;

    public CustomWebView(Context context) {
        super(context);
        this.webView = new WebView(context);
        this.webView.getSettings().setJavaScriptEnabled(true);

        this.button = new Button(context);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.BOTTOM;
        this.button.setLayoutParams(layoutParams);
        this.button.setText("Test");

        addView(this.webView);
        addView(this.button);
        this.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsPrompt(WebView view, String url,
                    String message, String defaultValue, JsPromptResult result) {
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
        });

        this.button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClick(v);
                }
            }
        });
    }

    public void setWebViewClient(WebViewClient webViewClient) {
        this.webView.setWebViewClient(webViewClient);
    }

    @Override
    public void loadUrl(String url) {
        webView.loadUrl(url);
    }

    @Override
    public void evaluateJavascript(String script, final OnValueCallback mCallback) {
        if (mCallback == null) {
            webView.evaluateJavascript(script, null);
            return;
        }
        webView.evaluateJavascript(script, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                mCallback.onCallBack(value);
            }
        });
    }

    public void setPromptResult(final PromptResultCallback callback) {
        this.callback = callback;
    }

    public void setOnclick(final OnClickListener mListener) {
        this.mListener = mListener;
    }


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

}
