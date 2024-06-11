package me.ciruu.obfuscator.structure

import org.objectweb.asm.tree.MethodNode

class MethodStruct(val methodNode: MethodNode) {
    val name: String = methodNode.name // Nombre del método
    val desc: String = methodNode.desc // Descripción del método
    var obfName: String = name // Nombre ofuscado
    val locals: MutableMap<String, String> = mutableMapOf() // Mapa de variables locales
}