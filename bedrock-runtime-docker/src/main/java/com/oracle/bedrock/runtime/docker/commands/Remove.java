/*
 * File: Remove.java
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

package com.oracle.bedrock.runtime.docker.commands;

import com.oracle.bedrock.runtime.options.Argument;
import com.oracle.bedrock.runtime.options.Arguments;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A representation of the Docker rm and rmi commands to remove containers,
 * container links and images.
 * <p>
 * Instances of {@link Remove} are <strong>immutable</strong>, methods that
 * add options and configuration to this {@link Remove} command return a
 * new instance of a {@link Remove} command with the modifications applied.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public abstract class Remove<R extends Remove> extends CommandWithArgumentList<R>
{
    /**
     * Create a {@link Remove} command with the specified {@link Arguments}
     * and items to be removed.
     *
     * @param arguments  the command {@link Arguments}
     * @param names      the names of the items to remove
     */
    private Remove(Arguments arguments,
                   List<?>   names)
    {
        super(arguments, names);
    }


    /**
     * Create a {@link Remove} command.
     *
     * @param command  the name of the command (rm or rmi)
     * @param names    the names of the items to be removed
     */
    private Remove(String  command,
                   List<?> names)
    {
        super(command, names);
    }


    /**
     * Create a {@link RemoveContainer} command to remove specific containers.
     *
     * @param names  values that will resolve to a set of tags for containers
     *               to be removed
     *
     * @return  a {@link RemoveContainer} command to remove specific containers
     */
    public static RemoveContainer containers(Object... names)
    {
        return containers(Arrays.asList(names));
    }


    /**
     * Create a {@link RemoveContainer} command to remove specific containers.
     *
     * @param names  a {@link List} of values that will resolve to a set of
     *               tags for containers to be removed
     *
     * @return  a {@link RemoveContainer} command to remove specific containers
     */
    public static RemoveContainer containers(List<?> names)
    {
        return new RemoveContainer(names);
    }


    /**
     * Create a {@link Remove} command to remove a link between two containers.
     *
     * @param first   the name of the first container
     * @param second  the name of the second container
     *
     * @return  a {@link Remove} command to remove a link between two containers
     */
    public static Remove link(String first,
                              String second)
    {
        List<?> names = Collections.singletonList(first + '/' + second);

        return new RemoveLink(names).withCommandArguments(Argument.of("--link"));
    }


    /**
     * Create a {@link RemoveImage} command to remove specific images.
     *
     * @param tags  values that will resolve to a set of tags for images to be removed
     *
     * @return  a {@link RemoveImage} command to remove specific images
     */
    public static RemoveImage images(Object... tags)
    {
        return images(Arrays.asList(tags));
    }


    /**
     * Create a {@link RemoveImage} command to remove specific images.
     *
     * @param tags  a {@link List} of values that will resolve to a set of
     *              tags for images to be removed
     *
     * @return  a {@link RemoveImage} command to remove specific images
     */
    public static RemoveImage images(List<?> tags)
    {
        return new RemoveImage(tags);
    }


    /**
     * A representation of a Docker command to remove one or more
     * containers (equates to the Docker rm command).
     */
    public static class RemoveContainer extends Remove<RemoveContainer>
    {
        /**
         * Create a {@link RemoveContainer} command to reove
         * the specified containers.
         *
         * @param containers  the names of the containers to remove
         */
        private RemoveContainer(List<?> containers)
        {
            super("rm", containers);
        }


        /**
         * Create a {@link Remove} command with the specified {@link Arguments}
         * and containers to be removed.
         *
         * @param arguments   the command {@link Arguments}
         * @param containers  the names of the containers to remove
         */
        private RemoveContainer(Arguments arguments,
                                List<?>   containers)
        {
            super(arguments, containers);
        }


        /**
         * Force removal of the container (equates to the --force argument).
         *
         * @return  a new {@link RemoveContainer} instance that is the same as this
         *          instance with the --force option applied
         */
        public RemoveContainer force()
        {
            return force(true);
        }


        /**
         * Force removal of the container (equates to the --force argument).
         *
         * @param force  true to add the <code>--force</code> option or false
         *               to remove the <code>--force</code> option
         *
         * @return  a new {@link RemoveContainer} instance that is the same as this
         *          instance with the --force option applied
         */
        public RemoveContainer force(boolean force)
        {
            if (force)
            {
                return withCommandArguments(Argument.of("--force"));
            }

            return withoutCommandArguments(Argument.of("--force"));
        }


        /**
         * Remove the volumes associated with the container (equates to the --volumes argument).
         *
         * @return  a new {@link RemoveContainer} instance that is the same as this
         *          instance with the --volumes option applied
         */
        public RemoveContainer andVolumes()
        {
            return andVolumes(true);
        }


        /**
         * Remove the volumes associated with the container (equates to the --volumes argument).
         *
         * @param   remove  true to add the <code>--volumes</code> option, false to
         *                  remove the <code>--volumes</code> option
         *
         * @return  a new {@link RemoveContainer} instance that is the same as this
         *          instance with the --volumes option applied
         */
        public RemoveContainer andVolumes(boolean remove)
        {
            if (remove)
            {
                return withCommandArguments(Argument.of("--volumes"));
            }

            return withoutCommandArguments(Argument.of("--volumes"));
        }


        @Override
        protected RemoveContainer withCommandArguments(List<Argument> containers,
                                                       Argument...    args)
        {
            return new RemoveContainer(getCommandArguments().with(args), containers);
        }


        @Override
        protected RemoveContainer withoutCommandArguments(List<Argument> names,
                                                          Argument...    args)
        {
            return new RemoveContainer(getCommandArguments().without(args), names);
        }
    }


    /**
     * A representation of a Docker command to remove an image.
     */
    public static class RemoveImage extends Remove<RemoveImage>
    {
        /**
         * Create a {@link RemoveImage} command to remove the specified images.
         *
         * @param images  the names of the images to remove
         */
        private RemoveImage(List<?> images)
        {
            super("rmi", images);
        }


        /**
         * Create a {@link RemoveImage} command with the specified {@link Arguments}
         * and images to be removed.
         *
         * @param arguments  the command {@link Arguments}
         * @param names      the names of the images to remove
         */
        private RemoveImage(Arguments arguments,
                            List<?>   names)
        {
            super(arguments, names);
        }


        /**
         * Force removal of the image (equates to the --force argument).
         *
         * @return  a new {@link RemoveImage} instance that is the same as this
         *          instance with the --force option applied
         */
        public RemoveImage force()
        {
            return force(true);
        }


        /**
         * Force removal of the image (equates to the --force argument).
         *
         * @param force  true to add the <code>--force</code> option or false
         *               to remove the <code>--force</code> option
         *
         * @return  a new {@link RemoveImage} instance that is the same as this
         *          instance with the --force option applied
         */
        public RemoveImage force(boolean force)
        {
            if (force)
            {
                return withCommandArguments(Argument.of("--force"));
            }

            return withoutCommandArguments(Argument.of("--force"));
        }


        /**
         * Do not delete untagged parents (equates to the --no-prune argument).
         *
         * @return  a new {@link RemoveImage} instance that is the same as this
         *          instance with the --no-prune option applied
         */
        public RemoveImage noPrune()
        {
            return noPrune(true);
        }


        /**
         * Do not delete untagged parents (equates to the --no-prune argument).
         *
         * @param noPrune  true to add the <code>--no-prune</code> option or false
         *                 to remove the <code>--no-prune</code> option
         *
         * @return  a new {@link RemoveImage} instance that is the same as this
         *          instance with the --no-prune option applied
         */
        public RemoveImage noPrune(boolean noPrune)
        {
            if (noPrune)
            {
                return withCommandArguments(Argument.of("--no-prune"));
            }

            return withoutCommandArguments(Argument.of("--no-prune"));
        }


        @Override
        protected RemoveImage withCommandArguments(List<Argument> images,
                                                   Argument...    args)
        {
            return new RemoveImage(getCommandArguments().with(args), images);
        }


        @Override
        protected RemoveImage withoutCommandArguments(List<Argument> names,
                                                      Argument...    args)
        {
            return new RemoveImage(getCommandArguments().without(args), names);
        }
    }


    /**
     * A representation of a Docker command to remove a link between two
     * containers (equates to the Docker rm --link containerA/contaunerB command).
     */
    public static class RemoveLink extends Remove<RemoveLink>
    {
        private RemoveLink(List<?> names)
        {
            super("rm", names);
        }


        /**
         * Create a {@link RemoveLink} command with the specified {@link Arguments}
         * and containers to be un-linked.
         *
         * @param arguments  the command {@link Arguments}
         * @param names      the names of the containers to be unlinked
         */
        private RemoveLink(Arguments arguments,
                           List<?>   names)
        {
            super(arguments, names);
        }


        @Override
        protected RemoveLink withCommandArguments(List<Argument> containers,
                                                  Argument...    args)
        {
            return new RemoveLink(getCommandArguments().with(args), containers);
        }


        @Override
        protected RemoveLink withoutCommandArguments(List<Argument> names,
                                                     Argument...    args)
        {
            return new RemoveLink(getCommandArguments().without(args), names);
        }
    }
}
