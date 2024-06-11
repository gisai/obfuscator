package me.ciruu.obfuscator.utils

object MathUtil {
    fun probability(n: Int): Boolean {
        require(n in 0..100) { "Useless function probability must be from 0 to 100" }
        return (0..99).random() < n
    }
}