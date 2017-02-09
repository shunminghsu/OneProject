package com.transcend.otg.Browser;

import android.net.Uri;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by wangbojie on 2016/5/31.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {


    private static final int ITEM_VIEW_TYPE_CONTENT = 0;
    private static final int ITEM_VIEW_TYPE_FOOTER = 1;

    private ArrayList<FileInfo> mList;
    private HashMap<String, ArrayList<String>> mPathMap;
    private OnRecyclerItemCallbackListener mCallback;

    public enum LayoutType {
        LIST, GRID
    }

    public interface OnRecyclerItemCallbackListener {
        void onRecyclerItemClick(int position);

        void onRecyclerItemLongClick(int position);

        void onRecyclerItemInfoClick(int position);
    }

    public RecyclerViewAdapter(ArrayList<FileInfo> list) {
        updateList(list);
    }

    public void setOnRecyclerItemCallbackListener(OnRecyclerItemCallbackListener l) {
        mCallback = l;
    }

    public void updateList(ArrayList<FileInfo> list) {
        mList = list;
    }

    public void updateList(ArrayList<FileInfo> list, HashMap<String, ArrayList<String>> pathMap) {
        mList = list;
        mPathMap = pathMap;
    }


    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_VIEW_TYPE_CONTENT) {
            int resource = R.layout.listitem_recyclerview;
            if (((RecyclerView) parent).getLayoutManager() instanceof GridLayoutManager)
                resource = R.layout.griditem_recyclerview;
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(resource, parent, false);
            return new ViewHolder(view, viewType);
        }
        if (viewType == ITEM_VIEW_TYPE_FOOTER) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.footitem_file_manage, parent, false);
            return new ViewHolder(view, viewType);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapter.ViewHolder holder, int position) {

        if (holder.viewType == ITEM_VIEW_TYPE_CONTENT) {
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
                holder.icon.setImageResource(resId);
            if (holder.info != null) {
                if (fileInfo.type.equals(FileInfo.TYPE.DIR)) {
                    holder.info.setImageResource(R.drawable.ic_menu_camera);
                    holder.info.setRotation(180);
                } else {
                    holder.info.setImageResource(R.drawable.ic_menu_camera);
                    holder.info.setRotation(0);
                }
            }


            holder.itemView.setSelected(fileInfo.checked);
            holder.mark.setVisibility(fileInfo.checked ? View.VISIBLE : View.INVISIBLE);
        }
        if (holder.viewType == ITEM_VIEW_TYPE_FOOTER) {
            // do nothing
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (isFooter(position))
            return ITEM_VIEW_TYPE_FOOTER;
        return ITEM_VIEW_TYPE_CONTENT;
    }

    @Override
    public int getItemCount() {
        return hasFooter() ? mList.size() + 1 : mList.size();
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
            if (viewType == ITEM_VIEW_TYPE_CONTENT) {
                if (itemView.getId() == R.id.listitem_file_manage) {
                    mark = (ImageView) itemView.findViewById(R.id.listitem_file_manage_mark);
                    icon = (ImageView) itemView.findViewById(R.id.listitem_file_manage_icon);
                    info = (ImageView) itemView.findViewById(R.id.listitem_file_manage_info);
                    title = (TextView) itemView.findViewById(R.id.listitem_file_manage_title);
                    subtitle = (TextView) itemView.findViewById(R.id.listitem_file_manage_subtitle);
                    setOnItemInfoClickListener();
                }
                if (itemView.getId() == R.id.griditem_file_manage) {
                    mark = (ImageView) itemView.findViewById(R.id.griditem_file_manage_mark);
                    icon = (ImageView) itemView.findViewById(R.id.griditem_file_manage_icon);
                    title = (TextView) itemView.findViewById(R.id.griditem_file_manage_title);
                }
                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
            }
            if (viewType == ITEM_VIEW_TYPE_FOOTER) {
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
