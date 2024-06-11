package me.ciruu.obfuscator.utils

import org.objectweb.asm.Opcodes.*

object OperationCodeUtil {

    fun isIntOp(opcode: Int): Boolean {
        return when(opcode) {
            IADD, ISUB, IMUL, IDIV, IREM, ISHL, ISHR, IUSHR, IAND, IOR, IXOR -> true
            else -> false
        }
    }

    fun isLongOp(opcode: Int): Boolean {
        return when(opcode) {
            LADD, LSUB, LMUL, LDIV, LREM, LAND, LOR, LXOR, LSHL, LSHR, LUSHR -> true
            else -> false
        }
    }

    fun isFloatOp(opcode: Int): Boolean {
        return when(opcode) {
            FADD, FSUB, FMUL, FDIV, FREM -> true
            else -> false
        }
    }

    fun isDoubleOp(opcode: Int): Boolean {
        return when(opcode) {
            DADD, DSUB, DMUL, DDIV, DREM -> true
            else -> false
        }
    }
}