/*
 * File: TestClasses.java
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

package com.oracle.bedrock.junit.options;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.junit.TestClassPredicate;
import com.oracle.bedrock.runtime.java.ClassPath;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * An option representing a set of test {@link Class}es.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public abstract class TestClasses implements Option.Collectable, Option, Serializable
{
    /**
     * The {@link Set} of {@link TestMatcher} to determine the tests
     * to include in a test run.
     */
    private Set<TestMatcher> includePatterns = new HashSet<>();

    /**
     * The {@link Set} of {@link TestMatcher} to determine the tests
     * to exclude from a test run.
     */
    private Set<TestMatcher> excludePatterns = new HashSet<>();

    /**
     * The {@link Predicate} to use to determine whether a given
     * {@link Class} is a valid JUnit test class and should be
     * included in the test run.
     */
    private Predicate<Class<?>> testClassPredicate = new TestClassPredicate();


    /**
     * Obtain the {@link Set} of {@link Class}es to test.
     * <p>
     * The classes returned will be determined by any
     * include and exclude patterns.
     *
     * @return  the {@link Set} of {@link Class}es to test
     */
    public abstract Set<Class<?>> resolveTestClasses();


    /**
     * Add the specified regular expression pattern to the set of
     * patterns to use to determine tests to be included in the
     * test run.
     * <p>
     * NOTE: The exclude patterns take precedence over the include
     * patterns, so if a test matches both include and exclude then
     * it is excluded.
     *
     * @param patterns  the regular expressions to use to match
     *                  tests included in the test run
     *
     * @return  this {@link TestClasses} instance for method chaining
     */
    public TestClasses include(String... patterns)
    {
        if (patterns != null)
        {
            for (String pattern : patterns)
            {
                if (!pattern.isEmpty())
                {
                    includePatterns.add(new TestMatcher(pattern));
                }
            }
        }

        return this;
    }


    /**
     * Add the specified regular expression pattern to the set of
     * patterns to use to determine tests to be excluded in the
     * test run.
     * <p>
     * NOTE: The exclude patterns take precedence over the include
     * patterns, so if a test matches both include and exclude then
     * it is excluded.
     *
     * @param patterns  the regular expressions to use to match
     *                  tests included in the test run
     *
     * @return  this {@link TestClasses} instance for method chaining
     */
    public TestClasses exclude(String... patterns)
    {
        if (patterns != null)
        {
            for (String pattern : patterns)
            {
                if (!pattern.isEmpty())
                {
                    excludePatterns.add(new TestMatcher(pattern));
                }
            }
        }

        return this;
    }


    /**
     * Obtain the {@link Filter} instance to be used by the test run
     * to determine tests that will be executed.
     *
     * @return   the {@link Filter} instance to be used by the test
     *           run to determine tests that will be executed
     */
    public Filter getTestFilter()
    {
        if (includePatterns.isEmpty() && excludePatterns.isEmpty())
        {
            return AlwaysRunFilter.INSTANCE;
        }

        return new TestFilter(includePatterns, excludePatterns);
    }


    /**
     * Obtain the {@link Predicate} to use when resolving the list
     * of classes to include in a test run.
     *
     * @return  the {@link Predicate} to use when resolving the list
     *          of classes to include in a test run
     */
    protected Predicate<Class<?>> getTestClassPredicate()
    {
        if (includePatterns.isEmpty() && excludePatterns.isEmpty())
        {
            return testClassPredicate;
        }

        return new IncludeExcludePredicate(testClassPredicate, includePatterns, excludePatterns);
    }


    @Override
    public Class<? extends Collector> getCollectorClass()
    {
        return Tests.class;
    }


    /**
     * Create a {@link TestClasses} option that will resolve test classes
     * from the specified {@link Class} instances
     *
     * @param classes  the {@link Class}es to use to resolve test classes
     *
     * @return  a {@link TestClasses} option that will resolve test classes
     *          from the specified {@link Class}es
     */
    public static TestClasses of(Class<?>... classes)
    {
        if (classes.length == 0)
        {
            return empty();
        }

        return new SpecificClasses(classes);
    }


    /**
     * Create a {@link TestClasses} option that will resolve test classes
     * from the specified {@link ClassPath}.
     *
     * @param classPath  the {@link ClassPath} to use to resolve test classes
     *
     * @return  a {@link TestClasses} option that will resolve test classes
     *          from the specified {@link ClassPath}
     */
    public static TestClasses from(ClassPath classPath)
    {
        if (classPath == null || classPath.isEmpty())
        {
            return empty();
        }

        return new ClassPathClasses(classPath);
    }


    /**
     * Create an empty set of test classes.
     *
     * @return  an empty set of test classes
     */
    @OptionsByType.Default
    public static TestClasses empty()
    {
        return new TestClasses()
        {
            @Override
            public Set<Class<?>> resolveTestClasses()
            {
                return Collections.emptySet();
            }
        };
    }


    /**
     * A JUnit test {@link Filter} that always matches everything.
     */
    public static class AlwaysRunFilter extends Filter
    {
        /**
         * Field description
         */
        public static final AlwaysRunFilter INSTANCE = new AlwaysRunFilter();


        @Override
        public String describe()
        {
            return "AlwaysRunFilter";
        }


        @Override
        public boolean shouldRun(Description description)
        {
            return true;
        }
    }


    /**
     * A {@link TestClasses} instance that resolves test classes
     * from a specific {@link ClassPath} instance.
     */
    protected static class ClassPathClasses extends TestClasses implements Serializable
    {
        /**
         * The array of class path entries to use to resolve classes.
         */
        private String[] paths;

        /**
         * The resolved {@link Set} of test classes.
         */
        private transient Set<Class<?>> classes;


        /**
         * Create a {@link ClassPathClasses} instance that will resolve test
         * classes from the specified {@link ClassPath} instance.
         *
         * @param classPath  the {@link ClassPath} to use to resolve test classes
         */
        public ClassPathClasses(ClassPath classPath)
        {
            paths = new String[classPath.size()];

            int index = 0;

            for (String path : classPath)
            {
                paths[index++] = path;
            }

        }


        @Override
        public Set<Class<?>> resolveTestClasses()
        {
            if (classes == null)
            {
                synchronized (this)
                {
                    if (classes == null)
                    {
                        try
                        {
                            Set<Class<?>>       testClasses = new HashSet<>();
                            ClassPath           classPath   = new ClassPath(paths);
                            URL[]               urLs        = classPath.getURLs();
                            List<Path>          paths       = new ArrayList<>();
                            Predicate<Class<?>> predicate   = getTestClassPredicate();

                            for (URL url : urLs)
                            {
                                paths.add(Paths.get(url.toURI()));
                            }

                            for (Path path : paths)
                            {
                                if (Files.isDirectory(path))
                                {
                                    testClasses.addAll(walkPath(path, predicate));
                                }
                                else
                                {
                                    String fileName = path.getFileName().toString();

                                    if (Files.isRegularFile(path) && ClassPath.isResourceAnArchive(fileName))
                                    {
                                        testClasses.addAll(walkFileSystem(FileSystems.newFileSystem(path, null),
                                                                          predicate));
                                    }
                                }
                            }

                            this.classes = testClasses;
                        }
                        catch (Exception e)
                        {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }

            return Collections.unmodifiableSet(classes);
        }


        /**
         * Walk the specified {@link FileSystem} and return all of the {@link Class}es that
         * match the specified {@link Predicate}.
         *
         * @param fileSystem  the {@lin FileSystem} to search for matching {@link Class} files
         * @param predicate   the {@link Predicate} to use to match {@link Class} files
         *
         * @return  the {@link Set} of {@link Class}es from the {@link FileSystem} that match the {@link Predicate}
         *
         * @throws IOException if there is an error walking the {@link FileSystem}
         */
        private Set<Class<?>> walkFileSystem(FileSystem          fileSystem,
                                             Predicate<Class<?>> predicate) throws IOException
        {
            Set<Class<?>> testClasses = new HashSet<>();

            for (Path path : fileSystem.getRootDirectories())
            {
                testClasses.addAll(walkPath(path, predicate));
            }

            return testClasses;
        }


        /**
         * Walk the specified {@link Path} and return all of the {@link Class}es that
         * match the specified {@link Predicate}.
         *
         * @param path       the {@lin Path} to search for matching {@link Class} files
         * @param predicate  the {@link Predicate} to use to match {@link Class} files
         *
         * @return  the {@link Set} of {@link Class}es from the {@link Path} that match the {@link Predicate}
         *
         * @throws IOException if there is an error walking the {@link Path}
         */
        private List<Class<?>> walkPath(Path                path,
                                        Predicate<Class<?>> predicate) throws IOException
        {
            List<Class<?>> testClasses = new ArrayList<>();

            Files.walkFileTree(path,
                               new SimpleFileVisitor<Path>()
                               {
                                   @Override
                                   public FileVisitResult visitFile(Path                file,
                                                                    BasicFileAttributes attrs) throws IOException
                                   {
                                       if (file.getFileName().toString().endsWith(".class"))
                                       {
                                           StringBuilder  className = new StringBuilder();
                                           Iterator<Path> iterator  = path.relativize(file).iterator();

                                           while (iterator.hasNext())
                                           {
                                               String name = iterator.next().toString();

                                               if (iterator.hasNext())
                                               {
                                                   className.append(name).append('.');
                                               }
                                               else
                                               {
                                                   className.append(name.substring(0, name.length() - 6));
                                               }
                                           }

                                           try
                                           {
                                               Class<?> testClass = Class.forName(className.toString());

                                               if (predicate.test(testClass))
                                               {
                                                   testClasses.add(testClass);
                                               }
                                           }
                                           catch (ClassNotFoundException e)
                                           {
                                               // ignored - can't load class so do not add it to tests
                                           }
                                       }

                                       return FileVisitResult.CONTINUE;
                                   }
                               });

            return testClasses;
        }
    }


    /**
     * A {@link Predicate} that uses an inner predicate as well as this {@link TestClasses}
     * include and exclude patterns to filter {@link Class} files.
     */
    public static class IncludeExcludePredicate implements Predicate<Class<?>>
    {
        /**
         * The included {@link Set} of {@link TestMatcher}s to evaluate a class.
         */
        private Set<TestMatcher> includePatterns = new HashSet<>();

        /**
         * The excluded {@link Set} of {@link TestMatcher}s to evaluate a class.
         */
        private Set<TestMatcher> excludePatterns = new HashSet<>();

        /**
         * The inner {@link Predicate} to use to verify classes.
         */
        private final Predicate<Class<?>> predicate;


        /**
         * Create a {@link IncludeExcludePredicate} that will use the specified
         * {@link Predicate} and include and exclude patters to evaluate classes.
         *
         * @param predicate        the {@link Predicate} to use to evaluate classes
         * @param includePatterns  the included {@link Set} of {@link TestMatcher}s
         *                         to evaluate a class
         * @param excludePatterns  the excluded {@link Set} of {@link TestMatcher}s
         *                         to evaluate a class
         */
        protected IncludeExcludePredicate(Predicate<Class<?>> predicate,
                                          Set<TestMatcher>    includePatterns,
                                          Set<TestMatcher>    excludePatterns)
        {
            this.predicate       = predicate;
            this.includePatterns = includePatterns;
            this.excludePatterns = excludePatterns;
        }


        @Override
        public boolean test(Class<?> testClass)
        {
            if (testClass == null)
            {
                return false;
            }

            if (!predicate.test(testClass))
            {
                return false;
            }

            String  className = testClass.getCanonicalName();
            boolean include   = includePatterns.isEmpty();

            for (TestMatcher matcher : includePatterns)
            {
                if (!matcher.hasClassPattern() || matcher.matches(className))
                {
                    include = true;
                    break;
                }
            }

            if (include)
            {
                for (TestMatcher matcher : excludePatterns)
                {
                    if (!matcher.hasMethodPattern() && matcher.matches(className))
                    {
                        return false;
                    }
                }

                return true;
            }

            return false;
        }
    }


    /**
     * A {@link TestClasses} instance that uses a fixed set of {@link Class}es
     * as the set of tests to execute.
     */
    public static class SpecificClasses extends TestClasses implements Serializable
    {
        /**
         * The {@link Set} of names of test classes.
         */
        private Set<String> classNames;

        /**
         * The {@link Set} resolved of test {@link Class}es.
         */
        private transient Set<Class<?>> classes;


        /**
         * Create a {@link SpecificClasses} instance with the specified
         * test {@link Class}es.
         *
         * @param classes  the test {@link Class}es
         */
        private SpecificClasses(Class<?>... classes)
        {
            this.classNames = new HashSet<>(classes.length);

            for (Class<?> cls : classes)
            {
                this.classNames.add(cls.getCanonicalName());
            }
        }


        @Override
        public Set<Class<?>> resolveTestClasses()
        {
            // have we already resolved the list of classes
            if (this.classes == null)
            {
                synchronized (this)
                {
                    // No, then synchronize and check again
                    if (this.classes == null)
                    {
                        Set<Class<?>>       classes   = new HashSet<>();
                        Predicate<Class<?>> predicate = getTestClassPredicate();

                        // Add each class from the list that matches the predicate
                        for (String className : classNames)
                        {
                            try
                            {
                                Class<?> testClass = Class.forName(className);

                                if (predicate.test(testClass))
                                {
                                    classes.add(testClass);
                                }
                            }
                            catch (ClassNotFoundException e)
                            {
                                // cannot load the class so it will be skipped
                            }
                        }

                        this.classes = classes;
                    }
                }
            }

            return Collections.unmodifiableSet(classes);
        }


        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }

            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            SpecificClasses that = (SpecificClasses) o;

            return classNames.equals(that.classNames);

        }


        @Override
        public int hashCode()
        {
            return classNames.hashCode();
        }


        @Override
        public String toString()
        {
            return "TestClasses(" + "classNames=" + classNames + ')';
        }
    }


    /**
     * A JUnit test {@link Filter} that filters test {@link Class}
     * names and method names based on the set of included and
     * excluded {@link TestMatcher}s.
     */
    protected static class TestFilter extends Filter
    {
        /**
         * The {@link Set} of {@link TestMatcher} to determine the tests
         * to include in a test run.
         */
        private final Set<TestMatcher> includePatterns;

        /**
         * The {@link Set} of {@link TestMatcher} to determine the tests
         * to exclude from a test run.
         */
        private final Set<TestMatcher> excludePatterns;


        /**
         * Create a {@link TestFilter} that will use the specified include
         * and exclude patterns.
         *
         * @param includePatterns  the include patters to use
         * @param excludePatterns  the exclude patters to use
         */
        public TestFilter(Set<TestMatcher> includePatterns,
                          Set<TestMatcher> excludePatterns)
        {
            this.includePatterns = includePatterns;
            this.excludePatterns = excludePatterns;
        }


        @Override
        public String describe()
        {
            return "TestClasses Filter";
        }


        @Override
        public boolean shouldRun(Description description)
        {
            if (description.isSuite())
            {
                return true;
            }

            if (!description.isTest())
            {
                return false;
            }

            String name       = description.getDisplayName();
            String className  = null;
            String methodName = null;

            if (name != null)
            {
                name = name.trim();

                if (name.endsWith(")"))
                {
                    int index = name.lastIndexOf('(');

                    if (index != -1)
                    {
                        className  = name.substring(index + 1, name.length() - 1).trim();
                        methodName = name.substring(0, index).trim();
                    }
                }
            }
            else
            {
                return false;
            }

            if (className == null || className.isEmpty() || methodName == null || methodName.isEmpty())
            {
                return false;
            }

            boolean included = includePatterns.isEmpty();

            for (TestMatcher matcher : includePatterns)
            {
                if (matcher.matches(className, methodName))
                {
                    included = true;
                    break;
                }
            }

            if (!included)
            {
                return false;
            }

            for (TestMatcher matcher : excludePatterns)
            {
                if (matcher.matches(className, methodName))
                {
                    return false;
                }
            }

            return true;
        }
    }


    /**
     * A class that matches a class and method name
     * based on a pair of regular expressions.
     */
    protected static class TestMatcher implements Serializable
    {
        /**
         * The regular expression to use to match class names.
         */
        private String classPattern;

        /**
         * The regular expression to use to match method names.
         */
        private String methodPattern;


        /**
         * Create a {@link TestMatcher} using the specified pattern.
         * <p>
         * If the pattern contains a '#' charater it will be used to
         * split the pattern where the first part will be the class
         * name matcher and the second part will be the method name matcher.
         * <p>
         * If the pattern contains no '#' character then the whole pattern
         * will be used to match class names and method names will not be
         * filtered, i.e. all method names match.
         *
         * @param pattern  the pattern to use to match class and method names
         */
        protected TestMatcher(String pattern)
        {
            int index = pattern.indexOf('#');

            if (index < 0)
            {
                classPattern = pattern.trim();
            }
            else if (index == 0)
            {
                methodPattern = pattern.substring(1).trim();
            }
            else
            {
                classPattern  = pattern.substring(0, index).trim();
                methodPattern = pattern.substring(index + 1, pattern.length()).trim();
            }
        }


        /**
         * Obtain the regular expression that will be used to match test class names.
         *
         * @return  the regular expression that will be used to match test class names
         */
        public String getClassPattern()
        {
            return classPattern;
        }


        /**
         * Obtain the regular expression that will be used to match test method names.
         *
         * @return  the regular expression that will be used to match test method names
         */
        public String getMethodPattern()
        {
            return methodPattern;
        }


        /**
         * Determine whether the specified class name matches this {@link TestMatcher}'s
         * class name pattern.
         *
         * @param className  the class name to match
         *
         * @return  true if the specified class name matches this {@link TestMatcher}'s
         *          class name pattern, otherwise false
         */
        public boolean matches(String className)
        {
            return classPattern == null || className.matches(classPattern);
        }


        /**
         * Determine whether the specified class name matches this {@link TestMatcher}'s
         * class name pattern and specified mathd name matches this {@link TestMatcher}'s
         * method name pattern.
         *
         * @param className   the class name to match
         * @param methodName  the method name to match
         *
         * @return  true if the specified class name and method name matches this {@link TestMatcher}'s
         *          patterns, otherwise false
         */
        public boolean matches(String className,
                               String methodName)
        {
            if (classPattern == null || className.matches(classPattern))
            {
                return methodPattern == null || methodName.matches(methodPattern);
            }

            return false;
        }


        /**
         * Indicate whether this {@link TestMatcher} has a class pattern.
         *
         * @return  true if this {@link TestMatcher} has a class pattern
         */
        public boolean hasClassPattern()
        {
            return classPattern != null &&!classPattern.isEmpty();
        }


        /**
         * Indicate whether this {@link TestMatcher} has a method pattern.
         *
         * @return  true if this {@link TestMatcher} has a method pattern
         */
        public boolean hasMethodPattern()
        {
            return methodPattern != null &&!methodPattern.isEmpty();
        }


        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }

            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            TestMatcher that = (TestMatcher) o;

            if (classPattern != null ? !classPattern.equals(that.classPattern) : that.classPattern != null)
            {
                return false;
            }

            return methodPattern != null ? methodPattern.equals(that.methodPattern) : that.methodPattern == null;
        }


        @Override
        public int hashCode()
        {
            int result = classPattern != null ? classPattern.hashCode() : 0;

            result = 31 * result + (methodPattern != null ? methodPattern.hashCode() : 0);

            return result;
        }


        @Override
        public String toString()
        {
            return "TestMatcher(" + "classPattern='" + classPattern + '\'' + ", methodPattern='" + methodPattern + '\''
                   + ')';
        }
    }
}
