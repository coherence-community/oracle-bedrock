package com.oracle.tools.runtime.remote;

import com.oracle.tools.Options;

import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.SimpleApplicationSchema;

import org.junit.Test;

import java.net.InetAddress;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

import static org.junit.Assert.assertThat;

import static org.mockito.Mockito.mock;

/**
 * Tests for {@link AbstractRemoteApplicationEnvironment}.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class AbstractRemoteApplicationEnvironmentTest
{
    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnCommand() throws Exception
    {
        Platform                platform = mock(Platform.class);
        Options                 options  = new Options();
        SimpleApplicationSchema schema   = new SimpleApplicationSchema("foo");

        AbstractRemoteApplicationEnvironment environment =
                new AbstractRemoteApplicationEnvironment(schema, platform, options){};

        assertThat(environment.getRemoteCommandToExecute(), is("foo"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnCommandLineArguments() throws Exception
    {
        Platform                platform = mock(Platform.class);
        Options                 options  = new Options();
        SimpleApplicationSchema schema   = new SimpleApplicationSchema("foo")
                                                .addArgument("arg1")
                                                .addArgument("arg2");

        AbstractRemoteApplicationEnvironment environment =
                new AbstractRemoteApplicationEnvironment(schema, platform, options){};

        List<String> arguments = environment.getRemoteCommandArguments(InetAddress.getLocalHost());

        assertThat(arguments, containsInAnyOrder("arg1", "arg2"));
    }
}
