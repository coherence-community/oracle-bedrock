/*
 * File: OptionsTest.java
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

import com.oracle.tools.Option;
import com.oracle.tools.Options;

import com.oracle.tools.options.Timeout;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.nullValue;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.core.Is.is;

import java.util.concurrent.TimeUnit;

/**
 * Unit tests for {@link Options}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class OptionsTest
{
    /**
     * Ensure that we can add and get a specific {@link Option}.
     */
    @Test
    public void shouldAddAndGetSpecificOption()
    {
        Timeout option  = Timeout.after(5, TimeUnit.MINUTES);

        Options options = new Options(option);

        assertThat(options.get(Timeout.class), is(option));
    }


    /**
     * Ensure that we can add, get and then replace a specific {@link Option}.
     */
    @Test
    public void shouldReplaceAndGetSpecificOption()
    {
        Timeout option      = Timeout.after(5, TimeUnit.MINUTES);

        Options options     = new Options(option);

        Timeout otherOption = Timeout.after(1, TimeUnit.SECONDS);

        options.add(otherOption);

        assertThat(options.get(Timeout.class), is(otherOption));
    }


    /**
     * Ensure that we don't replace an existing specific {@link Option}.
     */
    @Test
    public void shouldNotReplaceAndGetSpecificOption()
    {
        Timeout option      = Timeout.after(5, TimeUnit.MINUTES);

        Options options     = new Options(option);

        Timeout otherOption = Timeout.after(1, TimeUnit.SECONDS);

        options.addIfAbsent(otherOption);

        assertThat(options.get(Timeout.class), is(option));
    }


    /**
     * Ensure that we can remove a specific {@link Option}.
     */
    @Test
    public void shouldRemoveASpecificOption()
    {
        Timeout option  = Timeout.after(5, TimeUnit.MINUTES);

        Options options = new Options(option);

        assertThat(options.get(Timeout.class), is(option));

        assertThat(options.remove(Timeout.class), is(true));
        assertThat(options.remove(Timeout.class), is(false));
        assertThat(options.get(Timeout.class), is(nullValue()));
    }


    /**
     * Ensure that the class of an {@link Option} for an class
     * that directly implements the {@link Option} interface is
     * the class itself.
     */
    @Test
    public void shouldDetermineDirectlyImplementedOptionClass()
    {
        Timeout option      = Timeout.after(5, TimeUnit.MINUTES);

        Class   optionClass = Options.getClassOfOption(option);

        assertThat(optionClass.equals(Timeout.class), is(true));
    }


    /**
     * Ensure that the class of an {@link Option} for an class
     * that indirectly implements the {@link Option} interface,
     * via an interface that extends {@link Option} is
     * the interface that extends {@link Option}.
     */
    @Test
    public void shouldDetermineIndirectlyImplementedOptionClass()
    {
        Enhanced option      = new Enhanced();

        Class    optionClass = Options.getClassOfOption(option);

        assertThat(optionClass.equals(EnhancedOption.class), is(true));
    }


    /**
     * Ensure that the class of an {@link Option} for an class
     * that extends another {@link Option} implementation is
     * the class / interface that directly extends {@link Option}.
     */

    @Test
    public void shouldDetermineExtendedOptionClass()
    {
        ExtendedEnhanced option      = new ExtendedEnhanced();

        Class            optionClass = Options.getClassOfOption(option);

        assertThat(optionClass.equals(EnhancedOption.class), is(true));
        assertThat(optionClass.equals(ExtendedEnhanced.class), is(false));
    }


    /**
     * Ensure that an abstract class of {@link Option} is not returned
     * as a concrete type of {@link Option}.
     */

    @Test
    public void shouldNotDetermineAbstractOptionClass()
    {
        Class optionClass = Options.getClassOfOption(AbstractEnhanced.class);

        assertThat(optionClass, is(nullValue()));
    }


    /**
     * An {@link EnhancedOption}.
     */
    public static interface EnhancedOption extends Option
    {
    }


    /**
     * An abstract {@link EnhancedOption}.
     */
    public abstract class AbstractEnhanced implements EnhancedOption
    {
    }


    /**
     * A simple {@link EnhancedOption}.
     */
    public class Enhanced extends AbstractEnhanced
    {
    }


    /**
     * An extended {@link EnhancedOption}.
     */
    public class ExtendedEnhanced extends Enhanced
    {
    }
}
