package com.transcend.otg.Utils;

import android.content.Context;

import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.LocalPreferences;

import org.apache.commons.io.FilenameUtils;

import java.util.Comparator;

/**
 * Created by wangbojie on 2017/2/6.
 */

public class FileInfoSort {
    public static Comparator<FileInfo> comparator(Context context) {
        int sort = LocalPreferences.getPref(context, LocalPreferences.BROWSER_SORT_PREFIX, Constant.SORT_BY_DATE);
        boolean sortAsc = LocalPreferences.getPref(context,
                LocalPreferences.BROWSER_SORT_ORDER_PREFIX, Constant.SORT_ORDER_AS) == Constant.SORT_ORDER_AS;
        if (sort == Constant.SORT_BY_DATE)
            return sortAsc ? new FileInfoSort.byDate() : new FileInfoSort.byReverseDate();
        else if (sort == Constant.SORT_BY_NAME)
            return sortAsc ? new FileInfoSort.byName() : new FileInfoSort.byReverseName();
        else if (sort == Constant.SORT_BY_SIZE)
            return sortAsc ? new FileInfoSort.bySize() : new FileInfoSort.byReverseSize();
        else
            return sortAsc ? new FileInfoSort.byDate() : new FileInfoSort.byReverseDate();
    }

    public static class byDate implements Comparator<FileInfo> {

        @Override
        public int compare(FileInfo lhs, FileInfo rhs) {
            int result = 0;
            if (lhs.time.equals(rhs.time)) {
                result = compareByName(lhs, rhs);
            } else {
                result = compareByDate(lhs, rhs);
            }
            return result;
        }

    }

    public static class byReverseDate implements Comparator<FileInfo> {

        @Override
        public int compare(FileInfo lhs, FileInfo rhs) {
            int result = 0;
            if (lhs.time.equals(rhs.time)) {
                result = compareByName(lhs, rhs);
            } else {
                result = -compareByDate(lhs, rhs);
            }
            return result;
        }

    }

    public static class byType implements Comparator<FileInfo> {

        @Override
        public int compare(FileInfo lhs, FileInfo rhs) {
            return (int) (lhs.type - rhs.type);
        }

    }

    public static class bySize implements Comparator<FileInfo> {

        @Override
        public int compare(FileInfo lhs, FileInfo rhs) {
            return (int) (lhs.size - rhs.size);
        }

    }

    public static class byReverseSize implements Comparator<FileInfo> {

        @Override
        public int compare(FileInfo lhs, FileInfo rhs) {
            return (int) (rhs.size - lhs.size);
        }

    }

    public static class byName implements Comparator<FileInfo> {

        @Override
        public int compare(FileInfo lhs, FileInfo rhs) {
            return compareByName(lhs, rhs);
        }

    }

    public static class byReverseName implements Comparator<FileInfo> {

        @Override
        public int compare(FileInfo lhs, FileInfo rhs) {
            return -compareByName(lhs, rhs);
        }

    }

    private static int compareByExtension(FileInfo lhs, FileInfo rhs) {
        return FilenameUtils.getExtension(lhs.name).compareTo(FilenameUtils.getExtension(rhs.name));
    }

    private static int compareByName(FileInfo lhs, FileInfo rhs) {
        return lhs.name.compareToIgnoreCase(rhs.name);
    }

    private static int compareByDate(FileInfo lhs, FileInfo rhs) {
        return FileInfo.getDate(lhs.time).compareTo(FileInfo.getDate(rhs.time));
    }
}
