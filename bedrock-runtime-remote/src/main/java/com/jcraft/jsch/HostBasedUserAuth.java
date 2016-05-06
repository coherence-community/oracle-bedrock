/*
 * File: HostBasedUserAuth.java
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

package com.jcraft.jsch;

import com.oracle.bedrock.runtime.remote.ssh.JSchSocketFactory;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.Vector;

/**
 * A JSch UserAuth implementation for SSH host based authentication,
 * <p>
 * This class creates the correct hostbased authentication messages as detailed
 * in http://tools.ietf.org/html/rfc4252#section-9 and https://www.ietf.org/rfc/rfc4252.txt
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class HostBasedUserAuth extends UserAuth
{
    /**
     * The standard name of the host based authentication method
     */
    public static final String AUTH_HOSTBASED = "hostbased";


    @SuppressWarnings("unchecked")
    @Override
    public boolean start(Session session) throws Exception
    {
        super.start(session);

        Logger             logger        = JSch.getLogger();
        InetAddress        localAddress  = ((JSchSocketFactory) session.socket_factory).getLastLocalAddress();
        String             localHostName = localAddress.getCanonicalHostName();
        Vector<Identity>   identities    = session.jsch.getIdentityRepository().getIdentities();
        Iterator<Identity> iterator      = identities.iterator();
        boolean            authenticated = false;

        // Try to authenticate using each of the available identities
        while (!authenticated && iterator.hasNext())
        {
            Identity identity = iterator.next();

            logger.log(Logger.DEBUG, "Trying hostbased auth with identity " + identity);

            // Obtain the public key blob from the identity
            byte[] publicKey = identity.getPublicKeyBlob();

            if (publicKey == null)
            {
                // There is no public key so try the next Identity
                logger.log(Logger.DEBUG, "Identity " + identity + " has no public key blob");
                continue;
            }

            try
            {
                // Create the message in this UserAuth internal buffer
                createAuthMessage(localHostName, identity.getAlgName(), publicKey);

                // Add the signature to the message
                addSignature(session, identity);

                // Send the authentication message
                session.write(packet);

                // handle the responses
                authenticated = handleAuthResponse(session);
            }
            catch (JSchPartialAuthException partial)
            {
                // The message for a JSchPartialAuthException contains the list of
                // authentication methods that can be tried. If hostbased is not in
                // the list there is no point continuing so just re-throw the exception
                if (partial.getMessage().toLowerCase().contains(AUTH_HOSTBASED))
                {
                    logger.log(Logger.INFO, "Authentication failed for " + identity);
                    continue;
                }

                throw partial;
            }
            catch (Exception e)
            {
                logger.log(Logger.INFO, "Authentication failed for " + identity + " - " + e.getMessage());
            }
        }

        return authenticated;
    }


    /**
     * Create the authentication message to send to the server. The message is created
     * in this {@link UserAuth}'s internal {@link #buf} field.
     * The formal of the message is defined by http://tools.ietf.org/html/rfc4252#section-9
     *
     * @param localHostName  the local host name being used for the hostbased authentication
     * @param algorithm      the name of the key algorithm
     * @param publicKeyBlob  the public key
     *
     * @throws Exception when the auth message can't be created
     */
    protected void createAuthMessage(String localHostName,
                                     String algorithm,
                                     byte[] publicKeyBlob) throws Exception
    {
/*
        https://www.ietf.org/rfc/rfc4252.txt section 9.  Host-Based Authentication: "hostbased"

          byte      SSH_MSG_USERAUTH_REQUEST
          string    user name
          string    service name
          string    "hostbased"
          string    public key algorithm for host key
          string    public host key and certificates for client host
          string    client host name expressed as the FQDN in US-ASCII
          string    user name on the client host in ISO-10646 UTF-8 encoding
                     [RFC3629]
          string    signature
*/

        // Reset the packet that we will send
        packet.reset();

        buf.putByte((byte) SSH_MSG_USERAUTH_REQUEST);
        buf.putString(Util.str2byte(username));
        buf.putString(Util.str2byte("ssh-connection"));
        buf.putString(Util.str2byte(AUTH_HOSTBASED));
        buf.putString(Util.str2byte(algorithm));
        buf.putString(publicKeyBlob);
        buf.putString(Util.str2byte(localHostName));
        buf.putString(Util.str2byte(username));
    }


    /**
     * Create a signature from the {@link Session#getSessionId()}
     * and current {@link #buf} contents.
     *
     * @param session   the {@link Session} to get the sessionId from
     * @param identity  the {@link Identity} being used to authenticate to the server
     *
     * @throws Exception when the signature can't be added
     */
    protected void addSignature(Session  session,
                                Identity identity) throws Exception
    {
/*
        https://www.ietf.org/rfc/rfc4252.txt section 9.

        The value of 'signature' is a signature with the private host key of
        the following data, in this order:

            string    session identifier
            byte      SSH_MSG_USERAUTH_REQUEST
            string    user name
            string    service name
            string    "hostbased"
            string    public key algorithm for host key
            string    public host key and certificates for client host
            string    client host name expressed as the FQDN in US-ASCII
            string    user name on the client host in ISO-10646 UTF-8 encoding
                     [RFC3629]
 */

        // The contents of the signature are identical th what we have already added to the buffer
        // with the session id prepended to it so we just copy the buffer, add the id and sign.

        byte[] sessionId = session.getSessionId();
        Buffer buffer    = new Buffer((sessionId.length + 4) + (buf.index - 5));

        buffer.putString(sessionId);
        buffer.putByte(buf.buffer, 5, buf.index - 5);

        byte[] signature = identity.getSignature(buffer.buffer);

        if (signature == null)
        {
            throw new JSchException("Could not obtain signature from identity " + identity);
        }

        // Add the signature to the buffer
        buf.putString(signature);
    }


    /**
     * Handle the authentication response from the server.
     *
     * @param session  the current {@link Session} being authenticated
     *
     * @return  true if authentication is successful, otherwise false
     *
     * @throws Exception if an error occurs
     */
    protected boolean handleAuthResponse(Session session) throws Exception
    {
        while (true)
        {
            // Read the response from the server
            buf = session.read(buf);

            // Obtain the response code
            int response = buf.getCommand() & 0xff;

            switch (response)
            {
            case SSH_MSG_USERAUTH_SUCCESS :

                // auth was successful so just return true
                return true;

            case SSH_MSG_USERAUTH_FAILURE :
                handleFailure();

                return false;

            case SSH_MSG_USERAUTH_BANNER :

                // we have a banner message so handle it and go around again
                handleBanner();
                break;

            default :

                // We have an unknown response so fail
                Logger logger = JSch.getLogger();

                logger.log(Logger.WARN, "Unknown response code from server " + response);

                return false;
            }
        }
    }


    /**
     * Handle a SSH_MSG_USERAUTH_BANNER response from the server.
     */
    protected void handleBanner()
    {
/*
        https://www.ietf.org/rfc/rfc4252.txt section 5.4.  Banner Message

        In some jurisdictions, sending a warning message before
        authentication may be relevant for getting legal protection.  Many
        UNIX machines, for example, normally display text from /etc/issue,
        use TCP wrappers, or similar software to display a banner before
        issuing a login prompt.

        The SSH server may send an SSH_MSG_USERAUTH_BANNER message at any
        time after this authentication protocol starts and before
        authentication is successful.  This message contains text to be
        displayed to the client user before authentication is attempted.  The
        format is as follows:

            byte      SSH_MSG_USERAUTH_BANNER
            string    message in ISO-10646 UTF-8 encoding [RFC3629]
            string    language tag [RFC3066]

        By default, the client SHOULD display the 'message' on the screen.
        However, since the 'message' is likely to be sent for every login
        attempt, and since some client software will need to open a separate
        window for this warning, the client software may allow the user to
        explicitly disable the display of banners from the server.  The
        'message' may consist of multiple lines, with line breaks indicated
        by CRLF pairs.
*/

        skipBytes(buf, 6);

        // The response contains a banner message and a language indicator,
        // we only need the message as JSch has no way to handle the language
        String banner = Util.byte2str(buf.getString());

        if (userinfo == null)
        {
            // The userinfo field for this UserAuth is null so
            // we have no way of displaying the banner so we
            // will just log it
            Logger logger = JSch.getLogger();

            logger.log(Logger.INFO, banner);
        }
        else
        {
            userinfo.showMessage(banner);
        }
    }


    /**
     * Handle an SSH_MSG_USERAUTH_FAILURE response from the server.
     *
     * @throws JSchPartialAuthException if this is a partial failure
     */
    protected void handleFailure() throws JSchPartialAuthException
    {
/*
        https://www.ietf.org/rfc/rfc4252.txt section 5.1.  Responses to Authentication Requests

        If the server rejects the authentication request, it MUST respond
        with the following:

        byte         SSH_MSG_USERAUTH_FAILURE
        name-list    authentications that can continue
        boolean      partial success
 */
        skipBytes(buf, 6);

        String nameList = Util.byte2str(buf.getString());
        int    partial  = buf.getByte();

        if (partial != 0)
        {
            // Partial success indicates to JSch the remaining list of
            // possible authentication methods that can be tried
            throw new JSchPartialAuthException(nameList);
        }
    }


    /**
     * Skip the specified number of bytes in the specified buffer.
     *
     * @param buffer  the buffer to read bytes from
     * @param count   the number of btes to skip
     */
    private void skipBytes(Buffer buffer,
                           int    count)
    {
        for (int i = 0; i < count; i++)
        {
            buffer.getByte();
        }
    }
}
