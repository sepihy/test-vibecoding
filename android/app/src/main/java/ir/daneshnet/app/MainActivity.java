package ir.daneshnet.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.UUID;

public class MainActivity extends Activity {
    private static final String HOME = "https://daneshnet24.ir/wp-ac";
    private static final String PREFS = "daneshnet_app";
    private static final String DEVICE_KEY = "anonymous_device_key";
    private WebView webView;

    private String deviceKey() {
        SharedPreferences p = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String key = p.getString(DEVICE_KEY, null);
        if (key == null) {
            key = UUID.randomUUID().toString();
            p.edit().putString(DEVICE_KEY, key).apply();
        }
        return key;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setLoadsImagesAutomatically(true);
        s.setJavaScriptCanOpenWindowsAutomatically(false);
        s.setMediaPlaybackRequiresUserGesture(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setUserAgentString(s.getUserAgentString() + " DaneshNetAndroid/1.0.0");

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, false);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleUrl(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(Uri.parse(url));
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                reportNativeInstall(view);
            }
        });

        if (savedInstanceState == null) {
            webView.loadUrl(HOME);
        } else {
            webView.restoreState(savedInstanceState);
        }
    }

    private boolean handleUrl(Uri uri) {
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase();

        if ("https".equals(scheme) || "http".equals(scheme)) {
            if (host.equals("daneshnet24.ir") || host.endsWith(".daneshnet24.ir")) {
                return false;
            }
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            } catch (Exception ignored) {}
            return true;
        }

        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (Exception ignored) {}
        return true;
    }

    private void reportNativeInstall(WebView view) {
        String key = deviceKey().replace("\\", "\\\\").replace("'", "\\'");
        String js =
            "(function(){try{" +
            "var k='" + key + "';" +
            "fetch('/wp-json/dns-law/v1/app/telemetry'," +
            "{method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded;charset=UTF-8'}," +
            "body:new URLSearchParams({device_key:k,event:'standalone_open',display_mode:'standalone'," +
            "install_signal:'standalone_detected',platform:'android-apk',browser:'webview',outcome:'accepted'})" +
            "}).catch(function(){});" +
            "}catch(e){}})();";
        view.evaluateJavascript(js, null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
        super.onDestroy();
    }
}
