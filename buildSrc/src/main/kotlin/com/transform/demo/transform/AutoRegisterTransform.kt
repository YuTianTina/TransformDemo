package com.transform.demo.transform

import com.android.SdkConstants
import com.android.build.api.transform.TransformInvocation
import com.transform.demo.ScanConstants
import com.transform.demo.adapter.RouteAdapter
import com.transform.demo.utils.ScanCondition
import com.transform.demo.utils.ScanHelper
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 *
 * @author yutiantian email: yutiantina@gmail.com
 * @since 2019-04-23
 */
class AutoRegisterTransform(val p: Project): DemoTransform() {
    // 注册模块的集合列表
    private val registerModule = arrayListOf<String>()
    private var autoRegisterFile: File? = null

    override fun isIncremental(): Boolean = false

    override fun getName(): String = "AutoRegisterTransform"

    override fun getScanConditions(): MutableList<ScanCondition> {
        return mutableListOf(object : ScanCondition {
            var hasRegisterInSingleClass = false
            var hasRegisterInJar = false

            override fun scan(jar: JarFile, scanEntry: JarEntry) {
                if(scanEntry.name == ScanConstants.AUTOREGISTER){
                    hasRegisterInJar = true
                }else{
                    if(scanEntry.name.endsWith(SdkConstants.DOT_CLASS)){
                        scanByte(jar.getInputStream(scanEntry))
                    }
                }
            }

            override fun scan(inputFile: File) {
                if(inputFile.absolutePath.contains(ScanConstants.AUTOREGISTER)){
                    hasRegisterInSingleClass = true
                }else{
                    if(inputFile.name.endsWith(SdkConstants.DOT_CLASS)){
                        scanByte(inputFile.inputStream())
                    }
                }
            }

            override fun scanFileEnd(inputFile: File, destFile: File) {
                if(hasRegisterInSingleClass){
                    println("注册类在${destFile.absolutePath}")
                    autoRegisterFile = destFile
                }
            }

            override fun scanJarEnd(inputJar: File, destJar: File) {
                if(hasRegisterInJar){
                    println("注册类jar包内在${destJar.absolutePath}")
                    autoRegisterFile = destJar
                }
            }
        })
    }

    override fun transform(transformInvocation: TransformInvocation) {
        super.transform(transformInvocation)
        autoRegisterFile?.let {
            println("转换后的类在" + it.absolutePath )
            if(it.name.endsWith(SdkConstants.DOT_CLASS)){
                val rewrite = modifyRegisterByte((it.inputStream()))
                FileOutputStream(it).write(rewrite)
            }else if(it.name.endsWith(SdkConstants.DOT_JAR)){
                ScanHelper.dealJarFile(it) { jarEntry, jos ->
                    val isRegisterClass = jarEntry.name == ScanConstants.AUTOREGISTER
                    if(isRegisterClass){
                        jos.putNextEntry(JarEntry(jarEntry.name))
                        jos.write(modifyRegisterByte(it.inputStream()))
                    }
                    isRegisterClass
                }
            }
        }
    }

    private fun scanByte(ins: InputStream){
        val cr = ClassReader(ins)
        val cw = ClassWriter(0)
        val cv = RouteAdapter(cw, registerModule)
        cr.accept(cv, 0)
    }

    /**
     * 修改注册类
     */
    private fun modifyRegisterByte(ins: InputStream): ByteArray{
        val cw = ClassWriter(0)
        val cr = ClassReader(ins)
        val cn = ClassNode()
        cr.accept(cn, 0)
        cn.methods.removeIf { it.name == "getAllRoutes" && "()Ljava/util/List;" == it.desc }
        val mv = cn.visitMethod(ACC_PUBLIC + ACC_STATIC, "getAllRoutes", "()Ljava/util/List;", "()Ljava/util/List<Ljava/lang/String;>;", null)
        with(mv){
            val labels = arrayListOf<Label>()
            visitCode()

            val label = Label()
            visitLabel(label)
            visitTypeInsn(NEW, "java/util/ArrayList")
            visitInsn(DUP)
            visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false)
            visitVarInsn(ASTORE, 0)

            registerModule.forEach{
                @Suppress("NAME_SHADOWING") val label = Label()
                labels.add(label)
                visitLabel(label)
                visitVarInsn(ALOAD, 0)
                visitTypeInsn(NEW, it)
                visitInsn(DUP)
                visitMethodInsn(INVOKESPECIAL, it, "<init>", "()V", false)
                visitMethodInsn(INVOKEVIRTUAL, it, "getRoute", "()Ljava/util/List;", false)
                visitMethodInsn(INVOKEINTERFACE, "java/util/List", "addAll", "(Ljava/util/Collection;)Z", true)
                visitInsn(POP)
            }
            val label2 = Label()
            labels.add(label2)
            visitLabel(label2)
            visitVarInsn(ALOAD, 0)
            visitInsn(ARETURN)

            val label3 = Label()
            visitLabel(label3)
            visitLocalVariable("list", "Ljava/util/List;", "Ljava/util/List<Ljava/lang/String;>;", labels[0], label3, 0)
            visitEnd()
        }
        cn.accept(cw)
        return cw.toByteArray()
    }
}