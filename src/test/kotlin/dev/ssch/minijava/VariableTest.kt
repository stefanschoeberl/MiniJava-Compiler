package dev.ssch.minijava

import dev.ssch.minijava.util.CompilerTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class VariableTest : CompilerTest() {

    @Test
    fun `declare a variable`() {
        val output = """
            int a;
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("")
    }

    @Test
    fun `declare a variable and initilize it with a value`() {
        val output = """
            int a = 123;
            println(a);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `declare a variable and initilize it with another variable`() {
        val output = """
            int a = 123;
            int b = a;
            println(b);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `assign a value to a variable`() {
        val output = """
            int a;
            a = 123;
            println(a);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `assign a variable to another variable`() {
        val output = """
            int a = 123;
            int b = a;
            println(b);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("123", "")
    }
}