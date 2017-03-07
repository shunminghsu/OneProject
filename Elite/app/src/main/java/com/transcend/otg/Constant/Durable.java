package com.transcend.otg.Constant;

/**
 * Created by henry_hsu on 2017/3/3.
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Durable {
    public void reset();
    public void read(DataInputStream in) throws IOException;
    public void write(DataOutputStream out) throws IOException;
}
