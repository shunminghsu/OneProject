package com.transcend.otg.Security;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.transcend.otg.Browser.TabInfo;
import com.transcend.otg.MainActivity;
import com.transcend.otg.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangbojie on 2017/2/2.
 */

public class SecurityFragment extends Fragment implements SecurityListener.SecurityStatusListener{

    public SecurityFragment() {
    }

    private String TAG = SecurityFragment.class.getSimpleName();
    protected final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
    public TabInfo mCurTab = null;
    private LayoutInflater mInflater;

    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private List<PageView> pageViewList;
    protected Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mContext = getActivity();
        initSecurityListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SecurityListener.getInstance().removeSecurityListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInflater = inflater;
        CoordinatorLayout root = (CoordinatorLayout) inflater.inflate(R.layout.fragment_security, container, false);

        mTabLayout = (TabLayout) root.findViewById(R.id.tabLayout);
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.LRemovePW));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.LChangePW));

        initViewPage();
        mViewPager = (ViewPager) root.findViewById(R.id.viewPager);
        mViewPager.setAdapter(new MyPagerAdapter());

        initListener();
        return root;
    }

    private void initViewPage(){
        pageViewList = new ArrayList<>();
        pageViewList.add(new SecurityRemoveFragment(mContext));
        pageViewList.add(new SecurityChangeFragment(mContext));
    }

    private void Back2Home(){
        MainActivity activity = (MainActivity) getActivity();
        activity.setDrawerCheckItem(R.id.nav_home);
        activity.mToolbarTitle.setText(getResources().getString(R.string.drawer_home));
        activity.showHomeOrFragment(true);
    }

    private void initSecurityListener(){
        SecurityListener.getInstance().addSecurityListener(this);
    }

    @Override
    public void onSecurityChange(SecurityListener.SecurityStatus status) {
        switch (status){
            case Detached:
                Back2Home();
                break ;
            default:
                break;
        }
    }

    class MyPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return pageViewList.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(pageViewList.get(position));
            return pageViewList.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View)object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return object == view;
        }

    }

    private void initListener(){
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
    }
}
