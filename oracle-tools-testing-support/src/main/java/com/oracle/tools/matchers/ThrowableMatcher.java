/*
 * File: ThrowableMatcher.java
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

import com.oracle.tools.deferred.Eventually;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

/**
 * A {@link Matcher} that can be used to match {@link Throwable}s raised while
 * performing assertions instead of matching values.
 * <p>
 * Typically this class is used with the assertion methods provided by
 * the {@link Eventually} class.
 * <p>
 * For example:
 * <code>
 * Eventually.assertThat(invoking(object).someMethod(), willThrow(instanceOf(NullPointerException.class)));
 * </code>
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class ThrowableMatcher<T> extends BaseMatcher<T>
{
    /**
     * The {@link Matcher} for the {@link Throwable}.
     */
    private Matcher<? super Throwable> matcher;


    /**
     * Constructs a {@link ThrowableMatcher} for {@link Throwable}s
     * using the specified {@link Matcher}.
     *
     * @param matcher  the {@link Matcher}
     */
    private ThrowableMatcher(Matcher<? super Throwable> matcher)
    {
        this.matcher = matcher;
    }


    /**
     * Obtains a {@link ThrowableMatcher} for the specified class of {@link Throwable}.
     *
     * @param throwableClass  the class of {@link Throwable}
     * @param <T>             an expected type of value
     *
     * @return  a {@link ThrowableMatcher}
     */
    public static <T> ThrowableMatcher<T> willThrow(Class<? extends Throwable> throwableClass)
    {
        return new ThrowableMatcher<>(instanceOf(throwableClass));
    }


    /**
     * Obtains a {@link ThrowableMatcher} for a {@link Matcher} of {@link Throwable}s.
     *
     * @param matcher  the {@link Matcher} for {@link Throwable}s
     * @param <T>      an expected type of value
     *
     * @return  a {@link ThrowableMatcher}
     */
    public static <T> ThrowableMatcher<T> willThrow(Matcher<? super Throwable> matcher)
    {
        return new ThrowableMatcher<>(matcher);
    }


    /**
     * Obtains a new {@link ThrowableMatcher} that includes the matching the
     * {@link Throwable#getMessage()}
     *
     * @param matcher  the {@link Matcher}
     *
     * @return  a new {@link ThrowableMatcher}
     */
    public ThrowableMatcher<T> withMessage(Matcher<? super String> matcher)
    {
        return new ThrowableMatcher<>(allOf(this.matcher, ThrowableMessageMatcher.message(matcher)));
    }


    /**
     * Obtains a new {@link ThrowableMatcher} that includes matching the specified
     * message.
     *
     * @param message  the message to match
     *
     * @return  a new {@link ThrowableMatcher}
     */
    public ThrowableMatcher<T> withMessage(String message)
    {
        return new ThrowableMatcher<>(allOf(this.matcher, ThrowableMessageMatcher.message(equalTo(message))));
    }


    /**
     * Obtains a new {@link ThrowableMatcher} that includes matching a cause of the
     * {@link Throwable}.
     *
     * @param matcher  the matched for the caused-by {@link Throwable}
     *
     * @return  a new {@link ThrowableMatcher}
     */
    public ThrowableMatcher<T> causedBy(Matcher<? super Throwable> matcher)
    {
        return new ThrowableMatcher<>(allOf(this.matcher, matcher));
    }


    /**
     * Obtain the {@link Matcher} for the {@link Throwable}.
     *
     * @return  the {@link Throwable} {@link Matcher}
     */
    public Matcher<? super Throwable> getMatcher()
    {
        return matcher;
    }


    @Override
    public boolean matches(Object o)
    {
        return false;
    }


    @Override
    public void describeTo(Description description)
    {
        description.appendText("throws a throwable matching ");
        matcher.describeTo(description);
    }
}
