package com.transcend.otg;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.transcend.otg.Bitmap.IconHelper;
import com.transcend.otg.Browser.TabInfo;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.Loader.SearchLoader;
import com.transcend.otg.Photo.PhotoActivity;
import com.transcend.otg.Task.ComputeFilsNumberTask;
import com.transcend.otg.Task.ComputeFilsTotalSizeTask;
import com.transcend.otg.Utils.MediaUtils;

import java.util.ArrayList;

import static com.transcend.otg.MainActivity.mScreenW;

/**
 * Created by henry_hsu on 2017/2/6.
 */

public class SearchResults extends Fragment {
    private static final String TAG = "SearchResults";

    private static final String EMPTY_QUERY = "";

    private SearchView mSearchView;
    private EditText mEditTextView;

    private TextView mEmptyView;
    private RecyclerView mResultsListView;
    private SearchResultsAdapter mResultsAdapter;

    private ListPopupWindow mSuggestionsListView;
    private SuggestionsAdapter mSuggestionsAdapter;
    private UpdateSuggestionsTask mUpdateSuggestionsTask;

    private ViewGroup mLayoutResults;

    private String mQuery;

    private boolean mShowResults;

    private LoaderManager.LoaderCallbacks<ArrayList<FileInfo>> mCallbacks;
    IconHelper mIconHelper;

