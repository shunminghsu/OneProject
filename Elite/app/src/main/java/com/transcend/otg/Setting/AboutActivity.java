package com.transcend.otg.Setting;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.transcend.otg.BuildConfig;
import com.transcend.otg.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by wangbojie on 2017/3/28.
 */

public class AboutActivity extends AppCompatActivity {

    public static final String TAG = AboutActivity.class.getSimpleName();
    private static boolean isSubFragment = false;
    private TextView mTitle;
    private static LinearLayout mAbout;
    private static Context mContext;
    private TextView mVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_about);
        mAbout = (LinearLayout) findViewById(R.id.about_layout);
        mVersion = (TextView) findViewById(R.id.about_version);
        mVersion.setText(getString(R.string.about_copyright));
        initToolbar();
        initFragment();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(isSubFragment)
                    initFragment();
                else
                    finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(isSubFragment)
            initFragment();
        else
            finish();
    }

    @Override
    public void onDestroy() {
        mContext = null;
        super.onDestroy();
    }

    /**
     * INITIALIZATION
     */
    private void initToolbar() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.BLACK);
        Toolbar toolbar = (Toolbar) findViewById(R.id.about_toolbar);
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.mipmap.ic_navigation_arrow_white);
        mTitle =(TextView) toolbar.findViewById(R.id.about_title);
        mTitle.setText(mContext.getResources().getString(R.string.setting_about));
        mTitle.setTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void initFragment() {

        mAbout.setVisibility(View.VISIBLE);
        isSubFragment = false;
        int id = R.id.about_frame;
        Fragment f = new AboutFragment();
        getFragmentManager().beginTransaction().replace(id, f).commit();
    }

    /**
     * ABOUT FRAGMENT
     */
    public static class AboutFragment extends PreferenceFragment {

        private Toast mToast;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference_about);
            Preference appVersionPref = findPreference(getString(R.string.about_version));
            appVersionPref.setSummary(BuildConfig.VERSION_NAME);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            int id = -1;
            if (preference.getKey().equals(getString(R.string.about_enduser))) {
                id = R.string.about_enduser;
            } else if (preference.getKey().equals(getString(R.string.about_opensource))) {
                id = R.string.about_opensource;
            }

            if(id > 0) {
                Fragment f = new InfoFragment(id);
                getFragmentManager().beginTransaction().replace(R.id.about_frame,f).commit();
                mAbout.setVisibility(View.GONE);
                isSubFragment = true;
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

    }

    public static class InfoFragment extends Fragment {
        int id = -1;

        public InfoFragment(){

        }

        @SuppressLint("ValidFragment")
        public InfoFragment(int id){
            this.id = id;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = null;
            if(id == R.string.about_enduser){
                v = inflater.inflate(R.layout.fragment_term_of_use, container, false);
                TextView info = (TextView) v.findViewById(R.id.info);
                try {
                    info.setText(Html.fromHtml(readFromAssets(getActivity(), "eula.txt")));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            else if(id == R.string.about_opensource){
                v = inflater.inflate(R.layout.fragment_opensource, container, false);
                TextView info = (TextView) v.findViewById(R.id.info);
                try {
                    info.setText(Html.fromHtml(readFromAssets(getActivity(), "statement.txt")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return v;
        }

        public String readFromAssets(Context context, String filename) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename)));

            // do reading, usually loop until end of file reading
            StringBuilder sb = new StringBuilder();
            String mLine = reader.readLine();
            while (mLine != null) {
                if(mLine.endsWith(".") && mLine.length() < 30)
                    sb.append(String.format("<h3>%s</h3>", mLine));
                else {
                    sb.append(String.format("<p>%s</p>", mLine));
                }
                sb.append(System.getProperty("line.separator"));
                mLine = reader.readLine();
            }
            reader.close();
            return sb.toString();
        }
    }



    private static class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

}
