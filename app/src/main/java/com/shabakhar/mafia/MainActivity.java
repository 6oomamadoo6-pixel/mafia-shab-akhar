package com.yafta.app;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
 @Override public void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  getWindow().setStatusBarColor(0xFFEAF3FC);
  getWindow().setNavigationBarColor(0xFFEAF3FC);
  getWindow().getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
  WebView web = new WebView(this);
  web.setWebViewClient(new WebViewClient());
  WebSettings settings = web.getSettings();
  settings.setJavaScriptEnabled(true);
  settings.setDomStorageEnabled(true);
  web.setBackgroundColor(0xFFEAF3FC);
  setContentView(web);
  web.loadUrl("file:///android_asset/index.html");
 }
}