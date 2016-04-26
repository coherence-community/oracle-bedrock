/*
 * File: ImageCloseBehaviour.java
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
import com.oracle.tools.runtime.containers.docker.DockerImage;

import java.util.function.Consumer;

/**
 * An {@link Enum} and {@link Option} representing the types of behaviour
 * to occur when closing a {@link DockerImage}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class ImageCloseBehaviour implements Option, Consumer<DockerImage>
{
    /**
     * The {link {@link Consumer} to call when this {@link ImageCloseBehaviour}
     * is applied to a {@link DockerImage}
     */
    private Consumer<DockerImage> action;


    /**
     * Create an {@link ImageCloseBehaviour} that will perform
     * the specified action on a {@link DockerImage}.
     *
     * @param action  the action to perform
     */
    private ImageCloseBehaviour(Consumer<DockerImage> action)
    {
        this.action = action;
    }


    /**
     * Perform this {@link ImageCloseBehaviour}'s action on the
     * specified {@link DockerImage}.
     *
     * @param image  the {@link DockerImage} to have the action performed on
     */
    public void accept(DockerImage image)
    {
        action.accept(image);
    }


    /**
     * Do nothing when an image application is closed.
     */
    public static ImageCloseBehaviour none()
    {
        return new ImageCloseBehaviour((image) -> {});
    }


    /**
     * Remove the Docker image when an image application is closed.
     */
    @Options.Default
    public static ImageCloseBehaviour remove()
    {
        return new ImageCloseBehaviour(DockerImage::remove);
    }
}
