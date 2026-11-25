package com.yafta.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private static final String TAG = "YAFTA";
    private static final int FILE_CHOOSER_REQUEST = 4102;
    private ValueCallback<Uri[]> fileUploadCallback;
    private WebView webView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        getWindow().setNavigationBarColor(0xFFF3F6FA);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        webView = new WebView(this);
        webView.setBackgroundColor(0xFFF3F6FA);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);
        settings.setLoadsImagesAutomatically(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
                if ("https".equals(scheme) || "http".equals(scheme)
                    || "tel".equals(scheme) || "mailto".equals(scheme)) {
                    try {
                        Intent intent;
                        if ("tel".equals(scheme)) {
                            intent = new Intent(Intent.ACTION_DIAL, uri);
                        } else if ("mailto".equals(scheme)) {
                            intent = new Intent(Intent.ACTION_SENDTO, uri);
                        } else {
                            intent = new Intent(Intent.ACTION_VIEW, uri);
                        }
                        startActivity(intent);
                        return true;
                    } catch (ActivityNotFoundException error) {
                        Log.w(TAG, "No application can open " + uri, error);
                        return true;
                    }
                }
                return false;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(
                WebView view,
                ValueCallback<Uri[]> filePathCallback,
                FileChooserParams fileChooserParams
            ) {
                if (fileUploadCallback != null) fileUploadCallback.onReceiveValue(null);
                fileUploadCallback = filePathCallback;
                try {
                    Intent intent = fileChooserParams.createIntent();
                    startActivityForResult(intent, FILE_CHOOSER_REQUEST);
                    return true;
                } catch (Exception error) {
                    Log.e(TAG, "Unable to open image picker", error);
                    fileUploadCallback = null;
                    return false;
                }
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage message) {
                Log.d(TAG, "JS " + message.message() + " (" +
                    message.sourceId() + ":" + message.lineNumber() + ")");
                return true;
            }
        });

        setContentView(webView);
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    public void onBackPressed() {
        if (webView == null) {
            super.onBackPressed();
            return;
        }
        webView.evaluateJavascript(
            "(function(){return window.handleAndroidBack ? window.handleAndroidBack() : false;})()",
            value -> {
                if ("true".equals(value)) return;
                if (webView != null && webView.canGoBack()) webView.goBack();
                else MainActivity.super.onBackPressed();
            }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST && fileUploadCallback != null) {
            Uri[] results = null;
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri single = data.getData();
                if (single != null) {
                    results = new Uri[] { single };
                } else if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    results = new Uri[count];
                    for (int i = 0; i < count; i++) {
                        results[i] = data.getClipData().getItemAt(i).getUri();
                    }
                }
            }
            fileUploadCallback.onReceiveValue(results);
            fileUploadCallback = null;
        }
    }

    @Override
    protected void onDestroy() {
        if (fileUploadCallback != null) {
            fileUploadCallback.onReceiveValue(null);
            fileUploadCallback = null;
        }
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
