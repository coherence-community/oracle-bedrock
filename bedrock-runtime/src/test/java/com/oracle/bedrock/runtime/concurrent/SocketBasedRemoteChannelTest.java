/*
 * File: RemoteChannelTest.java
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

package com.oracle.bedrock.runtime.concurrent;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link RemoteChannel}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class SocketBasedRemoteChannelTest
{
    @Test
    public void shouldInjectIntoPublicStaticField() throws Exception
    {
        RemoteChannel channel = mock(RemoteChannel.class);

        TestTypeOne.channel = null;

        RemoteChannel.Injector.injectChannel(TestTypeOne.class, channel);

        assertThat(TestTypeOne.channel, is(sameInstance(channel)));
    }


    @Test
    public void shouldNotInjectIntoPrivateStaticField() throws Exception
    {
        RemoteChannel channel = mock(RemoteChannel.class);

        TestTypeTwo.channel = null;

        RemoteChannel.Injector.injectChannel(TestTypeTwo.class, channel);

        assertThat(TestTypeTwo.channel, is(nullValue()));
    }


    @Test
    public void shouldNotInjectIntoNonStaticField() throws Exception
    {
        RemoteChannel channel = mock(RemoteChannel.class);
        TestTypeThree      type    = new TestTypeThree();

        RemoteChannel.Injector.injectChannel(type.getClass(), channel);

        assertThat(type.channel, is(nullValue()));
    }


    @Test
    public void shouldNotInjectIntoWrongFieldType() throws Exception
    {
        RemoteChannel channel = mock(RemoteChannel.class);

        TestTypeFour.channel = null;

        RemoteChannel.Injector.injectChannel(TestTypeFour.class, channel);

        assertThat(TestTypeFour.channel, is(nullValue()));
    }


    @Test
    public void shouldInjectIntoPublicStaticMethod() throws Exception
    {
        RemoteChannel channel = mock(RemoteChannel.class);

        TestTypeFive.channel = null;

        RemoteChannel.Injector.injectChannel(TestTypeFive.class, channel);

        assertThat(TestTypeFive.channel, is(sameInstance(channel)));
    }


    @Test
    public void shouldNotInjectIntoNonPublicMethod() throws Exception
    {
        RemoteChannel channel = mock(RemoteChannel.class);

        TestTypeSix.channel = null;

        RemoteChannel.Injector.injectChannel(TestTypeSix.class, channel);

        assertThat(TestTypeSix.channel, is(nullValue()));
    }


    @Test
    public void shouldNotInjectIntoNonStaticMethod() throws Exception
    {
        RemoteChannel channel = mock(RemoteChannel.class);

        TestTypeSeven.channel = null;

        RemoteChannel.Injector.injectChannel(TestTypeSeven.class, channel);

        assertThat(TestTypeSeven.channel, is(nullValue()));
    }


    @Test
    public void shouldNotInjectIntoMethodWithWrongParameterType() throws Exception
    {
        RemoteChannel channel = mock(RemoteChannel.class);

        TestTypeEight.channel = null;

        RemoteChannel.Injector.injectChannel(TestTypeEight.class, channel);

        assertThat(TestTypeEight.channel, is(nullValue()));
    }


    @Test
    public void shouldNotInjectIntoMethodWithTooManyParameters() throws Exception
    {
        RemoteChannel channel = mock(RemoteChannel.class);

        TestTypeNine.channel = null;

        RemoteChannel.Injector.injectChannel(TestTypeNine.class, channel);

        assertThat(TestTypeNine.channel, is(nullValue()));
    }


    @Test
    public void shouldInjectIntoMethodWithSuperTypeParameter() throws Exception
    {
        RemoteChannel channel = mock(RemoteChannel.class);

        TestTypeTen.channel = null;

        RemoteChannel.Injector.injectChannel(TestTypeTen.class, channel);

        assertThat(TestTypeTen.channel, is((Object) channel));
    }


    @Test
    public void shouldInjectIntoFieldOfSuperType() throws Exception
    {
        RemoteChannel channel = mock(RemoteChannel.class);

        TestTypeEleven.channel = null;

        RemoteChannel.Injector.injectChannel(TestTypeEleven.class, channel);

        assertThat(TestTypeEleven.channel, is((Object) channel));
    }


    public static class TestTypeOne
    {
        /** Valid annotation use */
        @RemoteChannel.Inject
        public static RemoteChannel channel;
    }

    public static class TestTypeTwo
    {
        /** Invalid annotation use - field is not public */
        @RemoteChannel.Inject
        private static RemoteChannel channel;
    }

    public static class TestTypeThree
    {
        /** Invalid annotation use - field is not static */
        @RemoteChannel.Inject
        public RemoteChannel channel;
    }

    public static class TestTypeFour
    {
        /** Valid annotation use - field is wrong type */
        @RemoteChannel.Inject
        public static String channel;
    }

    public static class TestTypeFive
    {
        private static RemoteChannel channel;

        /** Valid annotation use */
        @RemoteChannel.Inject
        public static void setEventChannel(RemoteChannel eventChannel)
        {
            channel = eventChannel;
        }
    }

    public static class TestTypeSix
    {
        private static RemoteChannel channel;

        /** Invalid annotation use - method is not public */
        @RemoteChannel.Inject
        private static void setEventChannel(RemoteChannel eventChannel)
        {
            channel = eventChannel;
        }
    }

    public static class TestTypeSeven
    {
        private static RemoteChannel channel;

        /** Invalid annotation use - method is not static */
        @RemoteChannel.Inject
        public void setEventChannel(RemoteChannel eventChannel)
        {
            channel = eventChannel;
        }
    }

    public static class TestTypeEight
    {
        private static String channel;

        /** Invalid annotation use - method has wrong parameter type */
        @RemoteChannel.Inject
        public static void setEventChannel(String eventChannel)
        {
            channel = eventChannel;
        }
    }

    public static class TestTypeNine
    {
        private static Object channel;

        /** Invalid annotation use - method has too many parameters */
        @RemoteChannel.Inject
        public static void setEventChannel(RemoteChannel eventChannel, String foo)
        {
            channel = eventChannel;
        }
    }

    public static class TestTypeTen
    {
        private static Object channel;

        /** Valid annotation use - method parameter is super class of RemoteEventChannel */
        @RemoteChannel.Inject
        public static void setEventChannel(Object eventChannel)
        {
            channel = eventChannel;
        }
    }

    public static class TestTypeEleven
    {
        /** Valid annotation use - field type is superclass of RemoteEventChannel.Publisher */
        @RemoteChannel.Inject
        public static Object channel;
    }


}
