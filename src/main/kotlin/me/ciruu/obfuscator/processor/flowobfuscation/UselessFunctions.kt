package me.ciruu.obfuscator.processor.flowobfuscation

import me.ciruu.obfuscator.config.Config
import me.ciruu.obfuscator.processor.Process
import me.ciruu.obfuscator.processor.flowobfuscation.generator.FunctionGenerator
import me.ciruu.obfuscator.structure.ClassStruct
import me.ciruu.obfuscator.structure.MethodStruct
import me.ciruu.obfuscator.utils.MathUtil.probability
import me.ciruu.obfuscator.utils.MethodType
import me.ciruu.obfuscator.utils.RandomGen
import me.ciruu.obfuscator.utils.Util
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import kotlin.random.Random

class UselessFunctions(config: Config, classes: List<ClassStruct>, private val randomGen: RandomGen): Process(config, classes) {

    private val functionGenerator = FunctionGenerator(config, randomGen)
    override fun process() {
        if (!config.uselessFunctions)
            return

        Util.announce(Util.COLOR.PURPLE, "---------------------------------------------")
        Util.announce(Util.COLOR.PURPLE, "Useless functions process...")

        uselessFunctions()

        Util.announce(Util.COLOR.PURPLE, "Done!")
        Util.announce(Util.COLOR.PURPLE, "---------------------------------------------")
    }

    private fun uselessFunctions() {
        val newMethods = mutableMapOf<String, MethodStruct>()
        val methodCallerMap = mutableMapOf<MethodNode, Int>()
        for (clazz in classes) {
            newMethods.clear()
            methodCallerMap.clear()
            if (!clazz.editable)
                continue

            // Genera un número aleatorio de funciones inútiles
            val randomFuncs = Random.nextInt(3, config.uselessFunctionsMaxNumber)

            for (i in 1..randomFuncs) {
                val newFuncPair = functionGenerator.generateUselessFunction()
                val newFunc = newFuncPair.first
                val params = newFuncPair.second
                methodCallerMap[newFunc] = params
            }

            val list = ArrayList<MethodNode>()

            methodCallerMap.keys.forEach { methodNode ->
                list.add(methodNode)
            }

            // Genera las llamadas a las funciones creadas
            for (i in 0 until list.size) {

                val insnList = InsnList()
                if (i + 1 < list.size) {
                    for (j in 0..methodCallerMap[list[i + 1]]!!) {
                        randomizeParameters(insnList)
                    }
                    insnList.add(
                        MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            clazz.obfName,
                            list[i + 1].name,
                            list[i + 1].desc,
                            false
                        )
                    )
                }
                insnList.add(
                    InsnNode(Opcodes.RETURN)
                )
                list[i].instructions.add(insnList)
            }

            // Inserta las funciones inútiles en puntos aleatorios de los métodos existentes
            for (methodStruct in clazz.methods.values) {
                val method = methodStruct.methodNode
                if (MethodType.isIllegalMethod(method.name))
                    continue
                method.instructions.forEach { abstractInsnNode ->
                    if (isValidInst(abstractInsnNode, method) && probability(config.uselessFunctionsProbability)) {
                        val m = methodCallerMap.keys.random()
                        val labelNode = LabelNode(Label())
                        val insnList = InsnList()
                        insnList.add(labelNode)

                        for (i in 0..methodCallerMap[m]!!) {
                            randomizeParameters(insnList)
                        }
                        insnList.add(
                            MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                clazz.obfName,
                                m.name,
                                m.desc,
                                false
                            )
                        )
                        method.instructions.insertBefore(abstractInsnNode, insnList)
                    }
                }
            }

            // Añade los nuevos métodos generados a la clase
            methodCallerMap.keys.forEach { methodNode ->
                newMethods[methodNode.name] = MethodStruct(methodNode)
            }

            newMethods.values.forEach { methodStruct ->
                clazz.classNode.methods.add(methodStruct.methodNode)
            }
            clazz.methods.putAll(newMethods)

        }

    }

    // Añade instrucciones aleatorias para los parámetros del método
    private fun randomizeParameters(insnList: InsnList) {
        val randomOption = Random
        when (randomOption.nextInt(0, 2)) {
            0 -> insnList.add(LdcInsnNode(Random.nextInt(-64, 64)))
            1 -> {
                insnList.add(LdcInsnNode(randomGen.generateRandomString()))
                insnList.add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I", false))
            }
        }
    }

    // Verifica si una instrucción es válida para insertar código basura antes de ella
    private fun isValidInst(abstractInsnNode: AbstractInsnNode, methodNode: MethodNode): Boolean {
        // TODO ADD MORE INSTRUCTIONS
        return abstractInsnNode == methodNode.instructions.first
    }

}