/*
 * File: ClassLoaderAwareObjectInputStream.java
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

package com.oracle.bedrock.runtime.java.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * A {@link ClassLoaderAwareObjectInputStream} is a specialized {@link ObjectInputStream}
 * that attempts to resolve deserialized {@link Class}es using a specific {@link ClassLoader}
 * and should that fail it uses the regular {@link ObjectInputStream} serializer.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class ClassLoaderAwareObjectInputStream extends ObjectInputStream
{
    /**
     * The {@link ClassLoader} to use for resolving deserialized classes.
     */
    protected ClassLoader classLoader;


    /**
     * Construct a {@link ClassLoaderAwareObjectInputStream} that allows
     * resolving classes using a specific {@link ClassLoader}.
     *
     * @param classLoader  the {@link ClassLoader} to use when resolving classes
     * @param inputStream  the {@link InputStream} to adapt into an {@link ObjectInputStream}
     *
     * @throws IOException if the {@link ObjectInputStream} could not be established
     */
    public ClassLoaderAwareObjectInputStream(ClassLoader classLoader,
                                             InputStream inputStream) throws IOException
    {
        super(inputStream);

        this.classLoader = classLoader;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?> resolveClass(ObjectStreamClass objectStreamClass) throws IOException, ClassNotFoundException
    {
        try
        {
            return classLoader.loadClass(objectStreamClass.getName());
        }
        catch (Exception e)
        {
            return super.resolveClass(objectStreamClass);
        }
    }
}
