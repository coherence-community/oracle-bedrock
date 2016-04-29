/*
 * File: Images.java
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

package com.oracle.tools.runtime.docker.commands;

import com.oracle.tools.runtime.options.Argument;
import com.oracle.tools.runtime.options.Arguments;

import java.util.Collections;
import java.util.List;

/**
 * A representation of the Docker images command.
 * <p>
 * Instances of {@link Images} are <strong>immutable</strong>, methods that
 * add options and configuration to this {@link Images} command return a
 * new instance of a {@link Images} command with the modifications applied.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class Images extends CommandWithArgumentList<Images>
{
    /**
     * The filter to apply to list untagged (dangling) images.
     */
    public static final String FILTER_DANGLING = "dangling=true";

    /**
     * The format string to use to show the image ID.
     */
    public static final String FORMAT_ID = "{{.ID}}";

    /**
     * The format string to use to show the image repository.
     */
    public static final String FORMAT_REPOSITORY = "{{.Repository}}";

    /**
     * The format string to use to show the image tag.
     */
    public static final String FORMAT_TAG = "{{.Tag}}";

    /**
     * The format string to use to show the image digest.
     */
    public static final String FORMAT_DIGEST = "{{.Digest}}";

    /**
     * The format string to use to show the elapsed time since the image was created.
     */
    public static final String FORMAT_CREATED_SINCE = "{{.CreatedSince}}";

    /**
     * The format string to use to show the time that the image was created.
     */
    public static final String FORMAT_CREATED_AT = "{{.CreatedAt}}";

    /**
     * The format string to use to show the image size.
     */
    public static final String FORMAT_SIZE = "{{.Size}}";



    /**
     * Create a {@link Images} command to list Docker images.
     */
    private Images()
    {
        super("images", Collections.emptyList());
    }


    /**
     * Create a {@link Images} command to list Docker images
     * for a specific [REPOSITORY[:TAG]] combination.
     */
    private Images(String repoTag)
    {
        super("images", Collections.singletonList(repoTag));
    }


    /**
     * Create an {@link Images} command with the specified {@link Arguments}
     *
     * @param arguments  the command {@link Arguments}
     * @param tags       the [REPOSITORY[:TAG]] to list
     */
    private Images(Arguments arguments, List<?> tags)
    {
        super(arguments, tags);
    }


    /**
     * Show all images (default hides intermediate images)
     * equates to the -a or --all argument.
     *
     * @return  a new {@link Images} instance that is the same as this
     *          instance with the --all option applied
     */
    public Images all()
    {
        return withCommandArguments(Argument.of("--all"));
    }


    /**
     * Show image digests, equates to the --digests argument.
     *
     * @return  a new {@link Images} instance that is the same as this
     *          instance with the --digests option applied
     */
    public Images digests()
    {
        return withCommandArguments(Argument.of("--digests"));
    }


    /**
     * Filter the image list (equates to the --filter argument).
     *
     * @param filters  values that resolve to one or more valid filter arguments
     *
     * @return  a new {@link Images} instance that is the same as this
     *          instance with the --filter option applied
     */
    public Images filter(Object... filters)
    {
        if (filters == null || filters.length == 0)
        {
            return this;
        }

        return withCommandArguments(Argument.of("--filter", '=', new Argument.Multiple(filters)));
    }


    /**
     * Format the output (equates to the --format argument).
     *
     * @param formats  values that resolve to one or more valid format arguments
     *
     * @return  a new {@link Images} instance that is the same as this
     *          instance with the --format option applied
     */
    public Images format(Object... formats)
    {
        if (formats == null || formats.length == 0)
        {
            return this;
        }

        return withCommandArguments(Argument.of("--format", '=', new Argument.Multiple(formats)));
    }


    /**
     * Do not truncate output, equates to the --no-trunc argument.
     *
     * @return  a new {@link Images} instance that is the same as this
     *          instance with the --no-trunc option applied
     */
    public Images noTruncate()
    {
        return withCommandArguments(Argument.of("--no-trunc"));
    }


    /**
     * Only show numeric IDs, equates to the --quiet argument.
     *
     * @return  a new {@link Images} instance that is the same as this
     *          instance with the --quiet option applied
     */
    public Images quiet()
    {
        return withCommandArguments(Argument.of("--quiet"));
    }


    /**
     * Create a {@link Images} command to view all images.
     *
     * @return  a {@link Images} command to view all images
     */
    public static Images list()
    {
        return new Images();
    }


    /**
     * Create a {@link Images} command to view images for
     * a specific [REPOSITORY[:TAG]] combination.
     *
     * @param repoTag  the [REPOSITORY[:TAG]] combination to
     *                 list images for
     *
     * @return  a {@link Images} command to view images
     *          for a specific [REPOSITORY[:TAG]] combination
     */
    public static Images forRepo(String repoTag)
    {
        return new Images(repoTag);
    }


    @Override
    protected Images withCommandArguments(List<Argument> endArgs, Argument... args)
    {
        return new Images(getCommandArguments().with(args), endArgs);
    }


    @Override
    protected Images withoutCommandArguments(List<Argument> endArgs, Argument... args)
    {
        return new Images(getCommandArguments().without(args), endArgs);
    }
}
