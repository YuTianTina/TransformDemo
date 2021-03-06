package com.transform.demo.transform

import com.android.SdkConstants
import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Status.*
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import com.transform.demo.utils.ScanCondition
import com.transform.demo.utils.ScanHelper
import com.transform.demo.utils.createParent
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 *
 * transform
 * @author yutiantian email: yutiantina@gmail.com
 * @since 2019/4/9
 */
abstract class DemoTransform : Transform() {

    /**
     * 输入文件的类型
     * 可供我们去处理的有两种类型, 分别是编译后的java代码, 以及资源文件
     */
    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> = TransformManager.CONTENT_CLASS

    /**
     * 是否支持增量
     * 如果支持增量执行, 则变化输入内容可能包含 修改/删除/添加 文件的列表
     */
    override fun isIncremental(): Boolean = true

    /**
     * 指定作用范围
     */
    override fun getScopes(): MutableSet<in QualifiedContent.Scope> = TransformManager.SCOPE_FULL_PROJECT

    /**
     * transform的执行主函数
     */
    override fun transform(transformInvocation: TransformInvocation) {
        val outputProvider = transformInvocation.outputProvider
        println("有没有增量编译${transformInvocation.isIncremental}")
        for (input in transformInvocation.inputs){
            with(input){
                // 输入源为jar
                jarInputs.forEach {jarInput->
                    val inputJar = jarInput.file
                    val outputJar = outputProvider.getContentLocation(
                        jarInput.name,
                        jarInput.contentTypes,
                        jarInput.scopes,
                        Format.JAR
                    )
                    if(transformInvocation.isIncremental){
                        when(jarInput.status){
                            NOTCHANGED -> {}
                            ADDED,CHANGED -> transformJar(inputJar, outputJar)
                            REMOVED -> FileUtils.delete(outputJar)
                            else-> {}
                        }
                    }else{
                        transformJar(inputJar, outputJar)
                    }
                }

                // 输入源为文件夹
                directoryInputs.forEach {di->
                    val inputDir = di.file
                    val outputDir = outputProvider.getContentLocation(
                        di.name,
                        di.contentTypes,
                        di.scopes,
                        Format.DIRECTORY
                    )
                    if(transformInvocation.isIncremental){
                        for (entry in di.changedFiles.entries){
                            val inputFile = entry.key
                            when(entry.value){
                                NOTCHANGED -> {}
                                ADDED, CHANGED -> {
                                    if(!inputFile.isDirectory && inputFile.name.endsWith(SdkConstants.DOT_CLASS)){
                                        val out = toOutputFile(outputDir, inputDir, inputFile)
                                        transformFile(inputFile, out)
                                    }
                                }
                                REMOVED -> {
                                    val outputFile = toOutputFile(outputDir, inputDir, inputFile)
                                    FileUtils.deleteIfExists(outputFile)
                                }
                                else -> {}
                            }
                        }
                    }else{
                        FileUtils.getAllFiles(inputDir)
                            .filter {
                                true == it?.name?.endsWith(SdkConstants.DOT_CLASS)
                            }.forEach {fileIn ->
                                val out = toOutputFile(outputDir, inputDir, fileIn)
                                transformFile(fileIn, out)
                            }
                    }

                }
            }
        }
    }

    private fun toOutputFile(outputDir: File, inputDir: File, inputFile: File): File {
        return File(outputDir, FileUtils.relativePossiblyNonExistingPath(inputFile, inputDir))
    }

    private fun transformJar(inputJar: File, outputJar: File) {
        ScanHelper.scanFromJarInput(inputJar, outputJar, getScanConditions())
        outputJar.createParent()
        FileUtils.copyFile(inputJar, outputJar)

    }

    private fun transformFile(inputFile: File, out: File) {
        ScanHelper.scanFromDirectoryInput(inputFile, out, getScanConditions())
        out.createParent()
        FileUtils.copyFile(inputFile, out)
    }

    /**
     * 扫描动作
     */
    abstract fun getScanConditions(): MutableList<ScanCondition>
}