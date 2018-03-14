/*
 * File: HelmListTest.java
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
 * Tests for {@link HelmList}.
 * <p>
 * Copyright (c) 2018. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class HelmListTest
        extends CommonCommandTests<HelmList>
{
    @Test
    public void shouldCreateBasicCommand()
    {
        HelmList command = Helm.list();

        assertCommand(command, "list");
    }

    @Test
    public void shouldCreateBasicCommandWithFilter()
    {
        HelmList command = Helm.list("foo");

        assertCommand(command, "list", "foo");
    }

    @Test
    public void shouldCreateFromTemplate()
    {
        HelmCommand.Template command = Helm.template();
        HelmList             copy    = command.list("foo");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "list", "foo");
    }

    @Test
    public void shouldAddAllOption()
    {
        HelmList command = Helm.list();
        HelmList copy    = command.all();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "list", "--all");
    }

    @Test
    public void shouldAddDateOption()
    {
        HelmList command = Helm.list();
        HelmList copy    = command.sortByDate();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "list", "--date");
    }

    @Test
    public void shouldAddDeletedOption()
    {
        HelmList command = Helm.list();
        HelmList copy    = command.deleted();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "list", "--deleted");
    }

    @Test
    public void shouldAddDeletingOption()
    {
        HelmList command = Helm.list();
        HelmList copy    = command.deleting();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "list", "--deleting");
    }

    @Test
    public void shouldAddDeployedOption()
    {
        HelmList command = Helm.list();
        HelmList copy    = command.deployed();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "list", "--deployed");
    }

    @Test
    public void shouldAddFailedOption()
    {
        HelmList command = Helm.list();
        HelmList copy    = command.failed();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "list", "--failed");
    }

    @Test
    public void shouldAddMaxOption()
    {
        HelmList command = Helm.list();
        HelmList copy    = command.max(19);

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "list", "--max", "19");
    }

    @Test
    public void shouldAddNamespaceOption()
    {
        HelmList command = Helm.list();
        HelmList copy    = command.namespace("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "list", "--namespace", "bar");
    }

    @Test
    public void shouldAddOffsetOption()
    {
        HelmList command = Helm.list();
        HelmList copy    = command.offset("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "list", "--offset", "bar");
    }

    @Test
    public void shouldAddPendingOption()
    {
        HelmList command = Helm.list();
        HelmList copy    = command.pending();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "list", "--pending");
    }

    @Test
    public void shouldAddReverseOption()
    {
        HelmList command = Helm.list();
        HelmList copy    = command.reverse();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "list", "--reverse");
    }

    @Test
    public void shouldAddQuietOption()
    {
        HelmList command = Helm.list();
        HelmList copy    = command.quiet();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "list", "--short");
    }

    @Test
    public void shouldAddTlsOption()
    {
        HelmList command = Helm.list("foo");
        HelmList copy    = command.tls();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "list", "--tls", "foo");
    }

    @Test
    public void shouldAddTlsCaCertOption()
    {
        HelmList command = Helm.list("foo");
        HelmList copy    = command.tlsCaCert("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "list", "--tls-ca-cert", "bar", "foo");
    }

    @Test
    public void shouldAddTlsCertOption()
    {
        HelmList command = Helm.list("foo");
        HelmList copy    = command.tlsCert("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "list", "--tls-cert", "bar", "foo");
    }

    @Test
    public void shouldAddTlsKeyOption()
    {
        HelmList command = Helm.list("foo");
        HelmList copy    = command.tlsKey("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "list", "--tls-key", "bar", "foo");
    }

    @Test
    public void shouldAddTlsVerifyOption()
    {
        HelmList command = Helm.list("foo");
        HelmList copy    = command.tlsVerify();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "list", "--tls-verify", "foo");
    }

    @Override
    HelmList newInstance()
    {
        return Helm.list("foo");
    }
}
