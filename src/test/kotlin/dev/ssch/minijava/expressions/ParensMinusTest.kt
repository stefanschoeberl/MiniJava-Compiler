package dev.ssch.minijava.expressions

import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ParensMinusTest : CompilerTest() {

    @Test
    fun `arithmetic expressions`() {
        val output = """
            Console.println(((1)));
            Console.println((1));
            Console.println(-(1));
            Console.println((-1));
            Console.println(-(-1));
            Console.println(-(-(-1)));
            Console.println(-(1+2));
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("1", "1", "-1", "-1", "1", "-1", "-3", "")
    }
}