/*
 * File: DockerContainer.java
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

import com.oracle.bedrock.Options;
import com.oracle.bedrock.extensible.Extensible;
import com.oracle.bedrock.extensible.Feature;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.ApplicationListener;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.docker.commands.Inspect;
import com.oracle.bedrock.runtime.docker.commands.Remove;
import com.oracle.bedrock.runtime.docker.commands.Stop;
import com.oracle.bedrock.runtime.docker.options.ContainerCloseBehaviour;

import javax.json.JsonArray;
import javax.json.JsonValue;

/**
 * A representation of a Docker container.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class DockerContainer implements Feature, ApplicationListener<Application>
{
    /**
     * The name of this container.
     */
    private final String name;

    /**
     * The {@link Options} used to run this container
     */
    private final Options options;

    /**
     * The {@link Application} used to run this container.
     */
    private Application application;

    /**
     * The {@link Platform} for issuing Docker client commands, this will
     * typically be the {@link Platform} from the {@link Application} used
     * to run the container.
     */
    private Platform platform;


    /**
     * Create a {@link DockerContainer}.
     *
     * @param name     the name of the container
     * @param options  the {@link Options} used to run the container
     */
    public DockerContainer(String  name,
                           Options options)
    {
        if (name == null || name.trim().isEmpty())
        {
            throw new IllegalArgumentException("The container name cannot be null or empty String");
        }

        this.name    = name;
        this.options = options == null ? new Options() : new Options(options);
    }


    /**
     * Obtain the name of this container.
     *
     * @return  the name of this container
     */
    public String getName()
    {
        return name;
    }


    /**
     * Obtain the {@link Application} used to run this container.
     *
     * @return  the {@link Application} used to run this container
     */
    public Application getApplication()
    {
        return application;
    }


    /**
     * Obtain the {@link Options} used to run this container.
     *
     * @return  the {@link Options} used to run this container
     */
    public Options getOptions()
    {
        return options;
    }


    /**
     * Obtain the {@link Docker} environment used to run this container.
     *
     * @return  the {@link Docker} environment used to run this container
     */
    public Docker getDockerEnvironment()
    {
        return options.get(Docker.class);
    }


    /**
     * Obtain information about this {@link DockerContainer} as a {@link JsonArray}.
     * <p>
     * The {@link JsonArray} will have a single entry that is the
     * JSON representation of this  {@link DockerContainer}'s state.
     * <p>
     * This equates to running the <code>docker inspect</code> command for
     * this {@link DockerContainer}.
     *
     * @return  information about this  {@link DockerContainer} as a {@link JsonArray}
     *
     * @throws IllegalStateException  if this {@link DockerContainer} has not been added
     *                                to an {@link Application} as a {@link Feature}.
     */
    public JsonValue inspect()
    {
        return inspect(null);
    }


    /**
     * Obtain information about this {@link DockerContainer} as a {@link JsonArray}.
     * <p>
     * The {@link JsonArray} will have a single entry that is the
     * JSON representation of this  {@link DockerContainer}'s state.
     * <p>
     * This equates to running the <code>docker inspect</code> command for
     * this {@link DockerContainer}.
     *
     * @param format   the format
     *
     * @return  information about this  {@link DockerContainer} as a {@link JsonArray}
     *
     * @throws IllegalStateException  if this {@link DockerContainer} has not been added
     *                                to an {@link Application} as a {@link Feature}.
     */
    public JsonValue inspect(String format)
    {
        if (platform == null)
        {
            throw new IllegalStateException("No Platform is available, is this container a feature of an Application");
        }

        return createInspectCommand().format(format).run(platform, getDockerEnvironment());
    }


    /**
     * Obtain an {@link Inspect} command to inspect this container.
     *
     * @return  an {@link Inspect} command to inspect this container
     */
    Inspect createInspectCommand()
    {
        return Inspect.container(name);
    }


    /**
     * Determine whether the container is running.
     *
     * @return  true if on inspection the container JSON State.Running is true
     */
    public boolean isRunning()
    {
        JsonArray json = (JsonArray) inspect("{{.State.Running}}");

        return json.getBoolean(0);
    }


    /**
     * Determine the container status
     *
     * @return  the value of the JSON State.Status on inspecting the container
     */
    public String getStatus()
    {
        JsonArray json = (JsonArray) inspect("{{.State.Status}}");

        return json.getString(0);
    }


    /**
     * Stop this container.
     * <p>
     * This equates to running the <code>docker stop</code> command for
     * this container.
     *
     * @throws IllegalStateException  if this {@link DockerContainer} has not been added
     *                                to an {@link Application} as a {@link Feature}.
     */
    public void stop()
    {
        if (platform == null)
        {
            throw new IllegalStateException("No Platform is available, is this container a feature of an Application");
        }

        Docker docker = options.get(Docker.class);

        try (Application app = platform.launch(Stop.containers(name), docker))
        {
            app.waitFor();
        }
    }


    /**
     * Remove this {@link DockerContainer}.
     * <p>
     * This equates to running the <code>docker rm</code> command for
     * this {@link DockerContainer}.
     * <p>
     * This command will fail to execute if the {@link DockerContainer} is still running.
     *
     * @param force  whether to add --force to the Docker rm command
     *
     * @throws IllegalStateException  if this {@link DockerContainer} has not been added
     *                                to an {@link Application} as a {@link Feature}.
     */
    public void remove(boolean force)
    {
        if (platform == null)
        {
            throw new IllegalStateException("No Platform is available, is this container a feature of an Application");
        }

        Docker                 docker = options.get(Docker.class);
        Remove.RemoveContainer removeCommand;

        if (force)
        {
            removeCommand = Remove.containers(name).force(true);
        }
        else
        {
            removeCommand = Remove.containers(name);
        }

        try (Application app = platform.launch(removeCommand, docker))
        {
            app.waitFor();
        }
    }


    @Override
    public void onAddingTo(Extensible extensible)
    {
        this.application = (Application) extensible;
        this.platform    = this.application.getPlatform();
    }


    @Override
    public void onRemovingFrom(Extensible extensible)
    {
        // there is nothing to do here
    }


    @Override
    public void onLaunched(Application application)
    {
        // there is nothing to do here
    }


    @Override
    public void onClosing(Application application,
                          Options     options)
    {
        // there is nothing to do here
    }


    @Override
    public void onClosed(Application application,
                         Options     options)
    {
        Options closingOptions;

        if (application != null)
        {
            closingOptions = new Options(application.getOptions());
            closingOptions.addAll(options);
        }
        else
        {
            closingOptions = options;
        }

        ContainerCloseBehaviour behaviour = closingOptions.get(ContainerCloseBehaviour.class);

        behaviour.accept(this);
    }
}
