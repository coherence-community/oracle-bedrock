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

package com.oracle.bedrock.runtime.java.options;

import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.options.Decoration;
import com.oracle.bedrock.runtime.LocalPlatform;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.same;
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
    public void shouldCallResolveHandlers()
    {
        SystemProperty.ResolveHandler handler1      = mock(SystemProperty.ResolveHandler.class);
        SystemProperty.ResolveHandler handler2      = mock(SystemProperty.ResolveHandler.class);
        SystemProperty.ResolveHandler handler3      = mock(SystemProperty.ResolveHandler.class);
        OptionsByType                 optionsByType = OptionsByType.empty();

        optionsByType.add(SystemProperty.of("foo", "foo-value", Decoration.of(handler1), Decoration.of(handler2)));

        optionsByType.add(SystemProperty.of("bar", "bar-value", Decoration.of(handler1), Decoration.of(handler3)));

        optionsByType.get(SystemProperties.class).resolve(LocalPlatform.get(), optionsByType);

        ArgumentCaptor<String> names1  = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> values1 = ArgumentCaptor.forClass(String.class);

        verify(handler1, times(2)).onResolve(names1.capture(), values1.capture(), same(optionsByType));

        assertThat(names1.getAllValues(), contains("foo", "bar"));
        assertThat(values1.getAllValues(), contains("foo-value", "bar-value"));

        ArgumentCaptor<String> names2  = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> values2 = ArgumentCaptor.forClass(String.class);

        verify(handler2, times(1)).onResolve(names2.capture(), values2.capture(), same(optionsByType));

        assertThat(names2.getAllValues(), contains("foo"));
        assertThat(values2.getAllValues(), contains("foo-value"));

        ArgumentCaptor<String> names3  = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> values3 = ArgumentCaptor.forClass(String.class);

        verify(handler3, times(1)).onResolve(names3.capture(), values3.capture(), same(optionsByType));

        assertThat(names3.getAllValues(), contains("bar"));
        assertThat(values3.getAllValues(), contains("bar-value"));
    }

    @Test
    public void shouldCallSupplier()
    {
        OptionsByType optionsByType = OptionsByType.of(SystemProperty.of("property.one", () -> "foo"),
                SystemProperty.of("property.two", () -> "bar"));

        Properties properties = optionsByType.get(SystemProperties.class).resolve(LocalPlatform.get(), optionsByType);
        assertThat(properties.containsKey("property.one"), is(true));
        assertThat(properties.getProperty("property.one"), is("foo"));
        assertThat(properties.containsKey("property.two"), is(true));
        assertThat(properties.getProperty("property.two"), is("bar"));
    }

}
