package cn.sl.ehub.common.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
public class MapGlobalUtil {


    public static ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal<>();

    /**
     * 添加信息
     *
     * @param key
     * @param value
     */
    public static void addMapObj(String key, Object value) {
        Map<String, Object> map = threadLocal.get();
        if (null == map) {
            map = new HashMap<>();
            threadLocal.set(map);
        }
        map.put(key, value);
    }

    /**
     * 获取所有信息
     *
     * @return
     */
    public static Map<String, Object> getMapObjs() {
        Map<String, Object> map = threadLocal.get();
        if (null == map) {
            map = new HashMap<>();
            threadLocal.set(map);
        }
        return map;
    }

    /**
     * 获取单个信息
     *
     * @param key
     * @return
     */
    public static Object getMapObj(String key) {
        Map<String, Object> map = threadLocal.get();
        if (null == map) {
            map = new HashMap<>();
            threadLocal.set(map);
        }
        return map.get(key);
    }

    /**
     * 删除信息
     *
     * @param key
     */
    public static void removeMapObj(String key) {
        Map<String, Object> map = threadLocal.get();
        if (null == map) {
            map = new HashMap<>();
            threadLocal.set(map);
        }
        if (map.containsKey(key)) {
            map.remove(key);
        } else {
            return;
        }
    }

    /**
     * 模糊删除
     *
     * @param strKey
     */
    public static void removeObjs(String strKey) {
        Map<String, Object> map = threadLocal.get();
        if (null == map) {
            map = new HashMap<>();
            threadLocal.set(map);
        }
        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (key.startsWith(strKey)) {
                iterator.remove();
            }
        }
    }

}
