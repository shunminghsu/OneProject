package com.transcend.otg.Adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.transcend.otg.R;

import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;

/**
 * Created by wangbojie on 2017/2/16.
 */

public class SDGuideAdapter extends PagerAdapter {
    public static final String TAG = SDGuideAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<Integer> mList;

    public SDGuideAdapter(Context context) {
        mContext = context;
        mList = new ArrayList<Integer>();
        mList.add(R.mipmap.guigeimage_sd1);
        mList.add(R.mipmap.guigeimage_sd2);
        mList.add((R.mipmap.guigeimage_sd3));
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        int id = mList.get(position);
        PhotoView pv = new PhotoView(mContext);
        pv.setDrawingCacheEnabled(false);
        pv.setImageResource(id);
        container.addView(pv);
        return pv;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (object instanceof ImageView) {
            ImageView iv = (ImageView) object;
//            ImageLoader.getInstance().cancelDisplayTask(iv);
            container.removeView(iv);
        }
    }


    public void removeView(int index) {
        mList.remove(index);
        notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(Object object) {
        if (mList.contains((View) object)) {
            return mList.indexOf((View) object);
        } else {
            return POSITION_NONE;
        }
    }
}
