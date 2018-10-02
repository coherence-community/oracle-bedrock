package com.oracle.bedrock.util;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author jk  2018.10.02
 */
public class ProxyHelperTest
{
    @Test
    public void shouldProxyMethodWithVarArgs() throws Throwable
    {
        ProxyHelper.Interceptor interceptor = mock(ProxyHelper.Interceptor.class);
        Stub                    stub        = ProxyHelper.createProxyOf(Stub.class, interceptor);
        Method                  method      = Stub.class.getMethod("testMethod", int.class, String[].class);

        when(interceptor.intercept(any(Method.class), nullable(Object[].class))).thenReturn(19);

        assertThat(stub.testMethod(1, "a", "b"), is(19));
        verify(interceptor).intercept(eq(method), eq(new Object[]{1, new String[]{"a", "b"}}));
    }

    @Test
    public void shouldProxyMethodWithOneVarArgsArg() throws Throwable
    {
        ProxyHelper.Interceptor interceptor = mock(ProxyHelper.Interceptor.class);
        Stub                    stub        = ProxyHelper.createProxyOf(Stub.class, interceptor);
        Method                  method      = Stub.class.getMethod("testMethod", int.class, String[].class);

        when(interceptor.intercept(any(Method.class), nullable(Object[].class))).thenReturn(19);

        assertThat(stub.testMethod(1, "a"), is(19));
        verify(interceptor).intercept(eq(method), eq(new Object[]{1, new String[]{"a"}}));
    }

    @Test
    public void shouldProxyMethodWithZeroVarArgsArgs() throws Throwable
    {
        ProxyHelper.Interceptor interceptor = mock(ProxyHelper.Interceptor.class);
        Stub                    stub        = ProxyHelper.createProxyOf(Stub.class, interceptor);
        Method                  method      = Stub.class.getMethod("testMethod", int.class, String[].class);

        when(interceptor.intercept(any(Method.class), nullable(Object[].class))).thenReturn(19);

        assertThat(stub.testMethod(1), is(19));
        verify(interceptor).intercept(eq(method), eq(new Object[]{1, new String[0]}));
    }

    @Test
    public void shouldProxyMethodWithOneNullVarArgsArg() throws Throwable
    {
        ProxyHelper.Interceptor interceptor = mock(ProxyHelper.Interceptor.class);
        Stub                    stub        = ProxyHelper.createProxyOf(Stub.class, interceptor);
        Method                  method      = Stub.class.getMethod("testMethod", int.class, String[].class);

        when(interceptor.intercept(any(Method.class), nullable(Object[].class))).thenReturn(19);

        assertThat(stub.testMethod(1, (String) null), is(19));
        verify(interceptor).intercept(eq(method), eq(new Object[]{1, new String[]{null}}));
    }

    @Test
    public void shouldProxyMethodWithNullVarArgs() throws Throwable
    {
        ProxyHelper.Interceptor interceptor = mock(ProxyHelper.Interceptor.class);
        Stub                    stub        = ProxyHelper.createProxyOf(Stub.class, interceptor);
        Method                  method      = Stub.class.getMethod("testMethod", int.class, String[].class);

        when(interceptor.intercept(any(Method.class), nullable(Object[].class))).thenReturn(19);

        assertThat(stub.testMethod(1, (String[]) null), is(19));
        verify(interceptor).intercept(eq(method), eq(new Object[]{1, new String[]{null}}));
    }

    public static class Stub
    {
        public int testMethod(int i, String... s)
        {
            return s == null ? 0 : s.length;
        }
    }
}
