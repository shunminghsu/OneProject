package com.transcend.otg.Browser;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.transcend.otg.Bitmap.IconHelper;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.MainActivity;
import com.transcend.otg.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by wangbojie on 2016/5/31.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {


    private ArrayList<FileInfo> mList;
    private HashMap<String, ArrayList<String>> mPathMap;
    private TabInfo mTab;
    private OnRecyclerItemCallbackListener mCallback;

    IconHelper mIconHelper;


    public interface OnRecyclerItemCallbackListener {
        void onRecyclerItemClick(int position);

        void onRecyclerItemLongClick(int position);

        void onRecyclerItemInfoClick(int position);
    }

    public RecyclerViewAdapter(TabInfo tab, IconHelper iconHelper) {
        mTab = tab;
        mIconHelper = iconHelper;
    }

    boolean isEmpty() {
        return mList == null ? true : mList.isEmpty();
    }

    void update(@Nullable ArrayList<FileInfo> items) {
        mList = items;
        mTab.showLoadingResult(isEmpty());
        notifyDataSetChanged();
    }

    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        if (viewType == Constant.ITEM_LIST) {
            return new ViewHolder(layoutInflater.inflate(R.layout.listitem_recyclerview, parent, false), viewType);
        }
        if (viewType == Constant.ITEM_GRID) {
            return new ViewHolder(layoutInflater.inflate(R.layout.griditem_recyclerview, parent, false), viewType);
        }
        if (viewType == Constant.ITEM_FOOTER) {
            View view = layoutInflater.inflate(R.layout.footitem_file_manage, parent, false);
            return new ViewHolder(view, viewType);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapter.ViewHolder holder, int position) {

        if (holder.viewType == Constant.ITEM_LIST || holder.viewType == Constant.ITEM_GRID) {
            FileInfo fileInfo = mList.get(position);
            String name = fileInfo.name;
            String time = fileInfo.time;
            String path = fileInfo.path;
            Uri uri = null;
            if (fileInfo.uri != null)
                uri = Uri.parse(fileInfo.uri);

            int resId = R.drawable.ic_menu_camera;
            if (fileInfo.type.equals(FileInfo.TYPE.DIR))
                resId = R.drawable.ic_menu_camera;
            else if (fileInfo.type.equals(FileInfo.TYPE.PHOTO))
                resId = R.drawable.ic_menu_camera;
            else if (fileInfo.type.equals(FileInfo.TYPE.VIDEO))
                resId = R.drawable.ic_menu_camera;
            else if (fileInfo.type.equals(FileInfo.TYPE.MUSIC))
                resId = R.drawable.ic_menu_camera;
            else if (fileInfo.type.equals(FileInfo.TYPE.ENCRYPT))
                resId = R.drawable.ic_menu_camera;
            if (holder.title != null)
                holder.title.setText(name);
            if (holder.subtitle != null)
                holder.subtitle.setText(time);

            if (holder.icon != null)
                holder.icon.setImageDrawable(mIconHelper.getIcon());
            if (holder.info != null) {
                if (fileInfo.type.equals(FileInfo.TYPE.DIR)) {
                    holder.info.setImageDrawable(mIconHelper.getIcon());
                    holder.info.setRotation(180);
                } else {
                    holder.info.setImageDrawable(mIconHelper.getIcon());
                    holder.info.setRotation(0);
                }
            }


            holder.itemView.setSelected(fileInfo.checked);
            holder.mark.setVisibility(fileInfo.checked ? View.VISIBLE : View.INVISIBLE);
        }
        if (holder.viewType == Constant.ITEM_FOOTER) {
            // do nothing
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (isFooter(position))
            return Constant.ITEM_FOOTER;
        return mTab.mMode;
    }

    @Override
    public int getItemCount() {
        if (mList != null)
            return hasFooter() ? mList.size() + 1 : mList.size();
        else
            return 0;
    }

    public boolean isFooter(int position) {
        return (hasFooter() && (position == mList.size()));
    }

    public boolean hasFooter() {
        return mList.size() > 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        int viewType;

        View itemView;
        ImageView mark;
        ImageView icon;
        ImageView info;
        TextView title;
        TextView subtitle;

        public ViewHolder(View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;
            this.itemView = itemView;
            if (viewType == Constant.ITEM_LIST) {
                    mark = (ImageView) itemView.findViewById(R.id.listitem_file_manage_mark);
                    icon = (ImageView) itemView.findViewById(R.id.listitem_file_manage_icon);
                    info = (ImageView) itemView.findViewById(R.id.listitem_file_manage_info);
                    title = (TextView) itemView.findViewById(R.id.listitem_file_manage_title);
                    subtitle = (TextView) itemView.findViewById(R.id.listitem_file_manage_subtitle);
                    setOnItemInfoClickListener();
                    itemView.setOnClickListener(this);
                    itemView.setOnLongClickListener(this);
            }
            if (viewType == Constant.ITEM_GRID) {
                mark = (ImageView) itemView.findViewById(R.id.griditem_file_manage_mark);
                icon = (ImageView) itemView.findViewById(R.id.griditem_file_manage_icon);
                title = (TextView) itemView.findViewById(R.id.griditem_file_manage_title);
                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
            }

            if (viewType == Constant.ITEM_FOOTER) {
                // do nothing
            }
        }

        @Override
        public void onClick(View v) {
            if (mCallback != null) {
                int position = getAdapterPosition();
                mCallback.onRecyclerItemClick(position);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mCallback != null) {
                int position = getAdapterPosition();
                mCallback.onRecyclerItemLongClick(position);
            }
            return true;
        }

        private void setOnItemInfoClickListener() {
            info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCallback == null) return;
                    int position = getAdapterPosition();
                    mCallback.onRecyclerItemInfoClick(position);
                }
            });
        }

    }
}
