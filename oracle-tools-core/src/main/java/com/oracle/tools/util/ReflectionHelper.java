/*
 * File: ReflectionHelper.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of 
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting
 * or https://oss.oracle.com/licenses/CDDL
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

package com.oracle.tools.util;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A collection of utilities to assist in using Reflection to create objects.
 * <p>
 * Copyright (c) 2010. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Christer Fahlgren
 * @author Brian Oliver
 */
public class ReflectionHelper
{
    /**
     * Get a compatible constructor to the supplied parameter types.
     *
     * @param clazz the class which we want to construct
     * @param parameterTypes the types required of the constructor
     *
     * @return a compatible constructor or null if none exists
     */
    public static Constructor<?> getCompatibleConstructor(Class<?> clazz,
                                                          Class<?>[] parameterTypes)
    {
        Constructor<?>[] constructors = clazz.getConstructors();

        for (int i = 0; i < constructors.length; i++)
        {
            if (constructors[i].getParameterTypes().length == (parameterTypes != null ? parameterTypes.length : 0))
            {
                // If we have the same number of parameters there is a shot that we have a compatible
                // constructor
                Class<?>[] constructorTypes = constructors[i].getParameterTypes();
                boolean    isCompatible     = true;

                for (int j = 0; j < (parameterTypes != null ? parameterTypes.length : 0); j++)
                {
                    if (!constructorTypes[j].isAssignableFrom(parameterTypes[j]))
                    {
                        // The type is not assignment compatible, however
                        // we might be able to coerce from a basic type to a boxed type
                        if (constructorTypes[j].isPrimitive())
                        {
                            if (!isAssignablePrimitive(constructorTypes[j], parameterTypes[j]))
                            {
                                isCompatible = false;
                                break;
                            }
                        }
                    }
                }

                if (isCompatible)
                {
                    return constructors[i];
                }
            }
        }

        return null;
    }


    /**
     * Determines if a primitive type is assignable to a wrapper type.
     *
     * @param clzPrimitive  a primitive class type
     * @param clzWrapper    a wrapper class type
     *
     * @return true if primitive and wrapper are assignment compatible
     */
    public static boolean isAssignablePrimitive(Class<?> clzPrimitive,
                                                Class<?> clzWrapper)
    {
        return (clzPrimitive.equals(java.lang.Boolean.TYPE) && clzWrapper.equals(java.lang.Boolean.class))
               || (clzPrimitive.equals(java.lang.Byte.TYPE) && clzWrapper.equals(java.lang.Byte.class))
               || (clzPrimitive.equals(java.lang.Character.TYPE) && clzWrapper.equals(java.lang.Character.class))
               || (clzPrimitive.equals(java.lang.Double.TYPE) && clzWrapper.equals(java.lang.Double.class))
               || (clzPrimitive.equals(java.lang.Float.TYPE) && clzWrapper.equals(java.lang.Float.class))
               || (clzPrimitive.equals(java.lang.Integer.TYPE) && clzWrapper.equals(java.lang.Integer.class))
               || (clzPrimitive.equals(java.lang.Long.TYPE) && clzWrapper.equals(java.lang.Long.class))
               || (clzPrimitive.equals(java.lang.Short.TYPE) && clzWrapper.equals(java.lang.Short.class));
    }


    /**
     * Obtains the {@link Method} that is compatible to the supplied parameter types.
     *
     * @param clazz      the {@link Class} on which to find the {@link Method}
     * @param arguments  the arguments for the {@link Method}
     *
     * @return a compatible {@link Method} or <code>null</code> if one can't be found
     */
    public static Method getCompatibleMethod(Class<?> clazz,
                                             String methodName,
                                             Object... arguments)
    {
        // determine the types of the arguments
        Class<?>[] argumentTypes = new Class<?>[arguments.length];

        for (int i = 0; i < arguments.length; i++)
        {
            argumentTypes[i] = arguments[i] == null ? null : arguments[i].getClass();
        }

        try
        {
            // attempt to find the method on the specified class
            // (this may fail, in which case we should try super classes)
            return clazz.getDeclaredMethod(methodName, argumentTypes);
        }
        catch (SecurityException e)
        {
            return null;
        }
        catch (NoSuchMethodException e)
        {
            return clazz.getSuperclass() == null ? null : getCompatibleMethod(clazz.getSuperclass(),
                                                                              methodName,
                                                                              arguments);
        }
    }


