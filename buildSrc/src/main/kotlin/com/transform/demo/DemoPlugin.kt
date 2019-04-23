package com.transform.demo

import com.android.build.gradle.AppExtension
import com.transform.demo.transform.AutoRegisterTransform
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 自定义插件
 * @author yutiantian email: yutiantina@gmail.com
 * @since 2019/4/9
 */
class DemoPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.findByType(AppExtension::class.java)?.run {
            registerTransform(AutoRegisterTransform(target))
        }
    }
}