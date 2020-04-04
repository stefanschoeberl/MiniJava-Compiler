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

    @Test
    fun `concat Strings 1`() {
        val output = """
            Console.println("Hello" + " " + "world");
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("Hello world", "")
    }

    @Test
    fun `concat Strings 2`() {
        val output = """
            String hello = "Hello";
            String world = "world";
            Console.println(hello + " " + world);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("Hello world", "")
    }

    @Test
    fun `concat String and int`() {
        val output = """
            Console.println("Number: " + 123);
            Console.println(123 + " numbers");
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("Number: 123", "123 numbers", "")
    }

    @Test
    fun `concat String and float`() {
        val output = """
            Console.println("Number: " + 4.5f);
            Console.println(4.5f + " numbers");
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("Number: 4.5", "4.5 numbers", "")
    }

    @Test
    fun `concat String and boolean`() {
        val output = """
            Console.println("truth: " + true);
            Console.println(false + " truth");
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("truth: true", "false truth", "")
    }

    @Test
    fun `concat String and char`() {
        val output = """
            Console.println("char: " + 'a');
            Console.println('b' + " char");
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("char: a", "b char", "")
    }

    @Test
    fun `concat String and object`() {
        val output = """
            Console.println("obj: " + new Main());
            Console.println(new Main() + " obj");
        """.compileAndRunInMainFunction()
        assertThat(output.lines()[0]).matches("obj: Object@[0-9]*")
        assertThat(output.lines()[1]).matches("Object@[0-9]* obj")
    }

    @Test
    fun `concat String and null`() {
        val output = """
            Console.println("obj: " + null);
            Console.println(null + " obj");
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("obj: null", "null obj", "")
    }
}