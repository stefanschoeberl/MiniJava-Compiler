package dev.ssch.minijava

import dev.ssch.minijava.exception.IncompatibleTypeException
import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class WhileTest : CompilerTest() {

    @Test
    fun `while (false)`() {
        val output = """
            while (false) println(123);
            println(-1);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("-1", "")
    }

    @Test
    fun `while (false) {}`() {
        val output = """
            while (false) {
                println(123);
            }
            println(-1);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("-1", "")
    }

    @Test
    fun `simple while loop`() {
        val output = """
            int i = 0;
            while (i != 3) {
                println(i);
                i = i + 1;
            }
            println(-1);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("0", "1", "2", "-1", "")
    }

    @Test
    fun `nested while loop`() {
        val output = """
            int i = 1;
            while (i != 3) {
                int j = 1;
                while (j != 3) {
                    println(i * 100 + j);
                    j = j + 1;
                }
                i = i + 1;
            }
            println(-1);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("101", "102", "201", "202", "-1", "")
    }

    @Test
    fun `while with if 1`() {
        val output = """
            int i = 0;
            while (i != 10) {
                if (i == 1 || i == 3 || i == 5) {
                    println(i);
                }
                i = i + 1;
            }
            println(-1);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("1", "3", "5", "-1", "")
    }

    @Test
    fun `while with if 2`() {
        val output = """
            int i = 0;
            while (i < 10) {
                if (i >= 2 && i < 5) {
                    println(i);
                }
                i = i + 1;
            }
            println(-1);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("2", "3", "4", "-1", "")
    }

    @Test
    fun `non boolean condition`() {
        Assertions.assertThatThrownBy {
            """
                while (1 + 2) {
                    println(123);
                }
            """.runInMainFunction()
        }.isInstanceOf(IncompatibleTypeException::class.java)
    }
}