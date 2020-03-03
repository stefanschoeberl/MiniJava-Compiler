package dev.ssch.minijava

import dev.ssch.minijava.exception.ExpressionIsNotAnArrayException
import dev.ssch.minijava.exception.IncompatibleTypeException
import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ArrayTest : CompilerTest() {

    @Test
    fun `create int array`() {
        val output = """
            int[] a = new int[1];
            println(111);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("111", "")
    }

    @Test
    fun `create float array`() {
        val output = """
            float[] a = new float[1];
            println(111);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("111", "")
    }

    @Test
    fun `create boolean array`() {
        val output = """
            boolean[] a = new boolean[1];
            println(111);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("111", "")
    }

    // -----

    @Test
    fun `write one int element to array`() {
        val output = """
            int[] a = new int[1];
            a[0] = 123;
            println(111);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("111", "")
    }

    @Test
    fun `write three int elements to array`() {
        val output = """
            int[] a = new int[3];
            a[0] = 123;
            a[1] = 456;
            a[2] = 789;
            println(111);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("111", "")
    }

    @Test
    fun `write one float element to array`() {
        val output = """
            float[] a = new float[1];
            a[0] = 123f;
            println(111);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("111", "")
    }

    @Test
    fun `write three float elements to array`() {
        val output = """
            float[] a = new float[3];
            a[0] = 123f;
            a[1] = 456f;
            a[2] = 789f;
            println(111);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("111", "")
    }

    // -----

    @Test
    fun `write to and read from array with one int element`() {
        val output = """
            int[] a = new int[1];
            a[0] = 123;
            println(a[0]);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `write to and read from array with three int elements`() {
        val output = """
            int[] a = new int[3];
            a[0] = 123;
            a[1] = 456;
            a[2] = 789;
            println(a[0]);
            println(a[1]);
            println(a[2]);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("123", "456", "789", "")
    }

    @Test
    fun `write to and read from array with float element`() {
        val output = """
            float[] a = new float[1];
            a[0] = 123f;
            println(a[0]);
        """.runInMainFunction()
        output.lines().matches(v(123f, 0.0001f), v(""))
    }

    @Test
    fun `write to and read from array with three float elements`() {
        val output = """
            float[] a = new float[3];
            a[0] = 123f;
            a[1] = 456f;
            a[2] = 789f;
            println(a[0]);
            println(a[1]);
            println(a[2]);
        """.runInMainFunction()
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
            println(a[0]);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("true", "")
    }

    @Test
    fun `write to and read from array with three boolean elements`() {
        val output = """
            boolean[] a = new boolean[3];
            a[0] = true;
            a[1] = false;
            a[2] = true;
            println(a[0]);
            println(a[1]);
            println(a[2]);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("true", "false", "true", "")
    }

    @Test
    fun `array as parameter`() {
        val output = """
            void writeArray(int[] x) {
                println(x[0]);
                println(x[1]);
                println(x[2]);
            }
            
            public void main() {
                int[] a = new int[3];
                a[0] = 123;
                a[1] = 456;
                a[2] = 789;
                writeArray(a);
            }
        """.compileAndRunMainFunction()
        assertThat(output.lines()).containsExactly("123", "456", "789", "")
    }

    @Test
    fun `array as return value 1`() {
        val output = """
            int[] generateArray() {
                int[] a = new int[3];
                a[0] = 123;
                a[1] = 456;
                a[2] = 789;
                return a;
            }
            
            public void main() {
                int[] x = generateArray();
                println(x[0]);
                println(x[1]);
                println(x[2]);
            }
        """.compileAndRunMainFunction()
        assertThat(output.lines()).containsExactly("123", "456", "789", "")
    }

    @Test
    fun `array as return value 2`() {
        val output = """
            int[] generateArray() {
                int[] a = new int[3];
                a[0] = 123;
                a[1] = 456;
                a[2] = 789;
                return a;
            }
            
            public void main() {
                println(generateArray()[0]);
                println(generateArray()[1]);
                println(generateArray()[2]);
            }
        """.compileAndRunMainFunction()
        assertThat(output.lines()).containsExactly("123", "456", "789", "")
    }

    @Test
    fun `complex array expressions 1`() {
        val output = """
            int[] map(int[] input) {
                int[] a = new int[1];
                a[0] = input[0] * 10;
                return a;
            }
            
            public void main() {
                int[] a = new int[1];
                a[0] = 123;
            
                println(map(a)[0]);
                println(map(a)[0]);
            }
        """.compileAndRunMainFunction()
        assertThat(output.lines()).containsExactly("1230", "1230", "")
    }

    @Test
    fun `complex array expressions 2`() {
        val output = """
            int[] map(int[] input) {
                int[] a = new int[3];
                a[0] = input[0] * 10;
                a[1] = input[1] * 10;
                a[2] = input[2] * 10;
                return a;
            }
            
            public void main() {
                int[] a = new int[3];
                a[0] = 123;
                a[1] = 456;
                a[2] = 789;
            
                println(map(a)[0]);
                println(map(a)[1]);
                println(map(a)[2]);
            }
        """.compileAndRunMainFunction()
        assertThat(output.lines()).containsExactly("1230", "4560", "7890", "")
    }

    @Test
    fun `call imported int array`() {
        val output = """
            int[] a = new int[3];
            a[0] = 123;
            a[1] = 456;
            a[2] = 789;
            println(a);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("[123, 456, 789]", "")
    }

    @Test
    fun `call imported float array`() {
        val output = """
            float[] a = new float[3];
            a[0] = 1.5f;
            a[1] = 2.5f;
            a[2] = 3.5f;
            println(a);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("[1.5, 2.5, 3.5]", "")
    }

    @Test
    fun `call imported boolean array`() {
        val output = """
            boolean[] a = new boolean[3];
            a[0] = true;
            a[1] = false;
            a[2] = true;
            println(a);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("[true, false, true]", "")
    }

    @Test
    fun `create array with non-int size`() {
        Assertions.assertThatThrownBy {
            """
            int[] a = new int[true];
        """.runInMainFunction()
        }.isInstanceOf(IncompatibleTypeException::class.java)
    }

    @Test
    fun `index non-array variable`() {
        Assertions.assertThatThrownBy {
            """
            boolean a;
            println(a[0]);
        """.runInMainFunction()
        }.isInstanceOf(ExpressionIsNotAnArrayException::class.java)
    }

    @Test
    fun `index array with non-int expression`() {
        Assertions.assertThatThrownBy {
            """
            int[] a = new int[1];
            println(a[true]);
        """.runInMainFunction()
        }.isInstanceOf(IncompatibleTypeException::class.java)
    }
}