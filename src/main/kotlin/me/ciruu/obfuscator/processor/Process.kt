package me.ciruu.obfuscator.processor

import me.ciruu.obfuscator.config.Config
import me.ciruu.obfuscator.structure.ClassStruct
import org.objectweb.asm.tree.ClassNode

abstract class Process(val config: Config, val classes: List<ClassStruct>): IProcess