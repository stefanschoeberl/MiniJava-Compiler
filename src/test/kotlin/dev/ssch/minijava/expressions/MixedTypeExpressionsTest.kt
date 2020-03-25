package dev.ssch.minijava.expressions

import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MixedTypeExpressionsTest : CompilerTest() {

    @Test
    fun `add int and float`() {
        val output = """
            Console.println(1 + 1.5f);
            Console.println(1.5f + 1);
        """.compileAndRunInMainFunction()
        output.lines().matches(
            v(2.5f, 0.0001f),
            v(2.5f, 0.0001f),
            v(""))
    }

    @Test
    fun `subtract int and float`() {
        val output = """
            Console.println(1 - 1.5f);
            Console.println(1.5f - 1);
        """.compileAndRunInMainFunction()
        output.lines().matches(
            v(-0.5f, 0.0001f),
            v(0.5f, 0.0001f),
            v(""))
    }

    @Test
    fun `multiply int and float`() {
        val output = """
            Console.println(10 * 1.5f);
            Console.println(1.5f * 10);
        """.compileAndRunInMainFunction()
        output.lines().matches(
            v(15f, 0.0001f),
            v(15f, 0.0001f),
            v(""))
    }

    @Test
    fun `divide int and float`() {
        val output = """
            Console.println(5 / 2f);
            Console.println(5f / 2);
        """.compileAndRunInMainFunction()
        output.lines().matches(
            v(2.5f, 0.0001f),
            v(2.5f, 0.0001f),
            v(""))
    }

    @Test
    fun `== int and float`() {
        val output = """
            Console.println(5 == 2f);
            Console.println(5f == 2);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("false", "false", "")
    }

    @Test
    fun `!= int and float`() {
        val output = """
            Console.println(5 != 2f);
            Console.println(5f != 2);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("true", "true", "")
    }

    @Test
    fun `lt int and float`() {
        val output = """
            Console.println(5 < 2f);
            Console.println(5f < 2);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("false", "false", "")
    }

    @Test
    fun `lte int and float`() {
        val output = """
            Console.println(5 <= 2f);
            Console.println(5f <= 2);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("false", "false", "")
    }

    @Test
    fun `gt int and float`() {
        val output = """
            Console.println(5 > 2f);
            Console.println(5f > 2);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("true", "true", "")
    }

    @Test
    fun `gte int and float`() {
        val output = """
            Console.println(5 >= 2f);
            Console.println(5f >= 2);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("true", "true", "")
    }
}