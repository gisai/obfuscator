package me.ciruu.obfuscator

import me.ciruu.obfuscator.config.Config
import me.ciruu.obfuscator.processor.flowobfuscation.UselessFunctions
import me.ciruu.obfuscator.processor.flowobfuscation.ShuffleMembers
import me.ciruu.obfuscator.processor.info.RemoveInfo
import me.ciruu.obfuscator.processor.numberobfuscation.NumberObfuscation
import me.ciruu.obfuscator.processor.operation.OperationReplacer
import me.ciruu.obfuscator.processor.operation.OperationTransformer
import me.ciruu.obfuscator.processor.property.SyntheticMembers
import me.ciruu.obfuscator.processor.renamer.Renamer
import me.ciruu.obfuscator.processor.stringencoder.StringEncoder
import me.ciruu.obfuscator.structure.ClassStruct
import me.ciruu.obfuscator.structure.FieldStruct
import me.ciruu.obfuscator.structure.MethodStruct
import me.ciruu.obfuscator.utils.RandomGen
import me.ciruu.obfuscator.utils.Util
import me.ciruu.obfuscator.utils.Util.announce
import me.ciruu.obfuscator.utils.Util.info
import me.ciruu.obfuscator.utils.Util.key
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.lang.Exception
import java.time.Instant
import java.util.jar.JarInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class Obfuscator(
    private val inputFile: File,
    private val outputFile: File = inputFile,
    private val config: Config,
    private val temporalFile: File = File.createTempFile("tmp", ".jar", inputFile.parentFile)
) {

    // Class map
    private val classStructs = mutableMapOf<String, ClassStruct>()
    private var classesList: List<ClassStruct> = ArrayList()

    // File fields
    private val tmpFolderStream = ZipOutputStream(temporalFile.outputStream())
    private val zip = ZipFile(inputFile)
    private val randomGen = RandomGen(config)

    fun run() {
        // Clear data
        info("Clearing memory")
        classStructs.clear()

        val startTime = Instant.now()

        info("Extracting classes")
        extractClasses()

        info("Obfuscating")
        obfuscate()

        if (config.debug) {
            for (clazz in classStructs.values) {
                if (!clazz.editable)
                    continue
                announce(Util.COLOR.PURPLE, "CLASS : ${Util.COLOR.CYAN} ${clazz.name}")
                announce(Util.COLOR.PURPLE, "-----------------------------------")
                for (method in clazz.methods.values) {
                    announce(Util.COLOR.PURPLE, "METHOD : ${Util.COLOR.CYAN} ${method.name}: ${method.desc}")
                }
                announce(Util.COLOR.CYAN, "-----------------------------------")
                for (field in clazz.fields.values) {
                    announce(Util.COLOR.PURPLE, "FIELD : ${Util.COLOR.CYAN} ${field.name}: ${field.desc}")
                }
                announce(Util.COLOR.CYAN, "-----------------------------------")

            }
        }

        info("Saving classes to file: ${outputFile.path}")
        saveFiles()

        val endTime = Instant.now()

        info("Finished in ${endTime.toEpochMilli() - startTime.toEpochMilli()} ms")

    }

    private fun obfuscate() {
        classesList = sortClasses()
        Renamer(config, classesList, classStructs, getNameFromManifest(inputFile).removeSuffix(".class"), randomGen).process()
        StringEncoder(config, classesList, randomGen).process()
        RemoveInfo(config, classesList).process()
        UselessFunctions(config, classesList, randomGen).process()
        NumberObfuscation(config, classesList).process()
        OperationTransformer(config, classesList).process()
        OperationReplacer(config, classesList, randomGen).process()
        ShuffleMembers(config, classesList).process()
        SyntheticMembers(config, classesList).process()
    }

    private fun extractClasses() {
        val entries = zip.entries().toList()
        entries.forEach { zipEntry ->
            val inputStream = zip.getInputStream(zipEntry)
            if (zipEntry.name.endsWith(".class")) {
                val node = ClassNode()

                // From bytes to class node that can be modified
                val oldBytes = inputStream.readBytes()
                val reader = ClassReader(oldBytes)
                reader.accept(node, 0)

                // Create our classStructs from classNodes
                mapClass(node, zipEntry.name)

            } else {
                // Keep original file
                tmpFolderStream.putNextEntry(zipEntry)
                inputStream.copyTo(tmpFolderStream)
            }
        }
    }

    private fun saveFiles() {
        classStructs.values.forEach { classStruct ->
            // Convert back to bytes
            val writer = ClassWriter(ClassWriter.COMPUTE_MAXS)
            try {
                classStruct.classNode.accept(writer)
            }
            catch (e: TypeNotPresentException) {
                announce(Util.COLOR.BLUE, classStruct.classNode.name)
                e.printStackTrace()
                classStruct.classNode.accept(writer)
            }

            val newBytes = writer.toByteArray()

            // Write bytes to the jar entry
            val clone = ZipEntry(classStruct.obfName + ".class")
            tmpFolderStream.putNextEntry(clone)
            tmpFolderStream.write(newBytes)
        }

        // Close streams to flush content to disk
        zip.close()
        tmpFolderStream.close()

        // Replace original file with modified file
        temporalFile.copyTo(outputFile, overwrite = true)
        temporalFile.delete()
    }

    private fun mapClass(classNode: ClassNode, pathName: String) {
        val editable = config.validPaths.any { packageName -> pathName.replace('/', '.').startsWith(packageName) }
        val classStruct = ClassStruct(classNode, editable)
        classStruct.path = classStruct.name.substringBeforeLast("/")
        classStructs[classStruct.name] = classStruct

        mapMethods(classStruct)
        mapFields(classStruct)
    }

    private fun mapMethods(classStruct: ClassStruct) {
        classStruct.classNode.methods.forEach { method ->
            val m = MethodStruct(method)
            classStruct.methods[key(m.name, m.desc)] = m
        }
    }

    private fun mapFields(classStruct: ClassStruct) {
        classStruct.classNode.fields.forEach { field ->
            classStruct.fields[key(field.name, field.desc)] = FieldStruct(field)
        }
    }

    private fun getNameFromManifest(entry: File): String {
        val jarStream = JarInputStream(entry.inputStream())
        val attributes: String = try {
            jarStream.manifest.mainAttributes.getValue("Main-Class")
        } catch (e: Exception) {
            config.mainClassPath
        }
        jarStream.close()
        // Path to the entry in the jar file
        return attributes.replace(".", "/") + ".class"
    }

    private fun sortClasses(): List<ClassStruct> {
        // Sort classes by dependencies
        val result = mutableListOf<ClassStruct>()
        val remainingClasses = classStructs.toMutableMap()

        while (remainingClasses.isNotEmpty()) {
            val processableClasses = remainingClasses.filter { (_, v) -> v.dependencies.all { it !in remainingClasses } }

            if (processableClasses.isEmpty()) {
                val cyclicDependencies = remainingClasses.keys.joinToString(", ")
                throw IllegalStateException("Cyclic dependencies detected: $cyclicDependencies")
            }

            processableClasses.values.forEach { classStruct ->
                result += classStruct
                remainingClasses.remove(classStruct.name)
            }
        }

        return result
    }

}