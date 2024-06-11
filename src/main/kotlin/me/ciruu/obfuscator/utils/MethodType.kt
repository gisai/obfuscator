package me.ciruu.obfuscator.utils

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodNode

object MethodType {

    fun isIllegalMethod(methodName: String): Boolean {
        return methodName in setOf("<init>", "<clinit>", "toString", "hashCode", "equals", "clone", "valueOf") || methodName.startsWith("lambda$")
    }

    fun isMainMethod(methodNode: MethodNode): Boolean {
        // Comprobamos si el nombre del método es "main"
        if (methodNode.name != "main") {
            return false
        }

        // Comprobamos si el método es público y estático
        if ((methodNode.access and Opcodes.ACC_PUBLIC) == 0 ||
            (methodNode.access and Opcodes.ACC_STATIC) == 0) {
            return false
        }

        // Comprobamos si el método tiene un descriptor válido para el método main
        if (methodNode.desc != "([Ljava/lang/String;)V") {
            return false
        }

        return true
    }
}