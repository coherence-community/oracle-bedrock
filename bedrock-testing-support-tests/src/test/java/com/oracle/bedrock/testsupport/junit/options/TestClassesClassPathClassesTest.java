/*
 * File: TestClassesClassPathClassesTest.java
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

package com.oracle.bedrock.testsupport.junit.options;

import com.oracle.bedrock.testsupport.junit.AbstractJUnit4Test;
import com.oracle.bedrock.testsupport.junit.JUnit3Suite;
import com.oracle.bedrock.testsupport.junit.JUnit3Test;
import com.oracle.bedrock.testsupport.junit.JUnit4Test;
import com.oracle.bedrock.testsupport.junit.RunWithAnnotatedTest;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.java.ClassPath;
import com.oracle.bedrock.runtime.options.Argument;
import com.oracle.bedrock.runtime.options.Console;
import com.oracle.bedrock.runtime.options.WorkingDirectory;
import com.oracle.bedrock.testsupport.junit.options.TestClasses;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThat;

/**
 * Functional tests for {@link TestClasses.ClassPathClasses}
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class TestClassesClassPathClassesTest
{
    /**
     *Field description
     */
    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();


    @Test
    public void shouldNotLoadAbstractClass() throws Exception
    {
        File                         folder      = createClassesFolder(AbstractJUnit4Test.class);
        ClassPath                    classPath   = ClassPath.ofFile(folder);
        TestClasses.ClassPathClasses testClasses = new TestClasses.ClassPathClasses(classPath);
        Set<Class<?>>                classes     = testClasses.resolveTestClasses();

        assertThat(classes, is(notNullValue()));
        assertThat(classes.isEmpty(), is(true));
    }


    @Test
    public void shouldLoadJUnit3TestClass() throws Exception
    {
        File                         folder      = createClassesFolder(JUnit3Test.class);
        ClassPath                    classPath   = ClassPath.ofFile(folder);
        TestClasses.ClassPathClasses testClasses = new TestClasses.ClassPathClasses(classPath);
        Set<Class<?>>                classes     = testClasses.resolveTestClasses();

        assertThat(classes, is(notNullValue()));
        assertThat(classes, containsInAnyOrder(JUnit3Test.class));
    }


    @Test
    public void shouldLoadJUnit3TestClassFromJar() throws Exception
    {
        File                         jar         = createJar(JUnit3Test.class);
        ClassPath                    classPath   = ClassPath.ofFile(jar);
        TestClasses.ClassPathClasses testClasses = new TestClasses.ClassPathClasses(classPath);
        Set<Class<?>>                classes     = testClasses.resolveTestClasses();

        assertThat(classes, is(notNullValue()));
        assertThat(classes, containsInAnyOrder(JUnit3Test.class));
    }


    @Test
    public void shouldLoadJUnit3SuiteClass() throws Exception
    {
        File                         folder      = createClassesFolder(JUnit3Suite.class);
        ClassPath                    classPath   = ClassPath.ofFile(folder);
        TestClasses.ClassPathClasses testClasses = new TestClasses.ClassPathClasses(classPath);
        Set<Class<?>>                classes     = testClasses.resolveTestClasses();

        assertThat(classes, is(notNullValue()));
        assertThat(classes, containsInAnyOrder(JUnit3Suite.class));
    }


    @Test
    public void shouldLoadJUnit3SuiteClassFromJar() throws Exception
    {
        File                         jar         = createJar(JUnit3Suite.class);
        ClassPath                    classPath   = ClassPath.ofFile(jar);
        TestClasses.ClassPathClasses testClasses = new TestClasses.ClassPathClasses(classPath);
        Set<Class<?>>                classes     = testClasses.resolveTestClasses();

        assertThat(classes, is(notNullValue()));
        assertThat(classes, containsInAnyOrder(JUnit3Suite.class));
    }


    @Test
    public void shouldLoadJUnit4TestClass() throws Exception
    {
        File                         folder      = createClassesFolder(JUnit4Test.class);
        ClassPath                    classPath   = ClassPath.ofFile(folder);
        TestClasses.ClassPathClasses testClasses = new TestClasses.ClassPathClasses(classPath);
        Set<Class<?>>                classes     = testClasses.resolveTestClasses();

        assertThat(classes, is(notNullValue()));
        assertThat(classes, containsInAnyOrder(JUnit4Test.class));
    }


    @Test
    public void shouldLoadJUnit4TestClassFromJar() throws Exception
    {
        File                         jar         = createJar(JUnit4Test.class);
        ClassPath                    classPath   = ClassPath.ofFile(jar);
        TestClasses.ClassPathClasses testClasses = new TestClasses.ClassPathClasses(classPath);
        Set<Class<?>>                classes     = testClasses.resolveTestClasses();

        assertThat(classes, is(notNullValue()));
        assertThat(classes, containsInAnyOrder(JUnit4Test.class));
    }


    @Test
    public void shouldLoadRunWithAnnotatedTestClass() throws Exception
    {
        File                         folder      = createClassesFolder(RunWithAnnotatedTest.class);
        ClassPath                    classPath   = ClassPath.ofFile(folder);
        TestClasses.ClassPathClasses testClasses = new TestClasses.ClassPathClasses(classPath);
        Set<Class<?>>                classes     = testClasses.resolveTestClasses();

        assertThat(classes, is(notNullValue()));
        assertThat(classes, containsInAnyOrder(RunWithAnnotatedTest.class));
    }


    @Test
    public void shouldLoadRunWithAnnotatedTestClassFromJar() throws Exception
    {
        File                         jar         = createJar(RunWithAnnotatedTest.class);
        ClassPath                    classPath   = ClassPath.ofFile(jar);
        TestClasses.ClassPathClasses testClasses = new TestClasses.ClassPathClasses(classPath);
        Set<Class<?>>                classes     = testClasses.resolveTestClasses();

        assertThat(classes, is(notNullValue()));
        assertThat(classes, containsInAnyOrder(RunWithAnnotatedTest.class));
    }


    private File createJar(Class<?>... classes) throws Exception
    {
        File content = createClassesFolder(classes);
        File jar     = new File(content, "test.jar");

        try (Application app = LocalPlatform.get().launch("jar",
                                                          Argument.of("-cvf"),
                                                          Argument.of("test.jar"),
                                                          Argument.of("."),
                                                          WorkingDirectory.at(content),
                                                          Console.system()))
        {
            app.waitFor();
        }

        return jar;
    }


    private File createClassesFolder(Class<?>... classes) throws Exception
    {
        File content = temporaryFolder.newFolder();

        Files.createDirectories(content.toPath());

        for (Class<?> cls : classes)
        {
            URL    location    = cls.getProtectionDomain().getCodeSource().getLocation();
            Path   path        = Paths.get(location.toURI());

            String packageName = cls.getPackage().getName().replace(".", File.separator);

            Path sourcePath = Paths.get(path.toString(),
                                        cls.getCanonicalName().replace(".", File.separator) + ".class");
            Path contentPath = Paths.get(content.getCanonicalPath(), packageName);
            Path targetPath  = Paths.get(contentPath.toString(), cls.getSimpleName() + ".class");

            Files.createDirectories(contentPath);
            Files.copy(sourcePath, targetPath);
            System.out.println();
        }

        return content;
    }
}
