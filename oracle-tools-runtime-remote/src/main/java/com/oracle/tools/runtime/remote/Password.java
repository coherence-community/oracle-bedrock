/*
 * File: Password.java
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

package com.oracle.tools.runtime.remote;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import com.oracle.tools.runtime.remote.ssh.JSchBasedAuthentication;

/**
 * A password-based {@link Authentication}.
 * <p>
 * WARNING: It is generally unadvisable to use password-based {@link Authentication}.
 * Instead secure public/private key-based {@link Authentication}s should be used.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @see SecureKeys
 */
public class Password implements Authentication, JSchBasedAuthentication
{
    /**
     * The password or the user on the remote system.
     */
    private String password;


    /**
     * Constructs a {@link Password}.
     *
     * @param password  the password of the remote user
     */
    public Password(String password)
    {
        this.password = password;
    }


    @Override
    public void configureFramework(JSch jsch)
    {
        // skip - there's no requirement to configure the framework
        // when using password-based authentication
    }


    @Override
    public void configureSession(Session session)
    {
        // set the password for the session
        session.setPassword(password);
    }
}
