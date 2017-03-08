package com.transcend.otg.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.R;
import com.transcend.otg.Utils.FileFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by wangbojie on 2017/3/2.
 */

public class FolderExploreDropDownAdapter extends BaseAdapter {

    private static final String TAG = FolderExploreDropDownAdapter.class.getSimpleName();

    private static final String PREFIX_MAINPAGE = "Local";
    private static final String PREFIX_SD = "SD Card";

    private Spinner mDropdown;
    private List<String> mList;
    private boolean isActionLocate = false;
    private String sdPath;

    private OnDropdownItemSelectedListener mCallback;

    public interface OnDropdownItemSelectedListener {
        void onDropdownItemSelected(int position);
    }

    public FolderExploreDropDownAdapter(Context mContext, boolean actionLocate) {
        isActionLocate = actionLocate;
        mList = new ArrayList<String>();
        sdPath = FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path);

    }

    public void setOnDropdownItemSelectedListener(OnDropdownItemSelectedListener l) {
        mCallback = l;
    }

    public void updateList(String path) {
        if (path.contains(Constant.ROOT_LOCAL))
            path = path.replaceFirst(Constant.ROOT_LOCAL, PREFIX_MAINPAGE);
        else if (path.contains(sdPath))
            path = path.replaceFirst(sdPath, PREFIX_SD);

        List<String> list = new ArrayList<String>();
        String[] items = path.split("/");
        list = Arrays.asList(items);
        Collections.reverse(list);
        mList = list;
    }


    public void updateList(ArrayList<String> dropDownList) {
        List<String> list = new ArrayList<String>();
        for (int index = 0; index < dropDownList.size(); index++) {
            String[] splitList = dropDownList.get(index).split("@");
            list.add(splitList[0]);
        }
        Collections.reverse(list);
        mList = list;
    }

    public String getPath(int mMode, int position) {
        if (mMode == Constant.STORAGEMODE_LOCAL || mMode == Constant.STORAGEMODE_SD) {
            List<String> list = mList.subList(position, mList.size());
            Collections.reverse(list);
            StringBuilder builder = new StringBuilder();
            for (String item : list) {
                builder.append(item);
                builder.append("/");
            }
            String path = builder.toString();
            if (path.contains(PREFIX_SD))
                path = path.replaceFirst(PREFIX_SD, sdPath);
            else if (path.contains(PREFIX_MAINPAGE))
                path = path.replaceFirst(PREFIX_MAINPAGE, Constant.ROOT_LOCAL);

            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            return path;
        } else if (mMode == Constant.STORAGEMODE_OTG) {
            String path = mList.get(position);
            return path;
        }
        return "";
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (parent instanceof Spinner)
            mDropdown = (Spinner) parent;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.dropdown, parent, false);
            convertView = ViewHolder.get(view, R.id.dropdown_text);
        }
        ((TextView) convertView).setText(mList.get(0));
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, final ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.dropdown, parent, false);
        }
        convertView.setOnTouchListener(new OnDropdownItemTouchListener(position));
        TextView tv = ViewHolder.get(convertView, R.id.dropdown_text);
        tv.setText(mList.get(position));
        if (position > 0) {
            tv.setTextColor(isActionLocate ? Color.WHITE : Color.GRAY);
        } else {
            tv.setTextColor(isActionLocate ? Color.BLACK : Color.RED);
        }

        ImageView iv = ViewHolder.get(convertView, R.id.dropdown_icon);
        iv.setImageResource(isActionLocate ? R.drawable.ic_menu_camera : R.drawable.ic_menu_manage);
        RelativeLayout.LayoutParams margins = new RelativeLayout.LayoutParams(iv.getLayoutParams());
        margins.leftMargin = Math.min(8 * position, 32);
        iv.setLayoutParams(margins);

        return convertView;
    }

    public static class ViewHolder {
        @SuppressWarnings("unchecked")
        public static <T extends View> T get(View view, int id) {
            SparseArray<View> holder = (SparseArray<View>) view.getTag();
            if (holder == null) {
                holder = new SparseArray<View>();
                view.setTag(holder);
            }
            View child = holder.get(id);
            if (child == null) {
                child = view.findViewById(id);
                holder.put(id, child);
            }
            return (T) child;
        }
    }

    public class OnDropdownItemTouchListener implements View.OnTouchListener {

        private int mPosition;

        public OnDropdownItemTouchListener(int position) {
            mPosition = position;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (mCallback != null) {
                    mCallback.onDropdownItemSelected(mPosition);
                }
                dismissDropdownList();
            }
            return true;
        }

        /**
         * In order to make dropdown list scrollable,
         * onDropdownItemSelected callback should be called in ACTION_UP instead of ACTION_DOWN.
         * That causes one problem that dropdown list would not dismiss automatically.
         * One solution is to detach spinner from window by reflection method,
         * and dropdown list will disappear.
         */
        private void dismissDropdownList() {
            try {
                Method method = Spinner.class.getDeclaredMethod("onDetachedFromWindow");
                method.setAccessible(true);
                method.invoke(mDropdown);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
