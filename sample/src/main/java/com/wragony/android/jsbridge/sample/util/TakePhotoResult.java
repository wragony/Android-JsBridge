package com.wragony.android.jsbridge.sample.util;

import android.graphics.Bitmap;

public abstract class TakePhotoResult {

    public abstract void onSuccess(Bitmap bitmap);

    public void onFailure(String error) {

    }
}
