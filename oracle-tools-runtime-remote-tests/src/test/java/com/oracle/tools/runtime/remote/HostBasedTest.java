package com.oracle.tools.runtime.remote;

import com.jcraft.jsch.HostBasedUserAuth;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Jonathan Knight
 */
public class HostBasedTest
{
    @Test
    public void shouldCreateAuthenticationWithPrivateKey() throws Exception
    {
        JSch      jSch           = mock(JSch.class);
        HostBased authentication = new HostBased("foo");

        authentication.configureFramework(jSch);

        verify(jSch).addIdentity("foo", null, HostBased.NO_PASS_PHRASE);
    }


    @Test
    public void shouldCreateAuthenticationWithPrivateKeyAndPass() throws Exception
    {
        JSch      jSch           = mock(JSch.class);
        byte[]    passphrase     = "secret".getBytes();
        HostBased authentication = new HostBased("foo", passphrase);

        authentication.configureFramework(jSch);

        verify(jSch).addIdentity("foo", null, passphrase);
    }


    @Test
    public void shouldCreateAuthenticationWithPrivateKeyAndStringPass() throws Exception
    {
        JSch      jSch           = mock(JSch.class);
        HostBased authentication = new HostBased("foo", "secret");

        authentication.configureFramework(jSch);

        verify(jSch).addIdentity("foo", null, HostBased.stringToBytes("secret"));
    }


    @Test
    public void shouldCreateAuthenticationWithPrivateKeyPublicKeyAndPass() throws Exception
    {
        JSch   jSch       = mock(JSch.class);
        byte[] passphrase = "secret".getBytes();

        HostBased authentication = new HostBased("foo", "bar", passphrase);

        authentication.configureFramework(jSch);

        verify(jSch).addIdentity("foo", "bar", passphrase);
    }


    @Test
    public void shouldCreateAuthenticationWithPrivateKeyPublicKeyAndStringPass() throws Exception
    {
        JSch      jSch           = mock(JSch.class);
        HostBased authentication = new HostBased("foo", "bar", "secret");

        authentication.configureFramework(jSch);

        verify(jSch).addIdentity("foo", "bar", HostBased.stringToBytes("secret"));
    }


    @Test
    public void shouldAddIdentityWithPrivateKey() throws Exception
    {
        JSch      jSch           = mock(JSch.class);
        HostBased authentication = new HostBased("key1");

        authentication.addIdentity("foo");

        authentication.configureFramework(jSch);

        verify(jSch).addIdentity("key1", null, HostBased.NO_PASS_PHRASE);
        verify(jSch).addIdentity("foo", null, HostBased.NO_PASS_PHRASE);
    }


    @Test
    public void shouldAddIdentityWithPrivateKeyAndPass() throws Exception
    {
        JSch      jSch           = mock(JSch.class);
        byte[]    passphrase     = "secret".getBytes();
        HostBased authentication = new HostBased("key1");

        authentication.addIdentity("foo", passphrase);

        authentication.configureFramework(jSch);

        verify(jSch).addIdentity("key1", null, HostBased.NO_PASS_PHRASE);
        verify(jSch).addIdentity("foo", null, passphrase);
    }


    @Test
    public void shouldAddIdentityWithPrivateKeyAndStringPass() throws Exception
    {
        JSch      jSch           = mock(JSch.class);
        HostBased authentication = new HostBased("key1");

        authentication.addIdentity("foo", "secret");

        authentication.configureFramework(jSch);

        verify(jSch).addIdentity("key1", null, HostBased.NO_PASS_PHRASE);
        verify(jSch).addIdentity("foo", null, HostBased.stringToBytes("secret"));
    }


    @Test
    public void shouldAddIdentityWithPrivateKeyPublicKeyAndPass() throws Exception
    {
        JSch      jSch           = mock(JSch.class);
        byte[]    passphrase     = "secret".getBytes();
        HostBased authentication = new HostBased("key1");

        authentication.addIdentity("foo", "bar", passphrase);

        authentication.configureFramework(jSch);

        verify(jSch).addIdentity("key1", null, HostBased.NO_PASS_PHRASE);
        verify(jSch).addIdentity("foo", "bar", passphrase);
    }


    @Test
    public void shouldAddIdentityWithPrivateKeyPublicKeyAndStringPass() throws Exception
    {
        JSch      jSch           = mock(JSch.class);
        HostBased authentication = new HostBased("key1");

        authentication.addIdentity("foo", "bar", "secret");

        authentication.configureFramework(jSch);

        verify(jSch).addIdentity("key1", null, HostBased.NO_PASS_PHRASE);
        verify(jSch).addIdentity("foo", "bar", HostBased.stringToBytes("secret"));
    }


    @Test
    public void shouldSetUserAuthIntoSession() throws Exception
    {
        Session   session        = mock(Session.class);
        HostBased authentication = new HostBased("key1");

        authentication.configureSession(session);

        verify(session).setConfig("userauth.hostbased", HostBasedUserAuth.class.getCanonicalName());
    }


    @Test
    public void shouldSetHostbasedAsPreferredAuthenticationInSession() throws Exception
    {
        Session   session        = mock(Session.class);
        HostBased authentication = new HostBased("key1");

        when(session.getConfig("PreferredAuthentications")).thenReturn(null);

        authentication.configureSession(session);

        verify(session).setConfig("PreferredAuthentications", "hostbased");
    }

    @Test
    public void shouldAddHostbasedAsPreferredAuthenticationInSession() throws Exception
    {
        Session   session        = mock(Session.class);
        HostBased authentication = new HostBased("key1");

        when(session.getConfig("PreferredAuthentications")).thenReturn("password");

        authentication.configureSession(session);

        verify(session).setConfig("PreferredAuthentications", "hostbased,password");
    }
}
