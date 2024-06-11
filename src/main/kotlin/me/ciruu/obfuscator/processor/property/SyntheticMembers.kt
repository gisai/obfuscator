package me.ciruu.obfuscator.processor.property

import org.objectweb.asm.Opcodes
import me.ciruu.obfuscator.config.Config
import me.ciruu.obfuscator.processor.Process
import me.ciruu.obfuscator.structure.ClassStruct
import me.ciruu.obfuscator.utils.Util

class SyntheticMembers(config: Config, classes: List<ClassStruct>): Process(config, classes) {
    override fun process() {
        if (!config.syntheticMembers)
            return

        Util.announce(Util.COLOR.PURPLE, "---------------------------------------------")
        Util.announce(Util.COLOR.PURPLE, "Synthetic members process...")

        syntheticMembers()

        Util.announce(Util.COLOR.PURPLE, "Done!")
        Util.announce(Util.COLOR.PURPLE, "---------------------------------------------")
    }

    private fun syntheticMembers() {
        for (clazz in classes) {
            if (!clazz.editable) continue
            clazz.modified = true

            // Annotations cannot be marked SYNTHETIC
            if (!clazz.dependencies.contains("java/lang/annotation/Annotation")) {
                clazz.classNode.access = clazz.classNode.access or Opcodes.ACC_SYNTHETIC
            }

            clazz.methods.forEach { (_, method) ->
                method.methodNode.access = method.methodNode.access or Opcodes.ACC_SYNTHETIC
            }

            clazz.fields.forEach { (_, field) ->
                field.fieldNode.access = field.fieldNode.access or Opcodes.ACC_SYNTHETIC
            }
        }
    }
}