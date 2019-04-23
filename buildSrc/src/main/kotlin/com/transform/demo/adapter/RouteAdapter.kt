package com.transform.demo.adapter

import com.transform.demo.ScanConstants
import com.transform.demo.transform.AutoRegisterTransform
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

/**
 *
 * @author yutiantian email: yutiantina@gmail.com
 * @since 2019/4/12
 */
class RouteAdapter(cw: ClassWriter, private var registerModule: ArrayList<String>) : ClassVisitor(Opcodes.ASM6, cw), Opcodes {
    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        interfaces?.find {
            it == ScanConstants.IMODULE
        }?.let {
            registerModule.add(name)
        }
    }

}