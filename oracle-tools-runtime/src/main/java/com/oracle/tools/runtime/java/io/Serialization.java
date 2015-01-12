/*
 * File: Serialization.java
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

package com.oracle.tools.runtime.java.io;

import com.oracle.tools.runtime.java.container.Container;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;

/**
 * Helpers to aid in the serialization and deserialization of Objects.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Serialization
{
    /**
     * Serializes the specified {@link Object} into a byte array
     * (using Java Serialization).
     *
     * @param object  the {@link Object} to serialize
     *
     * @return  a byte array
     * @throws IOException  should serialization fail
     */
    public static byte[] toByteArray(Object object) throws IOException
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream    objectOutputStream    = new ObjectOutputStream(byteArrayOutputStream);

        // assume the object isn't serializable
        boolean isSerializable = false;

        try
        {
            objectOutputStream.writeBoolean(true);
            objectOutputStream.writeObject(object);
            isSerializable = true;
        }
        catch (IOException e)
        {
            // reconstruct the stream as the object wasn't serializable
            byteArrayOutputStream = new ByteArrayOutputStream();
            objectOutputStream    = new ObjectOutputStream(byteArrayOutputStream);
        }

        if (!isSerializable)
        {
            objectOutputStream.writeBoolean(false);
            objectOutputStream.writeObject(object.getClass().getName());
        }

        objectOutputStream.flush();
        objectOutputStream.close();

        return byteArrayOutputStream.toByteArray();
    }


    /**
     * Deserializes an {@link Object} from a byte array representation
     * (using Java Serialization).
     *
     * @param bytes        the byte array
     * @param clazz        the expected type of the object
     * @param classLoader  the {@link ClassLoader} to use for deserialization
     *
     * @return  an {@link Object}
     * @throws IOException  should deserialization fail
     */
    public static <T> T fromByteArray(byte[]      bytes,
                                      Class<T>    clazz,
                                      ClassLoader classLoader) throws IOException
    {
        PrintStream          stdout               = Container.getPlatformScope().getStandardOutput();

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ClassLoaderAwareObjectInputStream(classLoader, byteArrayInputStream);

        try
        {
            boolean isSerializable = objectInputStream.readBoolean();

            Object  object;

            if (isSerializable)
            {
                object = objectInputStream.readObject();
            }
            else
            {
                String clazzName = (String) objectInputStream.readObject();

                object = classLoader.loadClass(clazzName).newInstance();
            }

            if (object == null || clazz.isInstance(object))
            {
                return clazz.cast(object);
            }
            else
            {
                throw new ClassCastException("Expected " + clazz.getName() + ", Found " + object.getClass().getName()
                                             + "{" + object + "}");
            }
        }
        catch (ClassCastException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new IOException("Failed to read underyling exception", e);
        }
        finally
        {
            objectInputStream.close();
        }
    }
}
