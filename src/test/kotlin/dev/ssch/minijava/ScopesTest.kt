package dev.ssch.minijava

import dev.ssch.minijava.exception.UndefinedVariableException
import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class ScopesTest : CompilerTest() {

    @Test
    fun `use variable outside of block`() {
        Assertions.assertThatThrownBy {
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
        Assertions.assertThatThrownBy {
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
        Assertions.assertThatThrownBy {
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
        Assertions.assertThatThrownBy {
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
        Assertions.assertThat(output.lines()).containsExactly("1", "2", "3", "2", "1", "4", "5", "4", "6", "1", "4", "1", "")
    }
}