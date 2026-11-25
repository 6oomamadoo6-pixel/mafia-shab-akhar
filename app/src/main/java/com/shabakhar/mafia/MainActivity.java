package com.yafta.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceResponse;
import androidx.webkit.WebViewAssetLoader;

public class MainActivity extends Activity {
    private static final String TAG = "YAFTA";
    private static final int FILE_CHOOSER_REQUEST = 4102;
    private ValueCallback<Uri[]> fileUploadCallback;
    private WebView webView;
    private WebViewAssetLoader assetLoader;
    private View splashView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(0xFFF3F6FA);
        getWindow().setNavigationBarColor(0xFFF3F6FA);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );

        FrameLayout root = new FrameLayout(this);
        webView = new WebView(this);
        assetLoader = new WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
            .build();
        webView.setBackgroundColor(0xFFF3F6FA);
        webView.setVisibility(View.INVISIBLE);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);
        settings.setLoadsImagesAutomatically(true);
        settings.setUseWideViewPort(false);
        settings.setLoadWithOverviewMode(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.setVisibility(View.VISIBLE);
                if (splashView != null) {
                    View splash = splashView;
                    splash.animate().alpha(0f).setDuration(260).withEndAction(() -> {
                        if (splash.getParent() instanceof FrameLayout) {
                            ((FrameLayout) splash.getParent()).removeView(splash);
                        }
                        splashView = null;
                    }).start();
                }
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }

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

        ProgressBar pageProgress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        pageProgress.setMax(100);
        pageProgress.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFF1769AA));
        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, (int) (3 * getResources().getDisplayMetrics().density), Gravity.TOP
        );
        root.addView(webView, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
        ));
        root.addView(pageProgress, progressParams);

        FrameLayout splash = new FrameLayout(this);
        splash.setBackgroundColor(0xFFF3F6FA);
        LinearLayout splashContent = new LinearLayout(this);
        splashContent.setOrientation(LinearLayout.VERTICAL);
        splashContent.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView logo = new TextView(this);
        logo.setText("ی");
        logo.setTextColor(0xFFFFFFFF);
        logo.setTextSize(42);
        logo.setGravity(Gravity.CENTER);
        logo.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        GradientDrawable logoBackground = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[] { 0xFF2789C5, 0xFF12568F }
        );
        logoBackground.setCornerRadius(dp(28));
        logo.setBackground(logoBackground);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(dp(82), dp(82));
        logoParams.bottomMargin = dp(19);
        splashContent.addView(logo, logoParams);

        TextView appName = new TextView(this);
        appName.setText("یافتا");
        appName.setTextColor(0xFF173B58);
        appName.setTextSize(27);
        appName.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        splashContent.addView(appName);

        TextView tagline = new TextView(this);
        tagline.setText("بازار تازه‌ی خرید و فروش");
        tagline.setTextColor(0xFF7A8FA2);
        tagline.setTextSize(12);
        LinearLayout.LayoutParams taglineParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        taglineParams.topMargin = dp(7);
        taglineParams.bottomMargin = dp(25);
        splashContent.addView(tagline, taglineParams);

        ProgressBar splashProgress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        splashProgress.setIndeterminate(true);
        splashProgress.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(0xFF2789C5));
        splashContent.addView(splashProgress, new LinearLayout.LayoutParams(dp(118), dp(3)));

        FrameLayout.LayoutParams splashContentParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER
        );
        splash.addView(splashContent, splashContentParams);
        root.addView(splash, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
        ));
        splashView = splash;

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                pageProgress.setProgress(newProgress);
                pageProgress.setVisibility(newProgress >= 100 ? View.GONE : View.VISIBLE);
            }

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

        setContentView(root);
        webView.loadUrl("https://appassets.androidplatform.net/assets/index.html");
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
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
