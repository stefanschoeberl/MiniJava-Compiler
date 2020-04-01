package dev.ssch.minijava.compiler

import dev.ssch.minijava.compiler.exception.UndefinedVariableException
import dev.ssch.minijava.compiler.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ScopesTest : CompilerTest() {

    @Test
    fun `use variable outside of block`() {
        assertThatThrownBy {
        """
            int a;
            {
                int b = 123;
            }
            Console.println(b);
        """.compileAndRunInMainFunction()
        }.isInstanceOf(UndefinedVariableException::class.java)
    }

    @Test
    fun `use variable outside of block (complex nesting 1)`() {
        assertThatThrownBy {
        """
            int a = 111;
            {
                int b = 222;
                {
                    int c = 333;
                }
            }
            Console.println(c);
        """.compileAndRunInMainFunction()
        }.isInstanceOf(UndefinedVariableException::class.java)
    }

    @Test
    fun `use variable outside of block (complex nesting 2)`() {
        assertThatThrownBy {
        """
            int a = 111;
            {
                int b = 222;
                {
                    int c = 333;
                }
                Console.println(c);
            }
        """.compileAndRunInMainFunction()
        }.isInstanceOf(UndefinedVariableException::class.java)
    }

    @Test
    fun `use variable outside of block (complex nesting 3)`() {
        assertThatThrownBy {
        """
            int a = 111;
            {
                int b = 222;
                {
                    int c = 333;
                }
            }
            Console.println(b);
        """.compileAndRunInMainFunction()
        }.isInstanceOf(UndefinedVariableException::class.java)
    }

    @Test
    fun `multiple nested blocks`() {
        val output = """
            int a = 1;
            Console.println(a);
            {
                int b = 2;
                Console.println(b);
                {
                    int c = 3;
                    Console.println(c);
                }
                Console.println(b);
            }
            Console.println(a);
            {
                int b = 4;
                Console.println(b);
                {
                    int c = 5;
                    Console.println(c);
                }
                Console.println(b);
                {
                    int c = 6;
                    Console.println(c);
                    Console.println(a);
                }
                Console.println(b);
            }
            Console.println(a);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("1", "2", "3", "2", "1", "4", "5", "4", "6", "1", "4", "1", "")
    }
}