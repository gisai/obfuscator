package me.ciruu.obfuscator.utils

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.IntInsnNode
import org.objectweb.asm.tree.LdcInsnNode

object NumInstTypeUtil {
    fun getIntInst(inst: AbstractInsnNode): Int {
        when (inst.opcode) {
            Opcodes.LDC -> {
                if ((inst as LdcInsnNode).cst is Int)
                    return inst.cst as Int
            }
            Opcodes.SIPUSH -> {
                if (inst is IntInsnNode)
                    return inst.operand
            }
            Opcodes.BIPUSH -> {
                if (inst is IntInsnNode)
                    return inst.operand
            }
            Opcodes.ICONST_0 -> return 0
            Opcodes.ICONST_1 -> return 1
            Opcodes.ICONST_2 -> return 2
            Opcodes.ICONST_3 -> return 3
            Opcodes.ICONST_4 -> return 4
            Opcodes.ICONST_5 -> return 5
        }
        return Int.MAX_VALUE
    }

    fun isIntInst(inst: AbstractInsnNode): Boolean {
        when (inst.opcode){
            Opcodes.LDC -> {
                if ((inst as LdcInsnNode).cst is Int)
                    return true
            }
            Opcodes.SIPUSH -> return true
            Opcodes.BIPUSH -> return true
            Opcodes.ICONST_0 -> return true
            Opcodes.ICONST_1 -> return true
            Opcodes.ICONST_2 -> return true
            Opcodes.ICONST_3 -> return true
            Opcodes.ICONST_4 -> return true
            Opcodes.ICONST_5 -> return true
        }
        return false
    }

    fun getLongInst(inst: AbstractInsnNode): Long {
        when (inst.opcode) {
            Opcodes.LDC -> {
                if ((inst as LdcInsnNode).cst is Long)
                    return inst.cst as Long
            }
            Opcodes.LCONST_0 -> return 0
            Opcodes.LCONST_1 -> return 1
        }
        return Long.MAX_VALUE
    }

    fun isLongInst(inst: AbstractInsnNode): Boolean {
        when (inst.opcode){
            Opcodes.LDC -> {
                if ((inst as LdcInsnNode).cst is Long)
                    return true
            }
            Opcodes.LCONST_0 -> return true
            Opcodes.LCONST_1 -> return true
        }
        return false
    }

    fun getFloatInst(inst: AbstractInsnNode): Float {
        when (inst.opcode) {
            Opcodes.LDC -> {
                if ((inst as LdcInsnNode).cst is Float)
                    return inst.cst as Float
            }
            Opcodes.FCONST_0 -> return 0f
            Opcodes.FCONST_1 -> return 1f
            Opcodes.FCONST_2 -> return 2f
        }
        return Float.MAX_VALUE
    }

    fun isFloatInst(inst: AbstractInsnNode): Boolean {
        when (inst.opcode){
            Opcodes.LDC -> {
                if ((inst as LdcInsnNode).cst is Float)
                    return true
            }
            Opcodes.FCONST_0 -> return true
            Opcodes.FCONST_1 -> return true
            Opcodes.FCONST_2 -> return true
        }
        return false
    }

    fun getDoubleInst(inst: AbstractInsnNode): Double {
        when (inst.opcode) {
            Opcodes.LDC -> {
                if ((inst as LdcInsnNode).cst is Double)
                    return inst.cst as Double
            }
            Opcodes.DCONST_0 -> return 0.0
            Opcodes.DCONST_1 -> return 1.0
        }
        return Double.MAX_VALUE
    }

    fun isDoubleInst(inst: AbstractInsnNode): Boolean {
        when (inst.opcode){
            Opcodes.LDC -> {
                if ((inst as LdcInsnNode).cst is Double)
                    return true
            }
            Opcodes.DCONST_0 -> return true
            Opcodes.DCONST_1 -> return true
        }
        return false
    }
}