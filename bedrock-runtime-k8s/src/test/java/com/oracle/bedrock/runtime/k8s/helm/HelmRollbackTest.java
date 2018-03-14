/*
 * File: HelmRollbackest.java
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
 * Tests for {@link HelmRollback}.
 * <p>
 * Copyright (c) 2018. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class HelmRollbackTest
        extends CommonCommandTests<HelmRollback>
{
    @Test
    public void shouldCreateBasicCommand()
    {
        HelmRollback command = Helm.rollback("foo-release", "1.0");

        assertCommand(command, "rollback", "foo-release", "1.0");
    }

    @Test
    public void shouldCreateFromTemplate()
    {
        HelmCommand.Template command = Helm.template();
        HelmRollback          copy   = command.rollback("foo-release", "1.0");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "rollback", "foo-release", "1.0");
    }

    @Test
    public void shouldAddDryRunOption()
    {
        HelmRollback command = Helm.rollback("foo-release", "1.0");
        HelmRollback copy    = command.dryRun();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "rollback", "--dry-run", "foo-release", "1.0");
    }

    @Test
    public void shouldAddForceOption()
    {
        HelmRollback command = Helm.rollback("foo-release", "1.0");
        HelmRollback copy    = command.force();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "rollback", "--force", "foo-release", "1.0");
    }

    @Test
    public void shouldAddNoHooksOption()
    {
        HelmRollback command = Helm.rollback("foo-release", "1.0");
        HelmRollback copy    = command.noHooks();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "rollback", "--no-hooks", "foo-release", "1.0");
    }

    @Test
    public void shouldAddRecreatePodsOption()
    {
        HelmRollback command = Helm.rollback("foo-release", "1.0");
        HelmRollback copy    = command.recreatePods();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "rollback", "--recreate-pods", "foo-release", "1.0");
    }

    @Test
    public void shouldAddTimeoutOption()
    {
        HelmRollback command = Helm.rollback("foo-release", "1.0");
        HelmRollback copy    = command.timeout(19);

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "rollback", "--timeout", "19", "foo-release", "1.0");
    }

    @Test
    public void shouldAddTlsOption()
    {
        HelmRollback command = Helm.rollback("foo-release", "1.0");
        HelmRollback copy    = command.tls();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "rollback", "--tls", "foo-release", "1.0");
    }

    @Test
    public void shouldAddTlsCaCertOption()
    {
        HelmRollback command = Helm.rollback("foo-release", "1.0");
        HelmRollback copy    = command.tlsCaCert("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "rollback", "--tls-ca-cert", "bar", "foo-release", "1.0");
    }

    @Test
    public void shouldAddTlsCertOption()
    {
        HelmRollback command = Helm.rollback("foo-release", "1.0");
        HelmRollback copy    = command.tlsCert("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "rollback", "--tls-cert", "bar", "foo-release", "1.0");
    }

    @Test
    public void shouldAddTlsKeyOption()
    {
        HelmRollback command = Helm.rollback("foo-release", "1.0");
        HelmRollback copy    = command.tlsKey("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "rollback", "--tls-key", "bar", "foo-release", "1.0");
    }

    @Test
    public void shouldAddTlsVerifyOption()
    {
        HelmRollback command = Helm.rollback("foo-release", "1.0");
        HelmRollback copy    = command.tlsVerify();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "rollback", "--tls-verify", "foo-release", "1.0");
    }

    @Test
    public void shouldAddWaitOption()
    {
        HelmRollback command = Helm.rollback("foo-release", "1.0");
        HelmRollback copy    = command.waitForK8s();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "rollback", "--wait", "foo-release", "1.0");
    }

    @Test
    public void shouldAddWaitWithTimeoutOption()
    {
        HelmRollback command = Helm.rollback("foo-release", "1.0");
        HelmRollback copy    = command.waitForK8s(19);

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "rollback", "--wait", "--timeout", "19", "foo-release", "1.0");
    }

    @Override
    HelmRollback newInstance()
    {
        return Helm.rollback("foo-release", "1.0");
    }
}
