/*
 * File: RecordingMatcher.java
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

package com.oracle.bedrock.testsupport.matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link Matcher} that can be used to record the success and failures of another {@link Matcher}.
 * <p>
 * Typically this class is used for testing interaction with other {@link Matcher}s.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @param <T>  the type of value being matched
 *
 * @author Brian Oliver
 */
public class RecordingMatcher<T> extends BaseMatcher<T>
{
    /**
     * The {@link Matcher} being recorded.
     */
    private Matcher<? super T> matcher;

    /**
     * The number of successful matches.
     */
    private AtomicInteger successCount;

    /**
     * The number of failed matches.
     */
    private AtomicInteger failureCount;


    /**
     * Constructs a {@link RecordingMatcher}.
     *
     * @param matcher  the {@link Matcher} being recorded
     */
    public RecordingMatcher(Matcher<? super T> matcher)
    {
        this.matcher      = matcher;
        this.successCount = new AtomicInteger(0);
        this.failureCount = new AtomicInteger(0);
    }


    /**
     * Obtains a {@link RecordingMatcher} for a specified {@link Matcher}.
     *
     * @param matcher  the {@link Matcher}
     * @param <T>      the type of values being matched
     *
     * @return a {@link RecordingMatcher}
     */
    public static <T> RecordingMatcher<T> of(Matcher<? super T> matcher)
    {
        return new RecordingMatcher<>(matcher);
    }


    /**
     * Obtain the current number of successful matches.
     *
     * @return the successful match count
     */
    public int getSuccessCount()
    {
        return successCount.get();
    }


    /**
     * Determines if one or more successful matches with the {@link Matcher} has occurred.
     *
     * @return  <code>true</code> when one or more successful matches has occurred
     *          <code>false</code> otherwise
     */
    public boolean hasSucceeded()
    {
        return getSuccessCount() > 0;
    }


    /**
     * Obtains the current number of failed matches.
     *
     * @return the failed match count
     */
    public int getFailureCount()
    {
        return failureCount.get();
    }


    /**
     * Determines if one or more failures to match the {@link Matcher} has occurred.
     *
     * @return  <code>true</code> when one or more failures have occurred
     *          <code>false</code> otherwise
     */
    public boolean hasFailed()
    {
        return getFailureCount() > 0;
    }


    /**
     * Obtains the number of match attempts.
     *
     * @return the match attempt count
     */
    public int getAttemptCount()
    {
        return successCount.get() + failureCount.get();
    }


    /**
     * Determines if a match has been attempted with the {@link Matcher}.
     *
     * @return  <code>true</code> when a match has been attempted,
     *          <code>false</code> otherwise
     */
    public boolean attempted()
    {
        return getAttemptCount() > 0;
    }


    @Override
    public boolean matches(Object o)
    {
        boolean match = matcher.matches(o);

        if (match)
        {
            successCount.incrementAndGet();
        }
        else
        {
            failureCount.incrementAndGet();
        }

        return match;
    }


    @Override
    public void describeTo(Description description)
    {
        description.appendText("A recoding matcher for ");
        this.matcher.describeTo(description);
    }
}
