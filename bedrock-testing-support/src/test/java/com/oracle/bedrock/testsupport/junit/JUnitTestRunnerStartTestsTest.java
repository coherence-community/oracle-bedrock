/*
 * File: JUnitTestRunnerStartTestsTest.java
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

package com.oracle.bedrock.testsupport.junit;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.testsupport.junit.JUnitTestRunner;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.Serializable;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsArrayContainingInAnyOrder.arrayContainingInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link JUnitTestRunner.StartTests}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class JUnitTestRunnerStartTestsTest
{
    @Test
    public void shouldStartTests() throws Exception
    {
        Option                     opt1          = new DummyOptionOne();
        Option                     opt2          = new DummyOptionTwo();
        Option                     opt3          = new DummyOptionThree();
        OptionsByType              optionsByType = OptionsByType.of(opt1, opt2, opt3);
        JUnitTestRunner            runner        = mock(JUnitTestRunner.class);
        JUnitTestRunner.StartTests startTests    = new JUnitTestRunner.StartTests(optionsByType);

        startTests.setRunner(runner);

        startTests.call();

        ArgumentCaptor<OptionsByType> captor = ArgumentCaptor.forClass(OptionsByType.class);

        verify(runner).run(captor.capture());

        OptionsByType arg = captor.getValue();

        assertThat(arg.asArray(), is(arrayContainingInAnyOrder(opt1, opt2)));
    }


    /**
     * Class description
     *
     * @version        Enter version here..., 16/05/06
     * @author         Enter your name here...
     */
    public static class DummyOptionOne implements Option, Serializable
    {
    }


    /**
     * Class description
     *
     * @version        Enter version here..., 16/05/06
     * @author         Enter your name here...
     */
    public static class DummyOptionThree implements Option
    {
    }


    /**
     * Class description
     *
     * @version        Enter version here..., 16/05/06
     * @author         Enter your name here...
     */
    public static class DummyOptionTwo implements Option, Serializable
    {
    }
}
