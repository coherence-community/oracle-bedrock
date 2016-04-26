/*
 * File: ContainerCloseBehaviour.java
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

package com.oracle.tools.runtime.containers.docker.options;

import com.oracle.tools.Option;
import com.oracle.tools.Options;
import com.oracle.tools.runtime.containers.docker.DockerContainer;

import java.util.function.Consumer;

/**
 * An {@link Enum} and {@link Option} representing the types of behaviour
 * to occur when closing a {@link DockerContainer}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class ContainerCloseBehaviour implements Option, Consumer<DockerContainer>
{
    /**
     * The {link {@link Consumer} to call when this {@link ContainerCloseBehaviour}
     * is applied to a {@link DockerContainer}
     */
    private Consumer<DockerContainer> action;


    /**
     * Create an {@link ContainerCloseBehaviour} that will perform
     * the specified action on a {@link DockerContainer}.
     *
     * @param action  the action to perform
     */
    private ContainerCloseBehaviour(Consumer<DockerContainer> action)
    {
        this.action = action;
    }


    /**
     * Perform this {@link ImageCloseBehaviour}'s action on the
     * specified {@link DockerContainer}.
     *
     * @param container  the {@link DockerContainer} to have the action performed on
     */
    public void accept(DockerContainer container)
    {
        action.accept(container);
    }


    /**
     * Do nothing when a container application is closed.
     */
    public static ContainerCloseBehaviour none()
    {
        return new ContainerCloseBehaviour((container) -> {});
    }


    /**
     * Stop the Docker container when a container application is closed.
     */
    public static ContainerCloseBehaviour stop()
    {
        return new ContainerCloseBehaviour(DockerContainer::stop);
    }


    /**
     * Stop and remove the Docker container when a container application is closed.
     */
    @Options.Default
    public static ContainerCloseBehaviour remove()
    {
        return new ContainerCloseBehaviour(((container) -> {
            container.stop();
            container.remove(true);
        }));
    }
}
