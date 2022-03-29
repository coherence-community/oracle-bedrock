/*
 * File: PartitionSetMatcher.java
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

package com.oracle.bedrock.runtime.coherence.testing.matchers;

import com.tangosol.net.partition.PartitionSet;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.HashSet;

/**
 * A Hamcrest-based {@link TypeSafeMatcher} for {@link PartitionSet}s.
 * <p>
 * Copyright (c) 2021. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class PartitionSetMatcher extends TypeSafeMatcher<PartitionSet>
{
    /**
     * The partition identifiers to match.
     */
    private HashSet<Integer> partitionIds;


    /**
     * Constructs a {@link PartitionSetMatcher}.
     *
     * @param partitionIds  the partition identifiers to match
     */
    private PartitionSetMatcher(int... partitionIds)
    {
        this.partitionIds = new HashSet<Integer>();

        if (partitionIds != null)
        {
            for (int partitionId : partitionIds)
            {
                this.partitionIds.add(partitionId);
            }
        }
    }


    @Override
    protected boolean matchesSafely(PartitionSet partitionSet)
    {
        for (int partitionId : partitionIds)
        {
            if (!partitionSet.contains(partitionId))
            {
                return false;
            }
        }

        return true;
    }


    @Override
    protected void describeMismatchSafely(PartitionSet partitionSet,
                                          Description  mismatchDescription)
    {
        mismatchDescription.appendText("partitionSet was " + partitionSet);
    }


    @Override
    public void describeTo(Description description)
    {
        description.appendText("partitionSet containing ").appendValueList("{", ", ", "}", partitionIds);
    }


    public static Matcher<PartitionSet> contains(int... partitionIds)
    {
        return new PartitionSetMatcher(partitionIds);
    }
}
