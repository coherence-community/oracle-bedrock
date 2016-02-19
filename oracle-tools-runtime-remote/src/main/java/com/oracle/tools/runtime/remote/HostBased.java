/*
 * File: HostBasedAuthentication.java
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
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.HostBasedUserAuth;
import com.oracle.tools.runtime.remote.ssh.JSchBasedAuthentication;
import com.oracle.tools.util.Triple;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * A host based {@link Authentication} see http://tools.ietf.org/html/rfc4252#section-9
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class HostBased implements Authentication, JSchBasedAuthentication
{
    /**
     * The empty pass phrase to use if none is required.
     */
    public static final byte[] NO_PASS_PHRASE = new byte[0];

    /**
     * The SSH configuration name for the preferred authentication methods list.
     */
    public static final String SSH_PREFERRED_AUTHENTICATIONS = "PreferredAuthentications";

    /**
     * The authentication method name for host based authentication.
     */
    public static final String AUTH_HOSTBASED = "hostbased";

    /**
     * The list of identities to try to use for authentication
     */
    private List<Triple<String,String,byte[]>> identities;


    /**
     * Create a {@link HostBased} using the specified
     * private key.
     *
     * @param privateKeyFileName  the private key to use to authenticate
     */
    public HostBased(String privateKeyFileName)
    {
        this(privateKeyFileName, null, NO_PASS_PHRASE);
    }


    /**
     * Create a {@link HostBased} using the specified
     * private key and pass phrase.
     *
     * @param privateKey  the private key to use to authenticate
     * @param passphrase  he optional pass phrase for the private key
     */
    public HostBased(String privateKey, byte[] passphrase)
    {
        this(privateKey, null, passphrase);
    }


    /**
     * Create a {@link HostBased} using the specified
     * private key and pass phrase.
     *
     * @param privateKey  the private key to use to authenticate
     * @param passphrase  he optional pass phrase for the private key
     */
    public HostBased(String privateKey, String passphrase)
    {
        this(privateKey, null, stringToBytes(passphrase));
    }


    /**
     * Create a {@link HostBased} using the specified
     * private key and pass phrase.
     *
     * @param privateKey  the private key to use to authenticate
     * @param publicKey   the optional public key to use to authenticate
     * @param passphrase  he optional pass phrase for the private key
     */
    public HostBased(String privateKey, String publicKey, String passphrase)
    {
        this(privateKey, publicKey, stringToBytes(passphrase));
    }


    /**
     * Create a {@link HostBased} using the specified
     * private key and pass phrase.
     *
     * @param privateKey  the private key to use to authenticate
     * @param publicKey   the optional public key to use to authenticate
     * @param passphrase  he optional pass phrase for the private key
     */
    public HostBased(String privateKey, String publicKey, byte[] passphrase)
    {
        if (privateKey == null || privateKey.isEmpty())
        {
            throw new IllegalArgumentException("The private key file name is required");
        }

        identities = new ArrayList<>();
        identities.add(new Triple<>(privateKey, publicKey, passphrase));
    }


    /**
     * Add an alternative identity that may be used to attempt authentication.
     *
     * @param privateKey  the private key to use to authenticate
     *
     * @return  this {@link HostBased}
     */
    public HostBased addIdentity(String privateKey)
    {
        return addIdentity(privateKey, null, NO_PASS_PHRASE);
    }


    /**
     * Add an alternative identity that may be used to attempt authentication.
     *
     * @param privateKey  the private key to use to authenticate
     * @param passphrase  he optional pass phrase for the private key
     *
     * @return  this {@link HostBased}
     */
    public HostBased addIdentity(String privateKey, String passphrase)
    {
        return addIdentity(privateKey, null, stringToBytes(passphrase));
    }


    /**
     * Add an alternative identity that may be used to attempt authentication.
     *
     * @param privateKey  the private key to use to authenticate
     * @param passphrase  he optional pass phrase for the private key
     *
     * @return  this {@link HostBased}
     */
    public HostBased addIdentity(String privateKey, byte[] passphrase)
    {
        return addIdentity(privateKey, null, passphrase);
    }


    /**
     * Add an alternative identity that may be used to attempt authentication.
     *
     * @param privateKey  the private key to use to authenticate
     * @param publicKey   the optional public key to use to authenticate
     * @param passphrase  he optional pass phrase for the private key
     *
     * @return  this {@link HostBased}
     */
    public HostBased addIdentity(String privateKey, String publicKey, String passphrase)
    {
        return addIdentity(privateKey, publicKey, stringToBytes(passphrase));
    }


    /**
     * Add an alternative identity that may be used to attempt authentication.
     *
     * @param privateKey  the private key to use to authenticate
     * @param publicKey   the optional public key to use to authenticate
     * @param passphrase  he optional pass phrase for the private key
     *
     * @return  this {@link HostBased}
     */
    public HostBased addIdentity(String privateKey, String publicKey, byte[] passphrase)
    {
        if (privateKey == null || privateKey.isEmpty())
        {
            throw new IllegalArgumentException("The private key file name is required");
        }

        identities.add(new Triple<>(privateKey, publicKey, passphrase));

        return this;
    }


    @Override
    public void configureFramework(JSch jsch)
    {
        try
        {
            // Add all of the identities
            for (Triple<String,String,byte[]> identity : identities)
            {
                String privateKey = identity.getX();
                String publicKey  = identity.getY();
                byte[] passphrase = identity.getZ();

                jsch.addIdentity(privateKey, publicKey, passphrase);
            }

        }
        catch (JSchException e)
        {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void configureSession(Session session)
    {
        // Tell JSch to use the HostBasedUserAuth class for hostbased authentication
        session.setConfig("userauth." + AUTH_HOSTBASED, HostBasedUserAuth.class.getCanonicalName());

        // Make sure that hostbased authentication is in the session's list of preferred authentication methods
        String preferredAuthentications = session.getConfig(SSH_PREFERRED_AUTHENTICATIONS);

        if (preferredAuthentications != null)
        {
            String[] auths        = preferredAuthentications.split(",");
            boolean  hasHostBased = false;

            for (int i = 0; i < auths.length && !hasHostBased; i++)
            {
                hasHostBased = auths[i].toLowerCase().equals(AUTH_HOSTBASED);
            }

            if (!hasHostBased)
            {
                session.setConfig(SSH_PREFERRED_AUTHENTICATIONS, AUTH_HOSTBASED + "," + preferredAuthentications);
            }
        }
        else
        {
            session.setConfig(SSH_PREFERRED_AUTHENTICATIONS, AUTH_HOSTBASED);
        }
    }


    /**
     * Convert the specified String to a UTF-8 byte array.
     *
     * @param s  the string to convert
     *
     * @return  the UTF-8 bytes representing the String
     */
    public static byte[] stringToBytes(String s)
    {
        try
        {
            return s.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }
}