    private TabInfo.OnItemCallbackListener mActionCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mResultsAdapter = new SearchResultsAdapter(getActivity());
        mSuggestionsAdapter = new SuggestionsAdapter(getActivity());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mIconHelper = new IconHelper(getActivity(), Constant.ITEM_LIST);
        mActionCallback = (TabInfo.OnItemCallbackListener) getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!mShowResults) {
            showSomeSuggestions();
        } else {
            updateSearchResults();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        getLoaderManager().destroyLoader(477);
        clearSuggestions();
    }

    @Override
    public void onDestroy() {
        mEmptyView = null;
        mResultsListView = null;
        mResultsAdapter = null;

        mSuggestionsListView = null;
        mSuggestionsAdapter = null;
        mUpdateSuggestionsTask = null;

        mSearchView = null;
        mActionCallback = null;
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final Context context = inflater.getContext();
        mCallbacks = new LoaderManager.LoaderCallbacks<ArrayList<FileInfo>>() {
            @Override
            public Loader<ArrayList<FileInfo>> onCreateLoader(int id, Bundle args) {
                return new SearchLoader(context, mQuery);
            }

            @Override
            public void onLoadFinished(Loader<ArrayList<FileInfo>> loader, ArrayList<FileInfo> data) {
                setResultsVisibility(true);
                mResultsAdapter.update(data);
            }

            @Override
            public void onLoaderReset(Loader<ArrayList<FileInfo>> loader) {

            }
        };

        final View view = inflater.inflate(R.layout.search_panel, container, false);
        mLayoutResults = (ViewGroup) view.findViewById(R.id.layout_results);

        mEmptyView = (TextView) view.findViewById(R.id.empty_view);
        mResultsListView = (RecyclerView) view.findViewById(R.id.list_results);
        mResultsListView.setLayoutManager(new LinearLayoutManager(context));
        mResultsListView.setAdapter(mResultsAdapter);


        mSuggestionsListView = new ListPopupWindow(context);
        mSuggestionsListView.setAdapter(mSuggestionsAdapter);
        mSuggestionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0) {
                    return;
                }
                final Cursor cursor = mSuggestionsAdapter.mCursor;
                cursor.moveToPosition(position);

                mShowResults = true;
                mQuery = cursor.getString(0);
                mSearchView.setQuery(mQuery, false);
                InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);// close soft keyboard
            }
        });
        return view;
    }

    private class UpdateSuggestionsTask extends AsyncTask<String, Void, Cursor> {
        @Override
        protected Cursor doInBackground(String... params) {
            return SearchSuggestion.getInstance(getActivity()).getSuggestions(params[0]);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            if (!isCancelled()) {
                setSuggestionsCursor(cursor);
                setSuggestionsVisibility(cursor.getCount() > 0);
            } else if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static class SuggestionItem {
        public String query;

        public SuggestionItem(String query) {
            this.query = query;
        }
    }

    private static class SuggestionsAdapter extends BaseAdapter {

        private static final int COLUMN_SUGGESTION_QUERY = 0;

        private Context mContext;
        private Cursor mCursor;
        private LayoutInflater mInflater;
        private boolean mDataValid = false;

        public SuggestionsAdapter(Context context) {
            mContext = context;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mDataValid = false;
        }

        public Cursor swapCursor(Cursor newCursor) {
            if (newCursor == mCursor) {
                return null;
            }
            Cursor oldCursor = mCursor;
            mCursor = newCursor;
            if (newCursor != null) {
                mDataValid = true;
                notifyDataSetChanged();
            } else {
                mDataValid = false;
                notifyDataSetInvalidated();
            }
            return oldCursor;
        }

        @Override
        public int getCount() {
            if (!mDataValid || mCursor == null || mCursor.isClosed()) return 0;
            return mCursor.getCount();
        }

        @Override
        public Object getItem(int position) {
            if (mDataValid && mCursor.moveToPosition(position)) {
                final String query = mCursor.getString(COLUMN_SUGGESTION_QUERY);

                return new SuggestionItem(query);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (!mDataValid && convertView == null) {
                throw new IllegalStateException(
                        "this should only be called when the cursor is valid");
            }
            if (!mCursor.moveToPosition(position)) {
                throw new IllegalStateException("couldn't move cursor to position " + position);
            }

            View view;

            if (convertView == null) {
                view = mInflater.inflate(R.layout.search_suggestion_item, parent, false);
            } else {
                view = convertView;
            }

            TextView query = (TextView) view.findViewById(R.id.title);

            SuggestionItem item = (SuggestionItem) getItem(position);
            query.setText(item.query);

            return view;
        }
    }

    private class SearchResultsAdapter extends RecyclerView.Adapter<SearchResults.ViewHolder> {
        private Context mContext;
        private ArrayList<FileInfo> mList;

        public SearchResultsAdapter(Context context) {
            mContext = context;
        }

        void update(@Nullable ArrayList<FileInfo> items) {
            mList = items;
            showLoadingResult(items.isEmpty());
            notifyDataSetChanged();
        }

        @Override
        public SearchResults.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final Context context = parent.getContext();
            View v = LayoutInflater.from(context).inflate(R.layout.listitem_recyclerview, parent, false);
            SearchResults.ViewHolder vh = new SearchResults.ViewHolder(v,
                    new SearchResults.ViewHolder.OnRecyclerItemListener() {
                @Override
                public void onRecyclerItemClick(int position) {
                    if (Constant.mActionMode != null) {
                        selectAtPosition(position);
                        mActionCallback.onItemClick(getSelectedCount());
                        return;
                    }
                    FileInfo fileInfo = mList.get(position);
                    switch (fileInfo.type) {
                        case Constant.TYPE_PHOTO:
                            Intent intent = new Intent(mContext, PhotoActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            intent.putParcelableArrayListExtra("photo_list", getPhotoList(mList, position));
                            intent.putExtra("list_index", mPhotoListPosition);
                            mContext.startActivity(intent);
                            break;
                        case Constant.TYPE_MUSIC:
                        case Constant.TYPE_VIDEO:
                        case Constant.TYPE_DOC:
                        case Constant.TYPE_OTHER_FILE:
                            if (fileInfo.storagemode == Constant.STORAGEMODE_OTG)
                                MediaUtils.executeUri(mContext, fileInfo.uri.toString(), mContext.getResources().getString(R.string.openin_title));
                            else
                                MediaUtils.execute(mContext, fileInfo.path, mContext.getResources().getString(R.string.openin_title));
                            break;
                        case Constant.TYPE_ENCRYPT:
                            break;
                        case Constant.TYPE_DIR:
                            Constant.mCurrentFile = fileInfo;
                            startActivityForResult(new Intent().setClass(context, FolderExploreActivity.class), FolderExploreActivity.REQUEST_CODE);
                            break;
                        default:
                    }
                }

                @Override
                public void onRecyclerItemLongClick(int position) {
                    selectAtPosition(position);
                    mActionCallback.onItemLongClick(getSelectedCount());
                }

                @Override
                public void onRecyclerItemInfoClick(int position) {
                    createInfoDialog(mContext, mList.get(position) , MainActivity.mScreenW);
                }
                    });
            return vh;
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

        private ArrayList<FileInfo> getSelectedFiles(){
            ArrayList<FileInfo> list = new ArrayList<>();
            for (FileInfo file : mList) {
                if (file.checked)
                    list.add(file);
            }
            return list;
        }

        private void clearAllSelection(){
            for (FileInfo file : mList)
                file.checked = false;
            notifyDataSetChanged();
        }

        private void setAllSelection(){
            for (FileInfo file : mList)
                file.checked = true;
            notifyDataSetChanged();
        }

        private boolean getSelectedAllorNot() {
            for (FileInfo file : mList) {
                if (!file.checked)
                    return false;
            }
            return true;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            FileInfo fileInfo = mList.get(position);

            if (fileInfo.type == Constant.TYPE_DIR || fileInfo.type == Constant.TYPE_OTHER_FILE || fileInfo.type == Constant.TYPE_DOC)
                holder.title.setText(fileInfo.name);
            else
                holder.title.setText(fileInfo.name.substring(0, fileInfo.name.lastIndexOf(".")));
            if (holder.subtitle != null)
                holder.subtitle.setText(fileInfo.time);

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
        public int getItemCount() {
            if (mList != null)
                return mList.size();
            else
                return 0;
        }
    }


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private OnRecyclerItemListener listener;

        View itemView;
        ImageView mark;
        ImageView icon;
        ImageView iconMime;
        ImageView info;
        TextView title;
        TextView subtitle;

        public ViewHolder(View itemView, OnRecyclerItemListener _listener) {
            super(itemView);
            this.itemView = itemView;
            listener = _listener;

            mark = (ImageView) itemView.findViewById(R.id.item_mark);
            icon = (ImageView) itemView.findViewById(R.id.item_icon);
            iconMime = (ImageView) itemView.findViewById(R.id.item_mime);
            info = (ImageView) itemView.findViewById(R.id.item_info);
            title = (TextView) itemView.findViewById(R.id.item_title);
            subtitle = (TextView) itemView.findViewById(R.id.item_subtitle);

            info.setOnClickListener(this);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == info) {
                listener.onRecyclerItemInfoClick(getAdapterPosition());
            } else {
                listener.onRecyclerItemClick(getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            listener.onRecyclerItemLongClick(getAdapterPosition());
            return true;
        }

        public interface OnRecyclerItemListener {
            void onRecyclerItemClick(int position);
            void onRecyclerItemLongClick(int position);
            void onRecyclerItemInfoClick(int position);
        }
    }


    public void setSearchView(SearchView searchView, EditText edit_view) {
        mSearchView = searchView;
        mSuggestionsListView.setAnchorView(edit_view);
        mSuggestionsListView.setWidth(2*mScreenW/3);
        mEditTextView = edit_view;
        mEditTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSuggestionsListView.show();
            }
        });
    }

    private void setSuggestionsVisibility(boolean visible) {
        if (visible)
            mSuggestionsListView.show();
        else
            mSuggestionsListView.dismiss();
    }

    private void setResultsVisibility(boolean visible) {
        if (mLayoutResults != null) {
            mLayoutResults.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private void saveQueryToDatabase() {
        SearchSuggestion.getInstance(getActivity()).addSavedQuery(mQuery);
    }

    public boolean onQueryTextSubmit(String query) {
        mQuery = getFilteredQueryString(query);
        mShowResults = true;
        setSuggestionsVisibility(false);
        updateSearchResults();
        saveQueryToDatabase();
        return true;
    }

    public boolean onQueryTextChange(String query) {
        final String newQuery = getFilteredQueryString(query);

        mQuery = newQuery;
        if (TextUtils.isEmpty(mQuery)) {
            mShowResults = false;
            setResultsVisibility(false);
            updateSuggestions();
        } else {
            mShowResults = true;
            setSuggestionsVisibility(false);
            updateSearchResults();
        }

        return true;
    }

    private void updateSearchResults() {
        getLoaderManager().restartLoader(477, getArguments(), mCallbacks);
    }

    public void showSomeSuggestions() {
        setResultsVisibility(false);
        mQuery = EMPTY_QUERY;
        updateSuggestions();
    }

    private void clearSuggestions() {
        if (mUpdateSuggestionsTask != null) {
            mUpdateSuggestionsTask.cancel(false);
            mUpdateSuggestionsTask = null;
        }
        setSuggestionsCursor(null);
    }

    private void setSuggestionsCursor(Cursor cursor) {
        if (mSuggestionsAdapter == null) {
            return;
        }
        Cursor oldCursor = mSuggestionsAdapter.swapCursor(cursor);
        if (oldCursor != null) {
            oldCursor.close();
        }
    }

    private void clearAllTasks() {
        if (mUpdateSuggestionsTask != null) {
            mUpdateSuggestionsTask.cancel(false);
            mUpdateSuggestionsTask = null;
        }
    }

    private void updateSuggestions() {
        clearAllTasks();
        if (mQuery == null) {
            setSuggestionsCursor(null);
        } else {
            mUpdateSuggestionsTask = new UpdateSuggestionsTask();
            mUpdateSuggestionsTask.execute(mQuery);
        }
    }

    private String getFilteredQueryString(CharSequence query) {
        if (query == null) {
            return null;
        }
        final StringBuilder filtered = new StringBuilder();
        for (int n = 0; n < query.length(); n++) {
            char c = query.charAt(n);
            if (!Character.isLetterOrDigit(c) && !Character.isSpaceChar(c)) {
                continue;
            }
            filtered.append(c);
        }
        return filtered.toString();
    }

    public void showLoadingResult(boolean empty) {
        if (empty)
            mEmptyView.setVisibility(View.VISIBLE);
        else
            mEmptyView.setVisibility(View.GONE);
    }

    int mPhotoListPosition = 0;
    private ArrayList<FileInfo> getPhotoList(ArrayList<FileInfo> list, int position) {
        ArrayList<FileInfo> photoList = new ArrayList<FileInfo>();
        for (int i=0;i<list.size();i++) {
            if (list.get(i).type == Constant.TYPE_PHOTO)
                photoList.add(list.get(i));
            if (i == position)
                mPhotoListPosition = photoList.size() - 1;
        }
        return photoList;
    }

    private void createInfoDialog(Context context, FileInfo fileInfo, int dialog_size) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View mInfoDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_info, null);
        ((TextView) mInfoDialogView.findViewById(R.id.name)).setText(fileInfo.name);
        ((TextView) mInfoDialogView.findViewById(R.id.type)).setText(getFileTypeString(context, fileInfo.type));
        if (fileInfo.format_size == null) {
            ((TextView) mInfoDialogView.findViewById(R.id.size)).setText(Formatter.formatFileSize(context, fileInfo.size));
        } else {
            ((TextView) mInfoDialogView.findViewById(R.id.size)).setText(fileInfo.format_size);
        }
        ((TextView) mInfoDialogView.findViewById(R.id.modify_time)).setText(fileInfo.time);
        ((TextView) mInfoDialogView.findViewById(R.id.path)).setText(fileInfo.path);
        if (fileInfo.type == Constant.TYPE_DIR) {
            mInfoDialogView.findViewById(R.id.file_number_title).setVisibility(View.VISIBLE);
            TextView fileNumView = (TextView) mInfoDialogView.findViewById(R.id.file_number);
            fileNumView.setVisibility(View.VISIBLE);
            fileNumView.setText(context.getResources().getString(R.string.info_file_number_computing));
            new ComputeFilsNumberTask(context, fileInfo, fileNumView).execute();
            new ComputeFilsTotalSizeTask(context, fileInfo, (TextView) mInfoDialogView.findViewById(R.id.size)).execute();
        }

        builder.setView(mInfoDialogView);
        builder.setTitle(context.getResources().getString(R.string.info_title));
        builder.setIcon(R.mipmap.ic_info_gray);
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setLayout(dialog_size, dialog_size);
    }

    private String getFileTypeString(Context context, int type) {
        switch (type) {
            case Constant.TYPE_PHOTO:
                return context.getResources().getString(R.string.info_image);
            case Constant.TYPE_MUSIC:
                return context.getResources().getString(R.string.info_music);
            case Constant.TYPE_VIDEO:
                return context.getResources().getString(R.string.info_video);
            case Constant.TYPE_DOC:
                return context.getResources().getString(R.string.info_document);
            case Constant.TYPE_ENCRYPT:
                return context.getResources().getString(R.string.info_enc);
            case Constant.TYPE_DIR:
                return context.getResources().getString(R.string.info_folder);
            default: //Constant.TYPE_OTHER_FILE:
                return context.getResources().getString(R.string.info_other);
        }
    }

    public ArrayList<FileInfo> getSelectedFiles() {
        return mResultsAdapter.getSelectedFiles();
    }

    public void actionFinish() {
        mResultsAdapter.clearAllSelection();
        updateSearchResults();
    }

    public void selectAll() {
        mResultsAdapter.setAllSelection();
    }

    public boolean getSelectedAllorNot() {
        return mResultsAdapter.getSelectedAllorNot();
    }

    public int getItemsCount(){
        return mResultsAdapter.getItemCount();
    }
}
