package com.transcend.otg.Setting;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.MPPointF;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.R;
import com.transcend.otg.Utils.FileFactory;

import java.util.ArrayList;

/**
 * Created by wangbojie on 2017/3/29.
 */

public class CapacityActivity extends AppCompatActivity {

    private PieChart localChart, sdChart;
    private String sUsed, sFree;
    private long localUsedSize, localFreeSize, sdUsedSize, sdFreeSize;
    private TextView mTitle;
    private Button btnRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capacity);
        init();
        initToolbar();
        getStorageSize();

    }

    private void init() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.BLACK);
        localChart = (PieChart) findViewById(R.id.piechartlocal);
        sdChart = (PieChart) findViewById(R.id.piechartsd);
        sUsed = getResources().getString(R.string.used) + " : ";
        sFree = getResources().getString(R.string.free) + " :  ";
        btnRefresh = (Button) findViewById(R.id.btn_refresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getStorageSize();
            }
        });
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.capacity_toolbar);
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.mipmap.ic_navigation_arrow_white);
        mTitle = (TextView) toolbar.findViewById(R.id.capacity_title);
        mTitle.setText(getResources().getString(R.string.setting_capacity));
        mTitle.setTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }


    private void getStorageSize() {
        localUsedSize = FileFactory.getUsedStorageSizeLong(Constant.ROOT_LOCAL);
        localFreeSize = FileFactory.getStorageFreeSizeLong(Constant.ROOT_LOCAL);
        setStorageSize(localChart, localUsedSize, localFreeSize);
        String sdPath = FileFactory.getOuterStoragePath(this, Constant.sd_key_path);
        if (sdPath != null) {
            sdChart.setVisibility(View.VISIBLE);
            sdUsedSize = FileFactory.getUsedStorageSizeLong(sdPath);
            sdFreeSize = FileFactory.getStorageFreeSizeLong(sdPath);
            setStorageSize(sdChart, sdUsedSize, sdFreeSize);
        }else
            sdChart.setVisibility(View.GONE);

    }

    private void setStorageSize(PieChart pieChart, long usedSize, long freeSize) {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(0, 0, 0, 0);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        if (pieChart.equals(localChart))
            pieChart.setCenterText(getResources().getString(R.string.nav_local));
        else
            pieChart.setCenterText(getResources().getString(R.string.sdcard_name));
        pieChart.setCenterTextSize(20f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(getResources().getColor(R.color.colorWhite));


        pieChart.setHoleRadius(72f);
        pieChart.setTransparentCircleRadius(75f);

        pieChart.setDrawCenterText(true);

        pieChart.setRotationAngle(0);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(false);
        pieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        setData(pieChart, usedSize, freeSize);


        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(true);
        l.setXEntrySpace(0f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
        l.setTextSize(15f);

        pieChart.setDrawEntryLabels(false);
    }

    private void setData(PieChart pieChart, long usedSize, long freeSize) {
        sUsed = getResources().getString(R.string.used) + " : ";
        sFree = getResources().getString(R.string.free) + " : ";
        String usedString = Formatter.formatFileSize(this, usedSize);
        String freeString = Formatter.formatFileSize(this, freeSize);

        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
        entries.add(new PieEntry(freeSize, sFree += freeString));
        entries.add(new PieEntry(usedSize, sUsed += usedString));

        PieDataSet dataSet = new PieDataSet(entries, "");

        dataSet.setDrawIcons(false);

        dataSet.setSliceSpace(0f);
        dataSet.setSelectionShift(5f);
        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<Integer>();
        colors.add(Color.rgb(135, 206, 250));
        colors.add(Color.rgb(211, 211, 211));


        dataSet.setColors(colors);
        //dataSet.setSelectionShift(0f);

        PieData data = new PieData(dataSet);
        data.setDrawValues(false);
        pieChart.setData(data);

        // undo all highlights
        pieChart.highlightValues(null);

        pieChart.invalidate();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
