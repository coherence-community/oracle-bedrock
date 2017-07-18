/*
 * File: BedrockRunner.java
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

package com.oracle.bedrock.runtime.java.options;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.java.JavaApplication;
import com.oracle.bedrock.runtime.java.JavaApplicationRunner;

/**
 * Defines an {@link Option} defining if and possibly which Bedrock
 * {@link JavaApplicationRunner} should be used to run a {@link JavaApplication}.
 * <p>
 * Copyright (c) 2017. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class BedrockRunner implements Option
{
    /**
     * The class used for running a {@link JavaApplication}.
     *
     * (when <code>null</code>} no runner will be used).
     */
    private Class<?> classOfRunner;


    /**
     * Constructs a {@link BedrockRunner} {@link Option}.
     *
     * @param classOfRunner  the class to use for running a {@link JavaApplication}.
     */
    private BedrockRunner(Class<?> classOfRunner)
    {
        this.classOfRunner = classOfRunner;
    }


    /**
     * Determine if Bedrock will use a {@link JavaApplicationRunner} to run a {@link JavaApplication}.
     *
     * @return  <code>true</code> when Bedrock will use a {@link JavaApplicationRunner}
     *          <code>false</code> otherwise
     */
    public boolean isEnabled()
    {
        return classOfRunner != null;
    }


    /**
     * Obtains the default {@link BedrockRunner} {@link Option}, that uses the {@link JavaApplicationRunner}.
     *
     * @return a {@link BedrockRunner}
     */
    @OptionsByType.Default
    public static BedrockRunner automatic()
    {
        return new BedrockRunner(JavaApplicationRunner.class);
    }


    /**
     * Obtains a {@link BedrockRunner} {@link Option} based on a specific class of {@link JavaApplicationRunner}.
     *
     * @param classOfRunner  the class of the {@link JavaApplicationRunner}
     *
     * @return a {@link BedrockRunner}
     */
    public static BedrockRunner of(Class<?> classOfRunner)
    {
        return new BedrockRunner(classOfRunner);
    }


    /**
     * Obtains a {@link BedrockRunner} {@link Option} that is disabled.
     *
     * @return a {@link BedrockRunner}
     */
    public static BedrockRunner disabled()
    {
        return new BedrockRunner(null);
    }


    /**
     * Obtains the {@link Class} of the Application Runner.
     *
     * @return the {@link Class} of the application runner or <code>null</code> if none is required.
     */
    public Class<?> getClassOfRunner()
    {
        return classOfRunner;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof BedrockRunner))
        {
            return false;
        }

        BedrockRunner that = (BedrockRunner) o;

        return classOfRunner.equals(that.classOfRunner);
    }


    @Override
    public int hashCode()
    {
        return classOfRunner.hashCode();
    }


    @Override
    public String toString()
    {
        return "BedrockRunner{" + classOfRunner + '}';
    }
}
