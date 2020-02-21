package dev.ssch.minijava

import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CommentsTest : CompilerTest() {

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

}