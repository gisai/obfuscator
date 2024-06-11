package me.ciruu.obfuscator.processor.info

import me.ciruu.obfuscator.config.Config
import me.ciruu.obfuscator.processor.Process
import me.ciruu.obfuscator.structure.ClassStruct
import me.ciruu.obfuscator.utils.Util
import org.objectweb.asm.tree.LineNumberNode

class RemoveInfo(config: Config, classes: List<ClassStruct>): Process(config, classes) {

    private val removeLineNumber = true

    override fun process() {
        if (!config.removeInfo)
            return

        Util.announce(Util.COLOR.PURPLE, "---------------------------------------------")
        Util.announce(Util.COLOR.PURPLE, "Removing info process...")

        removeInfo()

        Util.announce(Util.COLOR.PURPLE, "Done!")
        Util.announce(Util.COLOR.PURPLE, "---------------------------------------------")
    }

    private fun removeInfo() {
        for (clazz in classes) {
            if (!clazz.editable)
                continue
            clazz.modified = true
            clazz.classNode.signature = null
            clazz.classNode.sourceFile = null
            clazz.classNode.sourceDebug = null

            for (methodStruct in clazz.methods.values) {
                val methodNode = methodStruct.methodNode
                methodNode.signature = null
                if (removeLineNumber) {
                    val it = methodNode.instructions.iterator()
                    while (it.hasNext()) {
                        val inst = it.next()
                        if (inst is LineNumberNode) {
                            it.remove()
                        }
                    }
                }
            }
            for (fieldStruct in clazz.fields.values) {
                val field = fieldStruct.fieldNode
                field.signature = null
            }
        }
    }
}