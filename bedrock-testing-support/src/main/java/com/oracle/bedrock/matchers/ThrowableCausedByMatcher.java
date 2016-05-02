/*
 * File: ThrowableCausedByMatcher.java
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

package com.oracle.bedrock.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * A {@link TypeSafeMatcher} for matching one of the {@link Throwable#getCause()} {@link Throwable}s.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class ThrowableCausedByMatcher<T extends Throwable> extends TypeSafeMatcher<T>
{
    /**
     * The {@link Matcher} for the {@link Throwable} message.
     */
    private Matcher<? super Throwable> matcher;


    /**
     * Constructs a {@link ThrowableCausedByMatcher} given a {@link Matcher} for the
     * {@link Throwable#getCause()} method.
     *
     * @param matcher  the message {@link Matcher}
     */
    private ThrowableCausedByMatcher(Matcher<? super Throwable> matcher)
    {
        this.matcher = matcher;
    }


    /**
     * Constructs a {@link ThrowableCausedByMatcher} for a {@link Throwable#getCause()}
     * matching the specified {@link Matcher}.
     *
     * @param matcher   the message {@link Matcher}
     * @param <T>       the type of {@link Throwable}
     *
     * @return  a {@link ThrowableCausedByMatcher}
     */
    public static <T extends Throwable> ThrowableCausedByMatcher<T> causedBy(Matcher<? super Throwable> matcher)
    {
        return new ThrowableCausedByMatcher<>(matcher);
    }


    @Override
    protected boolean matchesSafely(T t)
    {
        Throwable throwable = t;

        // ensure that one of the causes of the throwable matches
        while (throwable != null)
        {
            if (matcher.matches(throwable.getCause()))
            {
                return true;
            }
            else
            {
                throwable = throwable.getCause();
            }
        }

        // nothing matched
        return false;
    }


    @Override
    public void describeTo(Description description)
    {
        description.appendText("throwable caused by ");
        matcher.describeTo(description);
    }
}
