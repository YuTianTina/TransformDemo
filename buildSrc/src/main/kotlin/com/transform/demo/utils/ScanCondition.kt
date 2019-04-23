package com.transform.demo.utils

import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 *
 * @author yutiantian email: yutiantina@gmail.com
 * @since 2019-04-23
 */
interface ScanCondition {

    fun scan(jar: JarFile, scanEntry: JarEntry)
    /**
     * @param relativePath 相对地址
     */
    fun scan(inputFile: File)
    fun scanFileEnd(inputFile: File, destFile: File)
    fun scanJarEnd(inputJar: File, destJar: File)
}