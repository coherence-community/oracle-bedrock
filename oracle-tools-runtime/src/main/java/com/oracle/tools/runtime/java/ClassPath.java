/*
 * File: ClassPath.java
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

package com.oracle.tools.runtime.java;

import com.oracle.tools.lang.StringHelper;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * A platform independent, immutable representation of a Java ClassPath.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class ClassPath implements Iterable<String>
{
    /**
     * The known of Java Archives Types, typically used in class-paths and
     * file names. (strictly in ascending order).
     */
    public static final String[] JAVA_ARCHIVE_TYPES = new String[] {"aar", "car", "ear", "gar", "jar", "rar", "sar",
                                                                    "war", "zip"};

    /**
     * The paths that make up the ClassPath.
     */
    private final ArrayList<String> m_paths;


    /**
     * Constructs an empty ClassPath.
     */
    public ClassPath()
    {
        m_paths = new ArrayList<String>();
    }


    /**
     * Constructs a ClassPath based zero or more other ClassPaths.
     * (ie: a copy constructor)
     *
     * @param classPaths  the ClassPaths to copy
     */
    public ClassPath(ClassPath... classPaths)
    {
        m_paths = new ArrayList<String>();

        if (classPaths != null && classPaths.length > 0)
        {
            for (ClassPath classPath : classPaths)
            {
                for (String path : classPath)
                {
                    path = sanitizePath(path);
                    m_paths.add(path);
                }
            }
        }
    }


    /**
     * Constructs a ClassPath from zero or more standard Java class-paths
     * (using the system defined path and file separators).
     *
     * @param classPaths zero or more Java class-paths
     */
    public ClassPath(String... classPaths)
    {
        String pathSeparator = File.pathSeparator;

        m_paths = new ArrayList<String>();

        if (classPaths != null)
        {
            for (String classPath : classPaths)
            {
                // sanitize the entire classpath
                classPath = classPath == null ? "" : classPath.trim();

                // remove quotes
                classPath = StringHelper.dequote(classPath);

                if (!classPath.isEmpty())
                {
                    // separate the individual paths from the class path
                    String[] paths = classPath.split(pathSeparator);

                    for (String path : paths)
                    {
                        // sanitize the each path as well
                        path = sanitizePath(path);

                        if (!path.isEmpty())
                        {
                            m_paths.add(path);
                        }
                    }
                }
            }
        }
    }


    /**
     * Obtain the number of path elements in the ClassPath
     *
     * @return the number of path elements
     */
    public int size()
    {
        return m_paths.size();
    }


    /**
     * Determines if the ClassPath is empty (contains no path definitions).
     *
     * @return true iff the ClassPath is empty
     */
    public boolean isEmpty()
    {
        return m_paths.isEmpty();
    }


    /**
     * Obtains the paths defined by the ClassPath as an array of URLs.
     *
     * @return an array of URLs representing the paths defined by the ClassPath
     */
    public URL[] getURLs()
    {
        URL[] urls = new URL[m_paths.size()];
        int   i    = 0;

        for (String path : m_paths)
        {
            try
            {
                urls[i++] = new File(path).toURI().toURL();
            }
            catch (MalformedURLException e)
            {
                throw new RuntimeException("Failed to convert the path [" + path + "] into a URL", e);
            }
        }

        return urls;
    }


    /**
     * Determines if the ClassPath contains the specified path element.
     *
     * @param path the path element
     *
     * @return true iff the ClassPath contains the specified path element
     *         (exact match)
     */
    public boolean contains(String path)
    {
        if (path == null)
        {
            return false;
        }
        else
        {
            // sanitize the path
            path = sanitizePath(path);

            for (String aPath : m_paths)
            {
                if (aPath.equals(path))
                {
                    return true;
                }
            }

            return false;
        }
    }


    /**
     * Determines if the ClassPath contains all of the path elements defined
     * by the specified ClassPath.
     *
     * @param classPath  the ClassPath containing the path elements to confirm
     *                   that are in this ClassPath
     *
     * @return true iff the specified ClassPath is contained in this ClassPath
     */
    public boolean contains(ClassPath classPath)
    {
        if (classPath == null)
        {
            return false;
        }
        else
        {
            for (String path : classPath)
            {
                if (!contains(path))
                {
                    return false;
                }
            }

            return true;
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<String> iterator()
    {
        return m_paths.iterator();
    }


    /**
     * Obtains a String representation of the ClassPath that is suitable
     * for using as a Java class-path property (using the system defined
     * file and path separators).
     *
     * @return the Java class-path
     */
    @Override
    public String toString()
    {
        String        pathSeparator = File.pathSeparator;
        StringBuilder builder       = new StringBuilder();

        for (String path : m_paths)
        {
            if (builder.length() > 0)
            {
                builder.append(pathSeparator);
            }

            builder.append(path);
        }

        return builder.toString();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }

        if (other == null || getClass() != other.getClass())
        {
            return false;
        }

        ClassPath classPath = (ClassPath) other;

        return m_paths.equals(classPath.m_paths);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }


    /**
     * Sanitizes a class-path element (a single path) by removing unnecessary
     * pre and post fix spaces, together with single and double quotes.
     *
     * @param path  the path element to sanitize
     *
     * @return a sanitized non-null path
     */
    private static String sanitizePath(String path)
    {
        if (path == null)
        {
            return "";
        }
        else
        {
            // remove unnecessary white space
            path = path.trim();

            // remove quotes
            path = StringHelper.unquote(path);

            if (!path.isEmpty())
            {
                // add a file separator iff it's not an archive
                path = isResourceAnArchive(path) || path.endsWith(File.separator) ? path : path + File.separator;
            }

            return path;
        }
    }


    /**
     * Attempts to determine if the specified resource represents a known Java
     * Archive (based on the resource name, not attempting to load or unpack
     * it).
     *
     * @see #getResourceArchiveType(String)
     * @see #JAVA_ARCHIVE_TYPES
     *
     * @param resourceName  the resource name
     *
     * @return true if the resource name is a known Java Archive.
     */
    public static boolean isResourceAnArchive(String resourceName)
    {
        return getResourceArchiveType(resourceName) != null;
    }


    /**
     * Attempts to determine if the specified resource Java Archive type
     * (based on the resource name, not attempting to load or unpack it).
     * </p>
     * The algorithm succeeds if the specified resource ends in a
     * known Java Archive type extension (eg: .jar).  Alternatively it succeeds
     * if the specified resource uses a Java Archive type as a URI protocol
     * (eg: jar://example.jar!MyClass).
     *
     * @see #JAVA_ARCHIVE_TYPES
     *
     * @param resourceName  the resource name
     *
     * @return the Java Archive extension or <code>null</code> if the
     *         resource is not a Java Archive
     */
    public static String getResourceArchiveType(String resourceName)
    {
        // ensure we remove leading and trailing spaces
        resourceName = resourceName == null ? "" : resourceName.trim();

        // attempt to determine the archive type based on the extension
        int index = resourceName.lastIndexOf(".");

        if (index >= 0)
        {
            String extension = resourceName.substring(index + 1).toLowerCase();

            // search for the archive extension
            index = Arrays.binarySearch(JAVA_ARCHIVE_TYPES, extension);

            if (index >= 0)
            {
                return JAVA_ARCHIVE_TYPES[index];
            }
        }

        // attempt to determine the archive type based on a prefix
        for (String archiveType : JAVA_ARCHIVE_TYPES)
        {
            if (resourceName.startsWith(archiveType + ":"))
            {
                return archiveType;
            }
        }

        // not found, so we assume it's not an archive
        return null;
    }


    /**
     * Obtains the ClassPath for the specified resource using the provided ClassLoader.
     *
     * @param resourceName  the resource to locate
     * @param classLoader   the ClassLoader (or null indicating the current
     *                      Thread getContextClassLoader())
     *
     * @return a ClassPath representing the location of the specified resource
     *
     * @throws IOException  when the resource can't be located
     */
    public static ClassPath ofResource(String      resourceName,
                                       ClassLoader classLoader) throws IOException
    {
        // ensure we have a resource name
        if (resourceName == null)
        {
            throw new NullPointerException("Resource name must not be null");
        }
        else
        {
            // ensure we have a ClassLoader
            classLoader = classLoader == null ? Thread.currentThread().getContextClassLoader() : classLoader;

            // attempt to locate the resource
            Enumeration<URL> resources = classLoader.getResources(resourceName);

            if (resources.hasMoreElements())
            {
                URL url = resources.nextElement();

                // decode the URL to remove possible encoded characters
                String location = URLDecoder.decode(url.toExternalForm(), "UTF-8");

                // encode spaces as %20 so we can create a valid URI
                location = location.replace(" ", "%20");

                // remove the resource from the location
                // (as we want the location of the resource not the resource)
                location = location.substring(0, location.length() - resourceName.length() - 1);

                // determine the archive type
                String archiveType = getResourceArchiveType(location);

                if (archiveType != null && location.startsWith(archiveType + ":"))
                {
                    location = location.substring(archiveType.length() + 1, location.length() - 1);
                }

                try
                {
                    return new ClassPath(new File(new URI(location)).getAbsolutePath());
                }
                catch (URISyntaxException e)
                {
                    throw new IOException("Unable to create a ClassPath for [" + location + "] using ClassLoader ["
                                          + classLoader + "] as an illegal URI was encountered",
                                          e);
                }
            }
            else
            {
                throw new IOException("Unable to locate the specified resource [" + resourceName
                                      + "] using ClassLoader [" + classLoader + "] with ClassPath ["
                                      + ClassPath.ofSystem() + "]");
            }
        }
    }


    /**
     * Obtains the ClassPath for the specified resource using the current Thread
     * context ClassLoader
     *
     * @param resourceName  the resource to locate
     *
     * @return a ClassPath representing the location of the specified resource
     *
     * @throws IOException  when the resource can't be located
     */
    public static ClassPath ofResource(String resourceName) throws IOException
    {
        return ofResource(resourceName, Thread.currentThread().getContextClassLoader());
    }


    /**
     * Obtains the ClassPath for the specified Class.
     *
     * @param clazz  the class
     *
     * @return a ClassPath representing the location of the specified Class
     *
     * @throws IOException  when the resource can't be located
     */
    public static ClassPath ofClass(Class<?> clazz) throws IOException
    {
        // ensure we have a class name
        if (clazz == null)
        {
            throw new NullPointerException("Class must not be null");
        }
        else
        {
            // create a resource name for the class (must use a / here)
            String resourceName = clazz.getCanonicalName().replace(".", "/") + ".class";

            // determine the location as a resource
            return ofResource(resourceName, clazz.getClassLoader());
        }
    }


    /**
     * Obtains a ClassPath containing only absolute path of the specified File
     *
     * @param file the file
     */
    public static ClassPath ofFile(File file)
    {
        if (file == null)
        {
            throw new NullPointerException("File must not be null");
        }
        else
        {
            return new ClassPath(file.toString());
        }
    }


    /**
     * Obtains Java System class-path.
     *
     * @return a ClassPath representing the System java.class.path property.
     */
    public static ClassPath ofSystem()
    {
        return new ClassPath(System.getProperty("java.class.path"));
    }
}
