package dev.ssch.minijava.ast

import jdk.internal.org.objectweb.asm.commons.InstructionAdapter

sealed class Instruction {
    class i32_const (
        val value: Int
    ) : Instruction()

    class call (
        val address: Int
    ) : Instruction()

    class local_set (
        val address: Int
    ) : Instruction()

    class local_get (
        val address: Int
    ) : Instruction()

    class i32_add : Instruction()
    class i32_sub : Instruction()
    class i32_mul : Instruction()
    class i32_div_s : Instruction()

    class i32_eq : Instruction()
    class i32_ne : Instruction()

    class i32_and : Instruction()
    class i32_or : Instruction()

    class _if : Instruction()
    class _else: Instruction()
    class end: Instruction()
}

