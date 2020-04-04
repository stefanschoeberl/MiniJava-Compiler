package dev.ssch.minijava.compiler

import dev.ssch.minijava.compiler.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class StringTest : CompilerTest() {

    @Test
    fun `declare String variable`() {
        val output = """
            String s;
            Console.println(123);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `assign String literal to String variable`() {
        val output = """
            String s = "Hello world";
            Console.println(123);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `output String`() {
        val output = """
            String s = "Hello";
            Console.println(s);
            Console.println("world");
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("Hello", "world", "")
    }
}