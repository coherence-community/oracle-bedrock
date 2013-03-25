/*
 * File: ClassPath.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of 
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting
 * or https://oss.oracle.com/licenses/CDDL
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

package com.oracle.tools.runtime.java;

import java.io.File;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A platform independent representation of a Java ClassPath.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 */
public class ClassPath implements Iterable<String>
{
    /**
     * The default encoding to use for ClassPath URLs.
     */
    private static final String ENCODING = "UTF-8";

    /**
     * The paths that make up the ClassPath.
     */
    private ArrayList<String> m_paths;


    /**
     * Constructs an empty ClassPath.
     */
    public ClassPath()
    {
        m_paths = new ArrayList<String>();
    }


    /**
     * Constructs a ClassPath based on a File.
     *
     * @param file the file
     */
    public ClassPath(File file)
    {
        this(file.toString());
    }


    /**
     * Constructs a ClassPath from a standard Java class-path string
     * (using the system defined path and file separators).
     *
     * @param classPath a Java class-path string
     */
    public ClassPath(String classPath)
    {
        this(classPath, File.separator, File.pathSeparator);
    }


    /**
     * Constructs a ClassPath from a standard Java class-path string
     * (using the system defined path and file separators).
     *
     * @param classPath      the Java class-path
     * @param fileSeparator  the file separator being used in the classPath
     * @param pathSeparator  the path separator being used in the classPath
     */
    public ClassPath(String classPath,
                     String fileSeparator,
                     String pathSeparator)
    {
        // String   systemClassPath         = System.getProperty("java.class.path");

        // separate the individual paths
        String[] paths = classPath.split(pathSeparator);

        m_paths = new ArrayList<String>(paths.length);

        for (String path : paths)
        {
            // remove unnecessary white space
            path = path.trim();

            // add a file separator if it's not a jar/war etc.
            path = path.endsWith(".jar") || path.endsWith(".war") || path.endsWith(".ear") || path.endsWith(".car")
                   || path.endsWith(".gar") || path.endsWith(".sar") || path.endsWith(".zip") ? path
                                                                                              : path + fileSeparator;

            m_paths.add(path);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<String> iterator()
    {
        return m_paths.iterator();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return toString(File.separator, File.pathSeparator);
    }


    /**
     * Obtains a String representation of the ClassPath that is suitable
     * for using as a Java class-path property.
     *
     * @param fileSeparator  the file separator to be used in the resulting class-path
     * @param pathSeparator  the path separator to be used in the resulting class-path
     *
     * @return the Java class-path
     */
    public String toString(String fileSeparator,
                           String pathSeparator)
    {
        StringBuilder builder = new StringBuilder();

        for (String path : m_paths)
        {
            if (builder.length() > 0)
            {
                builder.append(pathSeparator);
            }

            boolean containsSpace = path.contains(" ");

            if (containsSpace)
            {
                builder.append("\"");
            }

            builder.append(path);

            if (containsSpace)
            {
                builder.append("\"");
            }
        }

        return builder.toString();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }

        if (other == null || getClass() != other.getClass())
        {
            return false;
        }

        ClassPath classPath = (ClassPath) other;

        if (!m_paths.equals(classPath.m_paths))
        {
            return false;
        }

        return true;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }
}
