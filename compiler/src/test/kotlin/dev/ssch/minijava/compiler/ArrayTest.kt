package dev.ssch.minijava.compiler

import dev.ssch.minijava.compiler.exception.ExpressionIsNotAnArrayException
import dev.ssch.minijava.compiler.exception.IncompatibleTypeException
import dev.ssch.minijava.compiler.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ArrayTest : CompilerTest() {

    @Test
    fun `create int array`() {
        val output = """
            int[] a = new int[1];
            Console.println(111);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("111", "")
    }

    @Test
    fun `create float array`() {
        val output = """
            float[] a = new float[1];
            Console.println(111);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("111", "")
    }

    @Test
    fun `create boolean array`() {
        val output = """
            boolean[] a = new boolean[1];
            Console.println(111);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("111", "")
    }

    // -----

    @Test
    fun `write one int element to array`() {
        val output = """
            int[] a = new int[1];
            a[0] = 123;
            Console.println(111);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("111", "")
    }

    @Test
    fun `write three int elements to array`() {
        val output = """
            int[] a = new int[3];
            a[0] = 123;
            a[1] = 456;
            a[2] = 789;
            Console.println(111);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("111", "")
    }

    @Test
    fun `write one float element to array`() {
        val output = """
            float[] a = new float[1];
            a[0] = 123f;
            Console.println(111);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("111", "")
    }

    @Test
    fun `write three float elements to array`() {
        val output = """
            float[] a = new float[3];
            a[0] = 123f;
            a[1] = 456f;
            a[2] = 789f;
            Console.println(111);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("111", "")
    }

    // -----

    @Test
    fun `write to and read from array with one int element`() {
        val output = """
            int[] a = new int[1];
            a[0] = 123;
            Console.println(a[0]);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `write to and read from array with three int elements`() {
        val output = """
            int[] a = new int[3];
            a[0] = 123;
            a[1] = 456;
            a[2] = 789;
            Console.println(a[0]);
            Console.println(a[1]);
            Console.println(a[2]);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("123", "456", "789", "")
    }

    @Test
    fun `write to and read from array with float element`() {
        val output = """
            float[] a = new float[1];
            a[0] = 123f;
            Console.println(a[0]);
        """.compileAndRunInMainFunction()
        output.lines().matches(v(123f, 0.0001f), v(""))
    }

    @Test
    fun `write to and read from array with three float elements`() {
        val output = """
            float[] a = new float[3];
            a[0] = 123f;
            a[1] = 456f;
            a[2] = 789f;
            Console.println(a[0]);
            Console.println(a[1]);
            Console.println(a[2]);
        """.compileAndRunInMainFunction()
        output.lines().matches(
            v(123f, 0.0001f),
            v(456f, 0.0001f),
            v(789f, 0.0001f),
            v(""))
    }

    @Test
    fun `write to and read from array with boolean element`() {
        val output = """
            boolean[] a = new boolean[1];
            a[0] = true;
            Console.println(a[0]);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("true", "")
    }

    @Test
    fun `write to and read from array with three boolean elements`() {
        val output = """
            boolean[] a = new boolean[3];
            a[0] = true;
            a[1] = false;
            a[2] = true;
            Console.println(a[0]);
            Console.println(a[1]);
            Console.println(a[2]);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("true", "false", "true", "")
    }

    @Test
    fun `array as parameter`() {
        val output = """
            static void writeArray(int[] x) {
                Console.println(x[0]);
                Console.println(x[1]);
                Console.println(x[2]);
            }
            
            public static void main() {
                int[] a = new int[3];
                a[0] = 123;
                a[1] = 456;
                a[2] = 789;
                writeArray(a);
            }
        """.compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly("123", "456", "789", "")
    }

    @Test
    fun `array as return value 1`() {
        val output = """
            static int[] generateArray() {
                int[] a = new int[3];
                a[0] = 123;
                a[1] = 456;
                a[2] = 789;
                return a;
            }
            
            public static void main() {
                int[] x = generateArray();
                Console.println(x[0]);
                Console.println(x[1]);
                Console.println(x[2]);
            }
        """.compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly("123", "456", "789", "")
    }

    @Test
    fun `array as return value 2`() {
        val output = """
            static int[] generateArray() {
                int[] a = new int[3];
                a[0] = 123;
                a[1] = 456;
                a[2] = 789;
                return a;
            }
            
            public static void main() {
                Console.println(generateArray()[0]);
                Console.println(generateArray()[1]);
                Console.println(generateArray()[2]);
            }
        """.compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly("123", "456", "789", "")
    }

    @Test
    fun `complex array expressions 1`() {
        val output = """
            static int[] map(int[] input) {
                int[] a = new int[1];
                a[0] = input[0] * 10;
                return a;
            }
            
            public static void main() {
                int[] a = new int[1];
                a[0] = 123;
            
                Console.println(map(a)[0]);
                Console.println(map(a)[0]);
            }
        """.compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly("1230", "1230", "")
    }

    @Test
    fun `complex array expressions 2`() {
        val output = """
            static int[] map(int[] input) {
                int[] a = new int[3];
                a[0] = input[0] * 10;
                a[1] = input[1] * 10;
                a[2] = input[2] * 10;
                return a;
            }
            
            public static void main() {
                int[] a = new int[3];
                a[0] = 123;
                a[1] = 456;
                a[2] = 789;
            
                Console.println(map(a)[0]);
                Console.println(map(a)[1]);
                Console.println(map(a)[2]);
            }
        """.compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly("1230", "4560", "7890", "")
    }

    @Test
    fun `call imported int array`() {
        val output = """
            int[] a = new int[3];
            a[0] = 123;
            a[1] = 456;
            a[2] = 789;
            Console.println(a);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("[123, 456, 789]", "")
    }

    @Test
    fun `call imported float array`() {
        val output = """
            float[] a = new float[3];
            a[0] = 1.5f;
            a[1] = 2.5f;
            a[2] = 3.5f;
            Console.println(a);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("[1.5, 2.5, 3.5]", "")
    }

    @Test
    fun `call imported boolean array`() {
        val output = """
            boolean[] a = new boolean[3];
            a[0] = true;
            a[1] = false;
            a[2] = true;
            Console.println(a);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("[true, false, true]", "")
    }

    @Test
    fun `create array with non-int size`() {
        assertThatThrownBy {
            """
            int[] a = new int[true];
        """.compileAndRunInMainFunction()
        }.isInstanceOf(IncompatibleTypeException::class.java)
    }

    @Test
    fun `index non-array variable`() {
        assertThatThrownBy {
            """
            boolean a;
            Console.println(a[0]);
        """.compileAndRunInMainFunction()
        }.isInstanceOf(ExpressionIsNotAnArrayException::class.java)
    }

    @Test
    fun `index array with non-int expression`() {
        assertThatThrownBy {
            """
            int[] a = new int[1];
            Console.println(a[true]);
        """.compileAndRunInMainFunction()
        }.isInstanceOf(IncompatibleTypeException::class.java)
    }

    @Test
    fun `array of objects`() {
        val output = Source.withMiniJava("""
            class Point {
                int x;
                int y;
            }
            
            class Main {
                public static void main() {
                    Point[] points = new Point[2];
                    points[0] = new Point();
                    points[1] = new Point();
                    
                    points[0].x = 1;
                    points[0].y = 2;
                    points[1].x = 3;
                    points[1].y = 4;
                    
                    println(points);
                }
                
                native static void println(Point[] points);
            }
        """).andJavaScript("""
            module.exports = function (runtime) {
                return {
                    "Main.println#Point[]": function(pointsRef) {
                        const points = runtime.wasmDeref(pointsRef);
                        console.log(points[0].x);
                        console.log(points[0].y);
                        console.log(points[1].x);
                        console.log(points[1].y);
                    }
                }
            }
        """).compileAndRun()
        assertThat(output.lines()).containsExactly("1", "2", "3", "4", "")
    }
}