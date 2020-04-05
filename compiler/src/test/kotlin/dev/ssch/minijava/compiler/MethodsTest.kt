package dev.ssch.minijava.compiler

import dev.ssch.minijava.compiler.exception.NotACallableExpressionException
import dev.ssch.minijava.compiler.exception.UndefinedClassException
import dev.ssch.minijava.compiler.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class MethodsTest : CompilerTest() {

    @Test
    fun `empty program`() {
        val output = "public static void main() { }".compileAndRunInMainClass()
        assertThat(output).hasLineCount(0)
    }

    @Test
    fun `simple main`() {
        val output = """
            public static void main() { 
                Console.println(1);
            }""".compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly("1", "")
    }

    @Test
    fun `declare multiple methods`() {
        val output = """
            static void other() {
                Console.println(2);
            }
            
            public static void main() { 
                Console.println(1);
            }""".compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly("1", "")
    }

    @Test
    fun `call other method without parameters`() {
        val output = """
            static void other() {
                Console.println(2);
            }
            
            public static void main() { 
                other();
            }""".compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly("2", "")
    }

    @Test
    fun `nested calls without parameters`() {
        val output = """
            public static void main() { 
                Console.println(1);
                a();
                Console.println(11);
            }
            
            static void a() {
                Console.println(2);
                b();
                Console.println(22);
            }
            
            static void b() {
                Console.println(3);
            }""".compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly("1", "2", "3", "22", "11", "")
    }

    @Test
    fun `call method with one parameter`() {
        val output = """
            public static void main() { 
                a(123);
            }
            
            static void a(int x) {
                Console.println(x);
            }""".compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `call method with one float parameter`() {
        val output = """
            public static void main() { 
                a(123f);
            }
            
            static void a(float x) {
                Console.println(x);
            }""".compileAndRunInMainClass()
        output.lines().matches(v(123f, 0.0001f), v(""))
    }

    @Test
    fun `call method with one parameter and local`() {
        val output = """
            public static void main() { 
                a(123);
            }
            
            static void a(int x) {
                int a = 100;
                Console.println(x * a);
            }""".compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly("12300", "")
    }

    @Test
    fun `call method with multiple parameters`() {
        val output = """
            public static void main() { 
                add(2, 3);
                sub(11, 3);
            }
            
            static void add(int x, int y) {
                Console.println(x + y);
            }
            
            static void sub(int x, int y) {
                Console.println(x - y);
            }
            """.compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly("5", "8", "")
    }

    @Test
    fun `nested calls with multiple parameters 1`() {
        val output = """
            public static void main() { 
                add(2, 3);
            }
            
            static void add(int x, int y) {
                Console.println(x + y);
                sub(x, y);
            }
            
            static void sub(int x, int y) {
                Console.println(x - y);
            }
            """.compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly("5", "-1", "")
    }

    @Test
    fun `nested calls with multiple parameters 2`() {
        val output = """
            public static void main() { 
                add(2, 3, 4);
            }
            
            static void add(int x, int y, int z) {
                Console.println(x + y + z);
                sub(x, y, z);
            }
            
            static void sub(int x, int y, int z) {
                Console.println(x - y - z);
            }
            """.compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly("9", "-5", "")
    }

    @Test
    fun `return a single value`() {
        val output = """
            public static void main() {
                int a = increment(10);
                Console.println(a);
            }
            
            static int increment(int x) {
                return x + 1;
            }
            """.compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly("11", "")
    }

    @Test
    fun `nested calls`() {
        val output = """
            public static void main() {
                Console.println(increment(increment(increment(1))));
            }
            
            static int increment(int x) {
                return x + 1;
            }
            """.compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly("4", "")
    }

    @Test
    fun `don't use return value`() {
        val output = """
            public static void main() { 
                increment(5);
                Console.println(increment(10));
            }
            
            static int increment(int x) {
                return x + 1;
            }
            """.compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly("11", "")
    }

    @Test
    fun `simple recursion`() {
        val output = """
            public static void main() { 
                Console.println(duplicate(1, 5));
            }
            
            static int duplicate(int a, int n) {
                if (n == 0) {
                    return a;
                } else {
                    return 2 * duplicate(a, n - 1);
                }
            }
            """.compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly("32", "")
    }

    @Test
    fun `fibonacci recursion`() {
        val output = """
            public static void main() {
                int i = 0;
                while (i < 10) {
                    Console.println(fib(i));
                    i = i + 1;
                }
            }
            
            static int fib(int n) {
                if (n == 0 || n == 1) {
                    return n;
                } else {
                    return fib(n - 1) + fib(n - 2);
                }
            }
            """.compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly("0", "1", "1", "2", "3", "5", "8", "13", "21", "34", "")
    }

    @Test
    fun `native method with multiple parameters`() {
        val output = Source.withMiniJava("""
            public static void main() {
                println(123, true, 123.123f);
            }
            
            native static void println(int a, boolean b, float c);
        """).andJavaScript("""
            module.exports = function (runtime) {
                return {
                    "Main.println#int#boolean#float": function(a, b, c) {
                        console.log(a);
                        console.log(runtime.wasmBoolean(b));
                        console.log(c);
                    }
                }
            }
        """).compileAndRunInMainClass()
        output.lines().matches(v(123), v(true), v(123.123f, 0.0001f), v(""))
    }

    @Test
    fun `call method on non-existent class`() {
        assertThatThrownBy {
        """
            Output.println();
        """.compileAndRunInMainFunction()
        }.isInstanceOf(UndefinedClassException::class.java)
    }

    @Test
    fun `call arithmetic expression as method`() {
        assertThatThrownBy {
        """
            (1+2)();
        """.compileAndRunInMainFunction()
        }.isInstanceOf(NotACallableExpressionException::class.java)
    }

    @Test
    fun `call object after new expression as method`() {
        assertThatThrownBy {
        """
            (new Main())();
        """.compileAndRunInMainFunction()
        }.isInstanceOf(NotACallableExpressionException::class.java)
    }
}