/*
 * File: DockerImage.java
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

package com.oracle.tools.runtime.containers.docker;

import com.oracle.tools.Options;
import com.oracle.tools.extensible.Extensible;
import com.oracle.tools.extensible.Feature;
import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationListener;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.containers.docker.commands.Inspect;
import com.oracle.tools.runtime.containers.docker.commands.Remove;
import com.oracle.tools.runtime.containers.docker.options.ImageCloseBehaviour;

import javax.json.JsonArray;
import javax.json.JsonValue;
import java.util.Collections;
import java.util.List;

/**
 * A representation of a Docker image.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class DockerImage implements Feature, ApplicationListener<Application>
{
    /**
     * The tags to identify this image.
     */
    private final List<String> tags;

    /**
     * The {@link Options} used to build this image.
     */
    private final Options options;

    /**
     * The {@link Application} used to build this image
     */
    private Application application;

    /**
     * The {@link Platform} for issuing Docker client commands, this will
     * typically be the {@link Platform} from the {@link Application} used
     * to build the image.
     */
    private Platform platform;


    /**
     * Create a {@link DockerImage}.
     *
     * @param tags     the tags to identify this image
     * @param options  the {@link Options} used to build this image
     */
    public DockerImage(List<String> tags, Options options)
    {
        if (tags == null || tags.isEmpty())
        {
            throw new IllegalArgumentException("The image tags cannot be null or empty List");
        }

        this.tags    = tags;
        this.options = options == null ? new Options() : new Options(options);
    }


    public Application getApplication()
    {
        return application;
    }


    /**
     * Obtain the {@link Options} used to build this image.
     *
     * @return  the {@link Options} used to build this image
     */
    public Options getOptions()
    {
        return options;
    }


    /**
     * Obtain the tags to identify this image.
     *
     * @return  the tags to identify this image
     */
    public List<String> getTags()
    {
        return Collections.unmodifiableList(tags);
    }


    /**
     * Obtain the first of the tags used to identify this image.
     *
     * @return  the first of the tags used to identify this image
     */
    public String getFirstTag()
    {
        return tags.iterator().next();
    }


    /**
     * Obtain the {@link Docker} environment used to build this {@link DockerImage}.
     *
     * @return  the {@link Docker} environment used to build this {@link DockerImage}
     */
    public Docker getDockerEnvironment()
    {
        return options.get(Docker.class);
    }


    /**
     * Obtain information about this {@link DockerImage} as a {@link JsonArray}.
     * <p>
     * The {@link JsonArray} will have a single entry that is the
     * JSON representation of this  {@link DockerImage}'s state.
     * <p>
     * This equates to running the <code>docker inspect</code> command for
     * this {@link DockerImage}.
     *
     * @return  information about this  {@link DockerImage} as a {@link JsonArray}
     */
    public JsonValue inspect()
    {
        if (platform == null)
        {
            throw new IllegalStateException("No Platform is available, is this image a feature of an Application");
        }

        return createInspectCommand()
                .run(platform, getDockerEnvironment());
    }


    /**
     * Obtain an {@link Inspect} command to inspect this container.
     *
     * @return  an {@link Inspect} command to inspect this container
     */
    Inspect createInspectCommand()
    {
        return Inspect.image(tags);
    }


    /**
     * Remove this {@link DockerImage}.
     * <p>
     * This equates to running the <code>docker rmi</code> command for
     * this {@link DockerImage}.
     */
    public boolean remove()
    {
        if (platform == null)
        {
            throw new IllegalStateException("No Platform is available, is this image a feature of an Application");
        }

        try (Application app = platform.launch(getDockerEnvironment(), Remove.images(tags)))
        {
            return app.waitFor() == 0;
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
    public void onClosing(Application application, Options options)
    {
        // there is nothing to do here
    }


    @Override
    public void onClosed(Application application, Options options)
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

        ImageCloseBehaviour behaviour = closingOptions.get(ImageCloseBehaviour.class);

        behaviour.accept(this);
    }
}
