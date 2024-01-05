package com.manpronet.beaconsender.javascript;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

public class MyJavaScriptInterface {
    private Context context;

    public MyJavaScriptInterface(Context context){
        this.context = context;
    }

    @JavascriptInterface
    public void showHTML(String url){
        Log.d("MyJavaScriptInterface","-> " + url);
    }
}
