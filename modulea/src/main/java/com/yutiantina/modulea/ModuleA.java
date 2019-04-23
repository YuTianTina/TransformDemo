package com.yutiantina.modulea;

import com.yutiantina.basemodule.IModule;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yutiantian email: yutiantina@gmail.com
 * @since 2019/4/11
 */
public class ModuleA implements IModule {

    @Override
    public List<String> getRoute() {
        List<String> routes = new ArrayList<>();
        routes.add("moduleA-page1");
        routes.add("moduleA-page2");
        return routes;
    }
}
