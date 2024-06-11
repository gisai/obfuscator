package me.ciruu.obfuscator.processor.stringencoder.types

import me.ciruu.obfuscator.structure.ClassStruct
import me.ciruu.obfuscator.structure.MethodStruct
import me.ciruu.obfuscator.utils.RandomGen
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import kotlin.random.Random


class XorEncoder(private val randomGen: RandomGen) {

    private val LETTER_ARRAY = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

    fun encodeXor(classStruct: ClassStruct) {
        var found = false
        val decryptMethod = generateDecryptMethod()
        classStruct.methods.forEach { methodStruct ->
            val method = methodStruct.value.methodNode
            val iterator = method.instructions.iterator()
            while (iterator.hasNext()) {
                val insn = iterator.next()
                when (insn.opcode) {
                    // Instrucciones que cargan una String en la pila
                    LDC -> {
                        val constInsn = insn as LdcInsnNode
                        if (constInsn.cst is String) {
                            classStruct.modified = true
                            found = true

                            val key = generateRandomString()
                            val encodedString = encrypt(key, constInsn.cst as String)

                            val invoke = MethodInsnNode(
                                INVOKESTATIC,
                                classStruct.classNode.name,
                                decryptMethod.name,
                                decryptMethod.desc,
                                false
                            )

                            iterator.remove()
                            iterator.add(LdcInsnNode(key))
                            iterator.add(LdcInsnNode(encodedString))
                            iterator.add(invoke)

                        }
                    }
                }
            }
        }
        if (found) {
            classStruct.classNode.methods.add(decryptMethod)
            val methodStruct = MethodStruct(decryptMethod)
            classStruct.methods[methodStruct.name] = methodStruct
        }
    }

    private fun generateRandomString(): String {
        val str = StringBuilder()
        for (i in 0..Random.nextInt(3, 10))
            str.append(generateRandomChar())
        return str.toString()
    }

    private fun generateRandomChar(): String {
        return LETTER_ARRAY[Random.nextInt(LETTER_ARRAY.length)].toString()
    }

    /**
     *     private static String decrypt(String key, String textEnc) {
     *         byte[] textEncB = textEnc.getBytes();
     *         byte[] keyB = key.getBytes();
     *
     *         for(int i = keyB.length - 1; i >= 0; --i) {
     *             byte keyByte = keyB[i];
     *
     *             for(int j = 0; j < textEncB.length; ++j) {
     *                 byte op = (byte)(keyByte ^ textEncB[j]);
     *                 textEncB[j] = op;
     *             }
     *         }
     *
     *         return new String(textEncB);
     *     }
     */

