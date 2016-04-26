/*
 * File: BuildTest.java
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

package com.oracle.tools.runtime.containers.docker.commands;

import com.oracle.tools.Options;
import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.containers.docker.DockerImage;
import com.oracle.tools.runtime.options.Argument;
import com.oracle.tools.runtime.options.Arguments;
import com.oracle.tools.runtime.remote.RemotePlatform;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for the {@link Build} class.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class BuildTest
{
    @Test
    public void shouldUseDefaultBuild() throws Exception
    {
        Build build = Build.fromDockerFile().withTags("foo");

        List<String> arguments = resolveArguments(build);

        assertThat(arguments.isEmpty(), is(false));
        assertThat(arguments.get(0), is("build"));
        assertThat(arguments, containsInAnyOrder("build", "--file=Dockerfile", "--tag=foo", "."));
    }


    @Test
    public void shouldUseSpecfiedDockefile() throws Exception
    {
        Build build = Build.fromDockerFile("MyFile").withTags("foo");

        List<String> arguments = resolveArguments(build);

        assertThat(arguments.isEmpty(), is(false));
        assertThat(arguments.get(0), is("build"));
        assertThat(arguments, containsInAnyOrder("build", "--file=MyFile", "--tag=foo", "."));
    }


    @Test
    public void shouldImmutablySetAlwaysPull() throws Exception
    {
        Build        build1 = Build.fromDockerFile().withTags("foo");
        List<String> before = resolveArguments(build1);
        Build        build2 = build1.alwaysPull();

        assertThat(build1, is (not(sameInstance(build2))));

        List<String> arguments1 = resolveArguments(build1);
        List<String> arguments2 = resolveArguments(build2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, contains("--pull"));
    }


    @Test
    public void shouldImmutablySetBuildArgs() throws Exception
    {
        Build        build1 = Build.fromDockerFile().withTags("foo");
        List<String> before = resolveArguments(build1);
        Build        build2 = build1.buildArgs("A", "B");

        assertThat(build1, is (not(sameInstance(build2))));

        List<String> arguments1 = resolveArguments(build1);
        List<String> arguments2 = resolveArguments(build2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--build-arg=A", "--build-arg=B"));
    }


    @Test
    public void shouldImmutablySetBuildContextFile() throws Exception
    {
        Build        build1 = Build.fromDockerFile().withTags("foo");
        List<String> before = resolveArguments(build1);
        Build        build2 = build1.buildContextAt(new File("/tmp/test"));

        assertThat(build1, is (not(sameInstance(build2))));

        List<String> arguments1 = resolveArguments(build1);
        List<String> arguments2 = resolveArguments(build2);

        assertThat(arguments1, is(before));

        assertThat(arguments2.get(arguments2.size() - 1), is("/tmp/test"));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, contains("/tmp/test"));
    }


    @Test
    public void shouldImmutablySetBuildContextURL() throws Exception
    {
        Build        build1 = Build.fromDockerFile().withTags("foo");
        List<String> before = resolveArguments(build1);
        Build        build2 = build1.buildContextAt(new URL("http://tmp/test"));

        assertThat(build1, is (not(sameInstance(build2))));

        List<String> arguments1 = resolveArguments(build1);
        List<String> arguments2 = resolveArguments(build2);

        assertThat(arguments1, is(before));

        assertThat(arguments2.get(arguments2.size() - 1), is("http://tmp/test"));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, contains("http://tmp/test"));
    }


    @Test
    public void shouldImmutablySetCGroupParent() throws Exception
    {
        Build        build1 = Build.fromDockerFile().withTags("foo");
        List<String> before = resolveArguments(build1);
        Build        build2 = build1.cgroupParent("my-parent");

        assertThat(build1, is (not(sameInstance(build2))));

        List<String> arguments1 = resolveArguments(build1);
        List<String> arguments2 = resolveArguments(build2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--cgroup-parent=my-parent"));
    }


    @Test
    public void shouldImmutablySetCPUPeriod() throws Exception
    {
        Build        build1 = Build.fromDockerFile().withTags("foo");
        List<String> before = resolveArguments(build1);
        Build        build2 = build1.cpuPeriod(99);

        assertThat(build1, is (not(sameInstance(build2))));

        List<String> arguments1 = resolveArguments(build1);
        List<String> arguments2 = resolveArguments(build2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--cpu-period=99"));
    }


    @Test
    public void shouldImmutablySetCPUQuota() throws Exception
    {
        Build        build1 = Build.fromDockerFile().withTags("foo");
        List<String> before = resolveArguments(build1);
        Build        build2 = build1.cpuQuota(123);

        assertThat(build1, is (not(sameInstance(build2))));

        List<String> arguments1 = resolveArguments(build1);
        List<String> arguments2 = resolveArguments(build2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--cpu-quota=123"));
    }


    @Test
    public void shouldImmutablySetCPUSetCPUs() throws Exception
    {
        Build        build1 = Build.fromDockerFile().withTags("foo");
        List<String> before = resolveArguments(build1);
        Build        build2 = build1.cpuSetCPUs("bar");

        assertThat(build1, is (not(sameInstance(build2))));

        List<String> arguments1 = resolveArguments(build1);
        List<String> arguments2 = resolveArguments(build2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--cpuset-cpus=bar"));
    }


    @Test
    public void shouldImmutablySetCPUSetMems() throws Exception
    {
        Build        build1 = Build.fromDockerFile().withTags("foo");
        List<String> before = resolveArguments(build1);
        Build        build2 = build1.cpuSetMems("bar");

        assertThat(build1, is (not(sameInstance(build2))));

        List<String> arguments1 = resolveArguments(build1);
        List<String> arguments2 = resolveArguments(build2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--cpuset-mems=bar"));
    }


    @Test
    public void shouldImmutablySetCPUShares() throws Exception
    {
        Build        build1 = Build.fromDockerFile().withTags("foo");
        List<String> before = resolveArguments(build1);
        Build        build2 = build1.cpuShares(true);

        assertThat(build1, is (not(sameInstance(build2))));

        List<String> arguments1 = resolveArguments(build1);
        List<String> arguments2 = resolveArguments(build2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--cpu-shares"));
    }


    @Test
    public void shouldImmutablySetCPUSharesDisabled() throws Exception
    {
        Build        build1 = Build.fromDockerFile().withTags("foo").cpuShares(true);
        List<String> before = resolveArguments(build1);
        Build        build2 = build1.cpuShares(false);

        assertThat(build1, is (not(sameInstance(build2))));

        List<String> arguments1 = resolveArguments(build1);
        List<String> arguments2 = resolveArguments(build2);

        assertThat(arguments1, is(before));

        assertThat(arguments2.contains("--cpu-shares"), is(false));
    }


    @Test
    public void shouldImmutablySetDisableContentTrust() throws Exception
    {
        Build        build1 = Build.fromDockerFile().withTags("foo");
        List<String> before = resolveArguments(build1);
        Build        build2 = build1.disableContentTrust();

        assertThat(build1, is (not(sameInstance(build2))));

        List<String> arguments1 = resolveArguments(build1);
        List<String> arguments2 = resolveArguments(build2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--disable-content-trust=true"));
    }


    @Test
    public void shouldImmutablySetEnableContentTrust() throws Exception
    {
        Build        build1 = Build.fromDockerFile().withTags("foo");
        List<String> before = resolveArguments(build1);
        Build        build2 = build1.enableContentTrust();

        assertThat(build1, is (not(sameInstance(build2))));


        List<String> arguments1 = resolveArguments(build1);
        List<String> arguments2 = resolveArguments(build2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--disable-content-trust=false"));
    }


    @Test
    public void shouldImmutablySetDockerfile() throws Exception
    {
        Build        build1 = Build.fromDockerFile().withTags("foo");
        List<String> before = resolveArguments(build1);
        Build        build2 = build1.dockerFileName("MyFile");

        assertThat(build1, is (not(sameInstance(build2))));

        List<String> arguments1 = resolveArguments(build1);
        List<String> arguments2 = resolveArguments(build2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--file=MyFile"));
    }


    @Test
    public void shouldImmutablySetForceRM() throws Exception
    {
        Build        build1 = Build.fromDockerFile().withTags("foo");
        List<String> before = resolveArguments(build1);
        Build        build2 = build1.forceRM(true);

        assertThat(build1, is (not(sameInstance(build2))));


        List<String> arguments1 = resolveArguments(build1);
        List<String> arguments2 = resolveArguments(build2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--force-rm"));
    }


    @Test
    public void shouldImmutablySetForceRMFalse() throws Exception
    {
        Build        build1 = Build.fromDockerFile().withTags("foo").forceRM(true);
        List<String> before = resolveArguments(build1);
        Build        build2 = build1.forceRM(false);

        assertThat(build1, is (not(sameInstance(build2))));


        List<String> arguments1 = resolveArguments(build1);
        List<String> arguments2 = resolveArguments(build2);

        assertThat(arguments1, is(before));

        assertThat(arguments2.contains("--force-rm"), is(false));
    }


    @Test
    public void shouldImmutablySetIsolation() throws Exception
    {
        Build        build1 = Build.fromDockerFile().withTags("foo");
        List<String> before = resolveArguments(build1);
        Build        build2 = build1.isolation("bar");

        assertThat(build1, is (not(sameInstance(build2))));


        List<String> arguments1 = resolveArguments(build1);
        List<String> arguments2 = resolveArguments(build2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--isolation=bar"));
    }


    @Test
    public void shouldImmutablyAddLabels() throws Exception
    {
        Build        build1 = Build.fromDockerFile().withTags("foo");
        List<String> before = resolveArguments(build1);
        Build        build2 = build1.labels("bar1", "bar2");

        assertThat(build1, is (not(sameInstance(build2))));


        List<String> arguments1 = resolveArguments(build1);
        List<String> arguments2 = resolveArguments(build2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--label=bar1", "--label=bar2"));
    }


    @Test
    public void shouldImmutablySetMemory() throws Exception
    {
        Build        build1 = Build.fromDockerFile().withTags("foo");
        List<String> before = resolveArguments(build1);
        Build        build2 = build1.memory("bar");

        assertThat(build1, is (not(sameInstance(build2))));


        List<String> arguments1 = resolveArguments(build1);
        List<String> arguments2 = resolveArguments(build2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--memory=bar"));
    }


    @Test
    public void shouldImmutablySetMemorySwap() throws Exception
    {
        Build        build1 = Build.fromDockerFile().withTags("foo");
        List<String> before = resolveArguments(build1);
        Build        build2 = build1.memorySwap("bar");

        assertThat(build1, is (not(sameInstance(build2))));


        List<String> arguments1 = resolveArguments(build1);
        List<String> arguments2 = resolveArguments(build2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--memory-swap=bar"));
    }


    @Test
    public void shouldImmutablySetNoCache() throws Exception
    {
        Build        build1 = Build.fromDockerFile().withTags("foo");
        List<String> before = resolveArguments(build1);
        Build        build2 = build1.noCache(true);

        assertThat(build1, is (not(sameInstance(build2))));


        List<String> arguments1 = resolveArguments(build1);
        List<String> arguments2 = resolveArguments(build2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--no-cache"));
    }


    @Test
    public void shouldImmutablySetNoCacheToFalse() throws Exception
    {
        Build        build1 = Build.fromDockerFile().withTags("foo").noCache(true);
        List<String> before = resolveArguments(build1);
        Build        build2 = build1.noCache(false);

        assertThat(build1, is (not(sameInstance(build2))));


        List<String> arguments1 = resolveArguments(build1);
        List<String> arguments2 = resolveArguments(build2);

        assertThat(arguments1, is(before));

        assertThat(arguments2.contains("--no-cache"), is(false));
    }


    @Test
    public void shouldImmutablySetQuiet() throws Exception
    {
        Build        build1 = Build.fromDockerFile().withTags("foo");
        List<String> before = resolveArguments(build1);
        Build        build2 = build1.quiet(true);

        assertThat(build1, is (not(sameInstance(build2))));


        List<String> arguments1 = resolveArguments(build1);
        List<String> arguments2 = resolveArguments(build2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--quiet"));
    }


    @Test
    public void shouldImmutablySetQuietFalse() throws Exception
    {
        Build        build1 = Build.fromDockerFile().withTags("foo").quiet(true);
        List<String> before = resolveArguments(build1);
        Build        build2 = build1.quiet(false);

        assertThat(build1, is (not(sameInstance(build2))));


        List<String> arguments1 = resolveArguments(build1);
        List<String> arguments2 = resolveArguments(build2);

        assertThat(arguments1, is(before));

        assertThat(arguments2.contains("--quiet"), is(false));
    }


    @Test
    public void shouldImmutablySetRM() throws Exception
    {
        Build        build1 = Build.fromDockerFile().withTags("foo");
        List<String> before = resolveArguments(build1);
        Build        build2 = build1.removeIntermidiateContainers(true);

        assertThat(build1, is (not(sameInstance(build2))));


        List<String> arguments1 = resolveArguments(build1);
        List<String> arguments2 = resolveArguments(build2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--rm=true"));
    }


    @Test
    public void shouldImmutablySetSHMSize() throws Exception
    {
        Build        build1 = Build.fromDockerFile().withTags("foo");
        List<String> before = resolveArguments(build1);
        Build        build2 = build1.shmSize("A", "B", "C");

        assertThat(build1, is (not(sameInstance(build2))));


        List<String> arguments1 = resolveArguments(build1);
        List<String> arguments2 = resolveArguments(build2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--shm-size=A", "--shm-size=B", "--shm-size=C"));
    }


    @Test
    public void shouldImmutablySetULimit() throws Exception
    {
        Build        build1 = Build.fromDockerFile().withTags("foo");
        List<String> before = resolveArguments(build1);
        Build        build2 = build1.ulimit("A", "B", "C");

        assertThat(build1, is (not(sameInstance(build2))));


        List<String> arguments1 = resolveArguments(build1);
        List<String> arguments2 = resolveArguments(build2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--ulimit=A", "--ulimit=B", "--ulimit=C"));
    }


    @Test
    public void shouldImmutablySetMultipleTags() throws Exception
    {
        Build        build1 = Build.fromDockerFile().withTags("foo");
        List<String> before = resolveArguments(build1);
        Build        build2 = build1.withTags("A", "B", "C");

        assertThat(build1, is (not(sameInstance(build2))));


        List<String> arguments1 = resolveArguments(build1);
        List<String> arguments2 = resolveArguments(build2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--tag=A", "--tag=B", "--tag=C"));
    }


    @Test
    public void shouldImmutablySetCustomArguments() throws Exception
    {
        Build        build1 = Build.fromDockerFile().withTags("foo");
        List<String> before = resolveArguments(build1);
        Build        build2 = build1.withCommandArguments(Argument.of("--test1"), Argument.of("--test2"));

        assertThat(build1, is (not(sameInstance(build2))));


        List<String> arguments1 = resolveArguments(build1);
        List<String> arguments2 = resolveArguments(build2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--test1", "--test2"));
    }


    @Test
    public void shouldImmutablyRemoveArguments() throws Exception
    {
        Build        build1 = Build.fromDockerFile().withTags("foo").withCommandArguments(Argument.of("--test1"), Argument.of("--test2"));
        List<String> before = resolveArguments(build1);
        Build        build2 = build1.withoutCommandArguments(Argument.of("--test1"), Argument.of("--test2"));

        assertThat(build1, is (not(sameInstance(build2))));


        List<String> arguments1 = resolveArguments(build1);
        List<String> arguments2 = resolveArguments(build2);

        assertThat(arguments1, is(before));
        assertThat(arguments1.contains("--test1"), is(true));
        assertThat(arguments1.contains("--test2"), is(true));
        assertThat(arguments2.contains("--test1"), is(false));
        assertThat(arguments2.contains("--test2"), is(false));
    }


    @Test
    public void shouldAddImageAsFeature() throws Exception
    {
        Platform       platform    = mock(Platform.class);
        Application    application = mock(Application.class);
        Options        options     = new Options(Arguments.of(Argument.of("--tag=foo"), Argument.of("--tag=bar")));
        Build          build       = Build.fromDockerFile().withTags("foo", "bar");

        build.onLaunched(platform, application, options);

        ArgumentCaptor<DockerImage> captor = ArgumentCaptor.forClass(DockerImage.class);

        verify(application).add(captor.capture());

        DockerImage image = captor.getValue();

        assertThat(image, is(notNullValue()));
        assertThat(image.getTags(), containsInAnyOrder("foo", "bar"));
    }


    private List<String> resolveArguments(Build build)
    {
        Platform platform = LocalPlatform.get();
        Options  options  = new Options();

        build.onFinalize(platform, options);

        Arguments arguments = options.get(Arguments.class);

        return arguments.resolve(platform, options);
    }
}
