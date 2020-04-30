package com.wragony.android.jsbridge;

import android.text.TextUtils;
import com.wragony.android.jsbridge.module.JsModule;
import java.util.ArrayList;
import java.util.List;

class JsBridgeConfigImpl extends JsBridgeConfig {

    private String protocol;
    private String readyFuncName;
    private List<Class<? extends JsModule>> defaultModule;
    private boolean isDebug;

    private JsBridgeConfigImpl() {
        defaultModule = new ArrayList<>();
    }

    private volatile static JsBridgeConfigImpl singleton;

    public static JsBridgeConfigImpl getInstance() {
        if (singleton == null) {
            synchronized (JsBridgeConfigImpl.class) {
                if (singleton == null) {
                    singleton = new JsBridgeConfigImpl();
                }
            }
        }
        return singleton;
    }

    @Override
    public JsBridgeConfig registerDefaultModule(Class<? extends JsModule>... modules) {
        if (modules != null) {
            for (Class<? extends JsModule> module : modules) {
                defaultModule.add(module);
            }
        }
        return this;
    }

    @Override
    public JsBridgeConfig setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    @Override
    public String getProtocol() {
        return TextUtils.isEmpty(protocol) ? JBUtils.DEFAULT_PROTOCOL : protocol;
    }

    @Override
    public JsBridgeConfig setLoadReadyMethod(String readyName) {
        this.readyFuncName = readyName;
        return this;
    }

    public String getReadyFuncName() {
        if (TextUtils.isEmpty(this.readyFuncName)) {
            return String.format("on%sReady", getProtocol());
        }
        return readyFuncName;
    }

    public List<Class<? extends JsModule>> getDefaultModule() {
        return defaultModule;
    }

    @Override
    public JsBridgeConfig debugMode(boolean debug) {
        this.isDebug = debug;
        return this;
    }

    public boolean isDebug() {
        return isDebug;
    }
}
