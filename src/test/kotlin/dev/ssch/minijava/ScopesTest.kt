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
            println(b);
        """.runInMainFunction()
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
            println(c);
        """.runInMainFunction()
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
                println(c);
            }
        """.runInMainFunction()
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
            println(b);
        """.runInMainFunction()
        }.isInstanceOf(UndefinedVariableException::class.java)
    }

    @Test
    fun `multiple nested blocks`() {
        val output = """
            int a = 1;
            println(a);
            {
                int b = 2;
                println(b);
                {
                    int c = 3;
                    println(c);
                }
                println(b);
            }
            println(a);
            {
                int b = 4;
                println(b);
                {
                    int c = 5;
                    println(c);
                }
                println(b);
                {
                    int c = 6;
                    println(c);
                    println(a);
                }
                println(b);
            }
            println(a);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("1", "2", "3", "2", "1", "4", "5", "4", "6", "1", "4", "1", "")
    }
}