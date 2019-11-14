package dev.ssch.minijava

import dev.ssch.minijava.exception.IncompatibleTypeException
import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class IfElseTest : CompilerTest() {

    @Test
    fun `simple if (true)`() {
        val output = """
            println(1);
            if (true) {
                println(123);
            }
            println(2);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("1", "123", "2", "")
    }

    @Test
    fun `simple if (false)`() {
        val output = """
            println(1);
            if (false) {
                println(123);
            }
            println(2);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("1", "2", "")
    }

    @Test
    fun `simple if-else (true)`() {
        val output = """
            println(1);
            if (true) {
                println(123);
            } else {
                println(456);
            }
            println(2);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("1", "123", "2", "")
    }

    @Test
    fun `simple if-else (false)`() {
        val output = """
            println(1);
            if (false) {
                println(123);
            } else {
                println(456);
            }
            println(2);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("1", "456", "2", "")
    }

    @Test
    fun `simple if (true) without block`() {
        val output = """
            println(1);
            if (true) println(123);
            println(2);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("1", "123", "2", "")
    }

    @Test
    fun `simple if (false) without block`() {
        val output = """
            println(1);
            if (false) println(123);
            println(2);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("1", "2", "")
    }

    @Test
    fun `simple if-else (true) without block`() {
        val output = """
            println(1);
            if (true) println(123); else println(456);
            println(2);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("1", "123", "2", "")
    }

    @Test
    fun `simple if-else (false) without block`() {
        val output = """
            println(1);
            if (false) println(123); else println(456);
            println(2);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("1", "456", "2", "")
    }

    @Test
    fun `dangling else true-false`() {
        val output = """
            println(1);
            if (true)
                if (false)
                    println(123);
                else 
                    println(456);
            println(2);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("1", "456", "2", "")
    }

    @Test
    fun `dangling else false-true`() {
        val output = """
            println(1);
            if (false)
                if (true)
                    println(123);
                else 
                    println(456);
            println(2);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("1", "2", "")
    }

    @Test
    fun `complex condition (true)`() {
        val output = """
            int a = 5;
            int b = 5;
            println(1);
            if (a == b) {
                println(123);
            } else {
                println(456);
            }
            println(2);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("1", "123", "2", "")
    }

    @Test
    fun `complex condition (false)`() {
        val output = """
            int a = 5;
            int b = 6;
            println(1);
            if (a == b) {
                println(123);
            } else {
                println(456);
            }
            println(2);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("1", "456", "2", "")
    }

    @Test
    fun `complex branches (true)`() {
        val output = """
            println(1);
            if (true) {
                int a = 123;
                println(a);
                println(a * 1000);
            } else {
                int b = 456;
                println(b);
                println(b * 1000);
            }
            println(2);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("1", "123", "123000", "2", "")
    }

    @Test
    fun `complex branches (false)`() {
        val output = """
            println(1);
            if (false) {
                int a = 123;
                println(a);
                println(a * 1000);
            } else {
                int b = 456;
                println(b);
                println(b * 1000);
            }
            println(2);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("1", "456", "456000", "2", "")
    }

    @Test
    fun `non boolean condition`() {
        Assertions.assertThatThrownBy {
            """
                if (1 + 2) {
                    println(123);
                } else {
                    println(456);
                }
            """.runInMainFunction()
        }.isInstanceOf(IncompatibleTypeException::class.java)
    }
}