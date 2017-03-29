package com.transcend.otg.Help;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.transcend.otg.R;

/**
 * Created by wangbojie on 2017/3/26.
 */

public class HelpFragment extends Fragment {

    WebView mWebView;
    public HelpFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        LinearLayout root = (LinearLayout) inflater.inflate(R.layout.fragment_help, container, false);

        mWebView = (WebView) root.findViewById(R.id.webview);
        mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        mWebView.setWebViewClient(mWebViewClient);
        mWebView.loadUrl("https://tw.transcend-info.com/support/cate-3");
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        return root;
    }


    WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    };
}
