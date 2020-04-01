package dev.ssch.minijava.compiler

import dev.ssch.minijava.compiler.exception.IncompatibleTypeException
import dev.ssch.minijava.compiler.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class IfElseTest : CompilerTest() {

    @Test
    fun `simple if (true)`() {
        val output = """
            Console.println(1);
            if (true) {
                Console.println(123);
            }
            Console.println(2);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("1", "123", "2", "")
    }

    @Test
    fun `simple if (false)`() {
        val output = """
            Console.println(1);
            if (false) {
                Console.println(123);
            }
            Console.println(2);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("1", "2", "")
    }

    @Test
    fun `simple if-else (true)`() {
        val output = """
            Console.println(1);
            if (true) {
                Console.println(123);
            } else {
                Console.println(456);
            }
            Console.println(2);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("1", "123", "2", "")
    }

    @Test
    fun `simple if-else (false)`() {
        val output = """
            Console.println(1);
            if (false) {
                Console.println(123);
            } else {
                Console.println(456);
            }
            Console.println(2);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("1", "456", "2", "")
    }

    @Test
    fun `simple if (true) without block`() {
        val output = """
            Console.println(1);
            if (true) Console.println(123);
            Console.println(2);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("1", "123", "2", "")
    }

    @Test
    fun `simple if (false) without block`() {
        val output = """
            Console.println(1);
            if (false) Console.println(123);
            Console.println(2);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("1", "2", "")
    }

    @Test
    fun `simple if-else (true) without block`() {
        val output = """
            Console.println(1);
            if (true) Console.println(123); else Console.println(456);
            Console.println(2);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("1", "123", "2", "")
    }

    @Test
    fun `simple if-else (false) without block`() {
        val output = """
            Console.println(1);
            if (false) Console.println(123); else Console.println(456);
            Console.println(2);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("1", "456", "2", "")
    }

    @Test
    fun `dangling else true-false`() {
        val output = """
            Console.println(1);
            if (true)
                if (false)
                    Console.println(123);
                else 
                    Console.println(456);
            Console.println(2);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("1", "456", "2", "")
    }

    @Test
    fun `dangling else false-true`() {
        val output = """
            Console.println(1);
            if (false)
                if (true)
                    Console.println(123);
                else 
                    Console.println(456);
            Console.println(2);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("1", "2", "")
    }

    @Test
    fun `complex condition (true)`() {
        val output = """
            int a = 5;
            int b = 5;
            Console.println(1);
            if (a == b) {
                Console.println(123);
            } else {
                Console.println(456);
            }
            Console.println(2);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("1", "123", "2", "")
    }

    @Test
    fun `complex condition (false)`() {
        val output = """
            int a = 5;
            int b = 6;
            Console.println(1);
            if (a == b) {
                Console.println(123);
            } else {
                Console.println(456);
            }
            Console.println(2);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("1", "456", "2", "")
    }

    @Test
    fun `complex branches (true)`() {
        val output = """
            Console.println(1);
            if (true) {
                int a = 123;
                Console.println(a);
                Console.println(a * 1000);
            } else {
                int b = 456;
                Console.println(b);
                Console.println(b * 1000);
            }
            Console.println(2);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("1", "123", "123000", "2", "")
    }

    @Test
    fun `complex branches (false)`() {
        val output = """
            Console.println(1);
            if (false) {
                int a = 123;
                Console.println(a);
                Console.println(a * 1000);
            } else {
                int b = 456;
                Console.println(b);
                Console.println(b * 1000);
            }
            Console.println(2);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("1", "456", "456000", "2", "")
    }

    @Test
    fun `non boolean condition`() {
        assertThatThrownBy {
            """
                if (1 + 2) {
                    Console.println(123);
                } else {
                    Console.println(456);
                }
            """.compileAndRunInMainFunction()
        }.isInstanceOf(IncompatibleTypeException::class.java)
    }
}