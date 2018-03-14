/*
 * File: HelmInstallTest.java
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

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for {@link HelmInstall}.
 * <p>
 * Copyright (c) 2018. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class HelmInstallTest
        extends CommonCommandTests<HelmInstall>
{
    @Test
    public void shouldCreateBasicCommand()
    {
        HelmInstall command = Helm.install("foo");

        assertCommand(command, "install", "foo");
    }

    @Test
    public void shouldCreateBasicCommandFromFile()
    {
        HelmInstall command = Helm.install(new File("/tmp"));

        assertCommand(command, "install", "/tmp");
    }

    @Test
    public void shouldCreateFromTemplate()
    {
        HelmCommand.Template command = Helm.template();
        HelmInstall          copy   = command.install("foo");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "install", "foo");
    }

    @Test
    public void shouldAddCaFileOption()
    {
        HelmInstall command = Helm.install("foo");
        HelmInstall copy    = command.caFile("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "install", "foo", "--ca-file", "bar");
    }

    @Test
    public void shouldAddCertFileOption()
    {
        HelmInstall command = Helm.install("foo");
        HelmInstall copy    = command.certFile("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "install", "foo", "--cert-file", "bar");
    }

    @Test
    public void shouldAddDevelOption()
    {
        HelmInstall command = Helm.install("foo");
        HelmInstall copy    = command.development();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "install", "foo", "--devel");
    }

    @Test
    public void shouldAddDryRunOption()
    {
        HelmInstall command = Helm.install("foo");
        HelmInstall copy    = command.dryRun();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "install", "foo", "--dry-run");
    }

    @Test
    public void shouldAddKeyFileOption()
    {
        HelmInstall command = Helm.install("foo");
        HelmInstall copy    = command.keyFile("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "install", "foo", "--key-file", "bar");
    }

    @Test
    public void shouldAddKeyRingOption()
    {
        HelmInstall command = Helm.install("foo");
        HelmInstall copy    = command.keyRing("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "install", "foo", "--keyring", "bar");
    }

    @Test
    public void shouldAddNameOption()
    {
        HelmInstall command = Helm.install("foo");
        HelmInstall copy    = command.name("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "install", "foo", "--name", "bar");
    }

    @Test
    public void shouldAddNameTemplateOption()
    {
        HelmInstall command = Helm.install("foo");
        HelmInstall copy    = command.nameTemplate("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "install", "foo", "--name-template", "bar");
    }

    @Test
    public void shouldAddNamespaceOption()
    {
        HelmInstall command = Helm.install("foo");
        HelmInstall copy    = command.namespace("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "install", "foo", "--namespace", "bar");
    }

    @Test
    public void shouldAddNoHooksOption()
    {
        HelmInstall command = Helm.install("foo");
        HelmInstall copy    = command.noHooks();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "install", "foo", "--no-hooks");
    }

    @Test
    public void shouldAddReplaceOption()
    {
        HelmInstall command = Helm.install("foo");
        HelmInstall copy    = command.replace();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "install", "foo", "--replace");
    }

    @Test
    public void shouldAddRepoOption()
    {
        HelmInstall command = Helm.install("foo");
        HelmInstall copy    = command.repo("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "install", "foo", "--repo", "bar");
    }

    @Test
    public void shouldAddSetOption()
    {
        HelmInstall command = Helm.install("foo");
        HelmInstall copy    = command.set("bar1", "bar2");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "install", "foo", "--set", "bar1,bar2");
    }

    @Test
    public void shouldAddSetOptionWithoutValues()
    {
        HelmInstall command = Helm.install("foo");
        HelmInstall copy    = command.set();

        assertThat(copy, is(sameInstance(command)));
    }

    @Test
    public void shouldAddTimeoutOption()
    {
        HelmInstall command = Helm.install("foo");
        HelmInstall copy    = command.timeout(19);

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "install", "foo", "--timeout", "19");
    }

    @Test
    public void shouldAddTlsOption()
    {
        HelmInstall command = Helm.install("foo");
        HelmInstall copy    = command.tls();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "install", "foo", "--tls");
    }

    @Test
    public void shouldAddTlsCaCertOption()
    {
        HelmInstall command = Helm.install("foo");
        HelmInstall copy    = command.tlsCaCert("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "install", "foo", "--tls-ca-cert", "bar");
    }

    @Test
    public void shouldAddTlsCertOption()
    {
        HelmInstall command = Helm.install("foo");
        HelmInstall copy    = command.tlsCert("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "install", "foo", "--tls-cert", "bar");
    }

    @Test
    public void shouldAddTlsKeyOption()
    {
        HelmInstall command = Helm.install("foo");
        HelmInstall copy    = command.tlsKey("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "install", "foo", "--tls-key", "bar");
    }

    @Test
    public void shouldAddTlsVerifyOption()
    {
        HelmInstall command = Helm.install("foo");
        HelmInstall copy    = command.tlsVerify();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "install", "foo", "--tls-verify");
    }

    @Test
    public void shouldAddValuesOption()
    {
        HelmInstall command = Helm.install("foo");
        HelmInstall copy    = command.values("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "install", "foo", "--values", "bar");
    }

    @Test
    public void shouldAddValuesFileOption()
    {
        HelmInstall command = Helm.install("foo");
        HelmInstall copy    = command.values(new File("/tmp"));

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "install", "foo", "--values", "/tmp");
    }

    @Test
    public void shouldAddVerifyOption()
    {
        HelmInstall command = Helm.install("foo");
        HelmInstall copy    = command.verify();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "install", "foo", "--verify");
    }

    @Test
    public void shouldAddVersionOption()
    {
        HelmInstall command = Helm.install("foo");
        HelmInstall copy    = command.version("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "install", "foo", "--version", "bar");
    }

    @Test
    public void shouldAddWaitOption()
    {
        HelmInstall command = Helm.install("foo");
        HelmInstall copy    = command.waitForK8s();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "install", "foo", "--wait");
    }

    @Test
    public void shouldAddWaitWithTimeoutOption()
    {
        HelmInstall command = Helm.install("foo");
        HelmInstall copy    = command.waitForK8s(19);

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "install", "foo", "--wait", "--timeout", "19");
    }

    @Override
    HelmInstall newInstance()
    {
        return Helm.install("foo");
    }
}
