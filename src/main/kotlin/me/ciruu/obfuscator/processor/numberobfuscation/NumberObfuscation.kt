package me.ciruu.obfuscator.processor.numberobfuscation

import me.ciruu.obfuscator.config.Config
import me.ciruu.obfuscator.processor.Process
import me.ciruu.obfuscator.processor.numberobfuscation.types.XorNumberObf
import me.ciruu.obfuscator.structure.ClassStruct
import me.ciruu.obfuscator.utils.Util

class NumberObfuscation(config: Config, classes: List<ClassStruct>): Process(config, classes) {
    private val xorNumberObf = XorNumberObf()

    override fun process() {
        if (!config.numberObfuscation)
            return

        Util.announce(Util.COLOR.PURPLE, "---------------------------------------------")
        Util.announce(Util.COLOR.PURPLE, "Number obfuscation process...")

        numberObfuscation()

        Util.announce(Util.COLOR.PURPLE, "Done!")
        Util.announce(Util.COLOR.PURPLE, "---------------------------------------------")
    }

    private fun numberObfuscation() {
        for (clazz in classes) {
            if (!clazz.editable)
                continue
            for (methodStruct in clazz.methods.values) {
                val methodNode = methodStruct.methodNode
                when (config.numberObfuscationType) {
                    Config.NumberObfType.XOR -> {
                        val passes = if (config.numberObfuscationPasses < 1) 1 else config.numberObfuscationPasses
                        for (i in 1 .. passes) {
                            xorNumberObf.obfuscateXOR(methodNode)
                        }
                    }
                }
            }
        }
    }


}