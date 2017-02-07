package com.transcend.otg;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.util.Date;
import java.util.concurrent.ExecutionException;

import static com.transcend.otg.SearchSuggestionDatabaseHelper.Tables;
/**
 * Created by henry_hsu on 2017/2/6.
 */

public class SearchSuggestion {
    private static final String LOG_TAG = "SearchSuggestion";
    private static long MAX_SAVED_SEARCH_QUERY = 64;
    private static final int MAX_PROPOSED_SUGGESTIONS = 5;
    private static SearchSuggestion sInstance;
    private Context mContext;

    public static SearchSuggestion getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SearchSuggestion(context);
        } else {
            sInstance.setContext(context);
        }
        return sInstance;
    }

    public SearchSuggestion(Context context) {
        mContext = context;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public long addSavedQuery(String query){
        final SaveSearchQueryTask task = new SaveSearchQueryTask();
        task.execute(query);
        try {
            return task.get();
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "Cannot insert saved query: " + query, e);
            return -1 ;
        } catch (ExecutionException e) {
            Log.e(LOG_TAG, "Cannot insert saved query: " + query, e);
            return -1;
        }
    }

    public Cursor getSuggestions(String query) {
        final String sql = buildSuggestionsSQL(query);
        Log.d(LOG_TAG, "Suggestions query: " + sql);
        return getReadableDatabase().rawQuery(sql, null);
    }

    private String buildSuggestionsSQL(String query) {
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT ");
        sb.append(SearchSuggestionDatabaseHelper.SavedQueriesColums.QUERY);
        sb.append(" FROM ");
        sb.append(Tables.TABLE_SAVED_QUERIES);

        if (TextUtils.isEmpty(query)) {
            sb.append(" ORDER BY rowId DESC");
        } else {
            sb.append(" WHERE ");
            sb.append(SearchSuggestionDatabaseHelper.SavedQueriesColums.QUERY);
            sb.append(" LIKE ");
            sb.append("'");
            sb.append(query);
            sb.append("%");
            sb.append("'");
        }

        sb.append(" LIMIT ");
        sb.append(MAX_PROPOSED_SUGGESTIONS);

        return sb.toString();
    }

    private SQLiteDatabase getReadableDatabase() {
        return SearchSuggestionDatabaseHelper.getInstance(mContext).getReadableDatabase();
    }

    private SQLiteDatabase getWritableDatabase() {
        return SearchSuggestionDatabaseHelper.getInstance(mContext).getWritableDatabase();
    }

    private class SaveSearchQueryTask extends AsyncTask<String, Void, Long> {

        @Override
        protected Long doInBackground(String... params) {
            final long now = new Date().getTime();

            final ContentValues values = new ContentValues();
            values.put(SearchSuggestionDatabaseHelper.SavedQueriesColums.QUERY, params[0]);
            values.put(SearchSuggestionDatabaseHelper.SavedQueriesColums.TIME_STAMP, now);

            final SQLiteDatabase database = getWritableDatabase();

            long lastInsertedRowId = -1;
            try {
                // First, delete all saved queries that are the same
                database.delete(Tables.TABLE_SAVED_QUERIES,
                        SearchSuggestionDatabaseHelper.SavedQueriesColums.QUERY + " = ?",
                        new String[] { params[0] });

                // Second, insert the saved query
                lastInsertedRowId =
                        database.insertOrThrow(Tables.TABLE_SAVED_QUERIES, null, values);

                // Last, remove "old" saved queries
                final long delta = lastInsertedRowId - MAX_SAVED_SEARCH_QUERY;
                if (delta > 0) {
                    int count = database.delete(Tables.TABLE_SAVED_QUERIES, "rowId <= ?",
                            new String[] { Long.toString(delta) });
                    Log.d(LOG_TAG, "Deleted '" + count + "' saved Search query(ies)");
                }
            } catch (Exception e) {
                Log.d(LOG_TAG, "Cannot update saved Search queries", e);
            }

            return lastInsertedRowId;
        }
    }
}
