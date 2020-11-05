package com.wragony.android.jsbridge.sample.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsPromptResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.wragony.android.jsbridge.JsBridge;
import com.wragony.android.jsbridge.sample.BuildConfig;
import com.wragony.android.jsbridge.sample.JBApplication;
import com.wragony.android.jsbridge.sample.module.ListenerModule;
import com.wragony.android.jsbridge.sample.module.NativeModule;
import com.wragony.android.jsbridge.sample.util.TakePhotoResult;
import com.wragony.android.jsbridge.sample.util.WebEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class CustomFragment extends Fragment implements WebEvent {

    private WebView webView;
    private JsBridge jsBridge;
    private TakePhotoResult result;
    private Uri imageUri;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        webView = new WebView(getContext());
        return webView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initWebView();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void initWebView() {
        jsBridge = JsBridge.loadModule("MyBridge", "onMyBridgeReady", new NativeModule(), new ListenerModule());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/fragment.html");
        WebView.setWebContentsDebuggingEnabled(true);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                if (jsBridge.callJsPrompt(message, result)) {
                    return true;
                }
                return super.onJsPrompt(view, url, message, defaultValue, result);
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                jsBridge.injectJs(view);
            }
        });
    }

    @Override
    public void takePhoto(TakePhotoResult result) {
        this.result = result;
        File outputImage = new File(JBApplication.getInstance().getExternalCacheDir(), "output_image.jpg");
        if (outputImage.exists()) {
            outputImage.delete();
        }
        try {
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageUri = FileProvider.getUriForFile(JBApplication.getInstance(), String.format("%s.appfileprovider", BuildConfig.APPLICATION_ID), outputImage);
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Bitmap photo = null;
            try {
                photo = BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(imageUri));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (result != null) {
                result.onSuccess(photo);
            }
        } else {
            if (result != null) {
                result.onFailure("user cancel");
            }
        }
    }
}