    private fun generateDecryptMethod(): MethodNode {
        val methodNode = MethodNode(
            ACC_PRIVATE or ACC_STATIC,
            randomGen.generateRandomString(),
            "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
            null,
            null
        )
        methodNode.visitCode()
        val label0 = Label()
        methodNode.visitLabel(label0)
        methodNode.visitVarInsn(ALOAD, 1)
        methodNode.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "getBytes", "()[B", false)
        methodNode.visitVarInsn(ASTORE, 2)
        val label1 = Label()
        methodNode.visitLabel(label1)
        methodNode.visitVarInsn(ALOAD, 0)
        methodNode.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "getBytes", "()[B", false)
        methodNode.visitVarInsn(ASTORE, 3)
        val label2 = Label()
        methodNode.visitLabel(label2)
        methodNode.visitVarInsn(ALOAD, 3)
        methodNode.visitInsn(ARRAYLENGTH)
        methodNode.visitInsn(ICONST_1)
        methodNode.visitInsn(ISUB)
        methodNode.visitVarInsn(ISTORE, 4)
        val label3 = Label()
        methodNode.visitLabel(label3)
        methodNode.visitFrame(F_APPEND, 3, arrayOf<Any>("[B", "[B", INTEGER), 0, null)
        methodNode.visitVarInsn(ILOAD, 4)
        val label4 = Label()
        methodNode.visitJumpInsn(IFLT, label4)
        val label5 = Label()
        methodNode.visitLabel(label5)
        methodNode.visitVarInsn(ALOAD, 3)
        methodNode.visitVarInsn(ILOAD, 4)
        methodNode.visitInsn(BALOAD)
        methodNode.visitVarInsn(ISTORE, 5)
        val label6 = Label()
        methodNode.visitLabel(label6)
        methodNode.visitInsn(ICONST_0)
        methodNode.visitVarInsn(ISTORE, 6)
        val label7 = Label()
        methodNode.visitLabel(label7)
        methodNode.visitFrame(F_APPEND, 2, arrayOf<Any>(INTEGER, INTEGER), 0, null)
        methodNode.visitVarInsn(ILOAD, 6)
        methodNode.visitVarInsn(ALOAD, 2)
        methodNode.visitInsn(ARRAYLENGTH)
        val label8 = Label()
        methodNode.visitJumpInsn(IF_ICMPGE, label8)
        val label9 = Label()
        methodNode.visitLabel(label9)
        methodNode.visitVarInsn(ILOAD, 5)
        methodNode.visitVarInsn(ALOAD, 2)
        methodNode.visitVarInsn(ILOAD, 6)
        methodNode.visitInsn(BALOAD)
        methodNode.visitInsn(IXOR)
        methodNode.visitInsn(I2B)
        methodNode.visitVarInsn(ISTORE, 7)
        val label10 = Label()
        methodNode.visitLabel(label10)
        methodNode.visitVarInsn(ALOAD, 2)
        methodNode.visitVarInsn(ILOAD, 6)
        methodNode.visitVarInsn(ILOAD, 7)
        methodNode.visitInsn(BASTORE)
        val label11 = Label()
        methodNode.visitLabel(label11)
        methodNode.visitIincInsn(6, 1)
        methodNode.visitJumpInsn(GOTO, label7)
        methodNode.visitLabel(label8)
        methodNode.visitFrame(F_CHOP, 2, null, 0, null)
        methodNode.visitIincInsn(4, -1)
        methodNode.visitJumpInsn(GOTO, label3)
        methodNode.visitLabel(label4)
        methodNode.visitFrame(F_CHOP, 1, null, 0, null)
        methodNode.visitTypeInsn(NEW, "java/lang/String")
        methodNode.visitInsn(DUP)
        methodNode.visitVarInsn(ALOAD, 2)
        methodNode.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V", false)
        methodNode.visitInsn(ARETURN)
        val label12 = Label()
        methodNode.visitLabel(label12)
        methodNode.visitLocalVariable(randomGen.generateRandomString(), "B", null, label10, label11, 7)
        methodNode.visitLocalVariable(randomGen.generateRandomString(), "I", null, label7, label8, 6)
        methodNode.visitLocalVariable(randomGen.generateRandomString(), "B", null, label6, label8, 5)
        methodNode.visitLocalVariable(randomGen.generateRandomString(), "I", null, label3, label4, 4)
        methodNode.visitLocalVariable(randomGen.generateRandomString(), "Ljava/lang/String;", null, label0, label12, 0)
        methodNode.visitLocalVariable(randomGen.generateRandomString(), "Ljava/lang/String;", null, label0, label12, 1)
        methodNode.visitLocalVariable(randomGen.generateRandomString(), "[B", null, label1, label12, 2)
        methodNode.visitLocalVariable(randomGen.generateRandomString(), "[B", null, label2, label12, 3)
        methodNode.visitMaxs(3, 8)
        methodNode.visitEnd()
        return methodNode
    }

    private fun encrypt(key: String, text: String): String {
        val textB: ByteArray = text.toByteArray()
        val keyB: ByteArray = key.toByteArray()
        val textEnc = ByteArray(textB.size)
        for (i in keyB.indices) {
            val keyByte = keyB[i]
            for (j in textB.indices) {
                val op = (textB[j].toInt() xor keyByte.toInt()).toByte()
                textB[j] = op
            }
        }
        for (i in textB.indices) {
            textEnc[i] = textB[i]
        }
        return String(textEnc)
    }

}