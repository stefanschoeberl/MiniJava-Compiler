package dev.ssch.minijava

import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class DefaultInitializationTest : CompilerTest() {

    @Test
    fun `declare a boolean`() {
        val output = """
            boolean a;
            println(a);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("false", "")
    }

    @Test
    fun `declare an integer`() {
        val output = """
            int a;
            println(a);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("0", "")
    }
}