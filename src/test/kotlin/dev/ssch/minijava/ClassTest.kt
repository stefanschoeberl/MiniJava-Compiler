package dev.ssch.minijava

import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
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
        """.compileAndRunMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `redefine class`() {
        Assertions.assertThatThrownBy {
            """
            class A {}
            class A {}
        """.compileAndRunMainFunction()
        } // TODO
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
        """.compileAndRunMainFunction()
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
        """.compileAndRunMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `instatiate class`() {
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
        """.compileAndRunMainFunction()
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
        """.compileAndRunMainFunction()
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
        """.compileAndRunMainFunction()
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
        """.compileAndRunMainFunction()
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
        """.compileAndRunMainFunction()
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
        """.compileAndRunMainFunction()
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
        """.compileAndRunMainFunction()
        assertThat(output.lines()).containsExactly("10", "20", "")
    }

    @Test
    fun `declare constructor without parameters`() {
        val output = """
            class Main {
                public static void main() {
                    Console.println(123);
                }
            }
            
            class Point {
                int x;
                int y;
                
                Point() {}
            }
        """.compileAndRunMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `call constructor without parameters`() {
        val output = """
            class Main {
                public static void main() {
                    Point p = new Point();
                }
            }
            
            class Point {
                int x;
                int y;
                
                Point() {
                    Console.println(123);
                }
            }
        """.compileAndRunMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }
}