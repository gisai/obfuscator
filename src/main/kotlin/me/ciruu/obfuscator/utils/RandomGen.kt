package me.ciruu.obfuscator.utils

import me.ciruu.obfuscator.config
import me.ciruu.obfuscator.config.Config
import kotlin.random.Random

val ijl1I_ARRAY = "il1Ij"
val RANDOM_ARRAY = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!@#%*1234567890"
val CRASHER_ARRAY = "!@#%*"
val LETTER_ARRAY = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
val EMOJI_ARRAY = "☺️ ☹️ ☠️ ✋ ✌️ ☝️ ✊ ✍️ ❤️ ♂️ ♀️ ⚖️ ✈️ ⚕️ ⛑ ☂️ ☘️ ☔️ ☂️ ❄️ ☃️ ⛄️ ⛈ ☀️ ⭐️ ✨ ⚡️ ☄️ ⛅️ ☁️ ❣️ ✝️ ⛎ ♈️ ♉️ ♊️ ♋️ ♌️ ♍️ ♎️ ♏️ ♐️ ♑️ ♒️ ♓️ ☢️ ☣️ ❗️ ❕ ❓ ❔ ‼️ ⁉️ 0️⃣ 1️⃣ 2️⃣ 3️⃣ 4️⃣ 5️⃣ 6️⃣ 7️⃣ 8️⃣ 9️⃣ #️⃣ *️⃣ ⏏️ ▶️ ⏸ ⏯ ⏹ ⏺ ⏭ ⏮ ⏩ ⏪ ⏫ ⏬ ◀️ ➡️ ⬅️ ⬆️ ⬇️ ↗️ ↘️ ↙️ ↖️ ↕️ ↔️ ↪️ ↩️ ⤴️ ⤵️ ➕ ➖ ➗ ✖️ ™️ ©️ ®️ 〰️ ➰ ➿ ▪️ ▫️ ◾️ ◽️ ◼️ ◻️ ♠️ ♣️ ♥️ ♦️"

class RandomGen(config: Config) {

    private var sequenceCount = 0
    private val maxChars = config.stringRandomizerMaxChars
    private val minChars = config.stringRandomizerMinChars
    fun generateRandomString(): String {
        if (config.stringRandomizerType == Config.StringRandomizerType.SEQUENCE) {
            return generateSequence()
        }
        var string = randomChar()
        for (i in 1..(minChars..maxChars).random()) {
            string += randomChar()
        }
        return string
    }

    private fun randomChar(): String {
        return when (config.stringRandomizerType) {
            Config.StringRandomizerType.RANDOM -> RANDOM_ARRAY[Random.nextInt(RANDOM_ARRAY.length)].toString()
            Config.StringRandomizerType.IJL1 -> ijl1I_ARRAY[Random.nextInt(ijl1I_ARRAY.length)].toString()
            Config.StringRandomizerType.CRASHER -> CRASHER_ARRAY[Random.nextInt(CRASHER_ARRAY.length)].toString()
            Config.StringRandomizerType.LETTER -> LETTER_ARRAY[Random.nextInt(LETTER_ARRAY.length)].toString()
            Config.StringRandomizerType.EMOJI -> selectRandomEmoji().toString()
            Config.StringRandomizerType.RARE -> generateRandomChar('ͣ', '᨟').toString()
            else -> ""
        }
    }

    private fun selectRandomEmoji(): Char {
        val emojis = EMOJI_ARRAY.split(" ")
        var randomIndex: Int
        var selectedEmoji: String

        do {
            randomIndex = (emojis.indices).random()
            selectedEmoji = emojis[randomIndex]
        } while (selectedEmoji.isEmpty())

        return selectedEmoji[0]
    }

    private fun generateRandomChar(start: Char, end: Char): Char {
        require(start <= end) { "El primer parámetro debe ser menor o igual que el segundo" }
        val randomInt = (start.code..end.code).random()
        return randomInt.toChar()
    }

    private fun generateSequence(): String {
        val sb = StringBuilder()
        var n = sequenceCount

        while (n >= 0) {
            val r = n % 52
            sb.append(if (r < 26) 'a' + r else 'A' + r - 26)
            n = (n - r) / 52 - 1
        }

        sequenceCount++
        return sb.reverse().toString()
    }

}