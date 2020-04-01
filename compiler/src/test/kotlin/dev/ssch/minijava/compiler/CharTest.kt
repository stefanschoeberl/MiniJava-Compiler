package dev.ssch.minijava.compiler

import dev.ssch.minijava.compiler.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CharTest : CompilerTest() {

    @Test
    fun `print char`() {
        val output = """
            char c = 'a';
            Console.println(c);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("a", "")
    }

    @Test
    fun `print special char`() {
        val output = """
            Console.println('ö');
            Console.println('_');
            Console.println('!');
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("ö", "_", "!", "")
    }

    @Test
    fun `char array`() {
        val output = """
            char[] c = new char[3];
            c[0] = 'a';
            c[1] = 'b';
            c[2] = 'c';
            c[3] = c[1];
            Console.println(c);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("['a', 'b', 'c', 'b']", "")
    }

    @Test
    fun `print char stored in field`() {
        val output = """
            char c;
            
            public static void main() {
                Main m = new Main();
                m.c = 'a';
                Console.println(m.c);
            }
        """.compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly("a", "")
    }

    @Test
    fun `char array as field`() {
        val output = """
            char[] c;
            
            public static void main() {
                Main m = new Main();
                m.c = new char[3];
                m.c[0] = 'a';
                m.c[1] = 'b';
                m.c[2] = 'c';
                m.c[3] = m.c[1];
                Console.println(m.c);
            }
        """.compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly("['a', 'b', 'c', 'b']", "")
    }

    @Test
    fun `calculations with chars 1`() {
        val output = """
            char f = 'f';
            Console.println((char)(f - 5));
            Console.println((char)(f + 5));
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("a", "k", "")
    }

    @Test
    fun `calculations with chars 2`() {
        val output = """
            char a = 'a';
            char z = 'z';
            Console.println((int)(z + a));
            Console.println((int)(z - a));
            Console.println((int)(z * a));
            Console.println((int)(z / a));
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("219", "25", "11834", "1", "")
    }

    @Test
    fun `comparison with chars`() {
        val output = """
            char a = 'a';
            char aa = 'a';
            char z = 'z';
            Console.println(a > z);
            Console.println(a < z);
            Console.println(a >= z);
            Console.println(a <= z);
            Console.println(a > aa);
            Console.println(a < aa);
            Console.println(a >= aa);
            Console.println(a <= aa);
            Console.println(a == z);
            Console.println(a != z);
            Console.println(a == aa);
            Console.println(a != aa);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("false", "true", "false", "true", "false", "false", "true", "true", "false", "true", "true", "false", "")
    }
}