/*
 * File: FutureMatcher.java
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

package com.oracle.bedrock.testsupport.deferred;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.core.Is;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.core.IsEqual.equalTo;

/**
 * A {@link Matcher} for values provided by {@link CompletableFuture}s.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 *
 * @param <T> the type of value to match
 */
public class FutureMatcher<T> extends TypeSafeDiagnosingMatcher<CompletableFuture<? super T>>
{
    /**
     * The {@link Matcher} of future value.
     */
    private final Matcher<? super T> matcher;


    /**
     * Constructs a {@link FutureMatcher}
     *
     * @param matcher  the {@link Matcher} of a future value
     */
    public FutureMatcher(Matcher<? super T> matcher)
    {
        this.matcher = matcher;
    }


    @Override
    protected boolean matchesSafely(CompletableFuture<? super T> item,
                                    Description                  mismatchDescription)
    {
        try
        {
            Object result = item.get();

            return matcher.matches(result);
        }
        catch (InterruptedException | ExecutionException e)
        {
            mismatchDescription.appendText(" but completed with exception ").appendValue(e);

            return false;
        }
    }


    @Override
    public void describeTo(Description description)
    {
        description.appendText("a CompletableFuture with result ").appendDescriptionOf(matcher);
    }


    /**
     * Decorates another Matcher, retaining its behaviour, but allowing tests
     * to be slightly more expressive.
     * <p>
     * For example:
     * <pre>assertThat(cheese, is(equalTo(smelly)))</pre>
     * instead of:
     * <pre>assertThat(cheese, equalTo(smelly))</pre>
     *
     * @param matcher  the value {@link Matcher}
     *
     * @param <T>      the type of the value
     *
     * @return the {@link Matcher}
     */
    public static <T> Matcher<T> futureOf(Matcher<T> matcher)
    {
        return new Is<T>(matcher);
    }


    /**
     * A shortcut to the frequently used <code>is(equalTo(x))</code>.
     * <p>
     * For example:
     * <pre>assertThat(cheese, is(smelly))</pre>
     * instead of:
     * <pre>assertThat(cheese, is(equalTo(smelly)))</pre>
     *
     * @param value  the value
     *
     * @param <T>    the type of the value
     *
     * @return the {@link Matcher}
     */
    public static <T> Matcher<T> futureOf(T value)
    {
        return futureOf(equalTo(value));
    }
}
