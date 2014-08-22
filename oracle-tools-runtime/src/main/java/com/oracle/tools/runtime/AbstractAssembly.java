/*
 * File: AbstractAssembly.java
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

package com.oracle.tools.runtime;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import java.util.concurrent.CopyOnWriteArraySet;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A base implementation of an {@link Assembly}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @param <A>  the type of {@link Application} that belongs to the {@link Assembly}.
 */
public abstract class AbstractAssembly<A extends Application> implements Assembly<A>
{
    /**
     * The {@link Application}s that belong to the {@link Assembly}.
     */
    protected CopyOnWriteArraySet<A> applications;

    /**
     * Is the {@link Assembly} closed?
     */
    protected AtomicBoolean isClosed;


    /**
     * Constructs an {@link AbstractAssembly} given a list of {@link Application}s.
     *
     * @param applications  the {@link Application}s in the {@link Assembly}.
     */
    public AbstractAssembly(List<? extends A> applications)
    {
        this.applications = new CopyOnWriteArraySet<A>(applications);
        this.isClosed     = new AtomicBoolean(false);
    }


    /**
     * Determine if the {@link Assembly} is closed.
     *
     * @return  <code>true</code> if the {@link Assembly} is closed
     */
    public boolean isClosed()
    {
        return isClosed.get();
    }


    /**
     * Obtains an {@link Application} in this group with the given name.  If
     * no such {@link Application} exists in the group, <code>null</code> will
     * be returned.  If multiple {@link Application}s in the group have the
     * given name, an arbitary {@link Application} of the name will be returned
     *
     * @param name  the name of the application to get
     * @return the application in this group with the given name or null
     *         if no application has been realized with the given name
     */
    public A getApplication(String name)
    {
        for (A application : applications)
        {
            if (application.getName().equals(name))
            {
                return application;
            }
        }

        return null;
    }


    /**
     * Obtains the {@link Application}s in this group where by their
     * {@link Application#getName()} starts with the specified prefix.
     *
     * @param prefix  the prefix of application names to return
     * @return the application in this group with the given name or null
     *         if no application has been realized with the given name
     */
    public Iterable<A> getApplications(String prefix)
    {
        LinkedList<A> list = new LinkedList<A>();

        for (A application : applications)
        {
            if (application.getName().startsWith(prefix))
            {
                list.add(application);
            }
        }

        return list;
    }


    /**
     * Adds the specified {@link Application} to the {@link Assembly}.
     *
     * @param application  the {@link Application} to add
     *
     * @throws IllegalStateException  when the {@link Assembly} {@link #isClosed}
     */
    public void addApplication(A application)
    {
        if (isClosed())
        {
            throw new IllegalStateException("Can't add [" + application + "] as the " + this.getClass().getName()
                                            + " is closed");
        }
        else
        {
            applications.add(application);
        }

    }


    /**
     * Removes the specified {@link Application} to the {@link Assembly}.
     *
     * @param application  the {@link Application} to remove
     *
     * @return <code>true</code> if the specified {@link Application} was removed
     *         <code>false</code> otherwise
     *
     * @throws IllegalStateException  when the {@link Assembly} {@link #isClosed}
     */
    public boolean removeApplication(A application)
    {
        if (isClosed())
        {
            throw new IllegalStateException("Can't add [" + application + "] as the " + this.getClass().getName()
                                            + " is closed");
        }
        else
        {
            return applications.remove(application);
        }
    }


    @Override
    public Iterator<A> iterator()
    {
        return applications.iterator();
    }


    @Override
    public void close()
    {
        if (isClosed.compareAndSet(false, true))
        {
            for (A application : applications)
            {
                if (application != null)
                {
                    try
                    {
                        application.close();
                    }
                    catch (Exception e)
                    {
                        // skip: we always ignore
                    }
                }
            }

            // now remove the applications
            applications.clear();
        }
    }


    @Override
    public int size()
    {
        return applications.size();
    }


    @Deprecated
    public void destroy()
    {
        close();
    }
}
