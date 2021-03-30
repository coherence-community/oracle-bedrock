/*
 * File: TestLogs.java
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

import com.oracle.bedrock.runtime.ApplicationConsole;
import com.oracle.bedrock.runtime.ApplicationConsoleBuilder;
import org.junit.rules.TestName;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.List;

/**
 * An extension of the JUnit {@link TestName} class that can be used as
 * a JUnit {@link org.junit.Rule} in a test class to easily create test
 * output folders and {@link ApplicationConsole}s.
 *
 * The {@link ApplicationConsoleBuilder} created also allows the capturing
 * of events based on log line output which can then be asserted in tests.
 *
 * @author jk  2018.10.17
 */
public class TestLogs
        extends AbstractTestLogs
        implements TestRule
{
    /**
     * Create a {@link TestLogs}.
     */
    public TestLogs()
    {
        this(null);
    }

    /**
     * Create a {@link TestLogs}.
     *
     * @param testClass  the test class
     */
    public TestLogs(Class<?> testClass)
    {
        this.testClass = testClass;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                List<Throwable> errors = new ArrayList<Throwable>();
                starting(description);
                try {
                    base.evaluate();
                } catch (Throwable e) {
                    errors.add(e);
                }
                MultipleFailureException.assertEmpty(errors);
            }
        };
    }

    protected void starting(Description description)
    {
        Class<?> cls = description.getTestClass();

        if (cls == null)
        {
            try
            {
                String clsName = description.getClassName();
                cls = Class.forName(clsName);
            }
            catch (ClassNotFoundException e)
            {
                // ignored
            }
        }

        init(cls, description.getMethodName());
    }
}
