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

import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

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
     * The collection of {@link Application}s that belong to the {@link ApplicationGroup}.
     */
    protected LinkedHashMap<String, A> m_applications;


    /**
     * Constructs an {@link AbstractApplicationGroup} given a list of {@link Application}s.
     *
     * @param applications  the list of {@link Application}s in the {@link ApplicationGroup}.
     */
    public AbstractApplicationGroup(List<A> applications)
    {
        m_applications = new LinkedHashMap<String, A>();

        for (A application : applications)
        {
            m_applications.put(application.getName(), application);
        }
    }


    /**
     * Obtains the {@link Application} in this group with the given name.  If
     * no such {@link Application} exists in the group, <code>null</code> will
     * be returned.
     *
     * @param name  the name of the application to get
     * @return the application in this group with the given name or null
     *         if no application has been realized with the given name.
     */
    public A getApplication(String name)
    {
        return m_applications.get(name);
    }


    /**
     * Obtains the {@link Application}s in this group starting with the specified
     * prefix.
     *
     * @param prefix  the prefix of application names to return
     * @return the application in this group with the given name or null
     *         if no application has been realized with the given name.
     */
    public Iterable<A> getApplications(String prefix)
    {
        ArrayList<A> applications = new ArrayList<A>();

        for (String name : m_applications.keySet())
        {
            if (name.startsWith(prefix))
            {
                applications.add(m_applications.get(name));
            }
        }

        return applications;
    }


    @Override
    public Iterator<A> iterator()
    {
        return m_applications.values().iterator();
    }


    @Override
    public void close()
    {
        for (A application : m_applications.values())
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
        m_applications.clear();
    }


    @Override
    @Deprecated
    public void destroy()
    {
        close();
    }
}
