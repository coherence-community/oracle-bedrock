/*
 * File: AbstractPlatformTest.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of 
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting https://oss.oracle.com/licenses/CDDL
 *
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file LICENSE.txt.
 *
 * MODIFICATIONS:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 */

package com.oracle.tools.runtime;

import com.oracle.tools.runtime.java.SimpleJavaApplication;
import com.oracle.tools.runtime.java.SimpleJavaApplicationSchema;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;

import static org.junit.Assert.assertThat;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.net.InetAddress;

/**
 * @author jk 2014.06.27
 */
public class AbstractPlatformTest
{
    @Test
    public void shouldHaveCorrectName() throws Exception
    {
        AbstractPlatform platform = new AbstractPlatformStub("Test");

        assertThat(platform.getName(), is("Test"));
    }


    @Test
    public void shouldHaveCorrectAddress() throws Exception
    {
        AbstractPlatform stub     = new AbstractPlatformStub("Test");
        AbstractPlatform platform = spy(stub);
        InetAddress      address  = mock(InetAddress.class);

        when(platform.getAddress()).thenReturn(address);

        assertThat(platform.getAddress(), is(sameInstance(address)));
    }


    @Test
    @SuppressWarnings("unchecked")
    public void shouldRealizeApplicationWithCorrectBuilder() throws Exception
    {
        ApplicationBuilder          builder  = mock(ApplicationBuilder.class);
        ApplicationConsole          console  = mock(ApplicationConsole.class);
        SimpleJavaApplicationSchema schema   = new SimpleJavaApplicationSchema("Dummy");
        SimpleJavaApplication       app      = mock(SimpleJavaApplication.class);

        AbstractPlatform            stub     = new AbstractPlatformStub("Test");
        AbstractPlatform            platform = spy(stub);

        when(platform.getApplicationBuilder(SimpleJavaApplication.class)).thenReturn(builder);
        when(builder.realize(same(schema), eq("TestApp"), same(console), same(platform))).thenReturn(app);

        SimpleJavaApplication result = platform.realize("TestApp", schema, console);

        assertThat(result, is(sameInstance(app)));
    }


    @Test
    public void shouldNotRealizeApplicationIfBuilderIsNull() throws Exception
    {
        ApplicationConsole          console  = mock(ApplicationConsole.class);
        SimpleJavaApplicationSchema schema   = new SimpleJavaApplicationSchema("Dummy");

        AbstractPlatform            stub     = new AbstractPlatformStub("Test");
        AbstractPlatform            platform = spy(stub);

        when(platform.getApplicationBuilder(SimpleJavaApplication.class)).thenReturn(null);

        SimpleJavaApplication result = platform.realize("TestApp", schema, console);

        assertThat(result, is(nullValue()));
    }


    /**
     * An abstract {@link Platform} stub.
     */
    public static class AbstractPlatformStub extends AbstractPlatform
    {
        /**
         * Constructs an {@link AbstractPlatformStub}.
         *
         * @param name  the name of the {@link Platform}
         */
        public AbstractPlatformStub(String name)
        {
            super(name);
        }


        @Override
        public InetAddress getAddress()
        {
            return null;
        }


        @Override
        public <A extends Application,
                B extends ApplicationBuilder<A>> B getApplicationBuilder(Class<A> applicationClass)
        {
            return null;
        }
    }
}
