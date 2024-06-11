package me.ciruu.obfuscator.structure

import org.objectweb.asm.tree.ClassNode
import java.io.File
import kotlin.random.Random

class ClassStruct(val classNode: ClassNode, val editable: Boolean) {
    val name: String = classNode.name
    val superClass: String = classNode.superName ?: "java/lang/Object"
    val dependencies: Set<String> = (classNode.interfaces + superClass).filter { it != "java/lang/Object" }.toSet()
    var obfName: String = name
    val methods = mutableMapOf<String, MethodStruct>()
    val fields = mutableMapOf<String, FieldStruct>()
    var path: String = ""
    var modified: Boolean = false
}