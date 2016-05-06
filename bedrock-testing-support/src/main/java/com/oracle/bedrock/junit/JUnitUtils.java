/*
 * File: JUnitUtils.java
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

import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Various JUnit utilities
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public abstract class JUnitUtils
{
    private static final Pattern BRACKETS = Pattern.compile("^" + ".+" + "\\((" + "[^\\\\(\\\\)]+" + ")\\)" + "$");


    public static String findClassName(Description description)
    {
        String  displayName = description.getDisplayName();
        Matcher matcher     = BRACKETS.matcher(displayName);
        String  name        = matcher.find() ? matcher.group(1) : displayName;

        if (name == null || "null".equals(name))
        {
            Description childDescription = description.getChildren().get(0);

            if (childDescription != null)
            {
                String childName = childDescription.getDisplayName();

                matcher = BRACKETS.matcher(childName);
                name    = matcher.find() ? matcher.group(1) : childName;
            }

            if (name == null)
            {
                name = "Error instantiating test";
            }
        }

        return name;
    }


    public static String getIgnoredMessage(Description description)
    {
        String message = null;

        Ignore ignore  = description.getAnnotation(Ignore.class);

        if (ignore != null)
        {
            message = ignore.value();
        }

        return message;
    }


    public static String getFailureMessage(Failure failure)
    {
        String header = failure.getTestHeader();

        if (header == null || "null".equals(header))
        {
            header = "Failed to construct test";
        }

        return header;
    }
}
