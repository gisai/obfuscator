package me.ciruu.obfuscator.processor.operation

import me.ciruu.obfuscator.utils.OperationCodeUtil.isIntOp
import me.ciruu.obfuscator.utils.OperationCodeUtil.isLongOp
import me.ciruu.obfuscator.utils.OperationCodeUtil.isFloatOp
import me.ciruu.obfuscator.utils.OperationCodeUtil.isDoubleOp
import me.ciruu.obfuscator.utils.RandomGen
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*

class OperationBuilder(private val randomGen: RandomGen) {
    fun generateIntMethod(opcode: Int): MethodNode? {
        if (isIntOp(opcode)) {
            val description: String = "(" + Type.INT_TYPE.toString() + Type.INT_TYPE.toString() + ")I"
            val methodNode = MethodNode(
                Opcodes.ACC_PRIVATE or Opcodes.ACC_STATIC,
                randomGen.generateRandomString(),
                description,
                null,
                arrayOfNulls(0)
            )

            methodNode.instructions = InsnList()

            methodNode.instructions.add(VarInsnNode(Opcodes.ILOAD, 0))
            methodNode.instructions.add(VarInsnNode(Opcodes.ILOAD, Type.INT_TYPE.size))
            methodNode.instructions.add(InsnNode(opcode))
            methodNode.instructions.add(InsnNode(Opcodes.IRETURN))

            return methodNode
        }
        return null
    }

    fun generateLongMethod(opcode: Int): MethodNode? {
        if (isLongOp(opcode)) {
            val description: String = "(" + Type.LONG_TYPE.toString() + Type.LONG_TYPE.toString() + ")J"
            val methodNode = MethodNode(
                Opcodes.ACC_PRIVATE or Opcodes.ACC_STATIC,
                randomGen.generateRandomString(),
                description,
                null,
                arrayOfNulls(0)
            )

            methodNode.instructions = InsnList()

            methodNode.instructions.add(VarInsnNode(Opcodes.LLOAD, 0))
            methodNode.instructions.add(VarInsnNode(Opcodes.LLOAD, Type.LONG_TYPE.size))
            methodNode.instructions.add(InsnNode(opcode))
            methodNode.instructions.add(InsnNode(Opcodes.LRETURN))

            return methodNode
        }
        return null
    }

    fun generateFloatMethod(opcode: Int): MethodNode? {
        if (isFloatOp(opcode)) {
            val description: String = "(" + Type.FLOAT_TYPE.toString() + Type.FLOAT_TYPE.toString() + ")F"
            val methodNode = MethodNode(
                Opcodes.ACC_PRIVATE or Opcodes.ACC_STATIC,
                randomGen.generateRandomString(),
                description,
                null,
                arrayOfNulls(0)
            )

            methodNode.instructions = InsnList()

            methodNode.instructions.add(VarInsnNode(Opcodes.FLOAD, 0))
            methodNode.instructions.add(VarInsnNode(Opcodes.FLOAD, Type.FLOAT_TYPE.size))
            methodNode.instructions.add(InsnNode(opcode))
            methodNode.instructions.add(InsnNode(Opcodes.FRETURN))

            return methodNode
        }
        return null
    }

    fun generateDoubleMethod(opcode: Int): MethodNode? {
        if (isDoubleOp(opcode)) {
            val description: String = "(" + Type.DOUBLE_TYPE.toString() + Type.DOUBLE_TYPE.toString() + ")D"
            val methodNode = MethodNode(
                Opcodes.ACC_PRIVATE or Opcodes.ACC_STATIC,
                randomGen.generateRandomString(),
                description,
                null,
                arrayOfNulls(0)
            )

            methodNode.instructions = InsnList()

            methodNode.instructions.add(VarInsnNode(Opcodes.DLOAD, 0))
            methodNode.instructions.add(VarInsnNode(Opcodes.DLOAD, Type.DOUBLE_TYPE.size))
            methodNode.instructions.add(InsnNode(opcode))
            methodNode.instructions.add(InsnNode(Opcodes.DRETURN))

            return methodNode
        }
        return null
    }
}