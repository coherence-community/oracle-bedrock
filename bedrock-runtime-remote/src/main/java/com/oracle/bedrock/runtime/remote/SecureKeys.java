/*
 * File: SecureKeys.java
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
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import com.oracle.bedrock.runtime.remote.ssh.JSchBasedAuthentication;

/**
 * A secure public-private key-based {@link Authentication}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class SecureKeys implements Authentication, JSchBasedAuthentication
{
    /**
     * The file containing the private key.
     */
    private String privateKeyFileName;

    /**
     * The file containing the public key.
     */
    private String publicKeyFileName;


    /**
     * Constructs a {@link SecureKeys} based on the private key
     * contained in the specified file.  Typically the private key
     * file is something like: ~/.ssh/id_dsa
     * <p>
     * The corresponding public key file is assumed to be the same as
     * the private key file, but ending in .pub.  For example: ~/.ssh/id_dsa.pub
     *
     * @param privateKeyFileName  the private key file name
     */
    private SecureKeys(String privateKeyFileName)
    {
        this(privateKeyFileName + ".pub", privateKeyFileName);
    }


    /**
     * Constructs a {@link SecureKeys}.
     * <p>
     * Typically the private key file is something like: ~/.ssh/id_dsa
     * and the corresponding public key file something like: ~/.ssh/id_dsa.pub
     *
     * @param publicKeyFileName   the public key file name
     * @param privateKeyFileName  the private key file name
     */
    private SecureKeys(String publicKeyFileName,
                       String privateKeyFileName)
    {
        this.privateKeyFileName = privateKeyFileName;
        this.publicKeyFileName  = publicKeyFileName;
    }


    @Override
    public void configureFramework(JSch jsch)
    {
        try
        {
            jsch.addIdentity(privateKeyFileName, publicKeyFileName);
        }
        catch (JSchException e)
        {
            throw new RuntimeException("Failed to configure security framework", e);
        }
    }


    @Override
    public void configureSession(Session session)
    {
        // nothing to do to configure a session
    }


    /**
     * Construct {@link SecureKeys} given a private key in a file.
     * <p>
     * Typically the private key file is something like: ~/.ssh/id_dsa
     * and the corresponding public key file is assumed to be something
     * like: ~/.ssh/id_dsa.pub
     *
     * @param privateKeyFileName  the path to the private key file
     *
     * @return  {@link SecureKeys}
     */
    public static SecureKeys fromPrivateKeyFile(String privateKeyFileName)
    {
        return new SecureKeys(privateKeyFileName);
    }


    /**
     * Construct {@link SecureKeys} given the public and private key files.
     * <p>
     * Typically the private key file is something like: ~/.ssh/id_dsa
     * and the corresponding public key file something like: ~/.ssh/id_dsa.pub
     *
     * @param publicKeyFileName   the path to the public key file
     * @param privateKeyFileName  the path to the private key file
     *
     * @return  {@link SecureKeys}
     */
    public static SecureKeys fromKeyFiles(String publicKeyFileName,
                                          String privateKeyFileName)
    {
        return new SecureKeys(publicKeyFileName, privateKeyFileName);
    }
}
