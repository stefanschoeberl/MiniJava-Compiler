package dev.ssch.minijava.compiler

import dev.ssch.minijava.compiler.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VariableTest : CompilerTest() {

    @Test
    fun `declare a variable`() {
        val output = """
            int a;
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("")
    }

    @Test
    fun `declare a variable and initialize it with a value`() {
        val output = """
            int a = 123;
            Console.println(a);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `declare a variable and initialize it with another variable`() {
        val output = """
            int a = 123;
            int b = a;
            Console.println(b);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `assign a value to a variable`() {
        val output = """
            int a;
            a = 123;
            Console.println(a);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `assign a variable to another variable`() {
        val output = """
            int a = 123;
            int b = a;
            Console.println(b);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }
}