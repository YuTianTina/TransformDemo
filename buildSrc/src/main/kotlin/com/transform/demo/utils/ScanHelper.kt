package com.transform.demo.utils

import com.android.SdkConstants
import com.transform.demo.ScanConstants
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 *
 * @author yutiantian email: yutiantina@gmail.com
 * @since 2019-04-23
 */
object ScanHelper {
    /**
     * 是否应该跳过搜索
     * true 为是
     */
    private fun shouldSkipScan(conditions: List<ScanCondition?>): Boolean{
        for (item in conditions){
            if(null != item){
                return false
            }
        }
        return true
    }
    /**
     * 扫描获取jar包内的文件
     */
    fun scanFromJarInput(inputJar: File, destJar: File, conditions: List<ScanCondition?>){
        if(shouldSkipScan(conditions)){
            return
        }
        if(ScanConstants.skipAndroidJar(inputJar.absolutePath)){
            val jar = JarFile(inputJar)
            jar.entries().iterator().forEach {
                conditions.forEach{condition->
                    condition?.scan(jar, it)
                }
            }
            conditions.forEach {
                it?.scanJarEnd(inputJar, destJar)
            }
        }
    }

    /**
     * 扫描文件夹内的文件
     */
    fun scanFromDirectoryInput(inputFile: File, destFile: File, conditions: List<ScanCondition?>){
        if(shouldSkipScan(conditions)){
            return
        }
        conditions.forEach { it?.scan(inputFile) }
        conditions.forEach {
            it?.scanFileEnd(inputFile, destFile)
        }
    }

    fun dealJarFile(jarFile: File, callback: (jarEntry: JarEntry, jos: JarOutputStream) -> Boolean){
        val jarAbsolutePath = jarFile.absolutePath
        val bakFilePath = jarAbsolutePath.substring(0, jarAbsolutePath.length - 4) + System.currentTimeMillis() + SdkConstants.DOT_JAR
        val bakFile = File(bakFilePath)
        jarFile.renameTo(bakFile)
        val bakJarFile = JarFile(bakFilePath)
        val jos = JarOutputStream(FileOutputStream(jarFile))
        for (jarEntry in bakJarFile.entries()) {
            if(!callback(jarEntry, jos)){
                jos.putNextEntry(ZipEntry(jarEntry))
            }
        }
        with(jos){
            flush()
            finish()
            close()
        }
        bakJarFile.close()
        bakFile.delete()
    }
}