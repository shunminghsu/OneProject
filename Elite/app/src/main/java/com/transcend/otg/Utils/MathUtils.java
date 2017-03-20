package com.transcend.otg.Utils;

import java.text.DecimalFormat;

/**
 * Created by wangbojie on 2017/3/17.
 */
public class MathUtils {
    public static final long KB = 1000;
    public static final long MB = KB * KB;
    public static final long GB = MB * KB;
    public static final long TB = GB * KB;

    public static final long KB_S = 1024;
    public static final long MB_S = KB_S * KB_S;
    public static final long GB_S = MB_S * KB_S;
    public static final long TB_S = GB_S * KB_S;

    public static String getBytes(long number) {
        long[] dividers = { TB, GB, MB, KB, 1 };
        String[] units = { "TB", "GB", "MB", "KB", "B" };
        for (int i = 0; i < dividers.length; i++) {
            if (number >= dividers[i]) {
                double value = (double) number / (double) dividers[i];
                DecimalFormat df = new DecimalFormat("#,##0.##");
                return String.format("%s%s", df.format(value), units[i]);
            }
        }
        return "0B";
    }

    public static String getStorageSize(long number) {
        long[] dividers = { TB_S, GB_S, MB_S, KB_S, 1 };
        String[] units = { "TB", "GB", "MB", "KB", "B" };
        for (int i = 0; i < dividers.length; i++) {
            if (number >= dividers[i]) {
                double value = (double) number / (double) dividers[i];
                DecimalFormat df = new DecimalFormat("#,##0.##");
                return String.format("%s%s", df.format(value), units[i]);
            }
        }
        return "0B";
    }
}
