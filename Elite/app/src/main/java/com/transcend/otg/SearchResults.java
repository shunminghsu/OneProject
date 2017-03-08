package com.transcend.otg;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.transcend.otg.Bitmap.IconHelper;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.Loader.SearchLoader;

import java.util.ArrayList;

import static com.transcend.otg.MainActivity.mScreenW;

/**
 * Created by henry_hsu on 2017/2/6.
 */

public class SearchResults extends Fragment {
    private static final String TAG = "SearchResults";

    private static final String EMPTY_QUERY = "";

    private SearchView mSearchView;

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
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!mShowResults) {
            showSomeSuggestions();
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

    public void onExpand(View edit_view) {
        mSuggestionsListView.setAnchorView(edit_view);
        mSuggestionsListView.setWidth(2*mScreenW/3);
        mSuggestionsListView.show();
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
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            return new ViewHolder(layoutInflater.inflate(R.layout.listitem_recyclerview, parent, false), viewType);

        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            FileInfo fileInfo = mList.get(position);

            holder.title.setText(fileInfo.name);
            if (holder.subtitle != null)
                holder.subtitle.setText(fileInfo.time);

            if (holder.info != null) {
                holder.info.setVisibility(fileInfo.type == Constant.TYPE_DIR ? View.GONE : View.VISIBLE);
            }

            if (fileInfo.type == Constant.TYPE_MUSIC) {
                mIconHelper.loadMusicThumbnail(fileInfo.path, fileInfo.album_id, holder.icon, holder.iconMime);
            } else if (fileInfo.type == Constant.TYPE_PHOTO && fileInfo.uri != null) {
                mIconHelper.loadThumbnail(fileInfo.uri, fileInfo.type, holder.icon, holder.iconMime);
            } else
                mIconHelper.loadThumbnail(fileInfo.path, fileInfo.type, holder.icon, holder.iconMime);

        }

        @Override
        public int getItemCount() {
            if (mList != null)
                return mList.size();
            else
                return 0;
        }
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
            Log.d(TAG, "onClick");

        }

        @Override
        public boolean onLongClick(View v) {
            Log.d(TAG, "onLongClick");

            return true;
        }

        private void setOnItemInfoClickListener() {

        }

    }


    public void setSearchView(SearchView searchView) {
        mSearchView = searchView;
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
}
