package dev.ssch.minijava.ast

sealed class Instruction {
    class i32_const (
        val value: Int
    ) : Instruction()

    class f32_const (
        val value: Float
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

    class br (
        val label: Int
    ) : Instruction()

    class br_if (
        val label: Int
    ) : Instruction()

    class i32_add : Instruction()
    class i32_sub : Instruction()
    class i32_mul : Instruction()
    class i32_div_s : Instruction()

    class i32_eq : Instruction()
    class i32_ne : Instruction()

    class i32_lt_s : Instruction()
    class i32_le_s : Instruction()
    class i32_gt_s : Instruction()
    class i32_ge_s : Instruction()

    class i32_eqz : Instruction()

    class i32_and : Instruction()
    class i32_or : Instruction()

    class _if : Instruction()
    class _else: Instruction()
    class end: Instruction()

    class block: Instruction()
    class loop: Instruction()
    class drop: Instruction()
    class _return: Instruction()
    class unreachable: Instruction()

    class f32_convert_i32_s : Instruction()
}

