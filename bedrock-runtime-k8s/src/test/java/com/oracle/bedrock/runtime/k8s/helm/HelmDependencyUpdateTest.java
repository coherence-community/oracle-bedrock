/*
 * File: HelmDependencyUpdateTest.java
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
 * Tests for {@link HelmDependencyBuild}.
 * <p>
 * Copyright (c) 2018. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class HelmDependencyUpdateTest
        extends CommonCommandTests<HelmDependencyUpdate>
{
    @Test
    public void shouldCreateBasicCommand()
    {
        HelmDependencyUpdate command = Helm.dependencyUpdate("foo");

        assertCommand(command, "dependency", "update", "foo");
    }

    @Test
    public void shouldCreateFromTemplate()
    {
        HelmCommand.Template command = Helm.template();
        HelmDependencyUpdate copy    = command.dependencyUpdate("foo");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "dependency", "update", "foo");
    }

    @Test
    public void shouldAddKeyRingOption()
    {
        HelmDependencyUpdate command = newInstance();
        HelmDependencyUpdate copy    = command.keyRing("bar");

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "dependency", "update", "--keyring", "bar", "foo");
    }

    @Test
    public void shouldAddSkipRefreshOption()
    {
        HelmDependencyUpdate command = newInstance();
        HelmDependencyUpdate copy    = command.skipRefresh();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "dependency", "update", "--skip-refresh", "foo");
    }

    @Test
    public void shouldAddVerifyOption()
    {
        HelmDependencyUpdate command = newInstance();
        HelmDependencyUpdate copy    = command.verify();

        assertThat(copy, is(not(sameInstance(command))));
        assertCommand(copy, "dependency", "update", "--verify", "foo");
    }

    @Override
    HelmDependencyUpdate newInstance()
    {
        return Helm.dependencyUpdate("foo");
    }
}
