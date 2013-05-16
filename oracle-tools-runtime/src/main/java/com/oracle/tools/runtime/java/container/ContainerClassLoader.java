/*
 * File: ContainerClassLoader.java
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

package com.oracle.tools.runtime.java.container;

import com.oracle.tools.runtime.PropertiesBuilder;

import com.oracle.tools.runtime.java.ClassPath;

import com.oracle.tools.runtime.network.AvailablePortIterator;

import java.net.URL;

import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * A {@link ContainerClassLoader} is a specialized {@link ContainerScopeClassLoader}
 * that uses the child-first / parent-last strategy for resolving classes.
 * <p>
 * A good introduction to the concept of classloading is here:
 * <a href="http://www.javalobby.org/java/forums/t18345.html">A Look At Java ClassLoaders</a>
 * <p>
 * Applications that use this {@link ClassLoader} are typically attempting to
 * isolate regular Java Applications, much like an application server would do
 * for Java EE applications.  That is, this {@link ClassLoader} provides
 * infrastructure for the scoping, containment of applications in one process.
 *
 * @see Container
 * @see ContainerScopeClassLoader
 * @see Scope
 * @see DelegatingProperties
 * @see DelegatingStdOutOutputStream
 * @see DelegatingStdErrOutputStream
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 * @author Andrew Wilson
 * @author Brian Oliver
 */
public class ContainerClassLoader extends ContainerScopeClassLoader
{
    /**
     * The java.class.path property.
     */
    public static final String PROPERTY_JAVA_CLASS_PATH = "java.class.path";

    /**
     * The container.exclude.packages property.
     */
    public static final String PROPERTY_EXCLUDED_PACKAGES = "container.exclude.packages";

    /**
     * The names of the packages (as prefixes) to load from the parent
     * {@link ClassLoader}.  ie: these are the "shared" packages.
     */
    private Set<String> m_packagesToLoadFromParent = new HashSet<String>();

    /**
     * The {@link Class}es currently loaded by the {@link ContainerClassLoader}.
     */
    private Map<String, Class<?>> m_loadedClasses = new HashMap<String, Class<?>>();

    /**
     * The currently loaded resources.
     */
    private Map<String, URL> m_loadedResources = new HashMap<String, URL>();

    /**
     * The root {@link ClassLoader}.
     */
    private ClassLoader m_rootClassLoader;

    /**
     * The parent {@link ClassLoader}.
     */
    private ClassLoader m_parentClassLoader;

    /**
     * The {@link ClassPath} of the {@link ContainerClassLoader}.
     */
    private ClassPath m_classPath;


    /**
     * Constructs a {@link ContainerClassLoader}.
     *
     * @param classPath  the {@link ClassPath}s for the {@link ClassLoader}
     * @param parent     the parent {@link ClassLoader}
     * @param scope      the {@link ContainerScope} for the {@link ContainerClassLoader}
     */
    private ContainerClassLoader(ClassPath      classPath,
                                 ClassLoader    parent,
                                 ContainerScope scope)
    {
        super(classPath, null, scope);
        m_parentClassLoader = parent;
        m_classPath         = classPath;

        while (parent.getParent() != null)
        {
            m_rootClassLoader = parent.getParent();
            parent            = parent.getParent();
        }
    }


    /**
     * A helper method to instantiate a new {@link ContainerClassLoader}
     * using the current platform class path and system properties.
     *
     * @param applicationName  the name of the application
     *
     * @return  a {@link ContainerClassLoader} for the application
     *
     * @throws Exception  if some exception occurs
     */
    public static ContainerClassLoader newInstance(String applicationName) throws Exception
    {
        PropertiesBuilder builder = PropertiesBuilder.fromCurrentSystemProperties();

        return newInstance(applicationName,
                           ClassPath.ofSystem(),
                           builder.realize(),
                           System.getProperties(),
                           Container.getAvailablePorts(),
                           false,
                           Container.PIPE_BUFFER_SIZE_BYTES);
    }


    /**
     * A helper method to instantiate a new {@link ContainerClassLoader}.
     *
     * @param applicationName  the name of the application
     * @param classPath        the {@link ClassPath} for the {@link ClassLoader}
     * @param localProperties  the local system properties for the {@link ClassLoader}
     *
     * @return  a {@link ContainerClassLoader} for the application
     *
     * @throws Exception  if some exception occurs
     */
    public static ContainerClassLoader newInstance(String     applicationName,
                                                   ClassPath  classPath,
                                                   Properties localProperties) throws Exception
    {
        return newInstance(applicationName,
                           classPath,
                           localProperties,
                           System.getProperties(),
                           Container.getAvailablePorts(),
                           false,
                           Container.PIPE_BUFFER_SIZE_BYTES);
    }


