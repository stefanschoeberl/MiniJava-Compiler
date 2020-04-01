package dev.ssch.minijava.compiler.expressions

import dev.ssch.minijava.compiler.exception.InvalidBinaryOperationException
import dev.ssch.minijava.compiler.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class EqualNotEqualReferencesTest : CompilerTest() {

    @Test
    fun `== two references 1`() {
        val output = """
            Main a = new Main();
            Main b = a;
            Console.println(a == b);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("true", "")
    }

    @Test
    fun `== two references 2`() {
        val output = """
            Main a = new Main();
            Main b = new Main();
            Console.println(a == b);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("false", "")
    }

    @Test
    fun `== with null 1`() {
        val output = """
            Main a = new Main();
            Console.println(a == null);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("false", "")
    }

    @Test
    fun `== with null 2`() {
        val output = """
            Main a = null;
            Console.println(a == null);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("true", "")
    }

    @Test
    fun `== with null 3`() {
        val output = """
            Main a = new Main();
            Console.println(null == a);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("false", "")
    }

    @Test
    fun `== with null 4`() {
        val output = """
            Main a = null;
            Console.println(null == a);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("true", "")
    }

    @Test
    fun `== with null 5`() {
        val output = """
            Console.println(null == null);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("true", "")
    }

    @Test
    fun `== two references (different type)`() {
        assertThatThrownBy {
            """
                class Other {}
                class Main {
                    public static void main() {
                        Main a = new Main();
                        Other b = new Other();
                        Console.println(a == b);
                    }
                }
            """.compileAndRun()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }
    
    // --

    @Test
    fun `!= two references 1`() {
        val output = """
            Main a = new Main();
            Main b = a;
            Console.println(a != b);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("false", "")
    }

    @Test
    fun `!= two references 2`() {
        val output = """
            Main a = new Main();
            Main b = new Main();
            Console.println(a != b);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("true", "")
    }

    @Test
    fun `!= with null 1`() {
        val output = """
            Main a = new Main();
            Console.println(a != null);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("true", "")
    }

    @Test
    fun `!= with null 2`() {
        val output = """
            Main a = null;
            Console.println(a != null);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("false", "")
    }

    @Test
    fun `!= with null 3`() {
        val output = """
            Main a = new Main();
            Console.println(null != a);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("true", "")
    }

    @Test
    fun `!= with null 4`() {
        val output = """
            Main a = null;
            Console.println(null != a);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("false", "")
    }

    @Test
    fun `!= with null 5`() {
        val output = """
            Console.println(null != null);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("false", "")
    }

    @Test
    fun `!= two references (different type)`() {
        assertThatThrownBy {
            """
                class Other {}
                class Main {
                    public static void main() {
                        Main a = new Main();
                        Other b = new Other();
                        Console.println(a != b);
                    }
                }
            """.compileAndRun()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }
}