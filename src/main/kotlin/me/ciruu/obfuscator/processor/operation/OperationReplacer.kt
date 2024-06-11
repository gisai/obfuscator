package me.ciruu.obfuscator.processor.operation

import me.ciruu.obfuscator.config.Config
import me.ciruu.obfuscator.processor.Process
import me.ciruu.obfuscator.structure.ClassStruct
import me.ciruu.obfuscator.structure.MethodStruct
import me.ciruu.obfuscator.utils.OpcodeUtil
import me.ciruu.obfuscator.utils.OperationCodeUtil.isDoubleOp
import me.ciruu.obfuscator.utils.OperationCodeUtil.isFloatOp
import me.ciruu.obfuscator.utils.OperationCodeUtil.isIntOp
import me.ciruu.obfuscator.utils.OperationCodeUtil.isLongOp
import me.ciruu.obfuscator.utils.RandomGen
import me.ciruu.obfuscator.utils.Util
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode

class OperationReplacer(
    config: Config,
    classes: List<ClassStruct>,
    randomGen: RandomGen
): Process(config, classes) {

    private val operationBuilder = OperationBuilder(randomGen)
    override fun process() {
        if (!config.operationReplacer)
            return

        Util.announce(Util.COLOR.PURPLE, "---------------------------------------------")
        Util.announce(Util.COLOR.PURPLE, "Operation replacer process...")

        operationReplacer()

        Util.announce(Util.COLOR.PURPLE, "Done!")
        Util.announce(Util.COLOR.PURPLE, "---------------------------------------------")
    }

    private fun operationReplacer() {
        val newOpMethods = mutableMapOf<String, MethodStruct>()
        for (clazz in classes) {
            if (!clazz.editable) continue
            clazz.modified = true
            newOpMethods.clear()
            for (method in clazz.methods.values) {
                for (inst in method.methodNode.instructions) {
                    var methodNode: MethodNode? = null
                    if (isIntOp(inst.opcode)) {
                        methodNode = operationBuilder.generateIntMethod(inst.opcode)
                    }
                    if (isLongOp(inst.opcode)) {
                        methodNode = operationBuilder.generateLongMethod(inst.opcode)
                    }
                    if (isFloatOp(inst.opcode)) {
                        methodNode = operationBuilder.generateFloatMethod(inst.opcode)
                    }
                    if (isDoubleOp(inst.opcode)) {
                        methodNode = operationBuilder.generateDoubleMethod(inst.opcode)
                    }
                    if (methodNode != null)
                        replaceMethod(methodNode, clazz.classNode, inst, newOpMethods, inst.opcode)
                }
            }
            newOpMethods.values.forEach { methodStruct ->
                clazz.classNode.methods.add(methodStruct.methodNode)
            }
            clazz.methods.putAll(newOpMethods)
        }

        if (config.transformAfterReplace)
            OperationTransformer(config, classes).process()
    }

    private fun replaceMethod(method: MethodNode, classNode: ClassNode, inst: AbstractInsnNode, newOpMethods: MutableMap<String, MethodStruct>, opcode: Int) {
        val methodStruct = MethodStruct(method)

        var methodNode = method

        if (!config.operationReplacerAggressive) {
            val index: String = OpcodeUtil.getOpCode(opcode) + method.desc


            if (newOpMethods[index] == null) {
                newOpMethods[index] = methodStruct
            }

            methodNode = newOpMethods[index]!!.methodNode
        }
        else{
            newOpMethods[method.name] = methodStruct
        }


        method.instructions.insert(
            inst,
            MethodInsnNode(
                INVOKESTATIC,
                classNode.name,
                methodNode.name,
                methodNode.desc,
                false
            )
        )
        method.instructions.remove(inst)
    }


}