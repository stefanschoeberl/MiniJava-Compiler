package dev.ssch.minijava.expressions

import dev.ssch.minijava.util.CompilerTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class MixedExpressionsTest : CompilerTest() {

    @Test
    fun `arithmetic expressions`() {
        val output = """
            println((2 + 3) * 4);
            println((2 + 3) * -4);
            println((2 + 3) * 4 / 9);
            println(1 - (2 + 3) * 4 + 5);
            println(1 - (-2 + 3) * 4 + 5);
            println(30 / (1 - (2 + 3) * 4 + 5));
            println(30 / (1 - (-2 + 3) * 4 + -5));
            println(30 / 1 - 2 + 3 * 4 + 5);
            println(-30 / 1 - 2 + 3 * -4 + -5);
        """.run()
        Assertions.assertThat(output.lines()).containsExactly("20", "-20", "2", "-14", "2", "-2", "-3", "45", "-49", "")
    }
}