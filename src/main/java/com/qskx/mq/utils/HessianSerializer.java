package com.qskx.mq.utils;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author 111111
 * @date 2018-06-10 14:18
 */
public class HessianSerializer {
    public static  <T> byte[] serialize(T obj){
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        HessianOutput ho = new HessianOutput(os);
        try {
            ho.writeObject(obj);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return os.toByteArray();
    }

    public static <T> Object deserialize(byte[] bytes, Class<T> clazz) {
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        HessianInput hi = new HessianInput(is);
        try {
            return hi.readObject();
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
