package net.sdvn.nascommon.utils;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.HashMap;
import java.util.Map;

public final class FragmentFactory {
    private FragmentFactory() {
    }

    @NonNull
    private static Map<Class, Fragment> map = new HashMap<>();


    //获取实例
    public static synchronized Fragment getInstance(@NonNull Class<? extends Fragment> clazz) {
        //从集合里面获取
        Fragment fragment = map.get(clazz);
        if (fragment == null) {
            try {
                fragment = clazz.newInstance();
                //保存进集合
                map.put(clazz, fragment);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return fragment;
    }

    //清空
    public static void clear() {
        map.clear();
    }

}