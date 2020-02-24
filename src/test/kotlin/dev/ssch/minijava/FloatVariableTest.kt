package dev.ssch.minijava

import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class FloatVariableTest : CompilerTest() {

    @Test
    fun `declare a variable`() {
        val output = """
            float a;
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("")
    }

    @Test
    fun `declare a variable and initialize it with a value`() {
        val output = """
            float a = 1.2f;
            println(a);
        """.runInMainFunction()
        output.lines().matches(v(1.2f, 0.0001f), v(""))
    }

    @Test
    fun `declare a variable and initialize it with another variable`() {
        val output = """
            float a = 1.2f;
            float b = a;
            println(b);
        """.runInMainFunction()
        output.lines().matches(v(1.2f, 0.0001f), v(""))
    }

    @Test
    fun `assign a value to a variable`() {
        val output = """
            float a;
            a = 1.2f;
            println(a);
        """.runInMainFunction()
        output.lines().matches(v(1.2f, 0.0001f), v(""))
    }

    @Test
    fun `assign a variable to another variable`() {
        val output = """
            float a = 1.2f;
            float b = a;
            println(b);
        """.runInMainFunction()
        output.lines().matches(v(1.2f, 0.0001f), v(""))
    }

    @Test
    fun `declare and a float variable and assign an int value to it`() {
        val output = """
            float a = 123;
            println(a);
        """.runInMainFunction()
        output.lines().matches(v(123f, 0.0001f), v(""))
    }

    @Test
    fun `declare a float variable and assign an int variable to it`() {
        val output = """
            int a = 123;
            float b = a;
            println(a);
        """.runInMainFunction()
        output.lines().matches(v(123f, 0.0001f), v(""))
    }

    @Test
    fun `assign an int value to a float variable`() {
        val output = """
            float a;
            a = 123;
            println(a);
        """.runInMainFunction()
        output.lines().matches(v(123f, 0.0001f), v(""))
    }

    @Test
    fun `assign an int variable to a float variable`() {
        val output = """
            int a = 123;
            float b;
            b = a;
            println(a);
        """.runInMainFunction()
        output.lines().matches(v(123f, 0.0001f), v(""))
    }
}