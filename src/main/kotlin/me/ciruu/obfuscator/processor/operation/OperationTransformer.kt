package me.ciruu.obfuscator.processor.operation

import me.ciruu.obfuscator.config.Config
import me.ciruu.obfuscator.processor.Process
import me.ciruu.obfuscator.structure.ClassStruct
import me.ciruu.obfuscator.utils.Util
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.*
import kotlin.random.Random

class OperationTransformer(
    config: Config,
    classes: List<ClassStruct>,
): Process(config, classes) {
    override fun process() {
        if (!config.operationTransformer)
            return

        Util.announce(Util.COLOR.PURPLE, "---------------------------------------------")
        Util.announce(Util.COLOR.PURPLE, "Operation transformer process...")

        operationTransformer()

        Util.announce(Util.COLOR.PURPLE, "Done!")
        Util.announce(Util.COLOR.PURPLE, "---------------------------------------------")
    }

    private fun operationTransformer() {
        for (clazz in classes) {
            if (!clazz.editable)
                continue
            clazz.modified = true
            for (methodStruct in clazz.methods.values) {
                val method = methodStruct.methodNode
                for (i in 1 .. config.operationTransformerPasses) {
                    for (insn in method.instructions) {
                        val instructions = method.instructions
                        if (isXOR(insn.opcode)) {
                            instructions.insertBefore(insn, replaceXOR())
                            instructions.remove(insn)
                        }
                        if (isAND(insn.opcode)) {
                            instructions.insertBefore(insn, replaceAND())
                            instructions.remove(insn)
                        }
                        if (isOR(insn.opcode)) {
                            instructions.insertBefore(insn, replaceOR())
                            instructions.remove(insn)
                        }
                        if (isADD(insn.opcode)) {
                            instructions.insertBefore(insn, replaceADD())
                            instructions.remove(insn)
                        }
                        if (isSUB(insn.opcode)) {
                            instructions.insertBefore(insn, replaceSUB())
                            instructions.remove(insn)
                        }
                    }
                }
            }
        }
    }

    private fun isXOR(opcode: Int): Boolean {
        return opcode == IXOR// || opcode == LXOR
    }

    private fun isAND(opcode: Int): Boolean {
        return opcode == IAND// || opcode == LAND
    }

    private fun isOR(opcode: Int): Boolean {
        return opcode == IOR// || opcode == LOR
    }

    private fun isADD(opcode: Int): Boolean {
        return opcode == IADD
    }

    private fun isSUB(opcode: Int): Boolean {
        return opcode == ISUB
    }

    private fun replaceXOR(): InsnList {
        val insnList = InsnList()

        when (Random.nextInt(0, 2)) {
            0 -> insnList.apply { // (x | y) & ~(x & y)
                add(InsnNode(DUP2)) // Duplica los dos valores en la cima de la pila.
                add(InsnNode(IOR)) // Realiza la operación OR entre los dos valores duplicados
                add(InsnNode(DUP_X2)) // Duplica el valor que estaba en la cima de la pila y lo coloca en la tercera posición de la pila.
                add(InsnNode(POP)) // Elimina el valor que estaba en la cima de la pila.
                add(InsnNode(IAND)) // Realiza la operación AND entre los dos valores que quedaron en la cima de la pila.
                add(InsnNode(ICONST_M1)) // Agrega el valor -1 a la pila.
                add(InsnNode(IXOR)) //Realiza la operación XOR entre el valor -1 y el resultado de la operación AND.
                add(InsnNode(IAND)) //Realiza la operación AND entre el resultado anterior y el resultado de la operación OR.
            }
            1 -> insnList.apply { // x + y - (x & y) * 2
                add(InsnNode(DUP2))
                add(InsnNode(IADD))
                add(InsnNode(DUP_X2))
                add(InsnNode(POP))
                add(InsnNode(IAND))
                add(InsnNode(ICONST_2))
                add(InsnNode(IMUL))
                add(InsnNode(ISUB))
            }
            2 -> insnList.apply {
                add(InsnNode(DUP2))
                add(InsnNode(ICONST_M1))
                add(InsnNode(IXOR))
                add(InsnNode(IAND))
                add(InsnNode(DUP_X2))
                add(InsnNode(POP))
                add(InsnNode(SWAP))
                add(InsnNode(ICONST_M1))
                add(InsnNode(IXOR))
                add(InsnNode(IAND))
                add(InsnNode(IOR))
            }
        }

        return insnList
    }

    private fun replaceADD(): InsnList {
        val insnList = InsnList()
        when (Random.nextInt(0, 1)) {
            0 -> insnList.apply {
                add(InsnNode(DUP2))
                add(InsnNode(IXOR))
                add(InsnNode(DUP_X2))
                add(InsnNode(POP))
                add(InsnNode(IAND))
                add(InsnNode(ICONST_2))
                add(InsnNode(IMUL))
                add(InsnNode(IADD))
            }
            1 -> insnList.apply {
                add(InsnNode(DUP2))
                add(InsnNode(IOR))
                add(InsnNode(DUP_X2))
                add(InsnNode(POP))
                add(InsnNode(IAND))
                add(InsnNode(IADD))
            }
        }

        return insnList
    }

    private fun replaceSUB(): InsnList {
        val insnList = InsnList()
        when (Random.nextInt(0, 1)) {
            0 -> insnList.apply {
                add(InsnNode(ICONST_M1))
                add(InsnNode(IXOR))
                add(InsnNode(IADD))
                add(InsnNode(ICONST_1))
                add(InsnNode(IADD))
            }
            1 -> insnList.apply {
                add(InsnNode(DUP2))
                add(InsnNode(ICONST_M1))
                add(InsnNode(IXOR))
                add(InsnNode(IAND))
                add(InsnNode(DUP_X2))
                add(InsnNode(POP))
                add(InsnNode(SWAP))
                add(InsnNode(ICONST_M1))
                add(InsnNode(IXOR))
                add(InsnNode(IAND))
                add(InsnNode(ISUB))

            }
        }

        return insnList
    }

    private fun replaceAND(): InsnList {
        val insnList = InsnList().apply {
            add(InsnNode(ICONST_M1)) // -1
            add(InsnNode(IXOR))
            add(InsnNode(SWAP)) // intercambiar x e y
            add(InsnNode(ICONST_M1)) // -1
            add(InsnNode(IXOR))
            add(InsnNode(SWAP)) // devolver x e y al orden original
            add(InsnNode(IOR)) // ~x | ~y
            add(InsnNode(ICONST_M1))
            add(InsnNode(IXOR)) // ~(~x | ~y)
        }
        return insnList
    }

    private fun replaceOR(): InsnList {


        val insnList = InsnList()
        when (Random.nextInt(0, 2)) {
            0 -> insnList.apply { // ~(~x & ~y)
                add(InsnNode(ICONST_M1)) // -1
                add(InsnNode(IXOR))
                add(InsnNode(SWAP)) // intercambiar x e y
                add(InsnNode(ICONST_M1)) // -1
                add(InsnNode(IXOR))
                add(InsnNode(SWAP)) // devolver x e y al orden original
                add(InsnNode(IAND)) // ~x & ~y
                add(InsnNode(ICONST_M1))
                add(InsnNode(IXOR)) // ~(~x & ~y)
            }
            1 -> insnList.apply {
                add(InsnNode(DUP_X1))
                add(InsnNode(ICONST_M1))
                add(InsnNode(IXOR))
                add(InsnNode(IAND))
                add(InsnNode(IADD))
            }
            2 -> insnList.apply {
                add(InsnNode(DUP2))
                add(InsnNode(IADD))
                add(InsnNode(DUP_X2))
                add(InsnNode(POP))
                add(InsnNode(IAND))
                add(InsnNode(ISUB))
            }
        }

        return insnList
    }
}