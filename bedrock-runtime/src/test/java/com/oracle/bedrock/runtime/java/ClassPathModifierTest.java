/*
 * File: ClassPathModifierTest.java
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

package com.oracle.bedrock.runtime.java;

import com.oracle.bedrock.OptionsByType;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * @author jk 2015.01.15
 */
public class ClassPathModifierTest
{
    @Test
    public void shouldApplyNoModification() throws Exception
    {
        ClassPathModifier modifier = ClassPathModifier.none();

        assertThat(modifier.modify("foo"), is("foo"));
    }


    @Test
    public void shouldApplyCygwinModification() throws Exception
    {
        ClassPathModifier modifier = ClassPathModifier.forCygwin();

        assertThat(modifier.modify("foo"), is("$(cygpath -wp foo)"));
    }


    @Test
    public void shouldFindNoneOption() throws Exception
    {
        ClassPathModifier none          = ClassPathModifier.none();
        OptionsByType     optionsByType = OptionsByType.of(none);
        ClassPathModifier modifier      = optionsByType.get(ClassPathModifier.class);

        assertThat(modifier, is(sameInstance(none)));
    }


    @Test
    public void shouldFindCygwinOption() throws Exception
    {
        ClassPathModifier cygwin        = ClassPathModifier.forCygwin();
        OptionsByType     optionsByType = OptionsByType.of(cygwin);
        ClassPathModifier modifier      = optionsByType.get(ClassPathModifier.class);

        assertThat(modifier, is(sameInstance(cygwin)));
    }
}
