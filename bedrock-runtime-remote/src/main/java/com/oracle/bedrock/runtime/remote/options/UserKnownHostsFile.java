/*
 * File: UserKnownHostsFile.java
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

package com.oracle.bedrock.runtime.remote.options;

import com.oracle.bedrock.Option;

/**
 * An option to define the user known hosts file for ssh sessions.
 * <p>
 * Copyright (c) 2018. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class UserKnownHostsFile implements Option
{
    private final String file;

    /**
     * Create a {@link UserKnownHostsFile} option.
     *
     * @param file  the location of the user known hosts file
     */
    public UserKnownHostsFile(String file)
    {
        this.file = file;
    }

    public String getFile()
    {
        return file;
    }

    /**
     * Create a {@link UserKnownHostsFile} option.
     *
     * @param file  the location of the user known hosts file
     *
     * @return  a {@link UserKnownHostsFile} option
     */
    public static UserKnownHostsFile at(String file)
    {
        return new UserKnownHostsFile(file);
    }
}
