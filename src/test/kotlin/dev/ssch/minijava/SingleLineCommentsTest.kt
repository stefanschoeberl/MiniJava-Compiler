package dev.ssch.minijava

import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SingleLineCommentsTest : CompilerTest() {

    @Test
    fun `empty line`() {
        val output = "//\nprintln(123);".runInMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `single line`() {
        val output = "// this is a comment\nprintln(123);".runInMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `single lines`() {
        val output = """
            // line 1
            // line 2
            // line 3
            println(123);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `empty lines`() {
        val output = """
            //
            //
            //
            println(123);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `single lines combined with code`() {
        val output = """
            // this is main
            public static void main() {
                // some variable
                int a = 123; // two
                println(a);
                // done
            }
        """.compileAndRunMainFunctionInMainClass()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `edge cases 1`() {
        val output = "///\nprintln(123);".runInMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }
    @Test
    fun `edge cases 2`() {
        val output = "////\nprintln(123);".runInMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `edge cases 3`() {
        val output = "// abc // abc\nprintln(123);".runInMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `edge cases 4`() {
        val output = """
            // //
            ////
            ///////
            println(123);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

}