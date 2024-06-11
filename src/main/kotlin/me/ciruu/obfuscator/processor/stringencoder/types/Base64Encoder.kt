package me.ciruu.obfuscator.processor.stringencoder.types

import me.ciruu.obfuscator.structure.ClassStruct
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import java.util.Base64

class Base64Encoder {
    fun encodeBase64(classStruct: ClassStruct) {
        classStruct.methods.forEach { methodStruct ->
            val method = methodStruct.value.methodNode
            val iterator = method.instructions.iterator()
            while (iterator.hasNext()) {
                val insn = iterator.next()
                when (insn.opcode) {
                    // Instrucciones que cargan una String en la pila
                    Opcodes.LDC -> {
                        classStruct.modified = true
                        val constInsn = insn as LdcInsnNode
                        if (constInsn.cst is String) {
                            val encodedValue = Base64.getEncoder().encodeToString((constInsn.cst as String).toByteArray())
                            val newStringInsn = TypeInsnNode(Opcodes.NEW, "java/lang/String")
                            val dupInsn = InsnNode(Opcodes.DUP)
                            val decoderType = Type.getType(Base64::class.java)
                            val decoderInsn = MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                decoderType.internalName,
                                "getDecoder",
                                "()" + decoderType.descriptor.removeSuffix(";") + "\$Decoder;",
                                false
                            )
                            val loadInsn = LdcInsnNode(encodedValue) // Cargamos el valor codificado en la pila

                            val decodeInsn = MethodInsnNode(
                                Opcodes.INVOKEVIRTUAL,
                                decoderType.internalName + "\$Decoder",
                                "decode",
                                "(Ljava/lang/String;)[B",
                                false
                            )

                            val charsetInsn = FieldInsnNode(
                                Opcodes.GETSTATIC,
                                "java/nio/charset/StandardCharsets",
                                "UTF_8",
                                "Ljava/nio/charset/Charset;"
                            )
                            val initInsn2 = MethodInsnNode(
                                Opcodes.INVOKESPECIAL,
                                "java/lang/String",
                                "<init>",
                                "([BLjava/nio/charset/Charset;)V",
                                false
                            )

                            iterator.remove() // Eliminamos instrucción original
                            iterator.add(newStringInsn)
                            iterator.add(dupInsn)
                            iterator.add(decoderInsn)
                            iterator.add(loadInsn)
                            iterator.add(decodeInsn)
                            iterator.add(charsetInsn)
                            iterator.add(initInsn2)

                        }
                    }
                }
            }
        }

    }
}