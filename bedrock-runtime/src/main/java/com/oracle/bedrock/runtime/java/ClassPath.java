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

package com.oracle.bedrock.runtime.java;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.lang.StringHelper;
import com.oracle.bedrock.runtime.options.PlatformSeparators;
import com.oracle.bedrock.table.Table;
import com.oracle.bedrock.table.Tabular;

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A local immutable representation of a Java class path.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class ClassPath implements Iterable<String>, Tabular, Option
{
    /**
     * The known of Java Archives Types, typically used in class-paths and
     * file names. (strictly in ascending order).
     */
    public static final String[] JAVA_ARCHIVE_TYPES = new String[] {"aar", "car", "ear", "gar", "jar", "rar", "sar",
                                                                    "war", "zip"};

    /**
     * The paths that make up the {@link ClassPath}.
     */
    private final LinkedHashSet<String> paths;

    /**
     * A list of reg-ex patterns to use to exclude artifacts from the path.
     */
    private final Set<Pattern> excludes;

    /**
     * A flag indicating whether to apply the default exclusions.
     */
    private final boolean useDefaultExcludes;

    /**
     * The default class path exclusions.
     */
    private static final Set<Pattern> defaultExcludes = new HashSet<>(Arrays.asList(
            Pattern.compile(".*idea_rt.*"),
            Pattern.compile(".*junit-rt.*"),
            Pattern.compile(".*junit5-rt.*")
    ));

    /**
     * Constructs an empty {@link ClassPath} using the system default
     * {@link File#separator} and {@link File#pathSeparator}s.
     */
    public ClassPath()
    {
        paths              = new LinkedHashSet<>();
        excludes           = new LinkedHashSet<>();
        useDefaultExcludes = true;
    }


    /**
     * Constructs a {@link ClassPath} based zero or more other {@link ClassPath}s.
     * (ie: a copy constructor)
     *
     * @param classPaths  the {@link ClassPath}s to copy
     */
    public ClassPath(ClassPath... classPaths)
    {
        this(Arrays.stream(classPaths).allMatch(cp -> cp.useDefaultExcludes), classPaths);
    }


    /**
     * Constructs a {@link ClassPath} based zero or more other {@link ClassPath}s.
     * (ie: a copy constructor)
     *
     * @param defaultsExcludes  {@code true} to use default file exclusions
     * @param classPaths        the {@link ClassPath}s to copy
     */
    private ClassPath(boolean defaultsExcludes, ClassPath... classPaths)
    {
        paths    = new LinkedHashSet<>();
        excludes = new LinkedHashSet<>();
        useDefaultExcludes = defaultsExcludes;

        if (classPaths != null && classPaths.length > 0)
        {
            for (ClassPath classPath : classPaths)
            {
                for (String path : classPath)
                {
                    path = sanitizePath(path);
                    paths.add(path);
                }

                excludes.addAll(classPath.excludes);
            }
        }
    }


    /**
     * Constructs a {@link ClassPath} based zero or more other {@link ClassPath}s.
     * (ie: a copy constructor)
     *
     * @param classPaths  the {@link ClassPath}s to copy
     */
    public ClassPath(Iterable<ClassPath> classPaths)
    {
        paths    = new LinkedHashSet<>();
        excludes = new LinkedHashSet<>();

        boolean useDefaults = true;

        if (classPaths != null)
        {
            for (ClassPath classPath : classPaths)
            {
                useDefaults = useDefaults && classPath.useDefaultExcludes;

                for (String path : classPath)
                {
                    path = sanitizePath(path);
                    paths.add(path);
                }

                excludes.addAll(classPath.excludes);
            }
        }

        useDefaultExcludes = useDefaults;
    }


    /**
     * Constructs a {@link ClassPath} from zero or more standard Java class-paths
     * (using the system defined path and file separators).
     *
     * @param classPaths zero or more Java class-paths
     */
    public ClassPath(String... classPaths)
    {
        paths              = new LinkedHashSet<>();
        excludes           = new LinkedHashSet<>();
        useDefaultExcludes = true;

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
                    String[] paths = classPath.split(File.pathSeparator);

                    for (String path : paths)
                    {
                        // sanitize the each path as well
                        path = sanitizePath(path);

                        if (!path.isEmpty())
                        {
                            this.paths.add(path);
                        }
                    }
                }
            }
        }
    }


    /**
     * Obtain the number of path elements in the {@link ClassPath}
     *
     * @return the number of path elements
     */
    public int size()
    {
        return (int) paths.stream()
                          .filter(this::include)
                          .count();
    }


    /**
     * Determines if the {@link ClassPath} is empty (contains no path definitions).
     *
     * @return true iff the {@link ClassPath} is empty
     */
    public boolean isEmpty()
    {
        return paths.stream().noneMatch(this::include);
    }


    /**
     * Obtains the paths defined by the {@link ClassPath} as an array of URLs.
     *
     * @return an array of URLs representing the paths defined by the {@link ClassPath}
     */
    public URL[] getURLs()
    {
        List<URL> urls = new ArrayList<>();

        for (String path : this)
        {
            if (include(path))
            {
                try
                {
                    urls.add(new File(path).toURI().toURL());
                }
                catch (MalformedURLException e)
                {
                    throw new RuntimeException("Failed to convert the path [" + path + "] into a URL", e);
                }
            }
        }

        return urls.toArray(URL[]::new);
    }


    /**
     * Determines if the {@link ClassPath} contains the specified path element.
     *
     * @param path the path element
     *
     * @return true iff the {@link ClassPath} contains the specified path element
     *         (exact match)
     */
    public boolean contains(String path)
    {
        if (path == null || !include(path))
        {
            return false;
        }
        else
        {
            // sanitize the path
            path = sanitizePath(path);

            for (String aPath : paths)
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
     * Determines if the {@link ClassPath} contains all of the path elements defined
     * by the specified {@link ClassPath}.
     *
     * @param classPath  the {@link ClassPath} containing the path elements to confirm
     *                   that are in this {@link ClassPath}
     *
     * @return true iff the specified {@link ClassPath} is contained in this {@link ClassPath}
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


    @Override
    public Iterator<String> iterator()
    {
        return paths.stream()
                    .filter(this::include)
                    .collect(Collectors.toList())
                    .iterator();
    }


    /**
     * Obtains a String representation of the {@link ClassPath} that is suitable
     * for using as a Java class-path property (using the system defined
     * file and path separators).
     *
     * @return the Java class-path
     */
    @Override
    public String toString()
    {
        return toString(PlatformSeparators.autoDetect());
    }


    /**
     * Obtains a String representation of the {@link ClassPath} that is suitable
     * for using as a Java class-path property (using the specified
     * file and path separators).
     * <p>
     * Note:  The returned String may contain spaces in which case the caller should
     * appropriate double-quote the String for their appropriate Platform.
     *
     * @param optionsByType  the {@link OptionsByType} for converting the {@link ClassPath} into a String
     *
     * @return the Java class-path
     */
    public String toString(OptionsByType optionsByType)
    {
        StringBuilder      builder       = new StringBuilder();
        PlatformSeparators separators    = optionsByType.get(PlatformSeparators.class);
        ClassPathModifier  modifier      = optionsByType.get(ClassPathModifier.class);
        String             pathSeparator = separators.getPathSeparator();
        Stream<String>     stream;

        if (excludes.isEmpty())
        {
            stream = paths.stream().filter(this::include);
        }
        else
        {
            stream = paths.stream().filter(this::include);
        }

        stream.forEach(path -> {
            if (builder.length() > 0)
            {
                builder.append(pathSeparator);
            }

            // NOTE: DON'T BE TEMPTED TO DOUBLE QUOTE THESE PATH STRINGS!
            // (on certain platforms they may be double quoted again!)
            builder.append(path);
        });

        // NOTE: DON'T BE TEMPTED TO DOUBLE QUOTE THESE PATH STRINGS!
        // (on certain platforms they may be double quoted again!)
        return modifier.modify(builder.toString());
    }


    /**
     * Obtains a String representation of the {@link ClassPath} that is suitable
     * for using as a Java class-path property (using the specified
     * file and path separators).
     * <p>
     * Note:  The returned String may contain spaces in which case the caller should
     * appropriate double-quote the String for their appropriate Platform.
     *
     * @param options   the {@link Option}s
     *
     * @return the Java class-path
     */
    public String toString(Option... options)
    {
        return toString(OptionsByType.of(options));
    }


    @Override
    public Table getTable()
    {
        Table table = new Table();

        for (String path : this)
        {
            // If the path is the current directory "./" or "./*" then just add it as-is
            if ("./".equals(path) || "./*".equals(path))
            {
                table.addRow(path);
            }
            else
            {
                File   file   = new File(path);
                String parent = file.getParent();

                table.addRow(file.getName(),
                             parent == null ? "" : "(" + parent + ")");
            }
        }

        return table;
    }


    /**
     * Create a {@link ClassPath} that is the same as this {@link ClassPath}
     * removing the default class path exclusions.
     *
     * @return a {@link ClassPath} that is the same as this {@link ClassPath}
     *         removing the default class path exclusions
     */
    public ClassPath withDefaultExcludes()
    {
        return new ClassPath(true, this);
    }


    /**
     * Create a {@link ClassPath} that is the same as this {@link ClassPath}
     * with the default class path exclusions added back.
     *
     * @return a {@link ClassPath} that is the same as this {@link ClassPath}
     *         with the default class path exclusions added back
     */
    public ClassPath withoutDefaultExcludes()
    {
        return new ClassPath(false, this);
    }


    /**
     * Create a {@link ClassPath} that is the same as this {@link ClassPath}
     * excluding any artifacts matching any of the specified expressions.
     *
     * @param exclude  the reg-ex expressions to use to exclude artifacts
     *
     * @return a {@link ClassPath} that is the same as this {@link ClassPath}
     *         excluding any artifacts matching any of the specified expressions
     */
    public ClassPath excluding(String... exclude)
    {
        if (exclude == null || exclude.length == 0)
        {
            return this;
        }

        ClassPath classPath = new ClassPath(this);

        for (String ex : exclude)
        {
            classPath.excludes.add(Pattern.compile(ex));
        }

        return classPath;
    }


    /**
     * Create a {@link ClassPath} that is the same as this {@link ClassPath}
     * excluding any artifacts matching any of those in the specified
     * {@link ClassPath}.
     *
     * @param exclude  {@link ClassPath} to exclude
     *
     * @return a {@link ClassPath} that is the same as this {@link ClassPath}
     *         excluding any artifacts matching any of those in the specified
     *         {@link ClassPath}
     */
    public ClassPath excluding(ClassPath exclude)
    {
        if (exclude.isEmpty())
        {
            return this;
        }

        ClassPath classPath = new ClassPath(this);

        for (String path : exclude.paths)
        {
            classPath.paths.remove(path);
        }

        return classPath;
    }


    private boolean include(String path)
    {
        boolean exclude = false;

        if (useDefaultExcludes)
        {
            exclude = defaultExcludes.stream()
                    .anyMatch(pattern -> pattern.matcher(path).matches());
        }

        if (!excludes.isEmpty())
        {
            exclude = exclude || excludes.stream()
                    .anyMatch(pattern -> pattern.matcher(path).matches());
        }

        return !exclude;
    }

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

        if (useDefaultExcludes != classPath.useDefaultExcludes)
        {
            return false;
        }

        return paths.equals(classPath.paths);
    }


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
                path = isResourceAnArchive(path)
                       || path.endsWith(File.separator)
                       || path.endsWith("*") ? path : path + File.separator;
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
     * <p>
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
     * Obtains the {@link ClassPath} for the specified resource using the provided ClassLoader.
     *
     * @param resourceName  the resource to locate
     * @param classLoader   the ClassLoader (or null indicating the current
     *                      Thread getContextClassLoader())
     *
     * @return a {@link ClassPath} representing the location of the specified resource
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
     * Obtains the {@link ClassPath} for the specified resource using the current Thread
     * context ClassLoader
     *
     * @param resourceName  the resource to locate
     *
     * @return a {@link ClassPath} representing the location of the specified resource
     *
     * @throws IOException  when the resource can't be located
     */
    public static ClassPath ofResource(String resourceName) throws IOException
    {
        return ofResource(resourceName, Thread.currentThread().getContextClassLoader());
    }


    /**
     * Obtains the {@link ClassPath} for the specified Class.
     *
     * @param clazz  the class
     *
     * @return a {@link ClassPath} representing the location of the specified Class
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
            // locate the declaring class
            while (clazz.getEnclosingClass() != null)
            {
                clazz = clazz.getEnclosingClass();
            }

            // create a resource name for the class (must use a / here)
            String resourceName = clazz.getCanonicalName().replace(".", "/") + ".class";

            // determine the location as a resource
            return ofResource(resourceName, clazz.getClassLoader());
        }
    }


    /**
     * Obtains the {@link ClassPath} for the specified Class.
     *
     * @param className    the class name
     * @param classLoader  the {@link ClassLoader} to use for loading the class
     *
     * @return a {@link ClassPath} representing the location of the specified Class
     *
     * @throws ClassNotFoundException  when the specified class can't be located
     * @throws IOException             when the resource can't be located
     */
    public static ClassPath ofClass(String      className,
                                    ClassLoader classLoader) throws ClassNotFoundException, IOException
    {
        Class<?> clazz = classLoader.loadClass(className);

        return ofClass(clazz);
    }


    /**
     * Obtains a {@link ClassPath} containing only absolute path of the specified File
     *
     * @param file the file
     *
     * @return a {@link ClassPath} of a file
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
     * @return a {@link ClassPath} representing the System java.class.path property.
     */
    @OptionsByType.Default
    public static ClassPath ofSystem()
    {
        return new ClassPath(System.getProperty("java.class.path"));
    }


    /**
     * Obtains a {@link ClassPath} for the specified {@link Iterable} of
     * path elements.
     *
     * @param paths  the paths for the {@link ClassPath}
     *
     * @return a {@link ClassPath} representing the paths
     */
    public static ClassPath of(Iterable<String> paths)
    {
        if (paths == null)
        {
            return new ClassPath();
        }
        else
        {
            ArrayList<ClassPath> classPaths = new ArrayList<ClassPath>();

            for (String path : paths)
            {
                classPaths.add(new ClassPath(path));
            }

            return new ClassPath(classPaths);
        }
    }


    /**
     * Obtains a {@link ClassPath} representing the specified {@link ClassPath}s.
     *
     * @param classPaths  the {@link ClassPath}s
     *
     * @return a {@link ClassPath} representing the {@link ClassPath}s
     */
    public static ClassPath of(ClassPath... classPaths)
    {
        if (classPaths == null)
        {
            return new ClassPath();
        }
        else
        {
            return new ClassPath(classPaths);
        }
    }


    /**
     * Obtains a {@link ClassPath} representing the specified class path {@link String}s.
     *
     * @param classPaths  the class path {@link String}s
     *
     * @return a {@link ClassPath} representing the {@link ClassPath}s
     */
    public static ClassPath of(String... classPaths)
    {
        if (classPaths == null)
        {
            return new ClassPath();
        }
        else
        {
            return new ClassPath(classPaths);
        }
    }
}
