package com.transcend.otg.Home;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.transcend.otg.Browser.BrowserFragment;
import com.transcend.otg.MainActivity;
import com.transcend.otg.R;

/**
 * Created by wangbojie on 2017/2/2.
 */

public class HomeFragment extends Fragment {
    private TextView tv_Browser;
    private TextView tv_Backup;
    private BrowserFragment browserFragment;
    private MainActivity activity;

    public HomeFragment() {    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        browserFragment = new BrowserFragment();
        activity = (MainActivity) getActivity();
        LinearLayout root = (LinearLayout) inflater.inflate(R.layout.fragment_home, container, false);
        tv_Browser = (TextView) root.findViewById(R.id.home_browser);
        tv_Browser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.setDrawerCheckItem(R.id.nav_browser);
                activity.replaceFragment(browserFragment);
            }
        });

        tv_Backup = (TextView) root.findViewById(R.id.home_backup);
        tv_Backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        return root;
    }

}
