/*
 * File: ConstantMatcher.java
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

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static org.hamcrest.core.IsEqual.equalTo;

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
public class ConstantMatcher<T> extends BaseMatcher<T>
{
    /**
     * The {@link Matcher} to use once the first value is provided.
     */
    private Matcher<T> matcher;


    /**
     * Constructs a {@link ConstantMatcher}.
     */
    public ConstantMatcher()
    {
        this.matcher = null;
    }


    /**
     * Obtains a {@link ConstantMatcher}.
     *
     * @param <T>      the type of values being matched
     *
     * @return a {@link ConstantMatcher}
     */
    public static <T> ConstantMatcher<T> constant()
    {
        return new ConstantMatcher<>();
    }


    @Override
    public boolean matches(Object o)
    {
        if (matcher == null)
        {
            matcher = equalTo((T) o);

            return true;
        }
        else
        {
            return matcher.matches(o);
        }

    }


    @Override
    public void describeTo(Description description)
    {
        if (matcher == null)
        {
            description.appendText("is constant");
        }
        else
        {
            description.appendText("constantly matches using ");
            matcher.describeTo(description);
        }
    }
}
