/*
 * File: ClassPathModifier.java
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

import com.oracle.bedrock.Option;
import com.oracle.bedrock.Options;

/**
 * An {@link Option} to define modifications that can be
 * applied to a Java class path String.
 *
 * @author jk 2015.01.15
 */
public class ClassPathModifier implements Option
{
    /**
     * Flag indicating whether the class path should be quoted
     */
    private boolean useQuotes = true;


    /**
     * Create an instance of {@link ClassPathModifier}
     *
     * @param useQuotes flag indicating whether the class
     *                  path should be quoted
     */
    public ClassPathModifier(boolean useQuotes)
    {
        this.useQuotes = useQuotes;
    }


    /**
     * Apply any required modifications to the original class path String.
     *
     * @param classPath  the class path String to modify
     *
     * @return the modified class path String.
     */
    public String modify(String classPath)
    {
        return classPath;
    }


    /**
     * Apply quotes to the specified class path if this
     * modifier has the {@link #useQuotes} flag set to true.
     *
     * @param classPath the class path to apply quotes to
     *
     * @return the quoted class path string
     */
    public String applyQuotes(String classPath)
    {
        if (useQuotes)
        {
            StringBuilder builder = new StringBuilder();

            if (!classPath.startsWith("\""))
            {
                builder.append('"');
            }

            builder.append(classPath);

            if (!classPath.endsWith("\""))
            {
                builder.append('"');
            }

            return builder.toString();
        }

        return classPath;
    }


    /**
     * Obtain a ClassPathModifier that performs no modification
     * on a class path String.
     *
     * @return a ClassPathModifier that performs no modification
     *         on a class path String
     */
    @Options.Default
    public static ClassPathModifier none()
    {
        return new ClassPathModifier(true);
    }


    /**
     * Obtain a ClassPathModifier that performs modifications
     * to the class path required by Cygwin platforms.
     *
     * @return a ClassPathModifier that performs modifications
     *         to the class path required by Cygwin platforms
     */
    public static ClassPathModifier forCygwin()
    {
        return new CygwinModifier(true);
    }


    /**
     * Obtain a ClassPathModifier that performs modifications
     * to the class path required by Cygwin platforms.
     *
     * @return a ClassPathModifier that performs modifications
     *         to the class path required by Cygwin platforms
     */
    public static ClassPathModifier forWindows()
    {
        return new WindowsModifier(false);
    }


    /**
     * Obtain a ClassPathModifier that performs modifications
     * to the class path required by Cygwin platforms.
     *
     * @return a ClassPathModifier that performs modifications
     *         to the class path required by Cygwin platforms
     */
    public static ClassPathModifier forWindowsWithQuotes()
    {
        return new WindowsModifier(true);
    }


    /**
     * An implementation of {@link ClassPathModifier} that
     * applies modifications for the Cygwin platform.
     */
    private static class CygwinModifier extends ClassPathModifier
    {
        /**
         * Constructs ...
         *
         *
         * @param useQuotes
         */
        public CygwinModifier(boolean useQuotes)
        {
            super(useQuotes);
        }


        @Override
        public String modify(String classPath)
        {
            return String.format("$(cygpath -wp %s)", classPath);
        }
    }


    /**
     * An implementation of {@link ClassPathModifier} that
     * applies modifications for the Windows platform.
     */
    private static class WindowsModifier extends ClassPathModifier
    {
        /**
         * Constructs ...
         *
         *
         * @param useQuotes
         */
        public WindowsModifier(boolean useQuotes)
        {
            super(useQuotes);
        }


        @Override
        public String modify(String classPath)
        {
            return classPath.replaceAll("/", "\\\\");
        }
    }
}
