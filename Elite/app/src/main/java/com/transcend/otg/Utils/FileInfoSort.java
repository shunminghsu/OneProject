package com.transcend.otg.Utils;

import android.content.Context;

import com.transcend.otg.Constant.FileInfo;

import org.apache.commons.io.FilenameUtils;

import java.util.Comparator;

/**
 * Created by wangbojie on 2017/2/6.
 */

public class FileInfoSort {
    public static Comparator<FileInfo> comparator(Context context) {
        Pref.Sort sort = Pref.getFileSortType(context);
        if (sort.equals(Pref.Sort.DATE))
            return new FileInfoSort.byDate();
        else if (sort.equals(Pref.Sort.NAME))
            return new FileInfoSort.byName();
        else if (sort.equals(Pref.Sort.REVERSEDATE))
            return new FileInfoSort.byReverseDate();
        else if (sort.equals(Pref.Sort.REVERSENAME))
            return new FileInfoSort.byReverseName();
        else
            return new FileInfoSort.byType();
    }

    public static class byType implements Comparator<FileInfo> {

        @Override
        public int compare(FileInfo lhs, FileInfo rhs) {
            int result = 0;
            if (lhs.type.equals(rhs.type)) {
                result = compareByName(lhs, rhs);
                if (result == 0)
                    result = compareByExtension(lhs, rhs);
            } else {
                result = compareByType(lhs, rhs);
            }
            return result;
        }

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

    private static int compareByType(FileInfo lhs, FileInfo rhs) {
        return lhs.type.compareTo(rhs.type);
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
