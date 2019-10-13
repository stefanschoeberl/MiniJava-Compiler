package dev.ssch.minijava.expressions

import dev.ssch.minijava.util.CompilerTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class MixedExpressionsTest : CompilerTest() {

    @Test
    fun `simple mixed expressions 1`() {
        val output = """
            int a;
            int b;
            a = 2;
            b = 3;
            int c;
            c = a + b;
            println(a);
            println(b);
            println(c);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("2", "3", "5", "")
    }

    @Test
    fun `simple mixed expressions 2`() {
        val output = """
            int a;
            int b;
            a = 2;
            b = 3;
            int c;
            c = b - a;
            println(a);
            println(b);
            println(c);
            c = a - b;
            println(c);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("2", "3", "1", "-1", "")
    }

    @Test
    fun `simple mixed expressions 3`() {
        val output = """
            int a;
            int b;
            int c;
            a = 3;
            b = 4;
            c = (a + a + a) - b;
            println(c);
            println((a + a + a) - b);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("5", "5", "")
    }

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
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("20", "-20", "2", "-14", "2", "-2", "-3", "45", "-49", "")
    }


}