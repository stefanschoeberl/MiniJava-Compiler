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
    fun `println a value`() {
        val output = "println(1);".runInMainFunction()
        assertThat(output.lines()).containsExactly("1", "")
    }

    @Test
    fun `println multiple values`() {
        val output = """
            println(1);
            println(2);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("1", "2", "")
    }
}