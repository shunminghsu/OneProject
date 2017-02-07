package com.transcend.otg;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by henry_hsu on 2017/2/6.
 */

public class SearchResults extends Fragment {
    private static final String TAG = "SearchResults";

    private static final String EMPTY_QUERY = "";

    private SearchView mSearchView;

    private ListView mResultsListView;
    private SearchResultsAdapter mResultsAdapter;
    private UpdateSearchResultsTask mUpdateSearchResultsTask;

    private ListView mSuggestionsListView;
    private SuggestionsAdapter mSuggestionsAdapter;
    private UpdateSuggestionsTask mUpdateSuggestionsTask;

    private ViewGroup mLayoutSuggestions;
    private ViewGroup mLayoutResults;

    private String mQuery;

    private boolean mShowResults;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mResultsAdapter = new SearchResultsAdapter(getActivity());
        mSuggestionsAdapter = new SuggestionsAdapter(getActivity());
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
        if (mUpdateSearchResultsTask != null) {
            mUpdateSearchResultsTask.cancel(false);
            mUpdateSearchResultsTask = null;
        }
        clearSuggestions();
    }

    @Override
    public void onDestroy() {
        mResultsListView = null;
        mResultsAdapter = null;
        mUpdateSearchResultsTask = null;

        mSuggestionsListView = null;
        mSuggestionsAdapter = null;
        mUpdateSuggestionsTask = null;

        mSearchView = null;

        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.search_panel, container, false);
        mLayoutSuggestions = (ViewGroup) view.findViewById(R.id.layout_suggestions);
        mLayoutResults = (ViewGroup) view.findViewById(R.id.layout_results);

        mResultsListView = (ListView) view.findViewById(R.id.list_results);
        mResultsListView.setAdapter(mResultsAdapter);
        mResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                saveQueryToDatabase();
            }
        });

        mSuggestionsListView = (ListView) view.findViewById(R.id.list_suggestions);
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
            }
        });
        return view;
    }

    private class UpdateSearchResultsTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

        }
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

    private static class SearchResultsAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mInflater;

        public SearchResultsAdapter(Context context) {
            mContext = context;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            return null;
        }
    }

    public void setSearchView(SearchView searchView) {
        mSearchView = searchView;
    }

    private void setSuggestionsVisibility(boolean visible) {
        if (mLayoutSuggestions != null) {
            mLayoutSuggestions.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
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
        //updateSearchResults();
        saveQueryToDatabase();
        return true;
    }

    public boolean onQueryTextChange(String query) {
        final String newQuery = getFilteredQueryString(query);

        mQuery = newQuery;
        if (TextUtils.isEmpty(mQuery)) {
            mShowResults = false;
            //setResultsVisibility(false);
            updateSuggestions();
        } else {
            mShowResults = true;
            setSuggestionsVisibility(false);
            //updateSearchResults();
        }

        return true;
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
}
