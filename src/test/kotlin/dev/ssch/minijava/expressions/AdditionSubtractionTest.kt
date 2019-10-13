package dev.ssch.minijava.expressions

import dev.ssch.minijava.util.CompilerTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class AdditionSubtractionTest : CompilerTest() {

    @Test
    fun `add two values`() {
        val output = """
            println(1 + 2);
        """.run()
        Assertions.assertThat(output.lines()).containsExactly("3", "")
    }

    @Test
    fun `add two variables`() {
        val output = """
            int a = 1;
            int b = 2;
            println(a + b);
        """.run()
        Assertions.assertThat(output.lines()).containsExactly("3", "")
    }

    @Test
    fun `nested addition`() {
        val output = """
            println((1 + 2) + 3);
            println(1 + 2 + 3);
            println(1 + (2 + 3));
        """.run()
        Assertions.assertThat(output.lines()).containsExactly("6", "6", "6", "")
    }

    @Test
    fun `subtract two values`() {
        val output = """
            println(1 - 2);
        """.run()
        Assertions.assertThat(output.lines()).containsExactly("-1", "")
    }

    @Test
    fun `subtract two variables`() {
        val output = """
            int a = 1;
            int b = 2;
            println(a - b);
        """.run()
        Assertions.assertThat(output.lines()).containsExactly("-1", "")
    }

    @Test
    fun `nested subtraction`() {
        val output = """
            println((1 - 2) - 3);
            println(1 - 2 - 3);
            println(1 - (2 - 3));
        """.run()
        Assertions.assertThat(output.lines()).containsExactly("-4", "-4", "2", "")
    }
}