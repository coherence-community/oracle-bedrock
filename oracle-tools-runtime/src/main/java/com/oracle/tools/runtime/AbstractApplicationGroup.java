/*
 * File: AbstractApplicationGroup.java
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An {@link AbstractApplicationGroup} is a base implementation of an {@link ApplicationGroup}.
 *
 * @param <A> The type of {@link Application} that belongs to the {@link ApplicationGroup}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public abstract class AbstractApplicationGroup<A extends Application<A>> implements ApplicationGroup<A>
{
    /**
     * The {@link Application}s that belong to the {@link ApplicationGroup}.
     */
    protected CopyOnWriteArraySet<A> applications;

    /**
     * Is the {@link ApplicationGroup} closed?
     */
    protected AtomicBoolean isClosed;


    /**
     * Constructs an {@link AbstractApplicationGroup} given a list of {@link Application}s.
     *
     * @param applications  the {@link Application}s in the {@link ApplicationGroup}.
     */
    public AbstractApplicationGroup(List<A> applications)
    {
        this.applications = new CopyOnWriteArraySet<A>(applications);
        this.isClosed     = new AtomicBoolean(false);
    }


    /**
     * Determine if the {@link ApplicationGroup} is closed.
     *
     * @return  <code>true</code> if the {@link ApplicationGroup} is closed
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
     * Adds the specified {@link Application} to the {@link ApplicationGroup}.
     *
     * @param application  the {@link Application} to add
     *
     * @throws IllegalStateException  when the {@link ApplicationGroup} {@link #isClosed}
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
     * Removes the specified {@link Application} to the {@link ApplicationGroup}.
     *
     * @param application  the {@link Application} to remove
     *
     * @throws IllegalStateException  when the {@link ApplicationGroup} {@link #isClosed}
     */
    public void removeApplication(A application)
    {
        if (isClosed())
        {
            throw new IllegalStateException("Can't add [" + application + "] as the " + this.getClass().getName()
                                            + " is closed");
        }
        else
        {
            applications.remove(application);
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
    @Deprecated
    public void destroy()
    {
        close();
    }
}
