package com.transcend.otg.Browser;

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
import java.util.HashMap;

/**
 * Created by wangbojie on 2016/5/31.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private String TAG = RecyclerViewAdapter.class.getSimpleName();
    private ArrayList<FileInfo> mList;
    private HashMap<String, ArrayList<String>> mPathMap;
    private TabInfo mTab;
    private OnRecyclerItemCallbackListener mCallback;
    public Boolean mShowSize = false;
    private Context mContext;
    private OnActionModeItemCallbackListener mActionModeCallback;

    IconHelper mIconHelper;


    public interface OnRecyclerItemCallbackListener {
        void onRecyclerItemClick(FileInfo file);

        void onRecyclerItemLongClick(FileInfo file);

        void onRecyclerItemInfoClick(String path);
    }

    public interface OnActionModeItemCallbackListener{
        void onItemClick(int count);
        void onItemLongClick();
    }

    public RecyclerViewAdapter(TabInfo tab, IconHelper iconHelper, Context context) {
        mTab = tab;
        mContext = context;
        mIconHelper = iconHelper;
        mCallback = tab;
        mShowSize = LocalPreferences.getPref(mContext,
                LocalPreferences.BROWSER_SORT_PREFIX, Constant.SORT_BY_DATE) == Constant.SORT_BY_SIZE;
        mActionModeCallback = tab;
    }

    boolean isEmpty() {
        return mList == null ? true : mList.isEmpty();
    }

    void update(@Nullable ArrayList<FileInfo> items) {
        mList = items;
        mShowSize = LocalPreferences.getPref(mContext,
                LocalPreferences.BROWSER_SORT_PREFIX, Constant.SORT_BY_DATE) == Constant.SORT_BY_SIZE;
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
                return new ViewHolder(layoutInflater.inflate(
                        (mTab.mType > BrowserFragment.LIST_TYPE_MUSIC) ? R.layout.griditem_recyclerview_simple : R.layout.griditem_recyclerview
                        , parent, false), viewType);
        }
        if (viewType == Constant.ITEM_FOOTER) {
            View view = layoutInflater.inflate(R.layout.footitem_file_manage, parent, false);
            return new ViewHolder(view, viewType);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapter.ViewHolder holder, int position) {
        if (holder.viewType == Constant.ITEM_FOOTER)
            return;

        FileInfo fileInfo = mList.get(position);

        holder.title.setText(fileInfo.name);
        if (holder.subtitle != null)
            holder.subtitle.setText(mShowSize ? fileInfo.format_size : fileInfo.time);

        if (fileInfo.type ==  FileInfo.TYPE.DIR && holder.info != null) {
            holder.info.setVisibility(View.GONE);
        }

        if (fileInfo.album_id != 0) {
            mIconHelper.loadMusicThumbnail(fileInfo.path, fileInfo.album_id, fileInfo.album_id, holder.icon, holder.iconMime);
        } else if (fileInfo.uri != null) {
            mIconHelper.loadThumbnail(fileInfo.uri, fileInfo.type, holder.icon, holder.iconMime);
        } else
            mIconHelper.loadThumbnail(fileInfo.path, fileInfo.type, holder.icon, holder.iconMime);


        if (holder.viewType == Constant.ITEM_GRID) {
            int resId = R.drawable.ic_menu_camera;

        } else { //holder.viewType == Constant.ITEM_LIST

        }

        holder.itemView.setSelected(fileInfo.checked);
        holder.mark.setVisibility(fileInfo.checked ? View.VISIBLE : View.INVISIBLE);
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
            return mList.size();
//            return hasFooter() ? mList.size() + 1 : mList.size();
        else
            return 0;
    }

    public boolean isFooter(int position) {
        return (hasFooter() && (position == mList.size()));
    }

    public boolean hasFooter() {
        return mList.size() > 0;
    }

    private void setIconForAllType() {

    }

    public void setAllSelection(){
        for (FileInfo file : mList)
            file.checked = true;
        notifyDataSetChanged();
    }


    public void clearAllSelection(){
        for (FileInfo file : mList)
            file.checked = false;
        notifyDataSetChanged();
    }

    public boolean getSelectedAllorNot() {
        for (FileInfo file : mList) {
            if (!file.checked)
                return false;
        }
        return true;
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
            title = (TextView) itemView.findViewById(R.id.item_title);
            info = (ImageView) itemView.findViewById(R.id.item_info); //be null when using simple grid layout
            subtitle = (TextView) itemView.findViewById(R.id.item_subtitle);//be null when using grid layout
            if (info != null)
                setOnItemInfoClickListener();
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            if (viewType == Constant.ITEM_FOOTER) {
                // do nothing
            }
        }

        @Override
        public void onClick(View v) {
            if (mCallback != null && Constant.mActionMode == null) {
                mCallback.onRecyclerItemClick(mList.get(getAdapterPosition()));
            }else{
                selectAtPosition(getAdapterPosition());
                if(mActionModeCallback!=null){
                    mActionModeCallback.onItemClick(getSelectedCount());
                }
            }
        }

        private void selectAtPosition(int position) {
            mList.get(position).checked = !(mList.get(position).checked);
            notifyItemChanged(position);
        }

        private int getSelectedCount() {
            int count = 0;
            for (FileInfo file : mList) {
                if (file.checked) count++;
            }
            return count;
        }

        @Override
        public boolean onLongClick(View v) {
            if(mActionModeCallback != null){
                mActionModeCallback.onItemLongClick();
            }
            return true;
        }

        private void setOnItemInfoClickListener() {
            info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCallback == null) return;
                    if (Constant.nowMODE == Constant.MODE.OTG) {
                        mCallback.onRecyclerItemInfoClick(mList.get(getAdapterPosition()).uri.toString());
                    } else {
                        mCallback.onRecyclerItemInfoClick(mList.get(getAdapterPosition()).path);
                    }
                }
            });
        }

    }
}
