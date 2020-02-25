package dev.ssch.minijava

import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
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

    @Disabled
    @Test
    fun `write to and read from array with one element`() {
        val output = """
            int[] a = new int[1];
            a[0] = 123;
            println(a[0]);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

    // TODO: Array as parameter, returnvalue
    // TODO: Array Exceptions
}