package me.ciruu.obfuscator.utils

object Util {

    fun announce(color: COLOR, text: String) {
        println(color.cod + text + COLOR.RESET.cod)
    }

    fun error(text: String) {
        println(COLOR.RED.cod + text + COLOR.RESET.cod)
    }

    fun warning(text: String) {
        println(COLOR.YELLOW.cod + text + COLOR.RESET.cod)
    }

    fun info(text: String) {
        println(COLOR.GREEN.cod + text + COLOR.RESET.cod)
    }

    enum class COLOR(val cod: String) {
        BLACK("\u001b[30m"),
        RED("\u001b[31m"),
        GREEN("\u001b[32m"),
        YELLOW("\u001b[33m"),
        BLUE("\u001b[34m"),
        PURPLE("\u001b[35m"),
        CYAN("\u001b[36m"),
        WHITE("\u001b[37m"),
        RESET("\u001b[0m");

        override fun toString(): String {
            return cod
        }
    }

    fun key(name: String, desc: String) = "$name: $desc"
}