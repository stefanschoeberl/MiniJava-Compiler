package dev.ssch.minijava

import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BasicTest : CompilerTest() {

    @Test
    fun `empty program`() {
        val output = "".runInMainFunction()
        assertThat(output).hasLineCount(0)
    }

    @Test
    fun `println a numeric value`() {
        val output = "Console.println(1);".runInMainFunction()
        assertThat(output.lines()).containsExactly("1", "")
    }

    @Test
    fun `println a boolean value`() {
        val output = "Console.println(true);".runInMainFunction()
        assertThat(output.lines()).containsExactly("true", "")
    }

    @Test
    fun `println a float value`() {
        val output = "Console.println(1.2f);".runInMainFunction()
        output.lines().matches(v(1.2f, 0.0001f), v(""))
    }

    @Test
    fun `println multiple values`() {
        val output = """
            Console.println(1);
            Console.println(2);
            Console.println(true);
            Console.println(false);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("1", "2", "true", "false", "")
    }
}