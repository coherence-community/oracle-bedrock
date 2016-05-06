/*
 * File: ProxyHelper.java
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

package com.oracle.bedrock.util;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

/**
 * A collection of utilities to assist in using Objenesis to create object proxies.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class ProxyHelper
{
    /**
     * Creates a dynamic proxy of the specified {@link Object} routing all
     * method calls to the specified {@link MethodInterceptor}.
     *
     * @param object       the {@link Object} to proxy
     * @param interceptor  the {@link MethodInterceptor}
     *
     * @param <T>          the type of the {@link Object} to proxy
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
     * @param <T>          the type of the {@link Class} to proxy
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
}
