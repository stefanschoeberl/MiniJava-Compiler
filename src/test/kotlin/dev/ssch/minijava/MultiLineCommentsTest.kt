package dev.ssch.minijava

import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MultiLineCommentsTest : CompilerTest() {

    @Test
    fun `empty comment`() {
        val output = "/**/Console.println(123);".runInMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `single line`() {
        val output = "/* this is a comment */Console.println(123);".runInMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `single lines`() {
        val output = """
            /* line 1 */
            /* line 2 */
            /* line 3 */
            Console.println(123);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `empty lines`() {
        val output = """
            /**/
            /**/
            /**/
            Console.println(123);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `combined with code`() {
        val output = """
            /* this is main */
            public static void main() {
                /* some variable */
                int a = 123; /* two */
                /* some 
                   multiline 
                   comment
                */
                Console.println(a);
                /* done */
            }
        """.compileAndRunMainFunctionInMainClass()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `edge cases 1`() {
        val output = "/***/Console.println(123);".runInMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `edge cases 2`() {
        val output = "/*/**/Console.println(123);".runInMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `edge cases 3`() {
        val output = "/*/* */Console.println(123);".runInMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

}