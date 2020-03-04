package dev.ssch.minijava

import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BlockStatementTest : CompilerTest() {

    @Test
    fun `simple block`() {
        val output = """
            {
                Console.println(123);
            }
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `nested blocks`() {
        val output = """
            {
                {
                    Console.println(123);
                }
                {
                    Console.println(456);
                }
            }
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("123", "456", "")
    }
}