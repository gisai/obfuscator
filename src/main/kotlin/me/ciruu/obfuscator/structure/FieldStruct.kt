package me.ciruu.obfuscator.structure

import org.objectweb.asm.tree.FieldNode

class FieldStruct(val fieldNode: FieldNode) {
    val name: String = fieldNode.name // Nombre del campo
    val desc: String = fieldNode.desc // Descripción del campo
    var obfName: String = name // Nombre ofuscado
}