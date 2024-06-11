package me.ciruu.obfuscator.processor.renamer

import me.ciruu.obfuscator.config.Config
import me.ciruu.obfuscator.processor.Process
import me.ciruu.obfuscator.structure.ClassStruct
import me.ciruu.obfuscator.structure.MethodStruct
import me.ciruu.obfuscator.utils.MethodType.isMainMethod
import me.ciruu.obfuscator.utils.MethodType.isIllegalMethod
import me.ciruu.obfuscator.utils.RandomGen
import me.ciruu.obfuscator.utils.Util
import me.ciruu.obfuscator.utils.Util.announce
import me.ciruu.obfuscator.utils.Util.info
import me.ciruu.obfuscator.utils.Util.key
import org.objectweb.asm.Handle
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import java.util.*

class Renamer(config: Config,
    classes: List<ClassStruct>,
    private val classStructs: MutableMap<String, ClassStruct>,
    private val mainClassName: String,
    private val randomGen: RandomGen
) : Process(config, classes) {

    private val CLASS_REF_REGEX = """^[^./()\s%\]]+(\.[^./()\s%\]]+)+$""".toRegex()
    private val STRING_REF_REGEX = """^[^./()\s%]+(/[^./()\s%]+)+$""".toRegex()
    private val STRING_REF2_REGEX = """^[^\[./()\s%][^./()\s%]+(\.[^./()\s%]+)+$""".toRegex()

    override fun process() {

        if (!config.renamer)
            return

        announce(Util.COLOR.PURPLE, "---------------------------------------------")
        announce(Util.COLOR.PURPLE, "Renamer process...")

        // Generate obfuscated names
        info("Generating obfuscated names...")
        generateObfuscatedNames()

        // Rename classes with the new name
        info("Renaming classes...")
        renameWithObfName()

        announce(Util.COLOR.PURPLE, "Done!")
        announce(Util.COLOR.PURPLE, "---------------------------------------------")
    }

    private fun generateObfuscatedNames() {
        for (clazz in classes) {
            if (!clazz.editable) continue

            if (config.classRenamer) {
                val obfName = if (config.keepMainMethodName && clazz.name == mainClassName) {
                    clazz.name.substringAfterLast("/")
                } else {
                    randomGen.generateRandomString() // Generamos el nombre ofuscado
                }

                val pkg = when {
                    config.keepMainMethodName && clazz.name == mainClassName -> clazz.path
                    config.packageRenamer -> config.packageName
                    else -> clazz.path
                }
                clazz.obfName = "${pkg.replace(".", "/")}/$obfName"
            }

            if (config.methodRenamer) {
                for (method in clazz.methods.values) {
                    // Ignore illegal methods and constructors
                    if (isIllegalMethod(method.methodNode.name)) continue

                    // Check for method overrides, they must match the original method name
                    if (clazz.dependencies.isNotEmpty()) {
                        // If an override is found we use it's obfName, but
                        // if it's not found we cannot change the original name
                        val overrideMethod = findOverrideMethod(method, clazz) ?: continue

                        if (overrideMethod === method) {
                            method.obfName = randomGen.generateRandomString()
                        } else {
                            method.obfName = overrideMethod.obfName
                        }
                        continue
                    }

                    // Generate obf method name
                    method.obfName = if (config.keepMainMethodName && isMainMethod(method.methodNode))
                        method.name
                    else
                        randomGen.generateRandomString()
                }
            }

            if (config.fieldRenamer) {
                for (field in clazz.fields.values) {
                    field.obfName = randomGen.generateRandomString()
                }
            }
        }
    }

    private fun renameWithObfName() {
        for (clazz in classes) {
            if (!clazz.editable) {
                continue
            }

            clazz.modified = true
            val classNode = clazz.classNode
            classNode.name = clazz.obfName

            // Renaming class annotations
            classNode.visibleAnnotations?.let { annotations ->
                if (config.removeKotlinMetadata) {
                    annotations.removeAll { it.desc == "Lkotlin/Metadata;" }
                }
                annotations.forEach { annotation ->
                    changeAnnotationNames(annotation)
                }
            }

            // Renaming superclass and interfaces
            classNode.superName = mapClassName(classNode.superName)
            classNode.interfaces = classNode.interfaces.map(::mapClassName)

            // Removing outer method and outer method desc
            classNode.outerMethod = null
            classNode.outerMethodDesc = null

            // Renaming inner classes
            classNode.innerClasses?.forEach { inner ->
                inner.name = mapInternalName(inner.name)
                inner.outerName = mapClassName(inner.outerName)

                val innerName = inner.name.substringAfterLast('/')
                inner.innerName = if ("$" in innerName) innerName.substringAfterLast('$') else innerName
            }

            // Renaming method names and descriptors
            clazz.methods.forEach { (_, method) ->
                val methodNode = method.methodNode
                methodNode.name = method.obfName

                // Renaming method parameter names
                if (config.localVariableRenamer) {
                    methodNode.localVariables?.forEach { variable ->
                        if (variable.name != "this") {
                            val obfName = randomGen.generateRandomString()
                            method.locals[variable.name] = obfName
                            variable.name = obfName
                        }
                        variable.desc = mapDescriptor(variable.desc)
                    }
                }

                // Renaming method annotations and descriptors
                methodNode.annotationDefault?.let { defaultAnnotationValue ->
                    if (defaultAnnotationValue is Array<*>) {
                        @Suppress("UNCHECKED_CAST")
                        defaultAnnotationValue as Array<String>
                        defaultAnnotationValue[0] = mapDescriptor(defaultAnnotationValue[0])
                    }
                }

                methodNode.desc = mapDescriptor(method.desc)

                // Renaming instructions inside method
                changeInstructionNames(methodNode.instructions)
            }

            // Renaming field names and descriptors
            clazz.fields.forEach { (_, field) ->
                val fieldNode = field.fieldNode
                fieldNode.name = field.obfName
                fieldNode.desc = mapDescriptor(field.desc)
            }
        }
    }

    private fun findOverrideMethod(method: MethodStruct, ownerClass: ClassStruct): MethodStruct? {
        val methodKey = key(method.name, method.desc)
        var missingData = false

        val queue = ArrayDeque<String>()
        val visited = mutableSetOf<String>()

        queue.addAll(ownerClass.dependencies)
        visited.addAll(ownerClass.dependencies)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val classFile = classStructs[current]

            if (classFile == null) {
                missingData = true
                continue
            }

            val nextDependencies = classFile.dependencies.filter { !visited.contains(it) }
            queue.addAll(nextDependencies)
            visited.addAll(nextDependencies)

            if (methodKey in classFile.methods) {
                return classFile.methods[methodKey]
            }
        }

        return if (missingData) null else method
    }


    private fun mapClassName(name: String?): String? {
        return name?.let { className ->
            classStructs[className]?.obfName ?: className
        }
    }

    private fun changeAnnotationNames(annotationNode: AnnotationNode) {
        // Se mapea el nombre de la anotación
        annotationNode.desc = mapDescriptor(annotationNode.desc)

        // Se comprueba si hay valores en la anotación
        annotationNode.values?.let { values ->
            // Se divide la lista de valores en pares clave-valor y se itera sobre ellos
            values.chunked(2).forEachIndexed { index, (_, value) ->
                // Se comprueba si el valor es una cadena
                val newValue = when (value) {
                    is String -> {
                        // Si el valor contiene una referencia de clase, se mapea el nombre interno de la clase
                        if (value.contains(CLASS_REF_REGEX)) {
                            mapInternalName(value.replace(".", "/")).replace("/", ".")
                        } else {
                            value
                        }
                    }
                    // Si el valor es una matriz, se itera sobre sus elementos y se mapean los nombres de clase y tipo
                    is Array<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        val valueArray = value as Array<String>
                        valueArray[0] = mapDescriptor(valueArray[0])
                        valueArray
                    }
                    // Si el valor no es una cadena ni una matriz, se devuelve el valor sin cambios
                    else -> value
                }
                // Se actualiza el valor en la lista de valores
                values[index * 2 + 1] = newValue
            }
            // Se actualizan los valores de la anotación con los nuevos valores
            annotationNode.values = values
        }
    }


    private fun mapMethodName(owner: String, name: String, desc: String): String {
        val className = when {
            owner.startsWith("[") -> Type.getType(owner).elementType.internalName
            owner.startsWith("(") -> Type.getType(owner).returnType.internalName
            else -> if (isInternalName(owner)) owner else Type.getType(owner).internalName
        }

        val key = key(name, desc)

        return findDependencies(className)
            .asSequence()
            .mapNotNull { it.methods[key] }
            .firstOrNull()
            ?.obfName ?: name
    }

    private fun mapFieldName(owner: String, name: String, desc: String): String {
        // Extract the internal name of the class containing the field
        val className = if (owner.startsWith("[")) {
            Type.getType(owner).elementType.internalName
        } else if (isInternalName(owner)) {
            owner
        } else {
            Type.getType(owner).internalName
        }

        // Create a key to look up the field by name and descriptor
        val key = key(name, desc)

        // Find dependencies of the class containing the field and look up the field by key
        val matchingDependency = findDependencies(className)
            .asSequence()
            .mapNotNull { it.fields[key]?.obfName }
            .firstOrNull()

        // Return the obfuscated name if found, otherwise the original name
        return matchingDependency ?: name
    }

    private fun findDependencies(className: String): List<ClassStruct> {
        val current: ClassStruct = classStructs[className] ?: return emptyList()
        return mutableListOf(current).apply {
            current.dependencies.forEach { dependency ->
                addAll(findDependencies(dependency))
            }
        }
    }

    // Ej. Ljava/lang/String;
    private fun mapInternalName(name: String): String {
        return mapTypeDescriptor(Type.getObjectType(name)).internalName
    }

    // Ej. [[I
    private fun mapDescriptor(desc: String): String {
        return mapTypeDescriptor(Type.getType(desc)).descriptor
    }

    private fun mapTypeDescriptor(ty: Type): Type {
        return when (ty.sort) {
            Type.OBJECT -> Type.getObjectType(mapClassName(ty.internalName))
            Type.ARRAY -> Type.getObjectType("[${mapDescriptor(ty.descriptor.substring(1))}")
            Type.METHOD -> {
                val args = ty.argumentTypes.map { mapTypeDescriptor(it) }
                val returnTy = mapTypeDescriptor(ty.returnType)
                Type.getMethodType(returnTy, *args.toTypedArray())
            }
            else -> ty
        }
    }

    private fun isInternalName(name: String): Boolean = name.isEmpty() || !setOf('[', 'V', 'Z', 'C', 'B', 'S', 'I', 'F', 'J', 'D', 'L', '(').contains(name[0])

    private fun changeInstructionNames(instructions: InsnList) {
        instructions.forEach { instruction ->
            when (instruction) {
                is MethodInsnNode -> {
                    instruction.name = mapMethodName(instruction.owner, instruction.name, instruction.desc)
                    instruction.owner = if (isInternalName(instruction.owner)) {
                        mapInternalName(instruction.owner)
                    } else {
                        mapDescriptor(instruction.owner)
                    }
                    instruction.desc = mapDescriptor(instruction.desc)
                }
                is InvokeDynamicInsnNode -> {
                    instruction.name = mapMethodName(instruction.desc, instruction.name, instruction.bsmArgs[0].toString())
                    instruction.desc = mapDescriptor(instruction.desc)
                    instruction.bsm = Handle(
                        instruction.bsm.tag,
                        mapClassName(instruction.bsm.owner),
                        instruction.bsm.name,
                        mapDescriptor(instruction.bsm.desc)
                    )
                    instruction.bsmArgs.forEachIndexed { index, argument ->
                        if (argument is Type) {
                            instruction.bsmArgs[index] = mapTypeDescriptor(argument)
                        }
                        if (argument is Handle) {
                            val methodName = mapMethodName(argument.owner, argument.name, argument.desc)
                            instruction.bsmArgs[index] = Handle(
                                argument.tag,
                                mapClassName(argument.owner),
                                methodName,
                                mapDescriptor(argument.desc)
                            )
                        }
                    }
                }
                is FieldInsnNode -> {
                    instruction.name = mapFieldName(instruction.owner, instruction.name, instruction.desc)
                    instruction.owner = if (isInternalName(instruction.owner)) {
                        mapInternalName(instruction.owner)
                    } else {
                        mapDescriptor(instruction.owner)
                    }
                    instruction.desc = mapDescriptor(instruction.desc)
                }
                is TypeInsnNode -> {
                    instruction.desc = if (isInternalName(instruction.desc)) {
                        mapInternalName(instruction.desc)
                    } else {
                        mapDescriptor(instruction.desc)
                    }
                }
                is LdcInsnNode -> {
                    val constant = instruction.cst
                    when {
                        constant is Type -> {
                            instruction.cst = mapTypeDescriptor(constant)
                        }
                        constant is String && constant.contains(STRING_REF_REGEX) -> {
                            instruction.cst = mapInternalName(constant)
                        }
                        constant is String && constant.contains(STRING_REF2_REGEX) -> {
                            val base = constant.replace(".", "/")
                            val newValue = mapInternalName(base)
                            if (base != newValue) {
                                instruction.cst = newValue.replace("/", ".")
                            }
                        }
                    }
                }
                is FrameNode -> {
                    instruction.local = instruction.local?.map {
                        if (it is String) {
                            if (isInternalName(it)) {
                                mapInternalName(it)
                            } else {
                                mapDescriptor(it)
                            }
                        } else {
                            it
                        }
                    }
                    instruction.stack = instruction.stack?.map {
                        if (it is String) {
                            if (isInternalName(it)) {
                                mapInternalName(it)
                            } else {
                                mapDescriptor(it)
                            }
                        } else {
                            it
                        }
                    }
                }
            }
        }
    }
}