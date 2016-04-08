/*
 * File: CoherenceNamedCache.java
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

package com.oracle.tools.runtime.coherence;

import com.oracle.tools.runtime.concurrent.callable.RemoteCallableStaticMethod;
import com.oracle.tools.runtime.concurrent.callable.RemoteMethodInvocation;

import com.oracle.tools.util.ReflectionHelper;

import com.tangosol.net.CacheService;
import com.tangosol.net.NamedCache;

import com.tangosol.util.Filter;
import com.tangosol.util.MapListener;
import com.tangosol.util.ValueExtractor;

import java.io.Serializable;

import java.lang.reflect.Method;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * A {@link NamedCache} implementation that provides a local representation and
 * thus allows interaction with a cache defined with in a {@link CoherenceClusterMember}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
class CoherenceNamedCache implements NamedCache
{
    /**
     * The {@link CoherenceClusterMember} that owns the {@link NamedCache}
     * that this {@link CoherenceNamedCache} represents.
     */
    private CoherenceClusterMember member;

    /**
     * The name of the {@link NamedCache}.
     */
    private String cacheName;

    /**
     * The {@link RemoteCallableStaticMethod} to use in the
     * {@link CoherenceClusterMember} to acquire the {@link NamedCache}.
     */
    private RemoteCallableStaticMethod<NamedCache> producer;

    /**
     * The {@link RemoteMethodInvocation.Interceptor} to use for intercepting
     * and transforming remote method invocations.
     */
    private RemoteMethodInvocation.Interceptor interceptor;


    /**
     * Constructs a {@link CoherenceNamedCache}.
     *
     * @param member     the {@link CoherenceClusterMember} that owns the {@link NamedCache}
     * @param cacheName  the name of the {@link NamedCache}
     */
    public CoherenceNamedCache(CoherenceClusterMember member,
                               String                 cacheName)
    {
        this.member    = member;
        this.cacheName = cacheName;
        this.producer = new RemoteCallableStaticMethod<NamedCache>("com.tangosol.net.CacheFactory",
                                                                   "getCache",
                                                                   cacheName);
        this.interceptor = new NamedCacheMethodInterceptor();
    }


    /**
     * Invoke the specified void method remotely in the {@link CoherenceClusterMember} on the
     * {@link NamedCache} provided by the {@link #producer}.
     *
     * @param methodName  the name of the method
     * @param arguments   the arguments for the method
     *
     * @throws RuntimeException  if any exception occurs remotely
     */
    protected void remotelyInvoke(String    methodName,
                                  Object... arguments)
    {
        // notify the interceptor that we're about make a remote invocation
        Method method = ReflectionHelper.getCompatibleMethod(NamedCache.class, methodName, arguments);

        interceptor.onBeforeRemoteInvocation(method, arguments);

        // submit the remote method invocation
        CompletableFuture future = member.submit(new RemoteMethodInvocation(producer, methodName, arguments, interceptor));

        try
        {
            // intercept the result after the remote invocation
            interceptor.onAfterRemoteInvocation(method, arguments, future.get());
        }
        catch (RuntimeException e)
        {
            // re-throw runtime exceptions
            throw e;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to execute " + methodName + " with arguments "
                                       + Arrays.toString(arguments),
                                       interceptor.onRemoteInvocationException(method, arguments, e));
        }
    }


    /**
     * Invoke the specified method remotely in the {@link CoherenceClusterMember} on the
     * {@link NamedCache} provided by the {@link #producer}.
     *
     * @param methodName  the name of the method
     * @param returnType  the expected return type from the method
     * @param arguments   the arguments for the method
     *
     * @throws RuntimeException  if any exception occurs remotely
     */
    protected <T> T remotelyInvoke(String    methodName,
                                   Class<T>  returnType,
                                   Object... arguments)
    {
        // notify the interceptor that we're about make a remote invocation
        Method method = ReflectionHelper.getCompatibleMethod(NamedCache.class, methodName, arguments);

        interceptor.onBeforeRemoteInvocation(method, arguments);

        // submit the remote method invocation
        CompletableFuture future = member.submit(new RemoteMethodInvocation(producer, methodName, arguments, interceptor));

        try
        {
            // intercept the result after the remote invocation
            return (T) interceptor.onAfterRemoteInvocation(method, arguments, future.get());
        }
        catch (RuntimeException e)
        {
            // re-throw runtime exceptions
            throw e;
        }
        catch (Exception e)
        {
            throw new RuntimeException(interceptor.onRemoteInvocationException(method, arguments, e));
        }
    }


    @Override
    public String getCacheName()
    {
        return cacheName;
    }


    @Override
    public CacheService getCacheService()
    {
        throw new UnsupportedOperationException("The method NamedCache.getCacheService is not supported for remote execution");
    }


    @Override
    public boolean isActive()
    {
        return remotelyInvoke("isActive", Boolean.class);
    }


    @Override
    public void release()
    {
        remotelyInvoke("release");
    }


    @Override
    public void destroy()
    {
        remotelyInvoke("destroy");
    }


    @Override
    public Object put(Object key,
                      Object value,
                      long   expiry)
    {
        return remotelyInvoke("put", Object.class, key, value, expiry);
    }


    @Override
    public Map getAll(Collection keys)
    {
        return remotelyInvoke("getAll", Map.class, keys);
    }


    @Override
    public boolean lock(Object key,
                        long   duration)
    {
        return remotelyInvoke("lock", Boolean.class, key, duration);
    }


    @Override
    public boolean lock(Object key)
    {
        return remotelyInvoke("lock", Boolean.class, key);
    }


    @Override
    public boolean unlock(Object key)
    {
        return remotelyInvoke("unlock", Boolean.class, key);
    }


    @Override
    public Object invoke(Object         key,
                         EntryProcessor processor)
    {
        return remotelyInvoke("invoke", Object.class, key, processor);
    }


    @Override
    public Map invokeAll(Collection     keys,
                         EntryProcessor processor)
    {
        return remotelyInvoke("invokeAll", Map.class, keys, processor);
    }


    @Override
    public Map invokeAll(Filter         filter,
                         EntryProcessor processor)
    {
        return remotelyInvoke("invokeAll", Map.class, filter, processor);
    }


    @Override
    public Object aggregate(Collection      collection,
                            EntryAggregator aggregator)
    {
        return remotelyInvoke("aggregate", Object.class, collection, aggregator);
    }


    @Override
    public Object aggregate(Filter          filter,
                            EntryAggregator aggregator)
    {
        return remotelyInvoke("aggregate", Object.class, filter, aggregator);
    }


    @Override
    public void addMapListener(MapListener listener)
    {
        throw new UnsupportedOperationException("The method NamedCache.addMapListener is not supported for remote execution");
    }


    @Override
    public void removeMapListener(MapListener listener)
    {
        throw new UnsupportedOperationException("The method NamedCache.removeMapListener is not supported for remote execution");
    }


    @Override
    public void addMapListener(MapListener listener,
                               Object      key,
                               boolean     lite)
    {
        throw new UnsupportedOperationException("The method NamedCache.addMapListener is not supported for remote execution");
    }


    @Override
    public void removeMapListener(MapListener listener,
                                  Object      key)
    {
        throw new UnsupportedOperationException("The method NamedCache.removeMapListener is not supported for remote execution");
    }


    @Override
    public void addMapListener(MapListener listener,
                               Filter      filter,
                               boolean     lite)
    {
        throw new UnsupportedOperationException("The method NamedCache.addMapListener is not supported for remote execution");
    }


    @Override
    public void removeMapListener(MapListener listener,
                                  Filter      filter)
    {
        throw new UnsupportedOperationException("The method NamedCache.removeMapListener is not supported for remote execution");
    }


    @Override
    public Set keySet(Filter filter)
    {
        return remotelyInvoke("keySet", Set.class, filter);
    }


    @Override
    public Set entrySet(Filter filter)
    {
        return remotelyInvoke("entrySet", Set.class, filter);
    }


    @Override
    public Set entrySet(Filter     filter,
                        Comparator comparator)
    {
        return remotelyInvoke("entrySet", Set.class, filter, comparator);
    }


    @Override
    public void addIndex(ValueExtractor valueExtractor,
                         boolean        ordered,
                         Comparator     comparator)
    {
        remotelyInvoke("addIndex", valueExtractor, ordered, comparator);
    }


    @Override
    public void removeIndex(ValueExtractor valueExtractor)
    {
        remotelyInvoke("removeIndex", valueExtractor);
    }


    @Override
    public int size()
    {
        return remotelyInvoke("size", Integer.class);
    }


    @Override
    public boolean isEmpty()
    {
        return remotelyInvoke("isEmpty", Boolean.class);
    }


    @Override
    public boolean containsKey(Object key)
    {
        return remotelyInvoke("containsKey", Boolean.class, key);
    }


    @Override
    public boolean containsValue(Object value)
    {
        return remotelyInvoke("containsValue", Boolean.class, value);
    }


    @Override
    public Object get(Object key)
    {
        return remotelyInvoke("get", Object.class, key);
    }


    @Override
    public Object put(Object key,
                      Object value)
    {
        return remotelyInvoke("put", Object.class, key, value);
    }


    @Override
    public Object remove(Object key)
    {
        return remotelyInvoke("remove", Object.class, key);
    }


    @Override
    public void putAll(Map map)
    {
        remotelyInvoke("putAll", map);
    }


    @Override
    public void clear()
    {
        remotelyInvoke("clear");
    }


    @Override
    public Set keySet()
    {
        return remotelyInvoke("keySet", Set.class);
    }


    @Override
    public Collection values()
    {
        return remotelyInvoke("values", Collection.class);
    }


    @Override
    public Set<Map.Entry> entrySet()
    {
        return remotelyInvoke("entrySet", Set.class);
    }


    /**
     * A Coherence specific {@link RemoteMethodInvocation.Interceptor} for {@link NamedCache} methods.
     */
    public static class NamedCacheMethodInterceptor implements RemoteMethodInvocation.Interceptor
    {
        @Override
        public void onBeforeRemoteInvocation(Method   method,
                                             Object[] arguments)
        {
            // ensure that the arguments for specific methods are serializable
            String name = method.getName();

            if ((name.equals("getAll") || name.equals("invokeAll") || name.equals("aggregate"))
                &&!(arguments[0] instanceof Serializable))
            {
                // ensure invocations of NamedCache.getAll / invokeAll / aggregate using collections are serializable
                arguments[0] = new ArrayList((Collection) arguments[0]);
            }
            else if (name.equals("putAll") &&!(arguments[0] instanceof Serializable))
            {
                arguments[0] = new HashMap((Map) arguments[0]);
            }
        }


        @Override
        public Object onAfterRemoteInvocation(Method   method,
                                              Object[] arguments,
                                              Object   result)
        {
            return result;
        }


        @Override
        public Exception onRemoteInvocationException(Method    method,
                                                     Object[]  arguments,
                                                     Exception exception)
        {
            return exception;
        }


        @Override
        public void onBeforeInvocation(Object   instance,
                                       Method   method,
                                       Object[] arguments)
        {
            // nothing to do before invocation
        }


        @Override
        public Object onAfterInvocation(Object   instance,
                                        Method   method,
                                        Object[] arguments,
                                        Object   result)
        {
            // ensure that the result of the method is serializable, including transforming it if necessary
            String name = method.getName();

            if (name.equals("invokeAll") || name.equals("getAll"))
            {
                // the result of invokeAll may not be serializable,
                // so copy them into a serializable map
                result = new HashMap((Map) result);
            }
            else if (name.equals("keySet"))
            {
                // the result of keySet may not be serializable,
                // so copy them into a serializable set
                result = new HashSet((Set) result);
            }
            else if (name.equals("entrySet"))
            {
                // the result of entrySet may not be serializable,
                // so copy the entries into a serializable set
                Set<Map.Entry> set       = (Set<Map.Entry>) result;
                Set<Map.Entry> resultSet = new HashSet();

                for (Map.Entry entry : set)
                {
                    resultSet.add(new AbstractMap.SimpleEntry(entry.getKey(), entry.getValue()));
                }

                result = resultSet;
            }
            else if (name.equals("values"))
            {
                // the result of values may not be serializable,
                // so copy them into a serializable set
                result = new ArrayList((Collection) result);
            }

            return result;
        }


        @Override
        public Exception onInvocationException(Object    instance,
                                               Method    method,
                                               Object[]  arguments,
                                               Exception exception)
        {
            return exception;
        }
    }
}
