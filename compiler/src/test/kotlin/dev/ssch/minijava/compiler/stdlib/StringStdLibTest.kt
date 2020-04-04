package dev.ssch.minijava.compiler.stdlib

import dev.ssch.minijava.compiler.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class StringStdLibTest : CompilerTest() {

    @Test
    fun `String length`() {
        val output = """
            String a = "Hello world";
            Console.println(a.length());
            Console.println("".length());
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("11", "0", "")
    }

    @Test
    fun `String charAt`() {
        val output = """
            String a = "Hello world";
            Console.println(a.charAt(0));
            Console.println(a.charAt(10));
            Console.println(a.charAt(4));
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("H", "d", "o", "")
    }

    @Test
    fun `String equals`() {
        val output = """
            String a = "Hello world";
            String b = "Hello" + " world";
            String c = "Hello";
            Console.println(a.equals(b));
            Console.println(a.equals(c));
            Console.println(b.equals(c));
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("true", "false", "false", "")
    }
}