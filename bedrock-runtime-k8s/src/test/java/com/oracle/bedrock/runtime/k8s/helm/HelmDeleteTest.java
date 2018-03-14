/*
 * File: HelmDeleteTest.java
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

package com.oracle.bedrock.runtime.k8s.helm;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for {@link HelmDelete}.
 * <p>
 * Copyright (c) 2018. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class HelmDeleteTest
        extends CommonCommandTests<HelmDelete>
{
    @Test
    public void shouldCreateBasicCommand()
    {
        HelmDelete command = Helm.delete("foo");

        assertCommand(command, "delete", "foo");
    }

    @Test
    public void shouldCreateFromTemplate() throws Exception
    {
        HelmCommand.Template command = Helm.template();
        HelmDelete           copy    = command.delete("foo");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "delete", "foo");
    }

    @Test
    public void shouldAddDryRunOption()
    {
        HelmDelete command = Helm.delete("foo");
        HelmDelete copy    = command.dryRun();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "delete", "--dry-run", "foo");
    }

    @Test
    public void shouldAddNoHooksOption()
    {
        HelmDelete command = Helm.delete("foo");
        HelmDelete copy    = command.noHooks();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "delete", "--no-hooks", "foo");
    }

    @Test
    public void shouldAddPurgeOption()
    {
        HelmDelete command = Helm.delete("foo");
        HelmDelete copy    = command.purge();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "delete", "--purge", "foo");
    }

    @Test
    public void shouldAddTimeoutOption()
    {
        HelmDelete command = Helm.delete("foo");
        HelmDelete copy    = command.timeout(19);

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "delete", "--timeout", "19", "foo");
    }

    @Test
    public void shouldAddTlsOption()
    {
        HelmDelete command = Helm.delete("foo");
        HelmDelete copy    = command.tls();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "delete", "--tls", "foo");
    }

    @Test
    public void shouldAddTlsCaCertOption()
    {
        HelmDelete command = Helm.delete("foo");
        HelmDelete copy    = command.tlsCaCert("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "delete", "--tls-ca-cert", "bar", "foo");
    }

    @Test
    public void shouldAddTlsCertOption()
    {
        HelmDelete command = Helm.delete("foo");
        HelmDelete copy    = command.tlsCert("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "delete", "--tls-cert", "bar", "foo");
    }

    @Test
    public void shouldAddTlsKeyOption()
    {
        HelmDelete command = Helm.delete("foo");
        HelmDelete copy    = command.tlsKey("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "delete", "--tls-key", "bar", "foo");
    }

    @Test
    public void shouldAddTlsVerifyOption()
    {
        HelmDelete command = Helm.delete("foo");
        HelmDelete copy    = command.tlsVerify();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "delete", "--tls-verify", "foo");
    }

    @Override
    HelmDelete newInstance()
    {
        return Helm.delete("foo");
    }
}
