package dev.ssch.minijava.expressions

import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class EqualNotEqualTest : CompilerTest() {

    @Test
    fun `== two boolean values`() {
        val output = """
            println(true == false);
            println(false == true);
            println(true == true);
            println(false == false);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("false", "false", "true", "true", "")
    }

    @Test
    fun `== two arithmetic values`() {
        val output = """
            println(1 == 2);
            println(1 == 1);
            println(2 == 1);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("false", "true", "false", "")
    }

    @Test
    fun `!= two boolean values`() {
        val output = """
            println(true != false);
            println(false != true);
            println(true != true);
            println(false != false);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("true", "true", "false", "false", "")
    }

    @Test
    fun `!= two arithmetic values`() {
        val output = """
            println(1 != 2);
            println(1 != 1);
            println(2 != 1);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("true", "false", "true", "")
    }
}