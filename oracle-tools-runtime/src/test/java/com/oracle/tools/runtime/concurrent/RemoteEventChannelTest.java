/*
 * File: RemoteEventChannelTest.java
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

package com.oracle.tools.runtime.concurrent;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link RemoteEventChannel}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class RemoteEventChannelTest
{
    @Test
    public void shouldInjectIntoPublicStaticField() throws Exception
    {
        RemoteEventChannel.Publisher channel = mock(RemoteEventChannel.Publisher.class);

        TestTypeOne.channel = null;

        RemoteEventChannel.Injector.injectPublisher(TestTypeOne.class, channel);

        assertThat(TestTypeOne.channel, is(sameInstance(channel)));
    }


    @Test
    public void shouldNotInjectIntoPrivateStaticField() throws Exception
    {
        RemoteEventChannel.Publisher channel = mock(RemoteEventChannel.Publisher.class);

        TestTypeTwo.channel = null;

        RemoteEventChannel.Injector.injectPublisher(TestTypeTwo.class, channel);

        assertThat(TestTypeTwo.channel, is(nullValue()));
    }


    @Test
    public void shouldNotInjectIntoNonStaticField() throws Exception
    {
        RemoteEventChannel.Publisher channel = mock(RemoteEventChannel.Publisher.class);
        TestTypeThree      type    = new TestTypeThree();

        RemoteEventChannel.Injector.injectPublisher(type.getClass(), channel);

        assertThat(type.channel, is(nullValue()));
    }


    @Test
    public void shouldNotInjectIntoWrongFieldType() throws Exception
    {
        RemoteEventChannel.Publisher channel = mock(RemoteEventChannel.Publisher.class);

        TestTypeFour.channel = null;

        RemoteEventChannel.Injector.injectPublisher(TestTypeFour.class, channel);

        assertThat(TestTypeFour.channel, is(nullValue()));
    }


    @Test
    public void shouldInjectIntoPublicStaticMethod() throws Exception
    {
        RemoteEventChannel.Publisher channel = mock(RemoteEventChannel.Publisher.class);

        TestTypeFive.channel = null;

        RemoteEventChannel.Injector.injectPublisher(TestTypeFive.class, channel);

        assertThat(TestTypeFive.channel, is(sameInstance(channel)));
    }


    @Test
    public void shouldNotInjectIntoNonPublicMethod() throws Exception
    {
        RemoteEventChannel.Publisher channel = mock(RemoteEventChannel.Publisher.class);

        TestTypeSix.channel = null;

        RemoteEventChannel.Injector.injectPublisher(TestTypeSix.class, channel);

        assertThat(TestTypeSix.channel, is(nullValue()));
    }


    @Test
    public void shouldNotInjectIntoNonStaticMethod() throws Exception
    {
        RemoteEventChannel.Publisher channel = mock(RemoteEventChannel.Publisher.class);

        TestTypeSeven.channel = null;

        RemoteEventChannel.Injector.injectPublisher(TestTypeSeven.class, channel);

        assertThat(TestTypeSeven.channel, is(nullValue()));
    }


    @Test
    public void shouldNotInjectIntoMethodWithWrongParameterType() throws Exception
    {
        RemoteEventChannel.Publisher channel = mock(RemoteEventChannel.Publisher.class);

        TestTypeEight.channel = null;

        RemoteEventChannel.Injector.injectPublisher(TestTypeEight.class, channel);

        assertThat(TestTypeEight.channel, is(nullValue()));
    }


    @Test
    public void shouldNotInjectIntoMethodWithTooManyParameters() throws Exception
    {
        RemoteEventChannel.Publisher channel = mock(RemoteEventChannel.Publisher.class);

        TestTypeNine.channel = null;

        RemoteEventChannel.Injector.injectPublisher(TestTypeNine.class, channel);

        assertThat(TestTypeNine.channel, is(nullValue()));
    }


    @Test
    public void shouldInjectIntoMethodWithSuperTypeParameter() throws Exception
    {
        RemoteEventChannel.Publisher channel = mock(RemoteEventChannel.Publisher.class);

        TestTypeTen.channel = null;

        RemoteEventChannel.Injector.injectPublisher(TestTypeTen.class, channel);

        assertThat(TestTypeTen.channel, is((Object) channel));
    }


    @Test
    public void shouldInjectIntoFieldOfSuperType() throws Exception
    {
        RemoteEventChannel.Publisher channel = mock(RemoteEventChannel.Publisher.class);

        TestTypeEleven.channel = null;

        RemoteEventChannel.Injector.injectPublisher(TestTypeEleven.class, channel);

        assertThat(TestTypeEleven.channel, is((Object) channel));
    }


    public static class TestTypeOne
    {
        /** Valid annotation use */
        @RemoteEventChannel.InjectPublisher
        public static RemoteEventChannel.Publisher channel;
    }

    public static class TestTypeTwo
    {
        /** Invalid annotation use - field is not public */
        @RemoteEventChannel.InjectPublisher
        private static RemoteEventChannel.Publisher channel;
    }

    public static class TestTypeThree
    {
        /** Invalid annotation use - field is not static */
        @RemoteEventChannel.InjectPublisher
        public RemoteEventChannel.Publisher channel;
    }

    public static class TestTypeFour
    {
        /** Valid annotation use - field is wrong type */
        @RemoteEventChannel.InjectPublisher
        public static RemoteExecutor channel;
    }

    public static class TestTypeFive
    {
        private static RemoteEventChannel.Publisher channel;

        /** Valid annotation use */
        @RemoteEventChannel.InjectPublisher
        public static void setEventChannel(RemoteEventChannel.Publisher eventChannel)
        {
            channel = eventChannel;
        }
    }

    public static class TestTypeSix
    {
        private static RemoteEventChannel.Publisher channel;

        /** Invalid annotation use - method is not public */
        @RemoteEventChannel.InjectPublisher
        private static void setEventChannel(RemoteEventChannel.Publisher eventChannel)
        {
            channel = eventChannel;
        }
    }

    public static class TestTypeSeven
    {
        private static RemoteEventChannel.Publisher channel;

        /** Invalid annotation use - method is not static */
        @RemoteEventChannel.InjectPublisher
        public void setEventChannel(RemoteEventChannel.Publisher eventChannel)
        {
            channel = eventChannel;
        }
    }

    public static class TestTypeEight
    {
        private static Object channel;

        /** Invalid annotation use - method has wrong parameter type */
        @RemoteEventChannel.InjectPublisher
        public static void setEventChannel(RemoteExecutor eventChannel)
        {
            channel = eventChannel;
        }
    }

    public static class TestTypeNine
    {
        private static Object channel;

        /** Invalid annotation use - method has too many parameters */
        @RemoteEventChannel.InjectPublisher
        public static void setEventChannel(RemoteEventChannel.Publisher eventChannel, String foo)
        {
            channel = eventChannel;
        }
    }

    public static class TestTypeTen
    {
        private static Object channel;

        /** Valid annotation use - method parameter is super class of RemoteEventChannel */
        @RemoteEventChannel.InjectPublisher
        public static void setEventChannel(Object eventChannel)
        {
            channel = eventChannel;
        }
    }

    public static class TestTypeEleven
    {
        /** Valid annotation use - field type is superclass of RemoteEventChannel.Publisher */
        @RemoteEventChannel.InjectPublisher
        public static Object channel;
    }


}
