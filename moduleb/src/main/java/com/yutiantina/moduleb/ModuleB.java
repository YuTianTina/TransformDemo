package com.yutiantina.moduleb;

import com.yutiantina.basemodule.IModule;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yutiantian email: yutiantina@gmail.com
 * @since 2019/4/11
 */
public class ModuleB implements IModule {

    @Override
    public List<String> getRoute() {
        List<String> routes = new ArrayList<>();
        routes.add("moduleB-page1");
        routes.add("moduleB-page2");
        return routes;
    }
}