    /**
     * Create an Object via reflection (using the specified {@link ClassLoader}).
     *
     * @param className The name of the class to instantiate.
     * @param classLoader The {@link ClassLoader} to use to load the class.
     *
     * @return A new instance of the class specified by the className
     *
     * @throws ClassNotFoundException if the class is not found
     * @throws NoSuchMethodException if there is no such constructor
     * @throws InstantiationException if it failed to instantiate
     * @throws IllegalAccessException if security doesn't allow the call
     * @throws InvocationTargetException if the constructor failed
     */
    public static Object createObject(String className,
                                      ClassLoader classLoader)
                                          throws ClassNotFoundException, NoSuchMethodException, InstantiationException,
                                                 IllegalAccessException, InvocationTargetException
    {
        Class<?>       clazz = Class.forName(className, true, classLoader);
        Constructor<?> con   = clazz.getDeclaredConstructor((Class[]) null);

        return con.newInstance((Object[]) null);
    }


    /**
     * Create an Object via reflection (using the specified {@link ClassLoader}).
     *
     * @param className The name of the class to instantiate.
     * @param classLoader The {@link ClassLoader} to use to load the class.
     * @param constructorParameterList The set of parameters to pass to the constructor
     *
     * @return A new instance of the class specified by the className
     *
     * @throws ClassNotFoundException if the class is not found
     * @throws NoSuchMethodException if there is no such constructor
     * @throws InstantiationException if it failed to instantiate
     * @throws IllegalAccessException if security doesn't allow the call
     * @throws InvocationTargetException if the constructor failed
     */
    public static Object createObject(String className,
                                      ClassLoader classLoader,
                                      Object... constructorParameterList)
                                          throws ClassNotFoundException, NoSuchMethodException, InstantiationException,
                                                 IllegalAccessException, InvocationTargetException
    {
        Class<?>       clazz          = Class.forName(className, true, classLoader);
        Class<?>[]     parameterTypes = getClassArrayFromObjectArray(constructorParameterList);
        Constructor<?> con            = ReflectionHelper.getCompatibleConstructor(clazz, parameterTypes);

        return con.newInstance(constructorParameterList);
    }


    /**
     * Returns an array of Class objects representing the class of the objects in the parameter.
     *
     * @param objectArray the array of Objects
     *
     * @return an array of Classes representing the class of the Objects
     */
    protected static Class<?>[] getClassArrayFromObjectArray(Object[] objectArray)
    {
        Class<?>[] parameterTypes = null;

        if (objectArray != null)
        {
            parameterTypes = new Class[objectArray.length];

            for (int i = 0; i < objectArray.length; i++)
            {
                parameterTypes[i] = objectArray[i].getClass();
            }
        }

        return parameterTypes;
    }


    /**
     * Creates a dynamic proxy of the specified {@link Object} routing all
     * method calls to the specified {@link MethodInterceptor}.
     *
     * @param object       the {@link Object} to proxy
     * @param interceptor  the {@link MethodInterceptor}
     *
     * @return a dynamic proxy of the specified {@link Object}
     */
    @SuppressWarnings("unchecked")
    public static <T> T createProxyOf(T object,
                                      MethodInterceptor interceptor)
    {
        return (T) createProxyOf(object.getClass(), interceptor);
    }


    /**
     * Creates a dynamic proxy of the specified {@link Class} routing all
     * method calls to the specified {@link MethodInterceptor}.
     *
     * @param clazz        the {@link Class} to proxy
     * @param interceptor  the {@link MethodInterceptor}
     *
     * @return a dynamic proxy of the specified {@link Class}
     */
    @SuppressWarnings("unchecked")
    public static <T> T createProxyOf(Class<T> clazz,
                                      MethodInterceptor interceptor)
    {
        // use a cglib enhancer to create the proxy
        Enhancer enhancer = new Enhancer();

        enhancer.setSuperclass(clazz);
        enhancer.setCallbackType(interceptor.getClass());

        Class<T> proxiedClass = (Class<T>) enhancer.createClass();

        Enhancer.registerCallbacks(proxiedClass, new Callback[] {interceptor});

        Objenesis objenesis = new ObjenesisStd();

        T         proxy     = (T) objenesis.newInstance(proxiedClass);

        return proxy;
    }
}
