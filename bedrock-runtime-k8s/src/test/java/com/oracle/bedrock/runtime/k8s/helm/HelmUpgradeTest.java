/*
 * File: HelmUpgradeTest.java
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
 * Tests for {@link HelmUpgrade}.
 * <p>
 * Copyright (c) 2018. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class HelmUpgradeTest
        extends CommonCommandTests<HelmUpgrade>
{
    @Test
    public void shouldCreateBasicCommand()
    {
        HelmUpgrade command = Helm.upgrade("foo-release", "foo-chart");

        assertCommand(command, "upgrade", "foo-release", "foo-chart");
    }

    @Test
    public void shouldCreateBasicCommandFromFile()
    {
        HelmUpgrade command = Helm.upgrade("foo", new File("/tmp"));

        assertCommand(command, "upgrade", "foo", "/tmp");
    }

    @Test
    public void shouldCreateFromTemplate()
    {
        HelmCommand.Template command = Helm.template();
        HelmUpgrade          copy   = command.upgrade("foo-release", "foo-chart");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "upgrade", "foo-release", "foo-chart");
    }

    @Test
    public void shouldAddCaFileOption()
    {
        HelmUpgrade command = Helm.upgrade("foo-release", "foo-chart");
        HelmUpgrade copy    = command.caFile("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "upgrade", "foo-release", "foo-chart", "--ca-file", "bar");
    }

    @Test
    public void shouldAddCertFileOption()
    {
        HelmUpgrade command = Helm.upgrade("foo-release", "foo-chart");
        HelmUpgrade copy    = command.certFile("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "upgrade", "foo-release", "foo-chart", "--cert-file", "bar");
    }

    @Test
    public void shouldAddDevelOption()
    {
        HelmUpgrade command = Helm.upgrade("foo-release", "foo-chart");
        HelmUpgrade copy    = command.development();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "upgrade", "foo-release", "foo-chart", "--devel");
    }

    @Test
    public void shouldAddDryRunOption()
    {
        HelmUpgrade command = Helm.upgrade("foo-release", "foo-chart");
        HelmUpgrade copy    = command.dryRun();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "upgrade", "foo-release", "foo-chart", "--dry-run");
    }

    @Test
    public void shouldAddForceOption()
    {
        HelmUpgrade command = Helm.upgrade("foo-release", "foo-chart");
        HelmUpgrade copy    = command.force();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "upgrade", "foo-release", "foo-chart", "--force");
    }

    @Test
    public void shouldAddInstallOption()
    {
        HelmUpgrade command = Helm.upgrade("foo-release", "foo-chart");
        HelmUpgrade copy    = command.install();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "upgrade", "foo-release", "foo-chart", "--install");
    }

    @Test
    public void shouldAddKeyFileOption()
    {
        HelmUpgrade command = Helm.upgrade("foo-release", "foo-chart");
        HelmUpgrade copy    = command.keyFile("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "upgrade", "foo-release", "foo-chart", "--key-file", "bar");
    }

    @Test
    public void shouldAddKeyRingOption()
    {
        HelmUpgrade command = Helm.upgrade("foo-release", "foo-chart");
        HelmUpgrade copy    = command.keyRing("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "upgrade", "foo-release", "foo-chart", "--keyring", "bar");
    }

    @Test
    public void shouldAddNameOption()
    {
        HelmUpgrade command = Helm.upgrade("foo-release", "foo-chart");
        HelmUpgrade copy    = command.name("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "upgrade", "foo-release", "foo-chart", "--name", "bar");
    }

    @Test
    public void shouldAddNameTemplateOption()
    {
        HelmUpgrade command = Helm.upgrade("foo-release", "foo-chart");
        HelmUpgrade copy    = command.nameTemplate("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "upgrade", "foo-release", "foo-chart", "--name-template", "bar");
    }

    @Test
    public void shouldAddNamespaceOption()
    {
        HelmUpgrade command = Helm.upgrade("foo-release", "foo-chart");
        HelmUpgrade copy    = command.namespace("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "upgrade", "foo-release", "foo-chart", "--namespace", "bar");
    }

    @Test
    public void shouldAddNoHooksOption()
    {
        HelmUpgrade command = Helm.upgrade("foo-release", "foo-chart");
        HelmUpgrade copy    = command.noHooks();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "upgrade", "foo-release", "foo-chart", "--no-hooks");
    }

    @Test
    public void shouldAddReplaceOption()
    {
        HelmUpgrade command = Helm.upgrade("foo-release", "foo-chart");
        HelmUpgrade copy    = command.replace();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "upgrade", "foo-release", "foo-chart", "--replace");
    }

    @Test
    public void shouldAddRepoOption()
    {
        HelmUpgrade command = Helm.upgrade("foo-release", "foo-chart");
        HelmUpgrade copy    = command.repo("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "upgrade", "foo-release", "foo-chart", "--repo", "bar");
    }

    @Test
    public void shouldAddSetOption()
    {
        HelmUpgrade command = Helm.upgrade("foo-release", "foo-chart");
        HelmUpgrade copy    = command.set("bar1", "bar2");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "upgrade", "foo-release", "foo-chart", "--set", "bar1,bar2");
    }

    @Test
    public void shouldAddSetOptionWithoutValues()
    {
        HelmUpgrade command = Helm.upgrade("foo-release", "foo-chart");
        HelmUpgrade copy    = command.set();

        assertThat(copy, is(sameInstance(command)));
    }

    @Test
    public void shouldAddTimeoutOption()
    {
        HelmUpgrade command = Helm.upgrade("foo-release", "foo-chart");
        HelmUpgrade copy    = command.timeout(19);

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "upgrade", "foo-release", "foo-chart", "--timeout", "19");
    }

    @Test
    public void shouldAddTlsOption()
    {
        HelmUpgrade command = Helm.upgrade("foo-release", "foo-chart");
        HelmUpgrade copy    = command.tls();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "upgrade", "foo-release", "foo-chart", "--tls");
    }

    @Test
    public void shouldAddTlsCaCertOption()
    {
        HelmUpgrade command = Helm.upgrade("foo-release", "foo-chart");
        HelmUpgrade copy    = command.tlsCaCert("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "upgrade", "foo-release", "foo-chart", "--tls-ca-cert", "bar");
    }

    @Test
    public void shouldAddTlsCertOption()
    {
        HelmUpgrade command = Helm.upgrade("foo-release", "foo-chart");
        HelmUpgrade copy    = command.tlsCert("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "upgrade", "foo-release", "foo-chart", "--tls-cert", "bar");
    }

    @Test
    public void shouldAddTlsKeyOption()
    {
        HelmUpgrade command = Helm.upgrade("foo-release", "foo-chart");
        HelmUpgrade copy    = command.tlsKey("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "upgrade", "foo-release", "foo-chart", "--tls-key", "bar");
    }

    @Test
    public void shouldAddTlsVerifyOption()
    {
        HelmUpgrade command = Helm.upgrade("foo-release", "foo-chart");
        HelmUpgrade copy    = command.tlsVerify();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "upgrade", "foo-release", "foo-chart", "--tls-verify");
    }

    @Test
    public void shouldAddValuesOption()
    {
        HelmUpgrade command = Helm.upgrade("foo-release", "foo-chart");
        HelmUpgrade copy    = command.values("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "upgrade", "foo-release", "foo-chart", "--values", "bar");
    }

    @Test
    public void shouldAddValuesFileOption()
    {
        HelmUpgrade command = Helm.upgrade("foo-release", "foo-chart");
        HelmUpgrade copy    = command.values(new File("/tmp"));

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "upgrade", "foo-release", "foo-chart", "--values", "/tmp");
    }

    @Test
    public void shouldAddVerifyOption()
    {
        HelmUpgrade command = Helm.upgrade("foo-release", "foo-chart");
        HelmUpgrade copy    = command.verify();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "upgrade", "foo-release", "foo-chart", "--verify");
    }

    @Test
    public void shouldAddVersionOption()
    {
        HelmUpgrade command = Helm.upgrade("foo-release", "foo-chart");
        HelmUpgrade copy    = command.version("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "upgrade", "foo-release", "foo-chart", "--version", "bar");
    }

    @Test
    public void shouldAddWaitOption()
    {
        HelmUpgrade command = Helm.upgrade("foo-release", "foo-chart");
        HelmUpgrade copy    = command.waitForK8s();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "upgrade", "foo-release", "foo-chart", "--wait");
    }

    @Test
    public void shouldAddWaitWithTimeoutOption()
    {
        HelmUpgrade command = Helm.upgrade("foo-release", "foo-chart");
        HelmUpgrade copy    = command.waitForK8s(19);

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "upgrade", "foo-release", "foo-chart", "--wait", "--timeout", "19");
    }

    @Override
    HelmUpgrade newInstance()
    {
        return Helm.upgrade("foo-release", "foo-chart");
    }
}
