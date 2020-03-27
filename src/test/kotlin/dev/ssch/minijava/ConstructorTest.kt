package dev.ssch.minijava

import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ConstructorTest : CompilerTest() {

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
        """.compileAndRun()
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
        """.compileAndRun()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `call constructor with parameters`() {
        val output = """
            class Main {
                public static void main() {
                    Point p = new Point(123, 456);
                }
            }
            
            class Point {
                int x;
                int y;
                
                Point(int a, int b) {
                    Console.println(a);
                    Console.println(b);
                }
            }
        """.compileAndRun()
        assertThat(output.lines()).containsExactly("123", "456", "")
    }

    @Test
    fun `access fields in constructor 1`() {
        val output = """
            class Main {
                public static void main() {
                    Point p = new Point(123);
                    Console.println(p.x);
                    Console.println(p.y);
                }
            }
            
            class Point {
                int x;
                int y;
                
                Point(int a) {
                    x = a;
                    y = x;
                }
            }
        """.compileAndRun()
        assertThat(output.lines()).containsExactly("123", "123", "")
    }

    @Test
    fun `access fields in constructor 2`() {
        val output = """
            class Main {
                public static void main() {
                    Point p = new Point(123, 456);
                    Console.println(p.x);
                    Console.println(p.y);
                }
            }
            
            class Point {
                int x;
                int y;
                
                Point(int a, int b) {
                    x = a;
                    y = b;
                    Console.println(x);
                    Console.println(y);
                    x = y;
                    y = a;
                    Console.println(x);
                    Console.println(y);
                    y = x;
                    x = a;
                    Console.println(x);
                    Console.println(y);
                }
            }
        """.compileAndRun()
        assertThat(output.lines()).containsExactly("123", "456", "456", "123", "123", "456", "123", "456", "")
    }

    @Test
    fun `access fields in constructor with this`() {
        val output = """
            class Main {
                public static void main() {
                    Point p = new Point(123, 456);
                    Console.println(p.x);
                    Console.println(p.y);
                }
            }
            
            class Point {
                int x;
                int y;
                
                Point(int x, int y) {
                    this.x = x;
                    this.y = y;
                    Console.println(this.x);
                    Console.println(this.y);
                    this.x = this.y;
                    this.y = x;
                    Console.println(this.x);
                    Console.println(this.y);
                    this.y = this.x;
                    this.x = x;
                    Console.println(this.x);
                    Console.println(this.y);
                }
            }
        """.compileAndRun()
        assertThat(output.lines()).containsExactly("123", "456", "456", "123", "123", "456", "123", "456", "")
    }
}