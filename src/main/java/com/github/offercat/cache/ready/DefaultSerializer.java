package com.github.offercat.cache.ready;

import com.alibaba.fastjson.JSON;
import com.github.offercat.cache.inte.Serializer;

import java.io.*;

/**
 * 默认序列化器
 *
 * @author 徐通 xutong34
 * @since 2020年03月14日 17:56:58
 */
@SuppressWarnings("unchecked")
public class DefaultSerializer implements Serializer {

    @Override
    public <T extends Serializable> byte[] serializeToBytes(T object) {
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(object);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
                if (oos != null) {
                    oos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public <T extends Serializable> T deserializeFromBytes(byte[] bytes) {
        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            bis = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bis);
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public <T extends Serializable> String serializeToString(T object) {
        return JSON.toJSONString(object);
    }

    @Override
    public <T extends Serializable> T deserializeFromString(String str, Class<T> type) {
        return JSON.parseObject(str, type);
    }
}
