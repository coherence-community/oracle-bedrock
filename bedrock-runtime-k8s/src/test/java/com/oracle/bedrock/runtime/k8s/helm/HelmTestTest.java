/*
 * File: HelmTimeoutTest.java
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
 * Tests for {@link HelmTest}.
 * <p>
 * Copyright (c) 2018. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class HelmTestTest
        extends CommonCommandTests<HelmTest>
{
    @Test
    public void shouldCreateBasicCommand()
    {
        HelmTest command = Helm.test("foo");

        assertCommand(command, "test", "foo");
    }

    @Test
    public void shouldCreateFromTemplate()
    {
        HelmCommand.Template command = Helm.template();
        HelmTest             copy   = command.test("foo");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "test", "foo");
    }

    @Test
    public void shouldAddCleanupOption()
    {
        HelmTest command = Helm.test("foo");
        HelmTest copy    = command.cleanup();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "test", "foo", "--cleanup");
    }

    @Test
    public void shouldAddTimeoutOption()
    {
        HelmTest command = Helm.test("foo");
        HelmTest copy    = command.timeout(19);

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "test", "foo", "--timeout", "19");
    }

    @Test
    public void shouldAddTlsOption()
    {
        HelmTest command = Helm.test("foo");
        HelmTest copy    = command.tls();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "test", "foo", "--tls");
    }

    @Test
    public void shouldAddTlsCaCertOption()
    {
        HelmTest command = Helm.test("foo");
        HelmTest copy    = command.tlsCaCert("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "test", "foo", "--tls-ca-cert", "bar");
    }

    @Test
    public void shouldAddTlsCertOption()
    {
        HelmTest command = Helm.test("foo");
        HelmTest copy    = command.tlsCert("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "test", "foo", "--tls-cert", "bar");
    }

    @Test
    public void shouldAddTlsKeyOption()
    {
        HelmTest command = Helm.test("foo");
        HelmTest copy    = command.tlsKey("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "test", "foo", "--tls-key", "bar");
    }

    @Test
    public void shouldAddTlsVerifyOption()
    {
        HelmTest command = Helm.test("foo");
        HelmTest copy    = command.tlsVerify();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "test", "foo", "--tls-verify");
    }

    @Override
    HelmTest newInstance()
    {
        return Helm.test("foo");
    }
}
