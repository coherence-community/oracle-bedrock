/*
 * File: HelmGetTest.java
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
 * Tests for {@link HelmGet}.
 * <p>
 * Copyright (c) 2018. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class HelmGetTest
        extends CommonCommandTests<HelmGet>
{
    @Test
    public void shouldCreateBasicCommand()
    {
        HelmGet command = Helm.get("foo");

        assertCommand(command, "get", "foo");
    }

    @Test
    public void shouldCreateFromTemplate()
    {
        HelmCommand.Template command = Helm.template();
        HelmGet              copy    = command.get("foo");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "get", "foo");
    }

    @Test
    public void shouldAddRevisionOption()
    {
        HelmGet command = Helm.get("foo");
        HelmGet copy    = command.revision(19);

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "get", "--revision", "19", "foo");
    }

    @Test
    public void shouldAddTlsOption()
    {
        HelmGet command = Helm.get("foo");
        HelmGet copy    = command.tls();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "get", "--tls", "foo");
    }

    @Test
    public void shouldAddTlsCaCertOption()
    {
        HelmGet command = Helm.get("foo");
        HelmGet copy    = command.tlsCaCert("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "get", "--tls-ca-cert", "bar", "foo");
    }

    @Test
    public void shouldAddTlsCertOption()
    {
        HelmGet command = Helm.get("foo");
        HelmGet copy    = command.tlsCert("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "get", "--tls-cert", "bar", "foo");
    }

    @Test
    public void shouldAddTlsKeyOption()
    {
        HelmGet command = Helm.get("foo");
        HelmGet copy    = command.tlsKey("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "get", "--tls-key", "bar", "foo");
    }

    @Test
    public void shouldAddTlsVerifyOption()
    {
        HelmGet command = Helm.get("foo");
        HelmGet copy    = command.tlsVerify();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "get", "--tls-verify", "foo");
    }

    @Override
    HelmGet newInstance()
    {
        return Helm.get("foo");
    }
}
