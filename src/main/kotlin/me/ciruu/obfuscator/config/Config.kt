package me.ciruu.obfuscator.config

import com.google.gson.annotations.SerializedName

data class Config(
    // Debug
    @SerializedName("debug") val debug: Boolean,

    // Filename
    @SerializedName("fileName") val fileName: String,

    // Output file name
    @SerializedName("outputFileName") val outputFileName: String,

    // Packages allowed to be obfuscated
    @SerializedName("validPaths") val validPaths: MutableSet<String>,

    // Renamer
    @SerializedName("renamer") val renamer: Boolean,
        // Package renamer
        @SerializedName("packageRenamer") val packageRenamer: Boolean,
        // Package name
        @SerializedName("packageName") val packageName: String,
        // Class renamer
        @SerializedName("classRenamer") val classRenamer: Boolean,
        // Method renamer
        @SerializedName("methodRenamer") val methodRenamer: Boolean,
        // Field renamer
        @SerializedName("fieldRenamer") val fieldRenamer: Boolean,
        // Local variable renamer
        @SerializedName("localVariableRenamer") val localVariableRenamer: Boolean,
        // String randomizer config
        @SerializedName("stringRandomizerType") val stringRandomizerType: StringRandomizerType,
        @SerializedName("stringRandomizerMaxChars") val stringRandomizerMaxChars: Int,
        @SerializedName("stringRandomizerMinChars") val stringRandomizerMinChars: Int,
        // Remove kotlin metadata
        @SerializedName("removeKotlinMetadata") val removeKotlinMetadata: Boolean,
        // Keep main method name
        @SerializedName("keepMainMethodName") val keepMainMethodName: Boolean,
        // Keep main method name
        @SerializedName("mainClassPath") val mainClassPath: String,

    // String encoder
    @SerializedName("encodeStrings") val encodeStrings: Boolean,
        // String encoder type
        @SerializedName("encodeType") val encodeType: EncodeType,

    // Shuffle members
    @SerializedName("shuffleMembers") val shuffleMembers: Boolean,

    // Synthetic members
    @SerializedName("removeInfo") val removeInfo: Boolean,

    // Synthetic members
    @SerializedName("syntheticMembers") val syntheticMembers: Boolean,

    // Number obfuscation
    @SerializedName("numberObfuscation") val numberObfuscation: Boolean,
        // Number obfuscation passes
        @SerializedName("numberObfuscationPasses") val numberObfuscationPasses: Int,
        // Number obfuscation type
        @SerializedName("numberObfuscationType") val numberObfuscationType: NumberObfType,

    // Number obfuscation
    @SerializedName("uselessTryCatch") val uselessTryCatch: Boolean,

    // Operation replacer
    @SerializedName("operationReplacer") val operationReplacer: Boolean,
        // Operation replacer aggressive, WARNING: MAY CONSUME A LOT OF MEMORY
        @SerializedName("operationReplacerAggressive") val operationReplacerAggressive: Boolean,
        // Operation replacer aggressive, WARNING: MAY CONSUME A LOT OF MEMORY
        @SerializedName("transformAfterReplace") val transformAfterReplace: Boolean,

    // Operation transformer
    @SerializedName("operationTransformer") val operationTransformer: Boolean,
        // Operation transformer
        @SerializedName("operationTransformerPasses") val operationTransformerPasses: Int,

    // Add useless instructions
    @SerializedName("uselessFunctions") val uselessFunctions: Boolean,
        // Useless instructions probability
        @SerializedName("uselessFunctionsProbability") val uselessFunctionsProbability: Int,
        // Max number of useless instructions
        @SerializedName("uselessFunctionsMaxNumber") val uselessFunctionsMaxNumber: Int,

    ) {
    enum class StringRandomizerType {
        RANDOM, IJL1, CRASHER, LETTER, SEQUENCE, EMOJI, RARE
    }

    enum class EncodeType {
        BASE64, XOR
    }

    enum class NumberObfType {
        XOR
    }
}