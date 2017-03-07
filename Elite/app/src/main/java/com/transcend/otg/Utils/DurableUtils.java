package com.transcend.otg.Utils;

/**
 * Created by henry_hsu on 2017/3/3.
 */

import android.os.BadParcelableException;
import android.os.Parcel;
import android.util.Log;

import com.transcend.otg.Constant.Durable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DurableUtils {
    public static <D extends Durable> byte[] writeToArray(D d) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        d.write(new DataOutputStream(out));
        return out.toByteArray();
    }

    public static <D extends Durable> D readFromArray(byte[] data, D d) throws IOException {
        if (data == null) throw new IOException("Missing data");
        final ByteArrayInputStream in = new ByteArrayInputStream(data);
        d.reset();
        try {
            d.read(new DataInputStream(in));
        } catch (IOException e) {
            d.reset();
            throw e;
        }
        return d;
    }

    public static <D extends Durable> void writeToParcel(Parcel parcel, D d) {
        try {
            parcel.writeByteArray(writeToArray(d));
        } catch (IOException e) {
            throw new BadParcelableException(e);
        }
    }

    public static <D extends Durable> D readFromParcel(Parcel parcel, D d) {
        try {
            return readFromArray(parcel.createByteArray(), d);
        } catch (IOException e) {
            throw new BadParcelableException(e);
        }
    }

    public static void writeNullableString(DataOutputStream out, String value) throws IOException {
        if (value != null) {
            out.write(1);
            out.writeUTF(value);
        } else {
            out.write(0);
        }
    }

    public static String readNullableString(DataInputStream in) throws IOException {
        if (in.read() != 0) {
            return in.readUTF();
        } else {
            return null;
        }
    }
}