    /**
     * A helper method to instantiate a new {@link ContainerClassLoader}.
     *
     * @param applicationName  the name of the application
     * @param classPath        the {@link ClassPath} of the application
     * @param localProperties  the local system properties for the {@link ClassLoader}
     * @param systemProperties the System properties to use to get the default class path
     *
     * @return  a {@link ContainerClassLoader} for the application
     *
     * @throws Exception  if some exception occurs
     */
    @SuppressWarnings("ConstantConditions")
    protected static ContainerClassLoader newInstance(String                applicationName,
                                                      ClassPath             classPath,
                                                      Properties            localProperties,
                                                      Properties            systemProperties,
                                                      AvailablePortIterator availablePorts,
                                                      boolean               redirectErrorStream,
                                                      int                   pipeBufferSizeBytes) throws Exception
    {
        if (classPath == null || classPath.isEmpty())
        {
            classPath = new ClassPath(systemProperties.getProperty(PROPERTY_JAVA_CLASS_PATH));

            if (classPath == null)
            {
                System.out.println("Classpath should not be null!");
            }
        }

        // acquire the platform
        Scope platformScope = Container.getPlatformScope();

        // establish an MBeanServerBuilder
        ContainerMBeanServerBuilder mBeanServerBuilder = new ContainerMBeanServerBuilder(Container.getAvailablePorts());

        // establish the Scope for the application
        ContainerScope scope = new ContainerScope(applicationName,
                                                  platformScope.getProperties(),
                                                  availablePorts,
                                                  mBeanServerBuilder,
                                                  redirectErrorStream,
                                                  pipeBufferSizeBytes);

        // override the MBeanServerBuilder for the Scope
        localProperties.put(ContainerMBeanServerBuilder.PROPERTY_JMX_MBEAN_SERVER_BUILDER,
                            DelegatingMBeanServerBuilder.class.getCanonicalName());

        // add local properties to the scope
        scope.getProperties().putAll(localProperties);

        ClassLoader          parentLoader        = ContainerClassLoader.class.getClassLoader();
        ContainerClassLoader loader              = new ContainerClassLoader(classPath, parentLoader, scope);

        String               excludedPackageList = localProperties.getProperty(PROPERTY_EXCLUDED_PACKAGES);

        if (excludedPackageList != null && excludedPackageList.trim().length() > 0)
        {
            String[] packages = excludedPackageList.split(",");

            for (String pack : packages)
            {
                if (pack.trim().length() > 0)
                {
                    loader.addPackageToLoadFromParent(pack.trim());
                }
            }
        }

        // we won't isolate the com.oracle.tools.runtime.java.* package
        loader.addPackageToLoadFromParent("com.oracle.tools.runtime.java.container");
        loader.addPackageToLoadFromParent("com.oracle.tools.runtime.java");

        return loader;
    }


    /**
     * Obtain the {@link ClassPath} of the {@link ContainerClassLoader}.
     *
     * @return  the {@link ClassPath}
     */
    public ClassPath getClassPath()
    {
        return m_classPath;
    }


    /**
     * Adds the specified package prefix to the list of packages that
     * should be loaded by the parent {@link ClassLoader}.
     *
     * @param packagePrefix  the name of the package (prefix without class name)
     */
    public void addPackageToLoadFromParent(String packagePrefix)
    {
        m_packagesToLoadFromParent.add(packagePrefix);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException
    {
        if (shouldLoadFromParent(name) && m_parentClassLoader != null)
        {
            return m_parentClassLoader.loadClass(name);
        }

        Class<?> c = m_loadedClasses.get(name);

        if (c == null)
        {
            try
            {
                c = super.loadClass(name);
            }
            catch (Throwable t)
            {
                c = m_rootClassLoader.loadClass(name);
            }

            m_loadedClasses.put(name, c);
        }

        return c;
    }


    /**
     * Determines if the specified class name should be loaded from the
     * parent {@link ClassLoader}.
     *
     * @param className  the name of the class to check
     *
     * @return  <code>true</code> if the specified class should be loaded
     *          from the parent {@link ClassLoader}, otherwise <code>false</code>
     */
    private boolean shouldLoadFromParent(String className)
    {
        if (ContainerClassLoader.class.getCanonicalName().equals(className))
        {
            return true;
        }

        for (String prefix : m_packagesToLoadFromParent)
        {
            if (className.startsWith(prefix))
            {
                return true;
            }
        }

        return false;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected PermissionCollection getPermissions(CodeSource codeSource)
    {
        Permissions permissions = new Permissions();

        permissions.add(new AllPermission());

        return permissions;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public URL getResource(String name)
    {
        URL c = m_loadedResources.get(name);

        if (c == null)
        {
            c = findResource(name);
            m_loadedResources.put(name, c);
        }

        if (c == null)
        {
            c = super.getResource(name);
        }

        return c;
    }
}
