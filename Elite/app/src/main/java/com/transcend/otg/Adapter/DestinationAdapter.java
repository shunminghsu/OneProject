package com.transcend.otg.Adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
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

public class DestinationAdapter extends RecyclerView.Adapter<DestinationAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<FileInfo> mList;
    private IconHelper mIconHelper;
    private Boolean mShowSize = false;
    private OnRecyclerItemCallbackListener mCallback;

    public DestinationAdapter(Context context, IconHelper iconHelper) {
        mContext = context;
        mIconHelper = iconHelper;
        mShowSize = LocalPreferences.getPref(mContext,
                LocalPreferences.BROWSER_SORT_PREFIX, Constant.SORT_BY_DATE) == Constant.SORT_BY_SIZE;
    }

    public interface OnRecyclerItemCallbackListener {
        void onRecyclerItemClick(int position);

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
    public DestinationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
    public void onBindViewHolder(DestinationAdapter.ViewHolder holder, int position) {

        FileInfo fileInfo = mList.get(position);

        holder.title.setText(fileInfo.name);
        if (holder.subtitle != null)
            holder.subtitle.setText(mShowSize ? fileInfo.format_size : fileInfo.time);

        if (holder.info != null) {
            holder.info.setVisibility(View.GONE);
        }

        if (fileInfo.type == Constant.TYPE_MUSIC) {
            mIconHelper.loadMusicThumbnail(fileInfo.path, fileInfo.album_id, holder.icon, holder.iconMime);
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

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

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
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mCallback != null) {
                int position = getAdapterPosition();
                mCallback.onRecyclerItemClick(position);
            }
        }

    }
}


