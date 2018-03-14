/*
 * File: HelmInitTest.java
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
 * Tests for {@link HelmInit}.
 * <p>
 * Copyright (c) 2018. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class HelmInitTest
        extends CommonCommandTests<HelmInit>
{
    @Test
    public void shouldCreateBasicCommand()
    {
        HelmInit command = Helm.init();

        assertCommand(command, "init");
    }

    @Test
    public void shouldCreateFromTemplate()
    {
        HelmCommand.Template command = Helm.template();
        HelmInit             copy    = command.init();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "init");
    }

    @Test
    public void shouldAddCanaryImageOption()
    {
        HelmInit command = Helm.init();
        HelmInit copy    = command.canaryImage();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "init", "--canary-image");
    }

    @Test
    public void shouldAddClientOnlyOption()
    {
        HelmInit command = Helm.init();
        HelmInit copy    = command.clientOnly();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "init", "--client-only");
    }

    @Test
    public void shouldAddDryRunOption()
    {
        HelmInit command = Helm.init();
        HelmInit copy    = command.dryRun();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "init", "--dry-run");
    }

    @Test
    public void shouldAddLocalRepoOption()
    {
        HelmInit command = Helm.init();
        HelmInit copy    = command.localRepoURL("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "init", "--local-repo-url", "bar");
    }

    @Test
    public void shouldAddNetHostOption()
    {
        HelmInit command = Helm.init();
        HelmInit copy    = command.netHost();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "init", "--net-host");
    }

    @Test
    public void shouldAddServiceAccountOption()
    {
        HelmInit command = Helm.init();
        HelmInit copy    = command.serviceAccount("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "init", "--service-account", "bar");
    }

    @Test
    public void shouldAddSkipRefreshOption()
    {
        HelmInit command = Helm.init();
        HelmInit copy    = command.skipRefresh();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "init", "--skip-refresh");
    }

    @Test
    public void shouldAddStableRepoOption()
    {
        HelmInit command = Helm.init();
        HelmInit copy    = command.stableRepoURL("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "init", "--stable-repo-url", "bar");
    }

    @Test
    public void shouldAddTillerImageOption()
    {
        HelmInit command = Helm.init();
        HelmInit copy    = command.tillerImage("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "init", "--tiller-image", "bar");
    }

    @Test
    public void shouldAddTillerTlsOption()
    {
        HelmInit command = Helm.init();
        HelmInit copy    = command.tillerTLS();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "init", "--tiller-tls");
    }

    @Test
    public void shouldAddTillerTlsCertOption()
    {
        HelmInit command = Helm.init();
        HelmInit copy    = command.tillerTLSCert("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "init", "--tiller-tls-cert", "bar");
    }

    @Test
    public void shouldAddTillerTlsKeyOption()
    {
        HelmInit command = Helm.init();
        HelmInit copy    = command.tillerTLSKey("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "init", "--tiller-tls-key", "bar");
    }

    @Test
    public void shouldAddTillerTlsVerifyOption()
    {
        HelmInit command = Helm.init();
        HelmInit copy    = command.tillerTLSVerify();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "init", "--tiller-tls-verify");
    }

    @Test
    public void shouldAddTlsCaCertOption()
    {
        HelmInit command = Helm.init();
        HelmInit copy    = command.tlsCACert("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "init", "--tls-ca-cert", "bar");
    }

    @Override
    HelmInit newInstance()
    {
        return Helm.init();
    }
}
