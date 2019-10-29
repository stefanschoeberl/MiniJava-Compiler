package dev.ssch.minijava

import dev.ssch.util.CompilerTest
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
}