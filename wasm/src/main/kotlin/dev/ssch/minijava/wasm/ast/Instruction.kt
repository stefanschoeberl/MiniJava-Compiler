package dev.ssch.minijava.wasm.ast

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

    class local_tee (
        val address: Int
    ) : Instruction()

    class br (
        val label: Int
    ) : Instruction()

    class br_if (
        val label: Int
    ) : Instruction()

    object i32_add : Instruction()
    object i32_sub : Instruction()
    object i32_mul : Instruction()
    object i32_div_s : Instruction()

    object f32_add : Instruction()
    object f32_sub : Instruction()
    object f32_mul : Instruction()
    object f32_div : Instruction()

    object i32_eq : Instruction()
    object i32_ne : Instruction()

    object f32_eq : Instruction()
    object f32_ne : Instruction()

    object i32_lt_s : Instruction()
    object i32_le_s : Instruction()
    object i32_gt_s : Instruction()
    object i32_ge_s : Instruction()

    object f32_lt : Instruction()
    object f32_le : Instruction()
    object f32_gt : Instruction()
    object f32_ge : Instruction()

    object i32_eqz : Instruction()

    object i32_and : Instruction()
    object i32_or : Instruction()

    object _if : Instruction()
    object _else: Instruction()
    object end: Instruction()

    object block: Instruction()
    object loop: Instruction()
    object drop: Instruction()
    object _return: Instruction()
    object unreachable: Instruction()

    object f32_convert_i32_s : Instruction()
    object i32_trunc_f32_s : Instruction()

    object i32_store: Instruction()
    object f32_store: Instruction()
    object i32_store8: Instruction()

    object i32_load: Instruction()
    object f32_load: Instruction()
    object i32_load8_s: Instruction()
}

