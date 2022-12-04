package com.JRead;

import androidx.annotation.NonNull;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.lang.reflect.Method;
import java.util.Map;

/**  sp文件存取的封装处理
 * @Author feisher  on 2017年10月26日09:50:08  更新，简化代码结构
 * Email：458079442@qq.com
 */
public class SPUtils {
    //保存在手机里面的文件名
    public static final String FILE_NAME = "cache";
    public static SharedPreferences sp ;

    private static SharedPreferences init(Context context) {
        return sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    /**
     *  存
     * @param key 键
     * @param value 值
     * @param <E> 泛型，自动根据值进行处理
     */
    public static <E>void put(@NonNull String key,@NonNull E value) {
        put(MyApplication.getContext(),key,value);
    }
    public static <E>void put1(int key,@NonNull E value) {
        put(String.valueOf(key),value);
    }
    public static <E>void putC(@NonNull String key,@NonNull E value) {
        putC(MyApplication.getContext(),key,value);
    }
    public static <E>void put1C(int key,@NonNull E value) {
        putC(String.valueOf(key),value);
    }

    /**
     *  取
     * @param key 键
     * @param defaultValue 默认值
     * @param <E> 泛型，自动根据值进行处理
     */
    public static <E>E get(@NonNull String key,@NonNull E defaultValue) {
        return get(MyApplication.getContext(),key,defaultValue);
    }
    public static <E>E get1(int key,@NonNull E defaultValue) {
        return get(String.valueOf(key),defaultValue);
    }

    /**
     * 插件间和宿主共用数据 必须 传入context
     */
    public static <E>void put(Context context,@NonNull String key,@NonNull E value) {
        SharedPreferences.Editor editor = init(context).edit();
        if (value instanceof String||value instanceof Integer||value instanceof Boolean||
                value instanceof Float|| value instanceof Long||value instanceof Double) {
            editor.putString(key, String.valueOf(value));
        }else {
            editor.putString(key, new Gson().toJson(value));
        }
        SPCompat.apply(editor);
    }
    public static <E>void putC(Context context,@NonNull String key,@NonNull E value) {
        SharedPreferences.Editor editor = init(context).edit();
        if (value instanceof String||value instanceof Integer||value instanceof Boolean||
                value instanceof Float|| value instanceof Long||value instanceof Double) {
            editor.putString(key, String.valueOf(value));
        }else {
            editor.putString(key, new Gson().toJson(value));
        }
        SPCompat.applyC(editor);
    }

    /**
     *插件间和宿主共用数据 必须 传入context
     */
    @SuppressWarnings("unchecked")
    public static <E>E get(Context context,@NonNull String key,@NonNull E defaultValue) {
        String value = init(context).getString(key, String.valueOf(defaultValue));
        if (defaultValue instanceof String){
            return (E) value;
        } else if (defaultValue instanceof Integer){
            return (E) Integer.valueOf(value);
        } else if (defaultValue instanceof Boolean){
            return (E) Boolean.valueOf(value);
        } else if (defaultValue instanceof Float){
            return (E) Float.valueOf(value);
        } else if (defaultValue instanceof Long){
            return (E) Long.valueOf(value);
        } else if (defaultValue instanceof Double){
            return (E) Double.valueOf(value);
        }
        //json为null的时候返回对象为null,gson已处理
        return (E) new Gson().fromJson(value,defaultValue.getClass());
    }

    /**
     * 移除某个key值已经对应的值
     */
    public static void remove(Context context, String key) {
        SharedPreferences.Editor editor = init(context).edit();
        editor.remove(key);
        SPCompat.apply(editor);
    }

    /**
     * 清除所有数据
     */
    public static void clear(Context context) {
        SharedPreferences.Editor editor = init(context).edit();
        editor.clear();
        SPCompat.apply(editor);
    }

    /**
     * 查询某个key是否已经存在
     */
    public static boolean contains(Context context, String key) {
        return init(context).contains(key);
    }
    public static boolean contains(String key) {
        return contains(MyApplication.getContext(),key);
    }

    /**
     * 返回所有的键值对
     */
    public static Map<String, ?> getAll(Context context) {
        return init(context).getAll();
    }

    /**
     * 保存对象到sp文件中 被保存的对象须要实现 Serializable 接口
     */
    public static void saveObject( String key, Object value) {
        put(key,value);
    }

    /**
     * desc:获取保存的Object对象
     * @return modified:
     */
    public static <T>T readObject(String key,  Class<T> clazz) {
        try {
            return (T) get(key,clazz.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 创建一个解决SharedPreferencesCompat.apply方法的一个兼容类
     */
    private static class SPCompat {
        private static final Method S_APPLY_METHOD = findApplyMethod();

        /**
         * 反射查找apply的方法
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        private static Method findApplyMethod() {
            try {
                Class clz = SharedPreferences.Editor.class;
                return clz.getMethod("apply");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * 如果找到则使用apply执行，否则使用commit
         */
        public static void apply(SharedPreferences.Editor editor) {
            try {
                if (S_APPLY_METHOD != null) {
                    S_APPLY_METHOD.invoke(editor);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            editor.apply();
        }
        public static void applyC(SharedPreferences.Editor editor) {
            try {
                if (S_APPLY_METHOD != null) {
                    S_APPLY_METHOD.invoke(editor);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            editor.commit();
        }
    }
}