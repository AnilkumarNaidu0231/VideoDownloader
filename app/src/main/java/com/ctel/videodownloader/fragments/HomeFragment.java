package com.ctel.videodownloader.fragments;

import android.app.DownloadManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.ctel.videodownloader.R;
import com.ctel.videodownloader.service.DownloadService;

import java.io.File;

public class HomeFragment extends Fragment {

    WebView webView;
    public static final String USER_AGENT = "Mozilla/5.0 (Linux; Android 4.1.1; Galaxy Nexus Build/JRO03C) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19";
    public static String videoId;
    public static String videoData;
    public static String mFilePath;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        webView = view.findViewById(R.id.webView1);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setUserAgentString(USER_AGENT);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.addJavascriptInterface(HomeFragment.this, "FBDownloader");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {

                webView.loadUrl("javascript:(function() { "
                        + "var el = document.querySelectorAll('div[data-sigil]');"
                        + "for(var i=0;i<el.length; i++)"
                        + "{"
                        + "var sigil = el[i].dataset.sigil;"
                        + "if(sigil.indexOf('inlineVideo') > -1){"
                        + "delete el[i].dataset.sigil;"
                        + "var jsonData = JSON.parse(el[i].dataset.store);"
                        + "el[i].setAttribute('onClick', 'FBDownloader.processVideo(\"'+jsonData['src']+'\");');"
                        + "}" + "}" + "})()");
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                webView.loadUrl("javascript:(function prepareVideo() { "
                        + "var el = document.querySelectorAll('div[data-sigil]');"
                        + "for(var i=0;i<el.length; i++)"
                        + "{"
                        + "var sigil = el[i].dataset.sigil;"
                        + "if(sigil.indexOf('inlineVideo') > -1){"
                        + "delete el[i].dataset.sigil;"
                        + "console.log(i);"
                        + "var jsonData = JSON.parse(el[i].dataset.store);"
                        + "el[i].setAttribute('onClick', 'FBDownloader.processVideo(\"'+jsonData['src']+'\",\"'+jsonData['videoID']+'\");');"
                        + "}" + "}" + "})()");
                webView.loadUrl("javascript:( window.onload=prepareVideo;"
                        + ")()");
            }
        });

        CookieSyncManager.createInstance(getActivity());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        CookieSyncManager.getInstance().startSync();

        webView.loadUrl("http://m.facebook.com");
        return view;
    }

    @JavascriptInterface
    public void processVideo(final String vidData, final String vidID) {
        videoId=vidID;
        videoData=vidData;
        try {
            String mBaseFolderPath = android.os.Environment
                    .getExternalStorageDirectory()
                    + File.separator
                    + "FacebookVideos" + File.separator;
            if (!new File(mBaseFolderPath).exists()) {
                new File(mBaseFolderPath).mkdir();
            }
             mFilePath = "file://" + mBaseFolderPath + "/" + vidID + ".mp4";
            Intent intent = new Intent(getActivity(), DownloadService.class);
            intent.putExtra(DownloadService.FILENAME, mFilePath);
            intent.putExtra(DownloadService.URLL,
                    vidData);
            getActivity().startService(intent);

            Log.d("TAG", "processVideo: "+vidData+"id"+vidID);
            Uri downloadUri = Uri.parse(vidData);
            DownloadManager.Request req = new DownloadManager.Request(downloadUri);
            req.setDestinationUri(Uri.parse(mFilePath));
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            DownloadManager dm = (DownloadManager) getActivity().getSystemService(getActivity().DOWNLOAD_SERVICE);
            dm.enqueue(req);
            Toast.makeText(getActivity(), "Download Started", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Download Failed: " + e.toString(), Toast.LENGTH_LONG).show();
        }
    }


}