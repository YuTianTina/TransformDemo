package com.transform.demo.utils

import java.io.File

/**
 *
 * @author yutiantian email: yutiantina@gmail.com
 * @since 2019/4/10
 */
fun File.createParent(): Boolean{
    if(!parentFile.exists()){
        return parentFile.mkdirs()
    }
    return true
}