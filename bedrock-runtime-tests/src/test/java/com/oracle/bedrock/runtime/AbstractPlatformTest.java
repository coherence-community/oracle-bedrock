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

package com.oracle.bedrock.runtime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

import static org.junit.Assert.assertThat;

import static org.mockito.Matchers.eq;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * Integration tests for the {@link AbstractPlatform} class.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class AbstractPlatformTest
{
//    @Test
//    public void shouldHaveCorrectName() throws Exception
//    {
//        AbstractPlatform platform = new AbstractPlatformStub("Test");
//
//        assertThat(platform.getName(), is("Test"));
//    }
//
//
//    @Test
//    public void shouldHaveCorrectAddress() throws Exception
//    {
//        AbstractPlatform stub     = new AbstractPlatformStub("Test");
//        AbstractPlatform platform = spy(stub);
//        InetAddress      address  = mock(InetAddress.class);
//
//        when(platform.getAddress()).thenReturn(address);
//
//        assertThat(platform.getAddress(), is(sameInstance(address)));
//    }
//
//
//    @Test
//    public void shouldNotRealizeApplicationIfBuilderSupplierIsNull() throws Exception
//    {
//        ApplicationConsole console  = mock(ApplicationConsole.class);
//        AbstractPlatform   stub     = new AbstractPlatformStub("Test");
//        AbstractPlatform   platform = spy(stub);
//
//        Application        result   = platform.launch(Application.class);
//
//        assertThat(result, is(nullValue()));
//    }
//
//
//    @Test
//    public void shouldNotRealizeApplicationIfBuilderIsNull() throws Exception
//    {
//        ApplicationConsole          console  = mock(ApplicationConsole.class);
//        ApplicationBuilder.Supplier supplier = mock(ApplicationBuilder.Supplier.class);
//        AbstractPlatform            stub     = new AbstractPlatformStub("Test");
//        AbstractPlatform            platform = spy(stub);
//
//        stub.getOptions().add(supplier);
//
//        when(supplier.getApplicationLauncher(same(platform), eq(Application.class))).thenReturn(null);
//
//        Application result = platform.launch(Application.class);
//
//        assertThat(result, is(nullValue()));
//    }
//
//
//    /**
//     * An abstract {@link Platform} stub.
//     */
//    public static class AbstractPlatformStub<P extends Platform> extends AbstractPlatform<P>
//    {
//        /**
//         * Constructs an {@link AbstractPlatformStub}.
//         *
//         * @param name  the name of the {@link Platform}
//         */
//        public AbstractPlatformStub(String name)
//        {
//            super(name);
//        }
//
//
//        @Override
//        public InetAddress getAddress()
//        {
//            return null;
//        }
//    }
}
