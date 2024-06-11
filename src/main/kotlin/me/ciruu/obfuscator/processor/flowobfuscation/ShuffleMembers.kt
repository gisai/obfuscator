package me.ciruu.obfuscator.processor.flowobfuscation

import me.ciruu.obfuscator.config.Config
import me.ciruu.obfuscator.processor.Process
import me.ciruu.obfuscator.structure.ClassStruct
import me.ciruu.obfuscator.utils.Util

class ShuffleMembers(config: Config, classes: List<ClassStruct>): Process(config, classes) {
    override fun process() {
        if (!config.shuffleMembers)
            return

        Util.announce(Util.COLOR.PURPLE, "---------------------------------------------")
        Util.announce(Util.COLOR.PURPLE, "Shuffle members process...")

        shuffleMembers()

        Util.announce(Util.COLOR.PURPLE, "Done!")
        Util.announce(Util.COLOR.PURPLE, "---------------------------------------------")
    }

    private fun shuffleMembers() {
        for (clazz in classes) {
            if (!clazz.editable)
                continue
            val classNode = clazz.classNode
            classNode.methods.shuffle()
            classNode.fields.shuffle()
            classNode.innerClasses.shuffle()
            classNode.interfaces.shuffle()

            if (classNode.invisibleAnnotations != null) classNode.invisibleAnnotations.shuffle()
            if (classNode.visibleAnnotations != null) classNode.visibleAnnotations.shuffle()
            if (classNode.invisibleTypeAnnotations != null) classNode.invisibleTypeAnnotations.shuffle()

            for (methodStruct in clazz.methods.values) {
                val method = methodStruct.methodNode
                if (method.invisibleAnnotations != null) method.invisibleAnnotations.shuffle()
                if (method.invisibleLocalVariableAnnotations != null) method.invisibleLocalVariableAnnotations.shuffle()
                if (method.invisibleTypeAnnotations != null) method.invisibleTypeAnnotations.shuffle()
                if (method.visibleAnnotations != null) method.visibleAnnotations.shuffle()
                if (method.visibleLocalVariableAnnotations != null) method.visibleLocalVariableAnnotations.shuffle()
                if (method.visibleTypeAnnotations != null) method.visibleTypeAnnotations.shuffle()
                method.exceptions.shuffle()
                if (method.localVariables != null) method.localVariables.shuffle()
                if (method.parameters != null) method.parameters.shuffle()

            }

            for (fieldStruct in clazz.fields.values) {
                val field = fieldStruct.fieldNode
                if (field.invisibleAnnotations != null) field.invisibleAnnotations.shuffle()
                if (field.invisibleTypeAnnotations != null) field.invisibleTypeAnnotations.shuffle()
                if (field.visibleAnnotations != null) field.visibleAnnotations.shuffle()
                if (field.visibleTypeAnnotations != null) field.visibleTypeAnnotations.shuffle()
            }
        }


    }
}