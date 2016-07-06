/*
 * File: DockerImageTest.java
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
import com.oracle.bedrock.runtime.MetaClass;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.docker.commands.Inspect;
import com.oracle.bedrock.runtime.docker.commands.Remove;
import com.oracle.bedrock.runtime.docker.options.ImageCloseBehaviour;
import com.oracle.bedrock.runtime.options.Arguments;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DockerImage}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class DockerImageTest
{
    @Test
    public void shouldHaveCorrectTags() throws Exception
    {
        DockerImage image = new DockerImage(Arrays.asList("foo", "bar"), OptionsByType.empty());

        assertThat(image.getTags(), contains("foo", "bar"));
    }


    @Test
    public void shouldHaveCorrectFirstTag() throws Exception
    {
        DockerImage image = new DockerImage(Arrays.asList("foo", "bar"), OptionsByType.empty());

        assertThat(image.getFirstTag(), is("foo"));
    }


    @Test
    public void shouldHaveCorrectOptions() throws Exception
    {
        Docker        docker        = Docker.auto();
        OptionsByType optionsByType = OptionsByType.of(docker);
        DockerImage   image         = new DockerImage(Arrays.asList("foo", "bar"), optionsByType);

        assertThat(image.getOptions(), is(notNullValue()));
        assertThat(image.getOptions().get(Docker.class), is(sameInstance(docker)));
    }


    @Test
    public void shouldNotHaveNullOptions() throws Exception
    {
        DockerImage image = new DockerImage(Arrays.asList("foo", "bar"), null);

        assertThat(image.getOptions(), is(notNullValue()));
    }


    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowNullTags() throws Exception
    {
        new DockerImage(null, OptionsByType.empty());
    }


    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowEmptyTags() throws Exception
    {
        new DockerImage(Collections.emptyList(), OptionsByType.empty());
    }


    @Test
    public void shouldGetDocker() throws Exception
    {
        Docker        docker        = Docker.auto();
        OptionsByType optionsByType = OptionsByType.of(docker);
        DockerImage   image         = new DockerImage(Arrays.asList("foo", "bar"), optionsByType);

        Docker        result        = image.getDockerEnvironment();

        assertThat(result, is(sameInstance(docker)));
    }


    @Test
    public void shouldAddAsFeature() throws Exception
    {
        Platform    platform    = mock(Platform.class);
        Application application = mock(Application.class);
        DockerImage image       = new DockerImage(Arrays.asList("foo", "bar"), OptionsByType.empty());

        when(application.getPlatform()).thenReturn(platform);

        image.onAddingTo(application);

        assertThat(image.getApplication(), is(sameInstance(application)));
    }


    @Test
    public void shouldCreateInspectCommand() throws Exception
    {
        Platform      platform      = mock(Platform.class);
        OptionsByType optionsByType = OptionsByType.empty();
        DockerImage   image         = new DockerImage(Arrays.asList("foo", "bar"), OptionsByType.empty());
        Inspect       inspect       = image.createInspectCommand();

        assertThat(inspect, is(notNullValue()));

        inspect.onLaunch(platform, optionsByType);

        Arguments    arguments = optionsByType.get(Arguments.class);
        List<String> values    = arguments.resolve(mock(Platform.class), OptionsByType.empty());

        assertThat(values, contains("inspect", "--type=image", "foo", "bar"));
    }


    @Test
    public void shouldRunInspect() throws Exception
    {
        Docker      docker      = Docker.auto();
        Platform    platform    = mock(Platform.class);
        Application application = mock(Application.class, "App");
        Application inspectApp  = mock(Application.class, "Inspect");
        Inspect     inspect     = mock(Inspect.class);

        DockerImage image       = new DockerImage(Arrays.asList("foo", "bar"), OptionsByType.of(docker));
        DockerImage imageSpy    = spy(image);

        when(application.getPlatform()).thenReturn(platform);
        when(platform.launch(any(MetaClass.class))).thenReturn(inspectApp);
        doReturn(inspect).when(imageSpy).createInspectCommand();

        imageSpy.onAddingTo(application);
        imageSpy.inspect();

        verify(inspect).run(same(platform), same(docker));
    }


    @Test(expected = IllegalStateException.class)
    public void shouldNotInspectIfNoApplication() throws Exception
    {
        DockerImage image = new DockerImage(Arrays.asList("foo", "bar"), OptionsByType.empty());

        image.inspect();
    }


    @Test
    public void shouldRemoveImage() throws Exception
    {
        Docker      docker      = Docker.auto();
        Platform    platform    = mock(Platform.class);
        Application application = mock(Application.class, "App");
        Application removeApp   = mock(Application.class, "Inspect");

        DockerImage image       = new DockerImage(Arrays.asList("foo"), OptionsByType.of(docker));

        when(application.getPlatform()).thenReturn(platform);
        when(platform.launch(any(MetaClass.class), anyVararg())).thenReturn(removeApp);

        image.onAddingTo(application);

        image.remove();

        ArgumentCaptor<MetaClass> captor = ArgumentCaptor.forClass(MetaClass.class);

        verify(platform).launch(captor.capture(), anyVararg());

        Remove.RemoveImage remove = (Remove.RemoveImage) captor.getValue();

        assertThat(remove, is(notNullValue()));

        OptionsByType optionsByType = OptionsByType.empty();

        remove.onLaunch(platform, optionsByType);

        Arguments    arguments = optionsByType.get(Arguments.class);
        List<String> values    = arguments.resolve(mock(Platform.class), OptionsByType.empty());

        assertThat(values, contains("rmi", "foo"));
    }


    @Test(expected = IllegalStateException.class)
    public void shouldNotRemoveIfNoApplication() throws Exception
    {
        DockerImage image = new DockerImage(Arrays.asList("foo", "bar"), OptionsByType.empty());

        image.remove();
    }


    @Test
    public void shouldCloseWithNullApplication() throws Exception
    {
        DockerImage         image     = new DockerImage(Arrays.asList("foo", "bar"), OptionsByType.empty());
        ImageCloseBehaviour behaviour = mock(ImageCloseBehaviour.class);

        image.onClosed(null, OptionsByType.of(behaviour));

        verify(behaviour).accept(same(image));
    }


    @Test
    public void shouldCloseWithApplicationAndUseOverridingBehaviour() throws Exception
    {
        Application         application       = mock(Application.class);
        DockerImage         image             = new DockerImage(Arrays.asList("foo", "bar"), OptionsByType.empty());
        ImageCloseBehaviour behaviourApp      = mock(ImageCloseBehaviour.class, "1");
        ImageCloseBehaviour behaviourOverride = mock(ImageCloseBehaviour.class, "2");

        when(application.getOptions()).thenReturn(OptionsByType.of(behaviourApp));

        image.onClosed(application, OptionsByType.of(behaviourOverride));

        verify(behaviourApp, never()).accept(same(image));
        verify(behaviourOverride).accept(same(image));
    }


    @Test
    public void shouldCloseWithApplicationAndUseApplicationCloseBehaviour() throws Exception
    {
        Application         application  = mock(Application.class);
        DockerImage         image        = new DockerImage(Arrays.asList("foo", "bar"), OptionsByType.empty());
        ImageCloseBehaviour behaviourApp = mock(ImageCloseBehaviour.class);

        when(application.getOptions()).thenReturn(OptionsByType.of(behaviourApp));

        image.onClosed(application, OptionsByType.empty());

        verify(behaviourApp).accept(same(image));
    }
}
