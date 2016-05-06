/*
 * File: JUnitTestRunMetaClassTest.java
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

package com.oracle.bedrock.junit;

import com.oracle.bedrock.Options;
import com.oracle.bedrock.runtime.java.JavaApplication;
import com.oracle.bedrock.runtime.java.options.ClassName;
import com.oracle.bedrock.runtime.options.DisplayName;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link JUnitTestRun.MetaClass}.
 * tests.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class JUnitTestRunMetaClassTest
{
    @Test
    public void shouldHaveCorectImplementation() throws Exception
    {
        JUnitTestRun.MetaClass metaClass = new JUnitTestRun.MetaClass();

        assertThat(metaClass.getImplementationClass(null, null), is(sameInstance(SimpleJUnitTestRun.class)));
    }


    @Test
    public void shouldSetCLassName() throws Exception
    {
        JUnitTestRun.MetaClass metaClass = new JUnitTestRun.MetaClass();
        Options                options   = new Options();

        metaClass.onLaunching(null, options);

        ClassName className = options.get(ClassName.class);

        assertThat(className, is(notNullValue()));
        assertThat(className.getName(), is(JUnitTestRunner.class.getCanonicalName()));
    }


    @Test
    public void shouldSetDisplayNameIfNotAlreadySet() throws Exception
    {
        JUnitTestRun.MetaClass metaClass = new JUnitTestRun.MetaClass();
        Options                options   = new Options();

        metaClass.onLaunching(null, options);

        DisplayName displayName = options.get(DisplayName.class);

        assertThat(displayName, is(notNullValue()));
    }


    @Test
    public void shouldNotOverwriteDisplayName() throws Exception
    {
        JUnitTestRun.MetaClass metaClass   = new JUnitTestRun.MetaClass();
        DisplayName            displayName = DisplayName.of("Foo");
        Options                options     = new Options(displayName);

        metaClass.onLaunching(null, options);

        DisplayName result = options.get(DisplayName.class);

        assertThat(result, is(sameInstance(displayName)));
    }


    @Test
    public void shouldDoNothingAfterLaunchForNonJUnitApplication() throws Exception
    {
        JavaApplication        application = mock(JavaApplication.class);
        Options                options     = new Options();
        JUnitTestRun.MetaClass metaClass   = new JUnitTestRun.MetaClass();

        when(application.get(JUnitTestRun.class)).thenReturn(null);

        metaClass.onLaunched(null, application, options);

        verify(application).get(JUnitTestRun.class);
        verifyNoMoreInteractions(application);
    }


    @Test
    public void shouldStartTests() throws Exception
    {
        JavaApplication        application = mock(JavaApplication.class);
        JUnitTestRun           testRun     = mock(JUnitTestRun.class);
        Options                options     = new Options();
        JUnitTestRun.MetaClass metaClass   = new JUnitTestRun.MetaClass();

        when(application.get(JUnitTestRun.class)).thenReturn(testRun);

        metaClass.onLaunched(null, application, options);

        verify(testRun).startTests(options);
    }
}
