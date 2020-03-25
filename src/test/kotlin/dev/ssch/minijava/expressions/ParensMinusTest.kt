package dev.ssch.minijava.expressions

import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions
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
        Assertions.assertThat(output.lines()).containsExactly("1", "1", "-1", "-1", "1", "-1", "-3", "")
    }
}