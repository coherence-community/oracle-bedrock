/*
 * File: RemoteEventChannel.java
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

package com.oracle.tools.runtime.concurrent;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Represents a channel to send and receive {@link RemoteEvent}s.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public interface RemoteEventChannel
{
    /**
     * A consumer of {@link RemoteEvent}s that will forward any
     * events received to registered {@link RemoteEventListener}s.
     */
    interface Consumer
    {
        /**
         * Register a {@link RemoteEventListener} to
         * receive any {@link RemoteEvent}s that are consumed.
         *
         * @param listener  the {@link RemoteEventListener} to register
         */
        void addEventListener(RemoteEventListener listener);

        /**
         * De-register the specified {@link RemoteEventListener} so
         * that is no longer receives any {@link RemoteEvent}s.
         *
         * @param listener  the {@link RemoteEventListener} to de-register
         */
        void removeEventListener(RemoteEventListener listener);
    }

    /**
     * A publisher that can publish {@link RemoteEvent}s.
     */
    interface Publisher
    {
        /**
         * Publish the specified {@link RemoteEvent}.
         *
         * @param event  the {@link RemoteEvent} to publish
         */
        void fireEvent(RemoteEvent event);
    }


    /**
     * Defines how a {@link Publisher} may be injected into a {@link Class}
     * that requires a reference to a {@link Publisher} in order to send
     * {@link RemoteEvent}s. The class is typically the main class that
     * runs as an application.
     * <p>
     * If this annotation is used on fields the fields should be public
     * static and of type {@link Publisher}.
     * <p>
     * If this annotation is used on methods the methods should be public
     * static and have a single parameter of type {@link Publisher}.
     * <p>
     *
     * The {@link InjectPublisher} annotation can be used to specify a
     * public static field to inject a {@link Publisher} reference into.
     * <pre><code>
     * public class EventApp {
     *     ...
     *     &#64;RemoteEventChannel.InjectPublisher
     *     public static RemoteEventChannel.Publisher PUBLISHER;
     *     ...
     * }
     * </code></pre>
     *
     * Alternatively, the {@link InjectPublisher} annotation can be used to specify that
     * the public static method is called for injecting the value.
     * <pre><code>
     * public class EventApp {
     *     ...
     *     &#64;RemoteEventChannel.InjectPublisher
     *     public static void setPublisher(RemoteEventChannel.Publisher publisher) {
     *         ...
     *     }
     *     ...
     * }
     * </code></pre>
     *
     * @see Injector#injectPublisher(Class, Publisher)
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD})
    @interface InjectPublisher
    {
    }

    /**
     * An abstract class with a single method to inject a {@link Publisher}
     * into other classes.
     */
    abstract class Injector
    {
        /**
         * Inject the specified {@link Publisher} into static fields or static methods of
         * the specified {@link Class}.
         *
         * @param targetClass  the {@link Class} to have the {@link Publisher} injected
         * @param publisher    the {@link Publisher} to inject
         */
        public static void injectPublisher(Class<?> targetClass, RemoteEventChannel.Publisher publisher)
        {
            if (publisher == null)
            {
                return;
            }

            try
            {
                ClassLoader loader     = targetClass.getClassLoader();
                Class<Annotation> annotation = (Class<Annotation>) loader.loadClass(InjectPublisher.class.getName());
                Class<?>          publisherClass = publisher.getClass();


                for (Method method : targetClass.getMethods())
                {
                    int modifiers = method.getModifiers();

                    if (method.getAnnotation(annotation) != null
                        && method.getParameterTypes().length == 1
                        && method.getParameterTypes()[0].isAssignableFrom(publisherClass)
                        && Modifier.isStatic(modifiers)
                        && Modifier.isPublic(modifiers))
                    {
                        try
                        {
                            method.invoke(null, publisher);
                        }
                        catch (Exception e)
                        {
                            // carry on... perhaps we can use another approach?
                        }
                    }
                }


                for (Field field : targetClass.getFields())
                {
                    int modifiers = field.getModifiers();

                    if (field.getAnnotation(annotation) != null
                        && Modifier.isStatic(modifiers)
                        && Modifier.isPublic(modifiers)
                        && field.getType().isAssignableFrom(publisherClass))
                    {
                        try
                        {
                            field.set(null, publisher);
                        }
                        catch (Exception e)
                        {
                            // carry on... perhaps we can use another approach?
                        }
                    }
                }
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }
        }
    }
}
