/*
 * File: DockerDefaultBaseImages.java
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

package com.oracle.tools.runtime.docker.options;

import com.oracle.tools.Option;
import com.oracle.tools.runtime.Application;

import java.util.HashMap;
import java.util.Map;

/**
 * A representation of a class hierarchy that maps {@link Application}
 * {@link Class}es to Docker base images.
 * <p>
 * <strong>Note:</strong> instances of DockerDefaultBaseImages are immutable
 * so calls to the {@linkplain #with(Class, String)} method return a new instance
 * of {@link DockerDefaultBaseImages} that is a copy of the original instance
 * with the addition applied.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class DockerDefaultBaseImages implements Option
{
    /**
     * The application {@link Class} that is the root of this {@link DockerDefaultBaseImages}.
     */
    private final Class<? extends Application> applicationClass;

    /**
     * The image to use for application classes that are equal to this
     * {@link DockerDefaultBaseImages#applicationClass} or are sub-classes of this
     * {@link DockerDefaultBaseImages#applicationClass} but do not match any of the
     * classes in the {@link DockerDefaultBaseImages#subClassBaseImages} list.
     */
    private final String baseImageName;


    /**
     * The list of base images for sub-classes of this {@link DockerDefaultBaseImages#applicationClass}.
     */
    private final Map<Class<? extends Application>, DockerDefaultBaseImages> subClassBaseImages;


    /**
     * Create a {@link DockerDefaultBaseImages} with the specified root application class
     * and base image name to be used for that class.
     *
     * @param applicationClass  the application class
     * @param baseImageName     the base image to use for classes
     */
    public DockerDefaultBaseImages(Class<? extends Application> applicationClass, String baseImageName)
    {
        this(applicationClass, baseImageName, new HashMap<>());
    }


    /**
     * A copy constructor to create a {@link DockerDefaultBaseImages} tree that is
     * a copy of another tree.
     *
     * @param toCopy  the {@link DockerDefaultBaseImages} tree to copy
     */
    private DockerDefaultBaseImages(DockerDefaultBaseImages toCopy)
    {
        this(toCopy.applicationClass, toCopy.baseImageName, new HashMap<>(toCopy.subClassBaseImages));
    }


    /**
     * Create a {@link DockerDefaultBaseImages} with the specified root application class
     * and base image name to be used for that class.
     *
     * @param applicationClass  the application class
     * @param baseImageName     the base image to use for classes
     */
    private DockerDefaultBaseImages(Class<? extends Application>                               applicationClass,
                                   String                                                     baseImageName,
                                   Map<Class<? extends Application>, DockerDefaultBaseImages> subClassBaseImages)
    {
        if (applicationClass == null)
        {
            throw new IllegalArgumentException("The application Class cannot be null");
        }

        if (baseImageName == null || baseImageName.trim().isEmpty())
        {
            throw new IllegalArgumentException("The base image name cannot be null or blank");
        }

        this.applicationClass   = applicationClass;
        this.baseImageName      = baseImageName.trim();
        this.subClassBaseImages = subClassBaseImages;
    }


    /**
     * Obtain the root application {@link Class} for this {@link DockerDefaultBaseImages}.
     *
     * @return  the root application {@link Class} for this {@link DockerDefaultBaseImages}
     */
    public Class<? extends Application> getApplicationClass()
    {
        return applicationClass;
    }


    /**
     * Add a {@link DockerDefaultBaseImages} to this tree.
     * <p>
     * The base image being added should have a root class that is
     * a sub-class of this {@link DockerDefaultBaseImages}'s root class.
     *
     * @param applicationClass  the application class
     * @param baseImageName     the base image to use for classes
     */
    public synchronized DockerDefaultBaseImages with(Class<? extends Application> applicationClass, String baseImageName)
    {
        // We do not add a null tree or add ourselves
        if (applicationClass == null)
        {
            throw new IllegalArgumentException("Cannot add a null application Class");
        }

        if (baseImageName == null || baseImageName.trim().isEmpty())
        {
            throw new IllegalArgumentException("The base image name cannot be null or blank");
        }

        DockerDefaultBaseImages newInstance;

        if (this.applicationClass.equals(applicationClass))
        {
            // the application class is the same as this tree so
            // create the new instance that is a copy of ourselves
            // with the new base image name
            newInstance = new DockerDefaultBaseImages(this.applicationClass,
                                                      baseImageName,
                                                      new HashMap<>(this.subClassBaseImages));
        }
        else
        {
            if (applicationClass.isAssignableFrom(this.applicationClass))
            {
                throw new IllegalArgumentException("Cannot add super class"
                                                   + applicationClass
                                                   + " to this tree class "
                                                   + this.applicationClass);
            }

            // the application class must be a sub-class of this tree

            // Do we already have the class or a super-class of it in our sub-class map
            DockerDefaultBaseImages parent = subClassBaseImages.values().stream()
                    .filter((value) -> value.applicationClass.isAssignableFrom(applicationClass))
                    .findFirst()
                    .orElse(null);

            if (parent != null)
            {
                // yes we have a parent for the class
                DockerDefaultBaseImages newParent = parent.with(applicationClass, baseImageName);

                // create the new instance as a copy of ourselves
                newInstance = new DockerDefaultBaseImages(this);

                // add the new parent
                newInstance.subClassBaseImages.put(newParent.applicationClass, newParent);
            }
            else
            {
                // no we do not have the application class or its super-class in our sub-class map
                // so we create a new tree that is a copy of ourselves then add the application class
                // and base image name to the new instances sub-class map

                newInstance = new DockerDefaultBaseImages(this);

                newInstance.subClassBaseImages.put(applicationClass,
                                                   new DockerDefaultBaseImages(applicationClass, baseImageName));
            }
        }

        return newInstance;
    }


    /**
     * Obtain the base image to use for the given {@link Class}.
     *
     * @param applicationClass  the {@link Class} to find the base image for
     *
     * @return  the base image to use for the specified class or null if
     *          the specified class is not the same as or a sub class of the
     *          root class of this tree
     */
    public String getBaseImage(Class<? extends Application> applicationClass)
    {
        if (this.applicationClass.equals(applicationClass))
        {
            return this.baseImageName;
        }
        else if (this.applicationClass.isAssignableFrom(applicationClass))
        {
            return this.subClassBaseImages.values().stream()
                            .map((baseImage) -> baseImage.getBaseImage(applicationClass))
                            .filter((imageName) -> imageName != null)
                            .findFirst()
                            .orElseGet(() -> this.baseImageName);
        }

        return null;
    }


    /**
     * Obtain the default {@link DockerDefaultBaseImages} that is used to determine
     * the base images for application classes if no specific overrides have been
     * specified.
     *
     * @return  the default {@link DockerDefaultBaseImages}
     */
    public static DockerDefaultBaseImages defaultImages()
    {
        // By default we use the open source Oracle Linux 7.1 image that is
        // freely available on Docker Hub.
        return new DockerDefaultBaseImages(Application.class, "oraclelinux:7.1");
    }
}
