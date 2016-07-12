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

package com.oracle.bedrock.runtime.remote;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.options.HttpProxy;
import com.oracle.bedrock.runtime.remote.http.HttpAuthenticationType;
import com.oracle.bedrock.runtime.remote.http.HttpBasedAuthentication;
import com.oracle.bedrock.runtime.remote.ssh.JSchBasedAuthentication;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

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
public class Password implements Authentication, JSchBasedAuthentication, HttpBasedAuthentication
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


    @Override
    public HttpURLConnection openConnection(URL           url,
                                            String        userName,
                                            OptionsByType optionsByType) throws IOException
    {
        HttpAuthenticationType authType = optionsByType.getOrSetDefault(HttpAuthenticationType.class,
                                                                        HttpAuthenticationType.Basic);
        HttpProxy         proxy = optionsByType.getOrSetDefault(HttpProxy.class, HttpProxy.none());
        HttpURLConnection connection;

        switch (authType)
        {
        case Basic :
            String userPassword = userName + ":" + password;
            String encoding     = new sun.misc.BASE64Encoder().encode(userPassword.getBytes());

            connection = proxy.openConnection(url);

            connection.setRequestProperty("Authorization", "Basic " + encoding);
            break;

        case NTLM :
        case Kerberos :
        default :
            throw new IllegalArgumentException("Unsupported HTTP authentication type " + authType);
        }

        return connection;
    }
}
