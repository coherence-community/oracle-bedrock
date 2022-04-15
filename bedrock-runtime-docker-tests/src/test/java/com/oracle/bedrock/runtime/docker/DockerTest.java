/*
 * File: DockerTest.java
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

package com.oracle.bedrock.runtime.docker;

import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.docker.options.DockerDefaultBaseImages;
import com.oracle.bedrock.runtime.options.Arguments;
import com.oracle.bedrock.runtime.options.EnvironmentVariables;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link Docker}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class DockerTest
{
    @Test
    public void shouldHaveCorrectDaemonAddress() throws Exception
    {
        String host   = "tcp://foo:1234";
        Docker docker = Docker.daemonAt(host);

        assertThat(docker.getDaemonAddress(), is(host));

        Arguments    arguments = Arguments.of(docker.getArguments());
        List<String> resolved  = arguments.resolve(LocalPlatform.get(), OptionsByType.empty());

        assertThat(resolved, contains(Docker.ARG_HOST + "=" + host));
    }


    @Test
    public void shouldImmutablySetDaemonAddress() throws Exception
    {
        String host1   = "tcp://foo:1234";
        String host2   = "tcp://bar:1234";
        Docker docker1 = Docker.daemonAt(host1);
        Docker docker2 = docker1.withDaemonAddress(host2);

        assertThat(docker1, is(not(sameInstance(docker2))));

        assertThat(resolveArguments(docker1), contains(Docker.ARG_HOST + "=" + host1));
        assertThat(resolveArguments(docker2), contains(Docker.ARG_HOST + "=" + host2));
    }


    @Test
    public void shouldHaveDefaultExecutable() throws Exception
    {
        Docker docker = Docker.auto();

        assertThat(docker.getDockerExecutable(), is("docker"));
    }


    @Test
    public void shouldImmutablySetExecutable() throws Exception
    {
        Docker docker1 = Docker.auto().dockerExecutableOf("foo");
        Docker docker2 = docker1.dockerExecutableOf("bar");

        assertThat(docker1, is(not(sameInstance(docker2))));

        assertThat(docker1.getDockerExecutable(), is("foo"));
        assertThat(docker2.getDockerExecutable(), is("bar"));
    }


    @Test
    public void shouldHaveDefaultBaseImage() throws Exception
    {
        Docker                  docker        = Docker.auto();
        DockerDefaultBaseImages defaultImages = DockerDefaultBaseImages.defaultImages();

        String                  imageName     = docker.getBaseImage(Application.class);

        assertThat(imageName, is(defaultImages.getBaseImage(Application.class)));
    }


    @Test
    public void shouldImmutablySetDebug() throws Exception
    {
        Docker docker1 = Docker.auto().debug(false);
        Docker docker2 = docker1.debug(true);

        assertThat(docker1, is(not(sameInstance(docker2))));

        assertThat(resolveArguments(docker1), contains(Docker.ARG_DEBUG + "=" + false));
        assertThat(resolveArguments(docker2), contains(Docker.ARG_DEBUG + "=" + true));
    }


    @Test
    public void shouldImmutablySetDriver() throws Exception
    {
        Docker docker1 = Docker.auto().driver("foo");
        Docker docker2 = docker1.driver("bar");

        assertThat(docker1, is(not(sameInstance(docker2))));

        assertThat(resolveEnvironment(docker1), hasEntry(Docker.ENV_DOCKER_DRIVER, "foo"));
        assertThat(resolveEnvironment(docker2), hasEntry(Docker.ENV_DOCKER_DRIVER, "bar"));
    }


    @Test
    public void shouldImmutablySetLogLevel() throws Exception
    {
        Docker docker1 = Docker.auto().logLevel("info");
        Docker docker2 = docker1.logLevel("error");

        assertThat(docker1, is(not(sameInstance(docker2))));

        assertThat(resolveArguments(docker1), contains(Docker.ARG_LOG_LEVEL + "=info"));
        assertThat(resolveArguments(docker2), contains(Docker.ARG_LOG_LEVEL + "=error"));
    }


    @Test
    public void shouldImmutablySetKernelWarning() throws Exception
    {
        Docker docker1 = Docker.auto().noWarnKernelVersion(true);
        Docker docker2 = docker1.noWarnKernelVersion(false);

        assertThat(docker1, is(not(sameInstance(docker2))));

        assertThat(resolveEnvironment(docker1), hasEntry(Docker.ENV_DOCKER_NOWARN_KERNEL_VERSION, "true"));
        assertThat(resolveEnvironment(docker2), hasEntry(Docker.ENV_DOCKER_NOWARN_KERNEL_VERSION, "false"));
    }


    @Test
    public void shouldImmutablySetRamDisk() throws Exception
    {
        Docker docker1 = Docker.auto().ramDisk(true);
        Docker docker2 = docker1.ramDisk(false);

        assertThat(docker1, is(not(sameInstance(docker2))));

        assertThat(resolveEnvironment(docker1), hasEntry(Docker.ENV_DOCKER_RAMDISK, "true"));
        assertThat(resolveEnvironment(docker2), hasEntry(Docker.ENV_DOCKER_RAMDISK, "false"));
    }


    @Test
    public void shouldImmutablySetTempLocation() throws Exception
    {
        Docker docker1 = Docker.auto().tempFilesAt(new File("foo"));
        Docker docker2 = docker1.tempFilesAt(new File("bar"));

        assertThat(docker1, is(not(sameInstance(docker2))));

        assertThat(resolveEnvironment(docker1), hasEntry(Docker.ENV_DOCKER_TMPDIR, "foo"));
        assertThat(resolveEnvironment(docker2), hasEntry(Docker.ENV_DOCKER_TMPDIR, "bar"));
    }


    @Test
    public void shouldImmutablySetUseTLS() throws Exception
    {
        Docker docker1 = Docker.auto().tls(true);
        Docker docker2 = docker1.tls(false);

        assertThat(docker1, is(not(sameInstance(docker2))));

        assertThat(resolveArguments(docker1), contains(Docker.ARG_TLS + "=" + true));
        assertThat(resolveArguments(docker2), contains(Docker.ARG_TLS + "=" + false));
    }


    @Test
    public void shouldImmutablySetTlsCaCerts() throws Exception
    {
        Docker docker1 = Docker.auto().tlsCACert(new File("foo"));
        Docker docker2 = docker1.tlsCACert(new File("bar"));

        assertThat(docker1, is(not(sameInstance(docker2))));

        assertThat(resolveArguments(docker1), contains(Docker.ARG_TLS_CA_CERT + "=foo"));
        assertThat(resolveArguments(docker2), contains(Docker.ARG_TLS_CA_CERT + "=bar"));
    }


    @Test
    public void shouldImmutablySetTlsCerts() throws Exception
    {
        Docker docker1 = Docker.auto().tlsCert(new File("foo"));
        Docker docker2 = docker1.tlsCert(new File("bar"));

        assertThat(docker1, is(not(sameInstance(docker2))));

        assertThat(resolveArguments(docker1), contains(Docker.ARG_TLS_CERT + "=foo"));
        assertThat(resolveArguments(docker2), contains(Docker.ARG_TLS_CERT + "=bar"));
    }


    @Test
    public void shouldImmutablySetTlsKeys() throws Exception
    {
        Docker docker1 = Docker.auto().tlsKey(new File("foo"));
        Docker docker2 = docker1.tlsKey(new File("bar"));

        assertThat(docker1, is(not(sameInstance(docker2))));

        assertThat(resolveArguments(docker1), contains(Docker.ARG_TLS_KEY + "=foo"));
        assertThat(resolveArguments(docker2), contains(Docker.ARG_TLS_KEY + "=bar"));
    }


    @Test
    public void shouldImmutablySetUseTLSVerify() throws Exception
    {
        Docker docker1 = Docker.auto().tlsVerify(true);
        Docker docker2 = docker1.tlsVerify(false);

        assertThat(docker1, is(not(sameInstance(docker2))));

        assertThat(resolveArguments(docker1), contains(Docker.ARG_TLS_VERIFY + "=" + true));
        assertThat(resolveArguments(docker2), contains(Docker.ARG_TLS_VERIFY + "=" + false));
    }


    @Test
    public void shouldImmutablySetConfigLocation() throws Exception
    {
        Docker docker1 = Docker.auto().configAt("Foo");
        Docker docker2 = docker1.configAt("Bar");

        assertThat(docker1, is(not(sameInstance(docker2))));

        assertThat(resolveArguments(docker1), contains(Docker.ARG_CONFIG + "=Foo"));
        assertThat(resolveArguments(docker2), contains(Docker.ARG_CONFIG + "=Bar"));
    }


    @Test
    public void shouldImmutablySetApiVersion() throws Exception
    {
        Docker docker1 = Docker.auto().apiVersion("Foo");
        Docker docker2 = docker1.apiVersion("Bar");

        assertThat(docker1, is(not(sameInstance(docker2))));

        assertThat(resolveEnvironment(docker1), hasEntry(Docker.ENV_DOCKER_API_VERSION, "Foo"));
        assertThat(resolveEnvironment(docker2), hasEntry(Docker.ENV_DOCKER_API_VERSION, "Bar"));
    }


    @Test
    public void shouldImmutablyEnableContentTrust() throws Exception
    {
        Docker docker1 = Docker.auto().contentTrustEnabled(true);
        Docker docker2 = docker1.contentTrustEnabled(false);

        assertThat(docker1, is(not(sameInstance(docker2))));

        assertThat(resolveEnvironment(docker1), hasEntry(Docker.ENV_DOCKER_CONTENT_TRUST, "true"));
        assertThat(resolveEnvironment(docker2), hasEntry(Docker.ENV_DOCKER_CONTENT_TRUST, "false"));
    }


    @Test
    public void shouldImmutablySetContentTrustLocation() throws Exception
    {
        Docker docker1 = Docker.auto().contentTrustAt("Foo");
        Docker docker2 = docker1.contentTrustAt("Bar");

        assertThat(docker1, is(not(sameInstance(docker2))));

        assertThat(resolveEnvironment(docker1), hasEntry(Docker.ENV_DOCKER_CONTENT_TRUST_SERVER, "Foo"));
        assertThat(resolveEnvironment(docker2), hasEntry(Docker.ENV_DOCKER_CONTENT_TRUST_SERVER, "Bar"));
    }


    private List<String> resolveArguments(Docker docker)
    {
        Arguments arguments1 = Arguments.of(docker.getArguments());

        return arguments1.resolve(LocalPlatform.get(), OptionsByType.empty());
    }


    private Properties resolveEnvironment(Docker docker)
    {
        EnvironmentVariables variables = EnvironmentVariables.custom().with(docker.getEnvironmentVariables());

        return variables.realize(LocalPlatform.get());
    }
}
