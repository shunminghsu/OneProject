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

    private WebView mWebView;
    public HelpFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        LinearLayout root = (LinearLayout) inflater.inflate(R.layout.fragment_help, container, false);
        mWebView = (WebView) root.findViewById(R.id.webview);
        mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mWebView.loadUrl("https://us.transcend-info.com/Support/Software-1/");
        mWebView.setWebViewClient(new WebViewClient());
        return root;
    }

}
