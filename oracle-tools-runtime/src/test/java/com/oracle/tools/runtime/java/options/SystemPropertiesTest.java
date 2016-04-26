/*
 * File: SystemPropertiesTest.java
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

package com.oracle.tools.runtime.java.options;

import com.oracle.tools.Options;
import com.oracle.tools.options.Decoration;
import com.oracle.tools.runtime.LocalPlatform;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link SystemProperties}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class SystemPropertiesTest
{
    @Test
    public void shouldCallResolveHandlers() throws Exception
    {
        SystemProperty.ResolveHandler handler1 = mock(SystemProperty.ResolveHandler.class);
        SystemProperty.ResolveHandler handler2 = mock(SystemProperty.ResolveHandler.class);
        SystemProperty.ResolveHandler handler3 = mock(SystemProperty.ResolveHandler.class);
        Options options  = new Options();

        options.add(SystemProperty.of("foo", "foo-value", Decoration.of(handler1), Decoration.of(handler2)));

        options.add(SystemProperty.of("bar", "bar-value", Decoration.of(handler1), Decoration.of(handler3)));

        options.get(SystemProperties.class).resolve(LocalPlatform.get(), options);

        ArgumentCaptor<String> names1  = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> values1 = ArgumentCaptor.forClass(String.class);

        verify(handler1, times(2)).onResolve(names1.capture(), values1.capture(), same(options));

        assertThat(names1.getAllValues(), contains("foo", "bar"));
        assertThat(values1.getAllValues(), contains("foo-value", "bar-value"));

        ArgumentCaptor<String> names2  = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> values2 = ArgumentCaptor.forClass(String.class);

        verify(handler2, times(1)).onResolve(names2.capture(), values2.capture(), same(options));

        assertThat(names2.getAllValues(), contains("foo"));
        assertThat(values2.getAllValues(), contains("foo-value"));

        ArgumentCaptor<String> names3  = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> values3 = ArgumentCaptor.forClass(String.class);

        verify(handler3, times(1)).onResolve(names3.capture(), values3.capture(), same(options));

        assertThat(names3.getAllValues(), contains("bar"));
        assertThat(values3.getAllValues(), contains("bar-value"));
    }
}
