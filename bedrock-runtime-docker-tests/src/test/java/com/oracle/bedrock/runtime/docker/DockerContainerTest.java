/*
 * File: DockerContainerTest.java
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

import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.Options;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.MetaClass;
import com.oracle.bedrock.runtime.docker.commands.Inspect;
import com.oracle.bedrock.runtime.docker.commands.Remove;
import com.oracle.bedrock.runtime.docker.commands.Stop;
import com.oracle.bedrock.runtime.docker.options.ContainerCloseBehaviour;
import com.oracle.bedrock.runtime.options.Arguments;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DockerContainer}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class DockerContainerTest
{
    @Test
    public void shouldHaveCorrectName() throws Exception
    {
        DockerContainer container = new DockerContainer("foo", new Options());

        assertThat(container.getName(), is("foo"));
    }


    @Test
    public void shouldHaveCorrectOptions() throws Exception
    {
        Docker          docker    = Docker.auto();
        Options         options   = new Options(docker);
        DockerContainer container = new DockerContainer("foo", options);

        assertThat(container.getOptions(), is(notNullValue()));
        assertThat(container.getOptions().get(Docker.class), is(sameInstance(docker)));
    }


    @Test
    public void shouldNotHaveNullOptions() throws Exception
    {
        DockerContainer container = new DockerContainer("foo", null);

        assertThat(container.getOptions(), is(notNullValue()));
    }


    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowNullName() throws Exception
    {
        new DockerContainer(null, new Options());
    }


    @Test
    public void shouldGetDocker() throws Exception
    {
        Docker          docker    = Docker.auto();
        Options         options   = new Options(docker);
        DockerContainer container = new DockerContainer("foo", options);

        Docker          result    = container.getDockerEnvironment();

        assertThat(result, is(sameInstance(docker)));
    }


    @Test
    public void shouldAddAsFeature() throws Exception
    {
        Platform    platform    = mock(Platform.class);
        Application application = mock(Application.class);

        DockerContainer container   = new DockerContainer("foo", new Options());

        when(application.getPlatform()).thenReturn(platform);

        container.onAddingTo(application);

        assertThat(container.getApplication(), is(sameInstance(application)));
    }


    @Test
    public void shouldCreateInspectCommand() throws Exception
    {
        Platform        platform  = mock(Platform.class);
        Options         options   = new Options();
        DockerContainer container = new DockerContainer("foo", new Options());
        Inspect         inspect   = container.createInspectCommand();

        assertThat(inspect, is(notNullValue()));

        inspect.onLaunch(platform, options);

        Arguments    arguments = options.get(Arguments.class);
        List<String> values    = arguments.resolve(mock(Platform.class), new Options());

        assertThat(values, contains("inspect", "--type=container", "foo"));
    }


    @Test
    public void shouldRunInspectWithoutFilter() throws Exception
    {
        Docker          docker       = Docker.auto();
        Platform        platform     = mock(Platform.class);
        Application     application  = mock(Application.class, "App");
        Application     inspectApp   = mock(Application.class, "Inspect");
        Inspect         inspect      = mock(Inspect.class);

        DockerContainer container    = new DockerContainer("foo", new Options(docker));
        DockerContainer containerSpy = spy(container);

        when(application.getPlatform()).thenReturn(platform);
        when(platform.launch(any(MetaClass.class))).thenReturn(inspectApp);
        when(inspect.format(anyString())).thenReturn(inspect);
        doReturn(inspect).when(containerSpy).createInspectCommand();

        containerSpy.onAddingTo(application);
        containerSpy.inspect();

        verify(inspect).run(same(platform), same(docker));
        verify(inspect).format((String) isNull());
    }


    @Test
    public void shouldRunInspectWithFilter() throws Exception
    {
        Docker          docker       = Docker.auto();
        Platform        platform     = mock(Platform.class);
        Application     application  = mock(Application.class, "App");
        Application     inspectApp   = mock(Application.class, "Inspect");
        Inspect         inspect      = mock(Inspect.class);

        DockerContainer container    = new DockerContainer("foo", new Options(docker));
        DockerContainer containerSpy = spy(container);

        when(application.getPlatform()).thenReturn(platform);
        when(platform.launch(any(MetaClass.class))).thenReturn(inspectApp);
        when(inspect.format(anyString())).thenReturn(inspect);
        doReturn(inspect).when(containerSpy).createInspectCommand();

        containerSpy.onAddingTo(application);
        containerSpy.inspect("{{filter}}");

        verify(inspect).run(same(platform), same(docker));
        verify(inspect).format(eq("{{filter}}"));
    }


    @Test(expected = IllegalStateException.class)
    public void shouldNotInspectIfNoApplication() throws Exception
    {
        DockerContainer container = new DockerContainer("foo", new Options());

        container.inspect();
    }


    @Test
    public void shouldRemoveContainer() throws Exception
    {
        Docker          docker      = Docker.auto();
        Platform        platform    = mock(Platform.class);
        Application     application = mock(Application.class, "App");
        Application     removeApp   = mock(Application.class, "Inspect");

        DockerContainer container   = new DockerContainer("foo", new Options(docker));

        when(application.getPlatform()).thenReturn(platform);
        when(platform.launch(any(MetaClass.class), anyVararg())).thenReturn(removeApp);

        container.onAddingTo(application);

        container.remove(false);

        ArgumentCaptor<MetaClass> captor = ArgumentCaptor.forClass(MetaClass.class);

        verify(platform).launch(captor.capture(), anyVararg());

        Options                options = new Options();
        Remove.RemoveContainer remove  = (Remove.RemoveContainer) captor.getValue();

        assertThat(remove, is(notNullValue()));

        remove.onLaunch(platform, options);

        Arguments    arguments = options.get(Arguments.class);
        List<String> values    = arguments.resolve(mock(Platform.class), new Options());

        assertThat(values, contains("rm", "foo"));
    }


    @Test
    public void shouldRemoveContainerWithForce() throws Exception
    {
        Docker          docker      = Docker.auto();
        Platform        platform    = mock(Platform.class);
        Application     application = mock(Application.class, "App");
        Application     removeApp   = mock(Application.class, "Inspect");

        DockerContainer container   = new DockerContainer("foo", new Options(docker));

        when(application.getPlatform()).thenReturn(platform);
        when(platform.launch(any(MetaClass.class), anyVararg())).thenReturn(removeApp);

        container.onAddingTo(application);

        container.remove(true);

        ArgumentCaptor<MetaClass> captor = ArgumentCaptor.forClass(MetaClass.class);

        verify(platform).launch(captor.capture(), anyVararg());

        Options                options = new Options();
        Remove.RemoveContainer remove  = (Remove.RemoveContainer) captor.getValue();

        assertThat(remove, is(notNullValue()));

        remove.onLaunch(platform, options);

        Arguments    arguments = options.get(Arguments.class);
        List<String> values    = arguments.resolve(mock(Platform.class), new Options());

        assertThat(values, contains("rm", "--force", "foo"));
    }


    @Test(expected = IllegalStateException.class)
    public void shouldNotRemoveIfNoApplication() throws Exception
    {
        DockerContainer container = new DockerContainer("foo", new Options());

        container.remove(true);
    }


    @Test
    public void shouldStopContainer() throws Exception
    {
        Docker          docker      = Docker.auto();
        Platform        platform    = mock(Platform.class);
        Application     application = mock(Application.class, "App");
        Application     removeApp   = mock(Application.class, "Inspect");

        DockerContainer container   = new DockerContainer("foo", new Options(docker));

        when(application.getPlatform()).thenReturn(platform);
        when(platform.launch(any(MetaClass.class), anyVararg())).thenReturn(removeApp);

        container.onAddingTo(application);

        container.stop();

        ArgumentCaptor<MetaClass> captor = ArgumentCaptor.forClass(MetaClass.class);

        verify(platform).launch(captor.capture(), anyVararg());

        Options options = new Options();
        Stop    stop    = (Stop) captor.getValue();

        assertThat(stop, is(notNullValue()));

        stop.onLaunch(platform, options);

        Arguments    arguments = options.get(Arguments.class);
        List<String> values    = arguments.resolve(mock(Platform.class), new Options());

        assertThat(values, contains("stop", "foo"));
    }


    @Test(expected = IllegalStateException.class)
    public void shouldNotStopIfNoApplication() throws Exception
    {
        DockerContainer container = new DockerContainer("foo", new Options());

        container.stop();
    }


    @Test
    public void shouldCloseWithNullApplication() throws Exception
    {
        DockerContainer         container = new DockerContainer("foo", new Options());
        ContainerCloseBehaviour behaviour = mock(ContainerCloseBehaviour.class);

        container.onClosed(null, new Options(behaviour));

        verify(behaviour).accept(same(container));
    }


    @Test
    public void shouldCloseWithApplicationAndUseOverridingBehaviour() throws Exception
    {
        Application             application       = mock(Application.class);
        DockerContainer         container         = new DockerContainer("foo", new Options());
        ContainerCloseBehaviour behaviourApp      = mock(ContainerCloseBehaviour.class, "1");
        ContainerCloseBehaviour behaviourOverride = mock(ContainerCloseBehaviour.class, "2");

        when(application.getOptions()).thenReturn(new Options(behaviourApp));

        container.onClosed(application, new Options(behaviourOverride));

        verify(behaviourApp, never()).accept(same(container));
        verify(behaviourOverride).accept(same(container));
    }


    @Test
    public void shouldCloseWithApplicationAndUseApplicationCloseBehaviour() throws Exception
    {
        Application             application  = mock(Application.class);
        DockerContainer         container    = new DockerContainer("foo", new Options());
        ContainerCloseBehaviour behaviourApp = mock(ContainerCloseBehaviour.class);

        when(application.getOptions()).thenReturn(new Options(behaviourApp));

        container.onClosed(application, new Options());

        verify(behaviourApp).accept(same(container));
    }
}
