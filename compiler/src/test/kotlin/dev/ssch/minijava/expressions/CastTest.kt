package dev.ssch.minijava.expressions

import dev.ssch.util.CompilerTest
import org.junit.jupiter.api.Test

class CastTest : CompilerTest() {

    @Test
    fun `cast values`() {
        val output = """
            int a = (int) 123;
            int b = (int) 123f;
            float c = (float) 123;
            float d = (float) 123f;
            boolean e = (boolean) true;
            
            Console.println(a);
            Console.println(b);
            Console.println(c);
            Console.println(d);
            Console.println(e);
        """.compileAndRunInMainFunction()
        output.lines().matches(
            v(123),
            v(123),
            v(123f, 0.0001f),
            v(123f, 0.0001f),
            v(true),
            v(""))
    }

    @Test
    fun `cast variables`() {
        val output = """
            int x = 123;
            float y = 123;
            
            int a = (int) x;
            int b = (int) y;
            float c = (float) x;
            float d = (float) y;
            
            Console.println(a);
            Console.println(b);
            Console.println(c);
            Console.println(d);
        """.compileAndRunInMainFunction()
        output.lines().matches(
            v(123),
            v(123),
            v(123f, 0.0001f),
            v(123f, 0.0001f),
            v(""))
    }

    @Test
    fun `cast with method call`() {
        val output = """
            static void a(int x) {
                Console.println(1111);
                Console.println(x);
            }
            
            static void a(float x) {
                Console.println(2222);
                Console.println(x);
            }
            
            public static void main() {
                a(1);
                a(1f);
                a((float)1);
                a((float)1f);
                a((int)1);
                a((int)1f);
            }
        """.compileAndRunInMainClass()
        output.lines().matches(
            v(1111), v(1),
            v(2222), v(1f, 0.0001f),
            v(2222), v(1f, 0.0001f),
            v(2222), v(1f, 0.0001f),
            v(1111), v(1),
            v(1111), v(1),
            v(""))
    }
}