package dev.ssch.minijava.expressions

import dev.ssch.minijava.util.CompilerTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class ParensMinusTest : CompilerTest() {

    @Test
    fun `arithmetic expressions`() {
        val output = """
            println(((1)));
            println((1));
            println(-(1));
            println((-1));
            println(-(-1));
            println(-(-(-1)));
            println(-(1+2));
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("1", "1", "-1", "-1", "1", "-1", "-3", "")
    }
}