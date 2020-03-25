package dev.ssch.minijava

import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class BooleanVariableTest : CompilerTest() {

    @Test
    fun `declare a variable`() {
        val output = """
            boolean a;
        """.compileAndRunInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("")
    }

    @Test
    fun `declare a variable and initilize it with a value`() {
        val output = """
            boolean a = true;
            Console.println(a);
        """.compileAndRunInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("true", "")
    }

    @Test
    fun `declare a variable and initilize it with another variable`() {
        val output = """
            boolean a = false;
            boolean b = a;
            Console.println(b);
        """.compileAndRunInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("false", "")
    }

    @Test
    fun `assign a value to a variable`() {
        val output = """
            boolean a;
            a = true;
            Console.println(a);
        """.compileAndRunInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("true", "")
    }

    @Test
    fun `assign a variable to another variable`() {
        val output = """
            boolean a = false;
            boolean b = a;
            Console.println(b);
        """.compileAndRunInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("false", "")
    }
}