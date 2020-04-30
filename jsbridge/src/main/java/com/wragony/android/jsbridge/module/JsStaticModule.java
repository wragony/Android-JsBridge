package com.wragony.android.jsbridge.module;

public abstract class JsStaticModule extends JsModule {

    private static final String STATIC_METHOD_NAME = "@static";

    @Override
    public final String getModuleName() {
        return STATIC_METHOD_NAME;
    }
}
