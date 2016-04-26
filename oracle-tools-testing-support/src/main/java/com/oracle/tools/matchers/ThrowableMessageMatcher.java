/*
 * File: ThrowableMessageMatcher.java
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

package com.oracle.tools.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * A {@link TypeSafeMatcher} for the content of a {@link Throwable#getMessage()}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class ThrowableMessageMatcher<T extends Throwable> extends TypeSafeMatcher<T>
{
    /**
     * The {@link Matcher} for the {@link Throwable} message.
     */
    private Matcher<? super String> matcher;


    /**
     * Constructs a {@link ThrowableMessageMatcher} given a {@link Matcher} for the
     * {@link Throwable#getMessage()} method.
     *
     * @param matcher  the message {@link Matcher}
     */
    private ThrowableMessageMatcher(Matcher<? super String> matcher)
    {
        this.matcher = matcher;
    }


    /**
     * Constructs a {@link ThrowableMessageMatcher} for a {@link Throwable#getMessage()}
     * matching the specified {@link Matcher}.
     *
     * @param matcher   the message {@link Matcher}
     * @param <T>       the type of {@link Throwable}
     *
     * @return  a {@link ThrowableMessageMatcher}
     */
    public static <T extends Throwable> ThrowableMessageMatcher<T> message(Matcher<? super String> matcher)
    {
        return new ThrowableMessageMatcher<>(matcher);
    }


    @Override
    protected boolean matchesSafely(T t)
    {
        return matcher.matches(t.getMessage());
    }


    @Override
    public void describeTo(Description description)
    {
        description.appendText("throwable message matches ");
        matcher.describeTo(description);
    }
}
