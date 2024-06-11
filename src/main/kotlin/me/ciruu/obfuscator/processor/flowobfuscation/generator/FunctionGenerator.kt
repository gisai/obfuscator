package me.ciruu.obfuscator.processor.flowobfuscation.generator

import me.ciruu.obfuscator.config.Config
import me.ciruu.obfuscator.utils.MathUtil.probability
import me.ciruu.obfuscator.utils.OpcodeUtil
import me.ciruu.obfuscator.utils.RandomGen
import me.ciruu.obfuscator.utils.Util
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import javax.print.attribute.IntegerSyntax
import kotlin.random.Random

class FunctionGenerator(val config: Config, private val randomGen: RandomGen) {

    // Genera una función inútil y devuelve un par que contiene un MethodNode y el número de parámetros
    fun generateUselessFunction(): Pair<MethodNode, Int> {
        val desc = StringBuilder()

        desc.append("(")
        val random = Random
        val params = random.nextInt(1, 10)
        for (i in 0..params) {
            desc.append(Type.INT_TYPE.toString()) // Todos los parámetros son de tipo entero
        }
        desc.append(")")
        desc.append(Type.VOID_TYPE.toString())

        // Crea un nuevo MethodNode con modificadores privados y estáticos
        val methodNode = MethodNode(
            ACC_PRIVATE or ACC_STATIC,
            randomGen.generateRandomString(), // Nombre aleatorio para el método
            desc.toString(),
            /*randomGen.generateRandomString()*/null,
            arrayOfNulls(0)
        )

        generateTrashCode(methodNode, params) // Genera código basura y lo añade al método

        return Pair(methodNode, params) // Devuelve el método y el número de parámetros
    }

    // Genera código basura dentro de un MethodNode
    private fun generateTrashCode(methodNode: MethodNode, params: Int) {
        methodNode.instructions = InsnList()

        // Añade instrucciones de carga de variables y operaciones aleatorias si la probabilidad lo permite
        if (probability(config.uselessFunctionsProbability)) {
            methodNode.instructions.add(VarInsnNode(ILOAD, 0)) // Carga el primer parámetro
            for (i in 1..params) {
                methodNode.instructions.add(VarInsnNode(ILOAD, i)) // Carga los siguientes parámetros
                methodNode.instructions.add(InsnNode(randomOp())) // Añade una operación aleatoria
            }
        }

        // Añade una variable de objeto aleatoria si la probabilidad lo permite
        if (probability(config.uselessFunctionsProbability)) {
            addRandomObjectVariable(methodNode, params)
        }

        // Añade una instrucción de carga de cadena aleatoria si la probabilidad lo permite
        if (probability(config.uselessFunctionsProbability)) {
            methodNode.instructions.add(LdcInsnNode(randomGen.generateRandomString()))
            methodNode.instructions.add(MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "length", "()I", false))
            methodNode.instructions.add(InsnNode(POP))
        }

    }

    // Añade una variable de objeto aleatoria al método
    private fun addRandomObjectVariable(methodNode: MethodNode, params: Int) {
        val labelNode = LabelNode()

        for (i in 0 until params) {
            methodNode.visitLocalVariable(randomGen.generateRandomString(), "I", null, labelNode.label, labelNode.label, methodNode.maxLocals)
            methodNode.maxLocals++
        }

        // Genera un objeto aleatorio
        val randomObjectPair = getRandomObject()

        val randomObject = randomObjectPair.first
        val randomObjectClass = randomObjectPair.second

        val instr = InsnList().apply {
            add(LdcInsnNode(randomObject))
            add(VarInsnNode(getOpStoreFromClass(randomObjectClass), methodNode.maxLocals))
        }

        methodNode.instructions.add(instr)

        val varDesc = Type.getDescriptor(randomObject.javaClass)

        methodNode.visitLocalVariable(randomGen.generateRandomString(), varDesc, null, labelNode.label, labelNode.label, methodNode.maxLocals)
        methodNode.maxLocals++
    }

    // Genera un opcode aleatorio para operaciones aritméticas
    private fun randomOp(): Int {
        return arrayOf(IADD, ISUB, IMUL, IDIV, IREM, ISHL, ISHR, IUSHR, IAND, IOR, IXOR).random()
    }

    private fun getRandomObject(): Pair<Any, Class<*>> {
        val random = Random
        val types = arrayOf(
            Boolean::class.java,
            Byte::class.java,
            Short::class.java,
            Int::class.java,
            Long::class.java,
            Float::class.java,
            Double::class.java,
            Char::class.java,
            String::class.java
        )
        val randomIndex = random.nextInt(types.size)
        val randomType = types[randomIndex]
        return if (randomType.isPrimitive) {
            getRandomPrimitiveValue(randomType)
        } else {
            Pair(randomType.getConstructor(String::class.java).newInstance(random.nextInt().toString()), randomType)
        }
    }

    private fun getRandomPrimitiveValue(type: Class<*>):  Pair<Any, Class<*>> {
        val random = Random
        return when (type) {
            Boolean::class.java -> Pair(random.nextBoolean(), Boolean::class.java)
            Byte::class.java -> Pair(random.nextInt(256).toByte(), Byte::class.java)
            Short::class.java -> Pair(random.nextInt(65536).toShort(), Short::class.java)
            Int::class.java -> Pair(random.nextInt(), Int::class.java)
            Long::class.java -> Pair(random.nextLong(), Long::class.java)
            Float::class.java -> Pair(random.nextFloat(), Float::class.java)
            Double::class.java -> Pair(random.nextDouble(), Double::class.java)
            Char::class.java -> Pair(random.nextInt(65536).toChar(), Char::class.java)
            else -> throw IllegalArgumentException("Unsupported primitive type: $type")
        }
    }

    private fun getOpStoreFromClass(type: Class<*>): Int {
        return when (type) {
            Boolean::class.java -> ISTORE
            Byte::class.java -> ISTORE
            Short::class.java -> ISTORE
            Int::class.java -> ISTORE
            Long::class.java -> LSTORE
            Float::class.java -> FSTORE
            Double::class.java -> DSTORE
            Char::class.java -> ISTORE
            else -> ASTORE
        }
    }
}