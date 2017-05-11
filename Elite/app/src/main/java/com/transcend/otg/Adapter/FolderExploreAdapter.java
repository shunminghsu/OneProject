package com.transcend.otg.Adapter;

import android.content.Context;
import android.support.annotation.Nullable;
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
import com.transcend.otg.LocalPreferences;
import com.transcend.otg.R;

import java.util.ArrayList;

/**
 * Created by wangbojie on 2017/3/2.
 */

public class FolderExploreAdapter extends RecyclerView.Adapter<FolderExploreAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<FileInfo> mList;
    private IconHelper mIconHelper;
    private Boolean mShowSize = false;
    private OnRecyclerItemCallbackListener mCallback;

    public FolderExploreAdapter(Context context, IconHelper iconHelper) {
        mContext = context;
        mIconHelper = iconHelper;
        mShowSize = LocalPreferences.getPref(mContext,
                LocalPreferences.BROWSER_SORT_PREFIX, Constant.SORT_BY_DATE) == Constant.SORT_BY_SIZE;
    }

    public interface OnRecyclerItemCallbackListener {
        void onRecyclerItemClick(int position);

        void onRecyclerItemLongClick(int position);

        void onRecyclerItemInfoClick(int position);
    }

    public void setOnRecyclerItemCallbackListener(OnRecyclerItemCallbackListener l) {
        mCallback = l;
    }


    public void update(@Nullable ArrayList<FileInfo> items) {
        mList = items;
        mShowSize = LocalPreferences.getPref(mContext,
                LocalPreferences.BROWSER_SORT_PREFIX, Constant.SORT_BY_DATE) == Constant.SORT_BY_SIZE;
        notifyDataSetChanged();
    }

    @Override
    public FolderExploreAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        if (viewType == Constant.ITEM_LIST) {
            return new ViewHolder(layoutInflater.inflate(R.layout.listitem_recyclerview, parent, false), viewType);
        }
        if (viewType == Constant.ITEM_GRID) {
            return new ViewHolder(layoutInflater.inflate(R.layout.griditem_recyclerview_simple, parent, false), viewType);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(FolderExploreAdapter.ViewHolder holder, int position) {

        FileInfo fileInfo = mList.get(position);

        if (fileInfo.type == Constant.TYPE_DIR || fileInfo.type == Constant.TYPE_OTHER_FILE || fileInfo.type == Constant.TYPE_DOC)
            holder.title.setText(fileInfo.name);
        else
            holder.title.setText(fileInfo.name.substring(0, fileInfo.name.lastIndexOf(".")));
        if (holder.subtitle != null)
            holder.subtitle.setText(mShowSize ? fileInfo.format_size : fileInfo.time);

        //if (holder.info != null) {
            //holder.info.setVisibility(fileInfo.type == Constant.TYPE_DIR ? View.GONE : View.VISIBLE);
        //}

        if (fileInfo.type == Constant.TYPE_MUSIC) {
            mIconHelper.loadMusicThumbnail(fileInfo, holder.icon, holder.iconMime);
        } else if (fileInfo.type == Constant.TYPE_PHOTO && fileInfo.uri != null) {
            mIconHelper.loadThumbnail(fileInfo.uri, fileInfo.type, holder.icon, holder.iconMime);
        } else
            mIconHelper.loadThumbnail(fileInfo.path, fileInfo.type, holder.icon, holder.iconMime);


        holder.itemView.setSelected(fileInfo.checked);
        holder.mark.setVisibility(fileInfo.checked ? View.VISIBLE : View.INVISIBLE);

    }

    @Override
    public int getItemViewType(int position) {
        return mIconHelper.getViewMode();
    }

    public ArrayList<FileInfo> getSelectedFiles(){
        ArrayList<FileInfo> list = new ArrayList<>();
        if (mList == null)
            return list;
        for (FileInfo file : mList) {
            if (file.checked)
                list.add(file);
        }
        return list;
    }

    public boolean getSelectedAllorNot() {
        if (mList == null)
            return false;
        for (FileInfo file : mList) {
            if (!file.checked)
                return false;
        }
        return true;
    }

    public int getItemSelectedCount() {
        int count = 0;
        if (mList == null)
            return count;
        for (FileInfo file : mList) {
            if(file.checked)
                count++;
        }
        return count;
    }

    public void setAllSelection(){
        if (mList == null)
            return;
        for (FileInfo file : mList)
            file.checked = true;
        notifyDataSetChanged();
    }


    public void clearAllSelection(){
        if (mList == null)
            return;
        for (FileInfo file : mList)
            file.checked = false;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mList != null)
            return mList.size();
        else
            return 0;
    }

    public ArrayList<FileInfo> getAllFiles(){
        return mList;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        int viewType;

        View itemView;
        ImageView mark;
        ImageView icon;
        ImageView iconMime;
        ImageView info;
        TextView title;
        TextView subtitle;

        public ViewHolder(View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;
            this.itemView = itemView;

            mark = (ImageView) itemView.findViewById(R.id.item_mark);
            icon = (ImageView) itemView.findViewById(R.id.item_icon);
            iconMime = (ImageView) itemView.findViewById(R.id.item_mime);
            info = (ImageView) itemView.findViewById(R.id.item_info);
            title = (TextView) itemView.findViewById(R.id.item_title);
            subtitle = (TextView) itemView.findViewById(R.id.item_subtitle);
            if (info != null)
                setOnItemInfoClickListener();
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
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


