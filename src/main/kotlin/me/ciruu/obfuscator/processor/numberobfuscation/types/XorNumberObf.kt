package me.ciruu.obfuscator.processor.numberobfuscation.types

import me.ciruu.obfuscator.utils.NumInstTypeUtil
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import kotlin.random.Random

class XorNumberObf {
    fun obfuscateXOR(methodNode: MethodNode) {
        val iterator = methodNode.instructions.iterator()

        while (iterator.hasNext()) {
            val insn = iterator.next()

            // INT Type
            if (NumInstTypeUtil.isIntInst(insn)) {
                val num = NumInstTypeUtil.getIntInst(insn)
                val random = Random.nextInt()
                val x = num xor random

                // Añadimos las nuevas instrucciones para cargar el número ofuscado
                methodNode.instructions.insertBefore(insn, InsnList().apply {
                    add(LdcInsnNode(random))
                    add(LdcInsnNode(x))
                    add(InsnNode(Opcodes.IXOR))
                })

                // Eliminamos la instrucción que carga el número original
                methodNode.instructions.remove(insn)
            }

            // LONG Type
            if (NumInstTypeUtil.isLongInst(insn)) {
                val num = NumInstTypeUtil.getLongInst(insn)
                val random = Random.nextLong()
                val x = num xor random

                // Añadimos las nuevas instrucciones para cargar el número ofuscado
                methodNode.instructions.insertBefore(insn, InsnList().apply {
                    add(LdcInsnNode(random))
                    add(LdcInsnNode(x))
                    add(InsnNode(Opcodes.LXOR))
                })

                // Eliminamos la instrucción que carga el número original
                methodNode.instructions.remove(insn)
            }

            // FLOAT Type
            if (NumInstTypeUtil.isFloatInst(insn)) {
                val num = java.lang.Float.floatToIntBits(NumInstTypeUtil.getFloatInst(insn))
                val random = Random.nextInt()
                val x = num xor random

                // Añadimos las nuevas instrucciones para cargar el número ofuscado
                methodNode.instructions.insertBefore(insn, InsnList().apply {
                    add(LdcInsnNode(random))
                    add(LdcInsnNode(x))
                    add(InsnNode(Opcodes.IXOR))
                    add(MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        "java/lang/Float",
                        "intBitsToFloat",
                        "(I)F",
                        false
                    ))
                })

                // Eliminamos la instrucción que carga el número original
                methodNode.instructions.remove(insn)
            }

            // DOUBLE Type
            if (NumInstTypeUtil.isDoubleInst(insn)) {
                val num = java.lang.Double.doubleToRawLongBits(NumInstTypeUtil.getDoubleInst(insn))
                val random = Random.nextLong()
                val x = num xor random

                // Añadimos las nuevas instrucciones para cargar el número ofuscado
                methodNode.instructions.insertBefore(insn, InsnList().apply {
                    add(LdcInsnNode(random))
                    add(LdcInsnNode(x))
                    add(InsnNode(Opcodes.LXOR))
                    add(MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        "java/lang/Double",
                        "longBitsToDouble",
                        "(J)D",
                        false
                    ))
                })

                // Eliminamos la instrucción que carga el número original
                methodNode.instructions.remove(insn)
            }
        }
    }
}