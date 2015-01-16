/*
 * File: PlatformSeparators.java
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

package com.oracle.tools.runtime.options;

import com.oracle.tools.Option;

import com.oracle.tools.lang.StringHelper;

import com.oracle.tools.runtime.Platform;

import java.io.File;

/**
 * An {@link Option} to define {@link Platform} specific separators.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class PlatformSeparators implements Option
{
    /**
     * The {@link String} used by a {@link Platform} to separate paths with in a fully qualified file name.
     * <p>
     * eg: On *unix this is "/", on Windows it is "\"
     */
    private String fileSeparator;

    /**
     * The {@link String} used by a {@link Platform} to separate individual
     * paths occurring in a collection of paths.  eg: a class path
     * <p>
     * eg: On *unix this is ":", on Windows it is ";"
     */
    private String pathSeparator;

    /**
     * The {@link String} used by the {@link Platform} to separate individual lines of text.
     * <p>
     * eg: On *unix this is "\n", on Windows it is "\r\n"
     */
    private String lineSeparator;


    /**
     * Privately constructs {@link PlatformSeparators} given explicit characters.
     *
     * @param fileSeparator  the file separator
     * @param pathSeparator  the path separator
     * @param lineSeparator  the line separator
     */
    private PlatformSeparators(String fileSeparator,
                               String pathSeparator,
                               String lineSeparator)
    {
        this.fileSeparator = fileSeparator;
        this.pathSeparator = pathSeparator;
        this.lineSeparator = lineSeparator;
    }


    /**
     * Obtains The {@link String} used by a {@link Platform} to separate paths with in a fully qualified file name.
     * <p>
     * eg: On *unix this is "/", on Windows it is "\"
     *
     * @return the file separator
     */
    public String getFileSeparator()
    {
        return fileSeparator;
    }


    /**
     * Obtains the {@link String} used by a {@link Platform} to separate individual paths occurring in a
     * collection of paths.  eg: a class path
     * <p>
     * eg: On *unix this is ":", on Windows it is ";"
     *
     * @return the path separator
     */
    public String getPathSeparator()
    {
        return pathSeparator;
    }


    /**
     * Obtains the {@link String} used by the {@link Platform} to separate individual lines of text.
     *
     * @return the line separator
     */
    public String getLineSeparator()
    {
        return lineSeparator;
    }


    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }

        if (!(other instanceof PlatformSeparators))
        {
            return false;
        }

        PlatformSeparators that = (PlatformSeparators) other;

        if (!fileSeparator.equals(that.fileSeparator))
        {
            return false;
        }

        if (!lineSeparator.equals(that.lineSeparator))
        {
            return false;
        }

        if (!pathSeparator.equals(that.pathSeparator))
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        int result = fileSeparator.hashCode();

        result = 31 * result + pathSeparator.hashCode();
        result = 31 * result + lineSeparator.hashCode();

        return result;
    }


    /**
     * Auto-detect the {@link PlatformSeparators} based on the platform the calling
     * thread is executing in.
     *
     * @return  the current {@link Platform} {@link PlatformSeparators}
     */
    public static PlatformSeparators autoDetect()
    {
        return new PlatformSeparators(File.separator, File.pathSeparator, System.lineSeparator());
    }


    /**
     * Obtains suitable {@link PlatformSeparators} for the Microsoft Windows Platform.
     *
     * @return  the {@link PlatformSeparators} for Windows
     */
    public static PlatformSeparators forWindows()
    {
        return new PlatformSeparators("\\", ";", "\r\n");
    }


    /**
     * Obtains suitable {@link PlatformSeparators} for typical Unix-based platforms.
     *
     * @return  the {@link PlatformSeparators} for Unix
     */
    public static PlatformSeparators forUnix()
    {
        return new PlatformSeparators("/", ":", "\n");
    }

    /**
     * Obtains suitable {@link PlatformSeparators} for typical Cygwin-based platforms.
     *
     * @return  the {@link PlatformSeparators} for Cygwin
     */
    public static PlatformSeparators forCygwin()
    {
        return new PlatformSeparators("/", ":", "\n");
    }
}
