package com.wragony.android.jsbridge;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.wragony.android.jsbridge.module.datatype.JBCallback;

final class JBCallbackImpl implements JBCallback {

    private String name;
    private JsMethod method;
    private Handler mHandler;

    JBCallbackImpl(@NonNull JsMethod method, @NonNull String name) {
        this.method = method;
        this.name = name;
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void apply(Object... args) {
        if (method == null || method.getModule() == null || method.getModule().mWebView == null
                || TextUtils.isEmpty(name)) {
            return;
        }
        String callback = method.getCallback();
        final StringBuilder builder = new StringBuilder();
        builder.append("if(" + callback + " && " + callback + "['" + name + "']){");
        builder.append("var callback = " + callback + "['" + name + "'];");
        builder.append("if (typeof callback === 'function'){callback(");
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                builder.append(JBUtils.toJsObject(args[i]));
                if (i != args.length - 1) {
                    builder.append(",");
                }
            }
        }
        builder.append(")}else{console.error(callback + ' is not a function')}}");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                /**
                 * update by wragony
                 */
                JBUtils.evalJs(method.getModule().mWebView,builder.toString(),null);
            }
        });
    }

}
