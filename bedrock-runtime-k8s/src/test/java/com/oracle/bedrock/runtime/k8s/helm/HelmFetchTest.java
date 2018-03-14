/*
 * File: HelmFetchTest.java
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
 * Tests for {@link HelmFetch}.
 * <p>
 * Copyright (c) 2018. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class HelmFetchTest
        extends CommonCommandTests<HelmFetch>
{
    @Test
    public void shouldCreateBasicCommand()
    {
        HelmFetch command = Helm.fetch("foo");

        assertCommand(command, "fetch", "foo");
    }

    @Test
    public void shouldCreateFromTemplate()
    {
        HelmCommand.Template command = Helm.template();
        HelmFetch            copy    = command.fetch("foo");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "fetch", "foo");
    }

    @Test
    public void shouldAddCaFileOption()
    {
        HelmFetch command = Helm.fetch("foo");
        HelmFetch copy    = command.caFile("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "fetch", "--ca-file", "bar", "foo");
    }

    @Test
    public void shouldAddCertFileOption()
    {
        HelmFetch command = Helm.fetch("foo");
        HelmFetch copy    = command.certFile("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "fetch", "--cert-file", "bar", "foo");
    }

    @Test
    public void shouldAddDevelOption()
    {
        HelmFetch command = Helm.fetch("foo");
        HelmFetch copy    = command.development();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "fetch", "--devel", "foo");
    }

    @Test
    public void shouldAddKeyFileOption()
    {
        HelmFetch command = Helm.fetch("foo");
        HelmFetch copy    = command.keyFile("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "fetch", "--key-file", "bar", "foo");
    }

    @Test
    public void shouldAddKeyRingOption()
    {
        HelmFetch command = Helm.fetch("foo");
        HelmFetch copy    = command.keyRing("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "fetch", "--keyring", "bar", "foo");
    }

    @Test
    public void shouldAddProvOption()
    {
        HelmFetch command = Helm.fetch("foo");
        HelmFetch copy    = command.prov();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "fetch", "--prov", "foo");
    }

    @Test
    public void shouldAddRepoOption()
    {
        HelmFetch command = Helm.fetch("foo");
        HelmFetch copy    = command.repo("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "fetch", "--repo", "bar", "foo");
    }

    @Test
    public void shouldAddUntarOption()
    {
        HelmFetch command = Helm.fetch("foo");
        HelmFetch copy    = command.untar();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "fetch", "--untar", "true", "foo");
    }

    @Test
    public void shouldAddUntarDirOption()
    {
        HelmFetch command = Helm.fetch("foo");
        HelmFetch copy    = command.untarInto("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "fetch", "--untardir", "bar", "foo");
    }

    @Test
    public void shouldAddUntarDirFileOption()
    {
        HelmFetch command = Helm.fetch("foo");
        HelmFetch copy    = command.untarInto(new File("/temp"));

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "fetch", "--untardir", "/temp", "foo");
    }

    @Test
    public void shouldAddVerifyOption()
    {
        HelmFetch command = Helm.fetch("foo");
        HelmFetch copy    = command.verify();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "fetch", "--verify", "foo");
    }

    @Test
    public void shouldAddVersionOption()
    {
        HelmFetch command = Helm.fetch("foo");
        HelmFetch copy    = command.version("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "fetch", "--version", "bar", "foo");
    }

    @Override
    HelmFetch newInstance()
    {
        return Helm.fetch("foo");
    }
}
