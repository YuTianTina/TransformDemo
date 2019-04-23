package com.yutiantina.transformdemo;

import com.yutiantina.modulea.ModuleA;
import com.yutiantina.moduleb.ModuleB;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yutiantian email: yutiantina@gmail.com
 * @since 2019/4/11
 */
public class ModuleRegister {
    public static List<String> getAllRoutes(){
        List<String> list = new ArrayList<>();
        list.addAll(new ModuleA().getRoute());
        list.addAll(new ModuleB().getRoute());
        return list;
    }
}
