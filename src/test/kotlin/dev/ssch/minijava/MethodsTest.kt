package dev.ssch.minijava

import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MethodsTest : CompilerTest() {

    @Test
    fun `empty program`() {
        val output = "public void main() { }".compileAndRunMainFunction()
        assertThat(output).hasLineCount(0)
    }

    @Test
    fun `simple main`() {
        val output = """
            public void main() { 
                println(1);
            }""".compileAndRunMainFunction()
        assertThat(output.lines()).containsExactly("1", "")
    }

    @Test
    fun `declare multiple functions`() {
        val output = """
            void other() {
                println(2);
            }
            
            public void main() { 
                println(1);
            }""".compileAndRunMainFunction()
        assertThat(output.lines()).containsExactly("1", "")
    }

    @Test
    fun `call other function without parameters`() {
        val output = """
            void other() {
                println(2);
            }
            
            public void main() { 
                other();
            }""".compileAndRunMainFunction()
        assertThat(output.lines()).containsExactly("2", "")
    }

    @Test
    fun `nested calls without parameters`() {
        val output = """
            public void main() { 
                println(1);
                a();
                println(11);
            }
            
            void a() {
                println(2);
                b();
                println(22);
            }
            
            void b() {
                println(3);
            }""".compileAndRunMainFunction()
        assertThat(output.lines()).containsExactly("1", "2", "3", "22", "11", "")
    }

}