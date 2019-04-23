package com.transform.demo

/**
 *
 * @author yutiantian email: yutiantina@gmail.com
 * @since 2019-04-20
 */
object ScanConstants {
    const val IMODULE = "com/yutiantina/basemodule/IModule"

    /**
     * 判断jar包是否是android 的support包
     */
    fun skipAndroidJar(path: String) : Boolean{
        return !path.contains("com.android.support")
                && !path.contains("/android/m2repository")
    }

    /**
     * 进行注册的类
     */
    const val AUTOREGISTER = "com/yutiantina/transformdemo/ModuleRegister.class"

}