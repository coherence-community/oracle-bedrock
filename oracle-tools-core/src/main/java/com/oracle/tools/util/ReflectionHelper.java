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

package com.oracle.tools.util;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.lang.annotation.Annotation;

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
     * @param clazz          the class which we want to construct
     * @param argumentTypes  the types required of the constructor
     *
     * @return a compatible constructor or null if none exists
     */
    public static <T> Constructor<T> getCompatibleConstructor(Class<T>    clazz,
                                                              Class<?>... argumentTypes)
    {
        // first attempt to find a constructor with the exact types
        try
        {
            return clazz.getConstructor(argumentTypes);
        }
        catch (NoSuchMethodException e)
        {
        }

        // try to find a constructor with compatible types
        for (Constructor constructor : clazz.getConstructors())
        {
            Class<?>[] formalParameterTypes = constructor.getParameterTypes();

            if (argumentTypes.length == formalParameterTypes.length)
            {
                boolean parametersMatch = true;

                // check compatibility of each formal and actual parameter
                for (int i = 0; i < argumentTypes.length && parametersMatch; i++)
                {
                    Class<?> argumentType        = argumentTypes[i];
                    Class<?> formalParameterType = formalParameterTypes[i];

                    if (argumentType == null)
                    {
                        // a null argument cannot be assigned to a primitive parameter
                        parametersMatch = !formalParameterType.isPrimitive();
                    }
                    else if (argumentType.isPrimitive())
                    {
                        parametersMatch = isAssignablePrimitive(argumentType, formalParameterType);
                    }
                    else
                    {
                        parametersMatch = formalParameterType.isAssignableFrom(argumentType);
                    }
                }

                if (parametersMatch)
                {
                    return constructor;
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
     * @param clazz            the {@link Class} on which to find the {@link Method}
     * @param actualArguments  the actual arguments for the {@link Method}
     *
     * @return a compatible {@link Method} or <code>null</code> if one can't be found
     */
    public static Method getCompatibleMethod(Class<?>  clazz,
                                             String    methodName,
                                             Object... actualArguments)
    {
        // determine the types of the arguments
        Class<?>[] argumentTypes = new Class<?>[actualArguments.length];

        for (int i = 0; i < actualArguments.length; i++)
        {
            argumentTypes[i] = actualArguments[i] == null ? null : actualArguments[i].getClass();
        }

        // first attempt to find a method with the exact types
        try
        {
            return clazz.getMethod(methodName, argumentTypes);
        }
        catch (NoSuchMethodException e)
        {
        }

        // try to find a method with compatible types
        for (Method method : clazz.getMethods())
        {
            if (methodName.equals(method.getName()))
            {
                Class<?>[] formalParameterTypes = method.getParameterTypes();

                if (argumentTypes.length == formalParameterTypes.length)
                {
                    boolean parametersMatch = true;

                    // check compatibility of each formal and actual parameter
                    for (int i = 0; i < argumentTypes.length && parametersMatch; i++)
                    {
                        Class<?> argumentType        = argumentTypes[i];
                        Class<?> formalParameterType = formalParameterTypes[i];

                        if (argumentType == null)
                        {
                            // a null argument cannot be assigned to a primitive parameter
                            parametersMatch = !formalParameterType.isPrimitive();
                        }
                        else if (argumentType.isPrimitive())
                        {
                            parametersMatch = isAssignablePrimitive(argumentType, formalParameterType);
                        }
                        else if (formalParameterType.isPrimitive())
                        {
                            parametersMatch = isAssignablePrimitive(formalParameterType, argumentType);
                        }
                        else
                        {
                            parametersMatch = formalParameterType.isAssignableFrom(argumentType);
                        }
                    }

                    if (parametersMatch)
                    {
                        return method;
                    }
                }
            }
        }

        return null;
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
    public static Object createObject(String      className,
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
    public static Object createObject(String      className,
                                      ClassLoader classLoader,
                                      Object...   constructorParameterList)
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
    public static <T> T createProxyOf(T                 object,
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
    public static <T> T createProxyOf(Class<T>          clazz,
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


    /**
     * Obtains the specified {@link Annotation} instance on the provided {@link Class},
     * interfaces implemented by the {@link Class} and/or the super-class(es)/super-interface(s)
     * of the {@link Class}.
     * <p>
     * Should the {@link Annotation} not be defined on the above, <code>null</code> is returned.
     *
     * @param onClass                 the {@link Class} on which seaching will take place
     * @param desiredAnnotationClass  the desired {@link Annotation} {@link Class}
     * @param <A>                     the type of the {@link Annotation}
     *
     * @return  the {@link Annotation} or <code>null</code> if not available
     */
    public static <A extends Annotation> A getAnnotation(Class<?> onClass,
                                                         Class<A> desiredAnnotationClass)
    {
        if (onClass == null || desiredAnnotationClass == null)
        {
            // when no class or annotation has been provided, we can't locate the desired annotation
            return null;
        }
        else if (onClass.getClass().equals(Object.class))
        {
            // we assume that the object class doesn't have the annotation we want
            return null;
        }
        else
        {
            // first check if the annotation is defined directly on the class
            A annotation = onClass.getAnnotation(desiredAnnotationClass);

            if (annotation == null)
            {
                // look for the annotation on all of the interfaces
                for (Class<?> onInterface : onClass.getInterfaces())
                {
                    annotation = getAnnotation(onInterface, desiredAnnotationClass);

                    if (annotation != null)
                    {
                        return annotation;
                    }
                }

                // finally look on the super class
                return getAnnotation(onClass.getSuperclass(), desiredAnnotationClass);
            }
            else
            {
                return annotation;
            }
        }
    }
}
