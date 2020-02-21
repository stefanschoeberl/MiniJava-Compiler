package dev.ssch.minijava

import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SingleLineCommentsTest : CompilerTest() {

    @Test
    fun `empty line`() {
        val output = "//".runInMainFunction()
        assertThat(output).hasLineCount(0)
    }

    @Test
    fun `single line`() {
        val output = "// this is a comment".runInMainFunction()
        assertThat(output).hasLineCount(0)
    }

    @Test
    fun `single lines`() {
        val output = """
            // line 1
            // line 2
            // line 3
        """.runInMainFunction()
        assertThat(output).hasLineCount(0)
    }

    @Test
    fun `empty lines`() {
        val output = """
            //
            //
            //
        """.runInMainFunction()
        assertThat(output).hasLineCount(0)
    }

    @Test
    fun `single lines combined with code`() {
        val output = """
            // this is main
            public void main() {
                // some variable
                int a = 123; // two
                println(a);
                // done
            }
        """.compileAndRunMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `edge cases 1`() {
        val output = "///".runInMainFunction()
        assertThat(output).hasLineCount(0)
    }
    @Test
    fun `edge cases 2`() {
        val output = "////".runInMainFunction()
        assertThat(output).hasLineCount(0)
    }

    @Test
    fun `edge cases 3`() {
        val output = "// abc // abc".runInMainFunction()
        assertThat(output).hasLineCount(0)
    }

    @Test
    fun `edge cases 4`() {
        val output = """
            // //
            ////
            ///////
        """.runInMainFunction()
        assertThat(output).hasLineCount(0)
    }

}