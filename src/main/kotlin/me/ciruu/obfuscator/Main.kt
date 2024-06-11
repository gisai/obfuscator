package me.ciruu.obfuscator

import com.google.gson.Gson
import me.ciruu.obfuscator.config.Config
import me.ciruu.obfuscator.utils.Util
import me.ciruu.obfuscator.utils.Util.announce
import me.ciruu.obfuscator.utils.Util.error
import java.io.File

lateinit var config: Config
fun main(args: Array<String>) {

    announce(Util.COLOR.CYAN,"Program arguments: ${args.joinToString()}")

    if (args[0].isEmpty()) {
        error("NO JSON CONFIGURATION FILE SPECIFIED!")
        return
    }

    if (!args[0].endsWith(".json")) {
        error("TYPE MISMATCH!")
        return
    }

    val jsonFileName = args[0]


    // Create a Gson object
    val gson = Gson()

    val configFile = File(System.getProperty("user.dir") + File.separator + jsonFileName)

    println(configFile.absoluteFile)

    val json = configFile.readText()

    // Deserialize JSON into Config data class
    config = gson.fromJson(json, Config::class.java)
    val fileName = config.fileName

    // Get all jar files into a list
    val files = findFiles(fileName)

    // Obfuscate every file
    files.forEach{ file -> Obfuscator(file, File(file.path.removeSuffix(file.name) + config.outputFileName), config).run() }
}


/**
 * @return all jar files in the current working directory
 */
fun findFiles(fileName: String): List<File> {
    // Get current directory
    val path = File(System.getProperty("user.dir"))

    // Add all jar files excluding this one to a list
    return path.walk().filter {
        it.name.equals(fileName)
    }.toList()
}
