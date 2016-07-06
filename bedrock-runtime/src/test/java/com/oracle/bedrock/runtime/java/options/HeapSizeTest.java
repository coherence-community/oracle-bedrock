/*
 * File: HeapSizeTest.java
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
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Unit tests for {@link HeapSize}s.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class HeapSizeTest
{
    /**
     * Ensure that {@link HeapSize}s are composed when provided to {@link OptionsByType}.
     */
    @Test
    public void shouldComposeHeapSizes()
    {
        OptionsByType optionsByType = OptionsByType.of(HeapSize.initial(128, HeapSize.Units.MB),
                                                       HeapSize.maximum(1, HeapSize.Units.GB));

        HeapSize heapSize = optionsByType.get(HeapSize.class);

        assertThat(heapSize.getInitialSizeAs(HeapSize.Units.MB), is(128L));
        assertThat(heapSize.getMaximumSizeAs(HeapSize.Units.GB), is(1L));
    }


    /**
     * Ensure that {@link HeapSize}s compose initial and minimum values when provided to {@link OptionsByType}.
     */
    @Test
    public void shouldUseComposeSameMinimumAndMaximumHeapSizes()
    {
        OptionsByType optionsByType = OptionsByType.of(HeapSize.initial(1, HeapSize.Units.GB),
                                                       HeapSize.maximum(1, HeapSize.Units.GB));

        HeapSize heapSize = optionsByType.get(HeapSize.class);

        assertThat(heapSize.getInitialSizeAs(HeapSize.Units.GB), is(1L));
        assertThat(heapSize.getMaximumSizeAs(HeapSize.Units.GB), is(1L));

        assertThat(heapSize.getInitialSizeAs(HeapSize.Units.KB), is(1048576L));
        assertThat(heapSize.getMaximumSizeAs(HeapSize.Units.KB), is(1048576L));
    }
}
