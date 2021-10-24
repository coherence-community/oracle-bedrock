/*
 * File: TestLogsExtension.java
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

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Method;

public class TestLogsExtension
        extends AbstractTestLogs
        implements BeforeEachCallback, BeforeAllCallback
{
    /**
     * Create a {@link TestLogsExtension}.
     */
    public TestLogsExtension()
    {
    }

    /**
     * Create a {@link TestLogsExtension}.
     *
     * @param testClass  the test class
     */
    public TestLogsExtension(Class<?> testClass)
    {
        this.testClass = testClass;
        init(testClass, "unknown");
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception
    {
        String   methodName = context.getTestMethod().map(Method::getName).orElse("unknown");
        Class<?> cls = context.getTestClass().orElse(testClass);
        init(cls, methodName);
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception
    {
        String   methodName = context.getTestMethod().map(Method::getName).orElse("BeforeAll");
        Class<?> cls = context.getTestClass().orElse(testClass);
        init(cls, methodName);
    }
}
