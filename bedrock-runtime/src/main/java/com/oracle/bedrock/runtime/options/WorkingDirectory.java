/*
 * File: WorkingDirectory.java
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

package com.oracle.bedrock.runtime.options;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.lang.ExpressionEvaluator;
import com.oracle.bedrock.runtime.Platform;

import java.io.File;
import java.util.Calendar;
import java.util.Iterator;

/**
 * An {@link Option} implementation that is used to determine the working directory
 * for an application.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class WorkingDirectory implements Option
{
    /**
     * The value that will be used to determine the working directory.
     */
    private final Object workingDirectory;


    /**
     * Create a {@link WorkingDirectory} that will use the specified
     * value to determine an application's working directory.
     *
     * @param workingDirectory  value that will be used to determine the working directory
     */
    private WorkingDirectory(Object workingDirectory)
    {
        this.workingDirectory = workingDirectory;
    }


    /**
     * Obtain the value that will be used to determine the working directory.
     *
     * @return  the value that will be used to determine the working directory
     */
    public Object getValue()
    {
        return workingDirectory;
    }


    /**
     * Resolve and obtain the {@link File} representing the working directory to use for an application.
     *
     * @param platform       the {@link Platform} that the application will run on
     * @param optionsByType  the {@link OptionsByType} to use
     *
     * @return  the {@link File} pointing to the working directory to use for an application
     */
    public File resolve(Platform      platform,
                        OptionsByType optionsByType)
    {
        if (workingDirectory == null)
        {
            return null;
        }

        if (workingDirectory instanceof File)
        {
            return (File) workingDirectory;
        }

        DisplayName         displayName   = optionsByType.get(DisplayName.class);

        Discriminator       discriminator = optionsByType.get(Discriminator.class);

        ExpressionEvaluator evaluator     = new ExpressionEvaluator(optionsByType);

        evaluator.defineVariable("applicationName", displayName.resolve(optionsByType));
        evaluator.defineVariable("displayName", displayName.resolve(null));

        if (discriminator != null)
        {
            evaluator.defineVariable("discriminator", discriminator.getValue());
        }

        evaluator.defineVariable("platform", platform);

        Object directoryValue = workingDirectory;

        if (directoryValue instanceof WorkingDirectory.ContextSensitiveDirectoryName)
        {
            WorkingDirectory.ContextSensitiveDirectoryName contextSensitiveValue =
                (WorkingDirectory.ContextSensitiveDirectoryName) directoryValue;

            directoryValue = contextSensitiveValue.resolve(platform, optionsByType);
        }

        if (directoryValue instanceof Iterator<?>)
        {
            Iterator<?> iterator = (Iterator<?>) directoryValue;

            if (iterator.hasNext())
            {
                directoryValue = iterator.next().toString();
            }
            else
            {
                throw new IndexOutOfBoundsException(String.format("No more values available for the working directory [%s]",
                                                                  workingDirectory));
            }
        }

        if (directoryValue instanceof File)
        {
            return (File) directoryValue;
        }

        String expression = directoryValue.toString().trim();

        Object result     = evaluator.evaluate(expression, Object.class);

        return new File(String.valueOf(result));
    }


    /**
     * Create a {@link WorkingDirectory} that will use the current directory
     * as the working directory.
     *
     * @return  a {@link WorkingDirectory} that will use the current directory
     *          as the working directory
     */
    public static WorkingDirectory currentDirectory()
    {
        return new WorkingDirectory(System.getProperty("user.dir"));
    }


    public static WorkingDirectory temporaryDirectory()
    {
        ContextSensitiveDirectoryName name = new ContextSensitiveDirectoryName()
        {
            @Override
            public Object resolve(Platform      platform,
                                  OptionsByType optionsByType)
            {
                DisplayName        displayName = optionsByType.get(DisplayName.class);
                PlatformSeparators separators  = optionsByType.get(PlatformSeparators.class);
                String sanitizedDisplayName    = separators.asSanitizedFileName(displayName.resolve(optionsByType));
                Calendar           now         = Calendar.getInstance();
                String temporaryDirectoryName = String.format("%1$s-%2$tY%2$tm%2$td-%2$tH%2$tM%2$tS-%2$tL",
                                                              sanitizedDisplayName,
                                                              now);

                TemporaryDirectory defaultTemp = TemporaryDirectory.at(separators.getFileSeparator() + "tmp");
                TemporaryDirectory temporaryDirectory = optionsByType.getOrDefault(TemporaryDirectory.class,
                                                                                   defaultTemp);

                return new File(temporaryDirectory.get().toFile(), temporaryDirectoryName);
            }
        };

        return new WorkingDirectory(name);
    }


    /**
     * Create a {@link WorkingDirectory} where the working directory for
     * the application will be at the specified fixed location.
     *
     * @param workingDirectory  the working directory location
     *
     * @return  a {@link WorkingDirectory} where the working directory for
     *          the application will be at the specified fixed location
     */
    public static WorkingDirectory at(File workingDirectory)
    {
        return (workingDirectory != null) ? new WorkingDirectory(workingDirectory) : currentDirectory();
    }


    /**
     * Create a {@link WorkingDirectory} where the working directory for
     * the application will be realized using the specified value
     *
     * @param workingDirectory  the value to use to determine the working directory
     *
     * @return  a {@link WorkingDirectory} where the working directory for
     *          the application will be realized using the specified value
     */
    public static WorkingDirectory at(Object workingDirectory)
    {
        return (workingDirectory != null) ? new WorkingDirectory(workingDirectory) : currentDirectory();
    }


    /**
     * Create a {@link WorkingDirectory} where the working directory for
     * the application will be a sub directory of the specified parent directory.
     *
     * @param parent  the parent directory of the working directory
     *
     * @return  a {@link WorkingDirectory} where the working directory for
     *          the application will be a sub directory of the specified
     *          parent directory
     */
    public static WorkingDirectory subDirectoryOf(final File parent)
    {
        ContextSensitiveDirectoryName name = new ContextSensitiveDirectoryName()
        {
            @Override
            public Object resolve(Platform      platform,
                                  OptionsByType optionsByType)
            {
                DisplayName        displayName = optionsByType.get(DisplayName.class);
                PlatformSeparators separators  = optionsByType.get(PlatformSeparators.class);
                String sanitizedDisplayName    = separators.asSanitizedFileName(displayName.resolve(optionsByType));
                File               parentFile  = (parent != null) ? parent : new File(System.getProperty("user.dir"));

                return new File(parentFile, sanitizedDisplayName);
            }
        };

        return new WorkingDirectory(name);
    }


    /**
     * A context sensitive directory name, possibly based on the application name,
     * the {@link Platform} and/or {@link Option}s.
     */
    public interface ContextSensitiveDirectoryName
    {
        /**
         * Obtains the value for the working directory, possibly based on the provided {@link Platform}
         * and {@link Option}s.
         *
         * @param platform       the {@link Platform} in which {@link Argument} is being used.
         * @param optionsByType  the {@link OptionsByType} to control directory creation
         *
         * @return the value
         */
        Object resolve(Platform      platform,
                       OptionsByType optionsByType);
    }
}
