package com.shik2.utils;

import java.io.*;

/**
 * @author shik2
 * @date 2020/12/19
 *
 * Description: 深拷贝工具类
 **/
public class DeepCopy {
    public static <T> T deepCopy(T src) {
        T dest = null;

        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(src);

            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            ObjectInputStream in = new ObjectInputStream(byteIn);
            dest = (T) in.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return dest;
    }
}
