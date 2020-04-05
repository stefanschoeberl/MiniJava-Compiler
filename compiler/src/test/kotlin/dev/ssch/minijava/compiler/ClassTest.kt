package dev.ssch.minijava.compiler

import dev.ssch.minijava.compiler.exception.*
import dev.ssch.minijava.compiler.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ClassTest : CompilerTest() {

    @Test
    fun `call static method of other class`() {
        val output = """
            class Main {
                public static void main() {
                    Other.doSomething(123);
                }
            }
            
            class Other {
                static void doSomething(int x) {
                    Console.println(x);
                }
            }
        """.compileAndRun()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `redefine class`() {
        assertThatThrownBy {
            """
            class A {}
            class A {}
        """.compileAndRun()
        }.isInstanceOf(RedefinedClassException::class.java)
    }

    @Test
    fun `declare fields`() {
        val output = """
            class Main {
                public static void main() {
                    Console.println(123);
                }
            }
            
            class Point {
                int x;
                int y;
            }
        """.compileAndRun()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `declare variable of class`() {
        val output = """
            class Main {
                public static void main() {
                    Point p;
                    Console.println(123);
                }
            }
            
            class Point {
                int x;
                int y;
            }
        """.compileAndRun()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `instantiate class`() {
        val output = """
            class Main {
                public static void main() {
                    Point p = new Point();
                    Console.println(123);
                }
            }
            
            class Point {
                int x;
                int y;
            }
        """.compileAndRun()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `assign value to field of class`() {
        val output = """
            class Main {
                public static void main() {
                    Point p = new Point();
                    p.x = 10;
                    Console.println(123);
                }
            }
            
            class Point {
                int x;
                int y;
            }
        """.compileAndRun()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `assign value to and read from field of class`() {
        val output = """
            class Main {
                public static void main() {
                    Point p = new Point();
                    p.x = 10;
                    Console.println(p.x);
                }
            }
            
            class Point {
                int x;
                int y;
            }
        """.compileAndRun()
        assertThat(output.lines()).containsExactly("10", "")
    }

    @Test
    fun `assign value to and read from multiple fields of class`() {
        val output = """
            class Main {
                public static void main() {
                    Point p = new Point();
                    p.x = 10;
                    p.y = 20;
                    p.x = 30;
                    p.y = 40;
                    Console.println(p.x);
                    Console.println(p.y);
                }
            }
            
            class Point {
                int x;
                int y;
            }
        """.compileAndRun()
        assertThat(output.lines()).containsExactly("30", "40", "")
    }

    @Test
    fun `assign value to and read from multiple fields of class (mixed types)`() {
        val output = """
            class Main {
                public static void main() {
                    Data data = new Data();
                    data.a = true;
                    data.b = 10;
                    data.c = false;
                    data.d = 2.0f;
                    data.a = false;
                    data.b = 100;
                    data.c = true;
                    data.d = -32.0f;
                    Console.println(data.a);
                    Console.println(data.b);
                    Console.println(data.c);
                    Console.println(data.d);
                }
            }
            
            class Data {
                boolean a;
                int b;
                boolean c;
                float d;
            }
        """.compileAndRun()
        output.lines().matches(
            v(false),
            v(100),
            v(true),
            v(-32f, 0.0001f),
            v("")
        )
    }

    @Test
    fun `pass object as parameter`() {
        val output = """
            class Main {
                static void writePoint(Point point) {
                    Console.println(point.x);
                    Console.println(point.y);
                }
            
                public static void main() {
                    Point p = new Point();
                    p.x = 10;
                    p.y = 20;
                    writePoint(p);
                }
            }
            
            class Point {
                int x;
                int y;
            }
        """.compileAndRun()
        assertThat(output.lines()).containsExactly("10", "20", "")
    }

    @Test
    fun `pass object as return value`() {
        val output = """
            class Main {
                static Point generatePoint() {
                    Point p = new Point();
                    p.x = 10;
                    p.y = 20;
                    return p;
                }
            
                public static void main() {
                    Point p = generatePoint();
                    Console.println(p.x);
                    Console.println(p.y);
                }
            }
            
            class Point {
                int x;
                int y;
            }
        """.compileAndRun()
        assertThat(output.lines()).containsExactly("10", "20", "")
    }

    @Test
    fun `nested objects`() {
        val output = Source.withMiniJava("""
            class A {
                B b;
            }
            
            class B {
                int value;
            }
            
            class Main {
                public static void main() {
                    A a = new A();
                    B b = new B();
                    a.b = b;
                    b.value = 123;
                    println(a);
                }
                
                native static void println(A a);
            }
        """).andJavaScript("""
            module.exports = function (runtime) {
                return {
                    "Main.println#A": function(aRef) {
                        const a = runtime.wasmDeref(aRef);
                        console.log(a.b.value);
                    }
                }
            }
        """).compileAndRun()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `nested arrays`() {
        val output = Source.withMiniJava("""
            class A {
                int[] a;
            }
            
            class Main {
                public static void main() {
                    A a = new A();
                    a.a = new int[1];
                    a.a[0] = 123;
                    println(a);
                }
                
                native static void println(A a);
            }
        """).andJavaScript("""
            module.exports = function (runtime) {
                return {
                    "Main.println#A": function(aRef) {
                        const a = runtime.wasmDeref(aRef);
                        console.log(a.a[0]);
                    }
                }
            }
        """).compileAndRun()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `create instance of unknown type`() {
        assertThatThrownBy {
        """
            Console.println(new Type());
        """.compileAndRunInMainFunction()
        }.isInstanceOf(UnknownTypeException::class.java)
    }

    @Test
    fun `create instance of a primitive type`() {
        assertThatThrownBy {
        """
            Console.println(new int());
        """.compileAndRunInMainFunction()
        }.isInstanceOf(NotAReferenceTypeException::class.java)
    }

    @Test
    fun `define field of unknown type`() {
        assertThatThrownBy {
        """
            Type a;
            public static void main() {}
        """.compileAndRunInMainClass()
        }.isInstanceOf(UnknownTypeException::class.java)
    }

    @Test
    fun `redefine field (same type)`() {
        assertThatThrownBy {
        """
            int a;
            int a;
            public static void main() {}
        """.compileAndRunInMainClass()
        }.isInstanceOf(RedefinedFieldException::class.java)
    }

    @Test
    fun `redefine field (different type)`() {
        assertThatThrownBy {
        """
            int a;
            float a;
            public static void main() {}
        """.compileAndRunInMainClass()
        }.isInstanceOf(RedefinedFieldException::class.java)
    }

    @Test
    fun `redefine constructor`() {
        assertThatThrownBy {
        """
            Main(int x) {}
            Main(int y) {}
            public static void main() {}
        """.compileAndRunInMainClass()
        }.isInstanceOf(RedefinedConstructorException::class.java)
    }

    @Test
    fun `define constructor with wrong type name`() {
        assertThatThrownBy {
        """
            Abc(int x) {}
            public static void main() {}
        """.compileAndRunInMainClass()
        }.isInstanceOf(InvalidConstructorNameException::class.java)
    }
}