package com.wragony.android.jsbridge.sample.module;

import android.util.Log;
import com.wragony.android.jsbridge.JsBridge;
import com.wragony.android.jsbridge.module.JSBridgeMethod;
import com.wragony.android.jsbridge.module.JsStaticModule;
import com.wragony.android.jsbridge.module.datatype.JBMap;

public class Static2Module extends JsStaticModule {

    @JSBridgeMethod
    public String mapToString2(JBMap map) {
        Log.e(JsBridge.TAG, map.getString("data"));
        return map.getString("data");
    }

}
