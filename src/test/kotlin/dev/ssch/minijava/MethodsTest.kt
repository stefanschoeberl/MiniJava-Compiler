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
    fun `declare multiple methods`() {
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
    fun `call other method without parameters`() {
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

    @Test
    fun `call method with one parameter`() {
        val output = """
            public void main() { 
                a(123);
            }
            
            void a(int x) {
                println(x);
            }""".compileAndRunMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `call method with one parameter and local`() {
        val output = """
            public void main() { 
                a(123);
            }
            
            void a(int x) {
                int a = 100;
                println(x * a);
            }""".compileAndRunMainFunction()
        assertThat(output.lines()).containsExactly("12300", "")
    }

    @Test
    fun `call method with multiple parameters`() {
        val output = """
            public void main() { 
                add(2, 3);
                sub(11, 3);
            }
            
            void add(int x, int y) {
                println(x + y);
            }
            
            void sub(int x, int y) {
                println(x - y);
            }
            """.compileAndRunMainFunction()
        assertThat(output.lines()).containsExactly("5", "8", "")
    }

    @Test
    fun `nested calls with multiple parameters 1`() {
        val output = """
            public void main() { 
                add(2, 3);
            }
            
            void add(int x, int y) {
                println(x + y);
                sub(x, y);
            }
            
            void sub(int x, int y) {
                println(x - y);
            }
            """.compileAndRunMainFunction()
        assertThat(output.lines()).containsExactly("5", "-1", "")
    }

    @Test
    fun `nested calls with multiple parameters 2`() {
        val output = """
            public void main() { 
                add(2, 3, 4);
            }
            
            void add(int x, int y, int z) {
                println(x + y + z);
                sub(x, y, z);
            }
            
            void sub(int x, int y, int z) {
                println(x - y - z);
            }
            """.compileAndRunMainFunction()
        assertThat(output.lines()).containsExactly("9", "-5", "")
    }

}