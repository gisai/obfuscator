package me.ciruu.obfuscator.processor.stringencoder

import me.ciruu.obfuscator.config.Config
import me.ciruu.obfuscator.processor.Process
import me.ciruu.obfuscator.processor.stringencoder.types.Base64Encoder
import me.ciruu.obfuscator.processor.stringencoder.types.XorEncoder
import me.ciruu.obfuscator.structure.ClassStruct
import me.ciruu.obfuscator.utils.RandomGen
import me.ciruu.obfuscator.utils.Util
import me.ciruu.obfuscator.utils.Util.announce
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import java.util.Base64

class StringEncoder(config: Config, classes: List<ClassStruct>, randomGen: RandomGen): Process(config, classes) {

    private val base64Encoder = Base64Encoder()
    private val xorEncoder = XorEncoder(randomGen)

    override fun process() {
        if (!config.encodeStrings)
            return

        announce(Util.COLOR.PURPLE, "---------------------------------------------")
        announce(Util.COLOR.PURPLE, "String encoder process...")

        encode()

        announce(Util.COLOR.PURPLE, "Done!")
        announce(Util.COLOR.PURPLE, "---------------------------------------------")
    }

    private fun encode() {
        for (clazz in classes) {
            if (!clazz.editable)
                continue
            when(config.encodeType) {
                Config.EncodeType.BASE64 -> base64Encoder.encodeBase64(clazz)
                Config.EncodeType.XOR -> xorEncoder.encodeXor(clazz)
            }
        }
    }
}