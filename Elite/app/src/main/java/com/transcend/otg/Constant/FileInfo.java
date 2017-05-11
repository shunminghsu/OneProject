package com.transcend.otg.Constant;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.transcend.otg.Utils.DurableUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


/**
 * Created by wangbojie on 2017/2/6.
 */

public class FileInfo implements Durable, Parcelable {

    @Override
    public void reset() {
        path = null;
        name = null;
        time = null;
        uri = null;
        format_size = null;
        size = 0;
//        album_id = -1;
        checked = false;
        type = 0;
        storagemode = 0;
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        path = DurableUtils.readNullableString(in);
        name = DurableUtils.readNullableString(in);
        time = DurableUtils.readNullableString(in);
        String uri_string = DurableUtils.readNullableString(in);
        uri = (uri_string == null) ? null : Uri.parse(uri_string);
        format_size = DurableUtils.readNullableString(in);
        size = in.readLong();
//        album_id = in.readLong();
        checked = in.readBoolean();
        type = in.readInt();
        storagemode = in.readInt();
    }

    @Override
    public void write(DataOutputStream out) throws IOException {

        DurableUtils.writeNullableString(out, path);
        DurableUtils.writeNullableString(out, name);
        DurableUtils.writeNullableString(out, time);
        DurableUtils.writeNullableString(out, (uri == null) ? null : uri.toString());
        DurableUtils.writeNullableString(out, format_size);
        out.writeLong(size);
//        out.writeLong(album_id);
        out.writeBoolean(checked);
        out.writeInt(type);
        out.writeInt(storagemode);
    }

    public String path;

    public String name;
    public String time;
    public int type;
    public Uri uri;
    public long size;
    public String format_size;
//    public long album_id;
    public boolean checked;
    public int storagemode;

    public FileInfo() {
        reset();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        DurableUtils.writeToParcel(dest, this);
    }

    public static final Parcelable.Creator<FileInfo> CREATOR = new Parcelable.Creator<FileInfo>() {
        public FileInfo createFromParcel(Parcel in) {
            final FileInfo file = new FileInfo();
            DurableUtils.readFromParcel(in, file);
            return file;
        }

        public FileInfo[] newArray(int size) {
            return new FileInfo[size];
        }
    };
}
