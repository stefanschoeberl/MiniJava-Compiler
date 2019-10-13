package dev.ssch.minijava

import dev.ssch.minijava.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BasicTest : CompilerTest() {

    @Test
    fun emptyProgram() {
        val output = "".run()
        assertThat(output).hasLineCount(0)
    }

    @Test
    fun simplePrintln() {
        val output = "println(1);".run()
        assertThat(output.lines()).containsExactly("1", "")
    }

    @Test
    fun multiplePrintln() {
        val output = """
            println(1);
            println(2);
        """.run()
        assertThat(output.lines()).containsExactly("1", "2", "")
    }
}