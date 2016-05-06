/*
 * File: ClassPathScanner.java
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

package com.oracle.bedrock.runtime.java;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * List resources available from a {@link ClassPath}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class ClassPathScanner
{
    /**
     * For all elements of a {@link ClassPath} get a {@link List} of resources
     * that match the specified pattern.
     * <p>
     * Pattern pattern = Pattern.compile(".*") gets all resources
     *
     * @param classPath  the {@link ClassPath}
     * @param pattern    the pattern to match, or null to match everything
     *
     * @return the resources in the order they are found
     *
     * @throws Exception  when the resources can't be located or determined
     */
    public List<String> getResources(ClassPath classPath,
                                     Pattern   pattern) throws Exception
    {
        if (pattern == null)
        {
            pattern = Pattern.compile(".*");
        }

        ArrayList<String> result    = new ArrayList<>();
        URLClassLoader    loader    = new URLClassLoader(classPath.getURLs());
        Enumeration<URL>  resources = loader.getResources("");

        while (resources.hasMoreElements())
        {
            URL  url  = resources.nextElement();
            File file = new File(url.toURI());

            for (String resource : getResources(file, pattern))
            {
                if (resource.endsWith(".class"))
                {
                    result.add(resource);
                }
            }
        }

        return result;
    }


    private Collection<String> getResources(File    file,
                                            Pattern pattern) throws IOException
    {
        ArrayList<String> retval = new ArrayList<>();

        if (file.isDirectory())
        {
            retval.addAll(getResourcesFromDirectory(file, pattern));
        }
        else
        {
            retval.addAll(getResourcesFromJarFile(file, pattern));
        }

        return retval;
    }


    private List<String> getResourcesFromJarFile(File    file,
                                                 Pattern pattern) throws IOException
    {
        if (!ClassPath.isResourceAnArchive(file.getCanonicalPath()))
        {
            return Collections.emptyList();
        }

        ArrayList<String> retval = new ArrayList<>();

        try (ZipFile zipFile = new ZipFile(file))
        {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements())
            {
                ZipEntry entry    = entries.nextElement();
                String   fileName = entry.getName();

                if (pattern.matcher(fileName).matches())
                {
                    retval.add(fileName);
                }
            }
        }

        return retval;
    }


    private List<String> getResourcesFromDirectory(File    directory,
                                                   Pattern pattern) throws IOException
    {
        File[] fileList = directory.listFiles();

        if (fileList == null)
        {
            return Collections.emptyList();
        }

        ArrayList<String> list = new ArrayList<>();

        for (File file : fileList)
        {
            if (file.isDirectory())
            {
                list.addAll(getResourcesFromDirectory(file, pattern));
            }
            else
            {
                String fileName = file.getCanonicalPath();

                if (pattern.matcher(fileName).matches())
                {
                    list.add(fileName);
                }
            }
        }

        return list;
    }


    /**
     * List the resources that match args[0]
     *
     * @param args args[0] is the pattern to match, or list all resources if
     *             there are no args
     *
     * @throws Exception  the exception
     */
    public static void main(String[] args) throws Exception
    {
        Pattern pattern = null;

        if (args.length > 0)
        {
            pattern = Pattern.compile(args[0]);
        }

        ClassPath          path = ClassPath.ofClass(ClassPathScanner.class);
        Collection<String> list = new ClassPathScanner().getResources(path, pattern);

        for (String name : list)
        {
            System.out.println(name);
        }
    }
}
