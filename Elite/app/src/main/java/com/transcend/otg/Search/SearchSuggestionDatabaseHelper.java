package com.transcend.otg.Search;

/**
 * Created by henry_hsu on 2017/2/6.
 */

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

public class SearchSuggestionDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "SearchDatabaseHelper";

    private static final String DATABASE_NAME = "search_index.db";
    private static final int DATABASE_VERSION = 115;

    public interface Tables {
        public static final String TABLE_META_INDEX = "meta_index";
        public static final String TABLE_SAVED_QUERIES = "saved_queries";
    }

    public interface MetaColumns {
        public static final String BUILD = "build";
    }

    public interface SavedQueriesColums  {
        public static final String QUERY = "query";
        public static final String TIME_STAMP = "timestamp";
    }

    private static final String CREATE_META_TABLE =
            "CREATE TABLE " + Tables.TABLE_META_INDEX +
                    "(" +
                    MetaColumns.BUILD + " VARCHAR(32) NOT NULL" +
                    ")";

    private static final String CREATE_SAVED_QUERIES_TABLE =
            "CREATE TABLE " + Tables.TABLE_SAVED_QUERIES +
                    "(" +
                    SavedQueriesColums.QUERY + " VARCHAR(64) NOT NULL" +
                    ", " +
                    SavedQueriesColums.TIME_STAMP + " INTEGER" +
                    ")";

    private static final String INSERT_BUILD_VERSION =
            "INSERT INTO " + Tables.TABLE_META_INDEX +
                    " VALUES ('" + Build.VERSION.INCREMENTAL + "');";

    private static final String SELECT_BUILD_VERSION =
            "SELECT " + MetaColumns.BUILD + " FROM " + Tables.TABLE_META_INDEX + " LIMIT 1;";

    private static SearchSuggestionDatabaseHelper sSingleton;

    public static synchronized SearchSuggestionDatabaseHelper getInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new SearchSuggestionDatabaseHelper(context);
        }
        return sSingleton;
    }

    public SearchSuggestionDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        bootstrapDB(db);
    }

    private void bootstrapDB(SQLiteDatabase db) {
        db.execSQL(CREATE_META_TABLE);
        db.execSQL(CREATE_SAVED_QUERIES_TABLE);
        db.execSQL(INSERT_BUILD_VERSION);
        Log.i(TAG, "Bootstrapped database");
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        Log.i(TAG, "Using schema version: " + db.getVersion());

        if (!Build.VERSION.INCREMENTAL.equals(getBuildVersion(db))) {
            Log.w(TAG, "SearchSuggestion needs to be rebuilt as build-version is not the same");
            // We need to drop the tables and recreate them
            reconstruct(db);
        } else {
            Log.i(TAG, "SearchSuggestion is fine");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < DATABASE_VERSION) {
            Log.w(TAG, "Detected schema version '" +  oldVersion + "'. " +
                    "SearchSuggestion needs to be rebuilt for schema version '" + newVersion + "'.");
            // We need to drop the tables and recreate them
            reconstruct(db);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Detected schema version '" +  oldVersion + "'. " +
                "SearchSuggestion needs to be rebuilt for schema version '" + newVersion + "'.");
        // We need to drop the tables and recreate them
        reconstruct(db);
    }

    private void reconstruct(SQLiteDatabase db) {
        dropTables(db);
        bootstrapDB(db);
    }

    private String getBuildVersion(SQLiteDatabase db) {
        String version = null;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(SELECT_BUILD_VERSION, null);
            if (cursor.moveToFirst()) {
                version = cursor.getString(0);
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Cannot get build version from SearchSuggestion metadata");
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return version;
    }

    private void dropTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + Tables.TABLE_META_INDEX);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.TABLE_SAVED_QUERIES);
    }
}

