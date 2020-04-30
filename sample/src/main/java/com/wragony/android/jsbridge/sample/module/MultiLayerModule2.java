package com.wragony.android.jsbridge.sample.module;

import android.widget.Toast;
import com.wragony.android.jsbridge.module.JSBridgeMethod;
import com.wragony.android.jsbridge.module.JsModule;

public class MultiLayerModule2 extends JsModule {
    @Override
    public String getModuleName() {
        return "native.extend.api";
    }

    @JSBridgeMethod
    public void toast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
