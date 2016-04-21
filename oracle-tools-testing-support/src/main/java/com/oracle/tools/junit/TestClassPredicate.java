/*
 * File: TestClassPredicate.java
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

package com.oracle.tools.junit;

import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Predicate;

/**
 * A {@link Predicate} that can be used to determine whether a
 * {@link Class} is a valid JUnit test or suite class.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class TestClassPredicate implements Predicate<Class<?>>, Serializable
{
    @Override
    public boolean test(Class<?> testClass)
    {
        if (testClass == null
            || testClass.isAnnotation()
            || testClass.isAnonymousClass()
            || testClass.isEnum()
            || testClass.isInterface())
        {
            return false;
        }

        return isJUnit3(testClass) || isJUnit4(testClass) || isJUnit48(testClass);
    }


    /**
     * Determine whether the specified {@link Class} is
     * a valid JUnit 3 {@link Class}.
     * <p>
     * A JUnit 3 test {@link Class} is a non-abstract class
     * extending {@link junit.framework.Test} or has a public
     * static suite() method.
     *
     * @param testClass  the {@link Class} to check
     *
     * @return  true if the specified {@link Class} is a
     *          valid JUnit 3 {@link Class}
     */
    public boolean isJUnit3(Class<?> testClass)
    {
        return !isAbstract(testClass)
               && (junit.framework.Test.class.isAssignableFrom(testClass) || hasSuiteMethod(testClass));
    }


    /**
     * Determine whether the specified {@link Class} is
     * a valid JUnit 4 {@link Class}.
     * <p>
     * A JUnit 4 test {@link Class} is a non-abstract class
     * having methods annotated with {@link org.junit.Test}
     * the {@link Class} itself is annotated with {@link RunWith}.
     *
     * @param testClass  the {@link Class} to check
     *
     * @return  true if the specified {@link Class} is a
     *          valid JUnit 4 {@link Class}
     */
    protected boolean isJUnit4(Class<?> testClass)
    {
        return !isAbstract(testClass) && (isAnnotatedWithRunWith(testClass) || hasMethodsAnnotatedWithTest(testClass));
    }


    /**
     * Determine whether the specified {@link Class} is
     * a valid JUnit 4.8 {@link Class}.
     * <p>
     * A JUnit 4.8 test {@link Class} is an abstract class
     * with {@link RunWith} where the runner is an instance
     * of the {@link Enclosed} runner.
     *
     * @param testClass  the {@link Class} to check
     *
     * @return  true if the specified {@link Class} is a
     *          valid JUnit 4.8 {@link Class}
     */
    protected boolean isJUnit48(Class<?> testClass)
    {
        return isAbstract(testClass) && usesRunWithEnclosed(testClass);
    }


    /**
     * Determine whether the specified {@link Class} is abstract.
     *
     * @param testClass  the {@link Class} to check
     *
     * @return  true if the specified {@link Class} is abstract
     */
    private boolean isAbstract(Class<?> testClass)
    {
        return Modifier.isAbstract(testClass.getModifiers());
    }


    /**
     * Determine whether the specified {@link Class} has a public
     * static suite() method.
     *
     * @param testClass  the {@link Class} to check
     *
     * @return  true if the specified {@link Class} has a public
     *          static suite() method
     */
    protected boolean hasSuiteMethod(Class<?> testClass)
    {
        try
        {
            Method suiteMethod = testClass.getDeclaredMethod("suite");

            if (suiteMethod != null)
            {
                int      modifiers  = suiteMethod.getModifiers();
                Class<?> returnType = suiteMethod.getReturnType();

                return Modifier.isPublic(modifiers)
                       && Modifier.isStatic(modifiers)
                       && junit.framework.Test.class.isAssignableFrom(returnType);
            }
        }
        catch (NoSuchMethodException e)
        {
            // ignored - does not have public static suite() method
        }

        return false;
    }


    /**
     * Determine whether the specified {@link Class} is annotated with
     * the JUnit {@link RunWith} annotation.
     *
     * @param testClass  the {@link Class} to check
     *
     * @return  true if the specified {@link Class} is annotated with
     *          the JUnit {@link RunWith} annotation
     */
    protected boolean isAnnotatedWithRunWith(Class<?> testClass)
    {
        RunWith runWith = testClass.getAnnotation(RunWith.class);

        return runWith != null;
    }


    /**
     * Determine whether the specified {@link Class} has any methods
     * annotated with the JUnit {@link org.junit.Test} annotation.
     *
     * @param testClass  the {@link Class} to check
     *
     * @return  true if the specified {@link Class} has any methods
     *          annotated with the JUnit {@link org.junit.Test} annotation.
     */
    protected boolean hasMethodsAnnotatedWithTest(Class<?> testClass)
    {
        for (Method method : testClass.getDeclaredMethods())
        {
            for (Annotation annotation : method.getAnnotations())
            {
                if (org.junit.Test.class.isAssignableFrom(annotation.annotationType()))
                {
                    return true;
                }
            }
        }

        Class<?> parent = testClass.getSuperclass();

        if (Object.class.equals(parent))
        {
            return false;
        }

        return hasMethodsAnnotatedWithTest(parent);
    }


    /**
     * Determine whether the specified {@link Class} is annotated with
     * the JUnit {@link RunWith} annotation and the runner specified
     * in the annotation is the {@link Enclosed} runner.
     *
     * @param testClass  the {@link Class} to check
     *
     * @return  true if the specified {@link Class} is annotated with
     *          the JUnit {@link RunWith} annotation and the runner
     *          specified in the annotation is the {@link Enclosed}
     *          runner
     */
    protected boolean usesRunWithEnclosed(Class<?> testClass)
    {
        RunWith runWith = testClass.getAnnotation(RunWith.class);

        if (runWith == null)
        {
            return false;
        }

        Class runnerClass = runWith.value();

        return runnerClass != null && Enclosed.class.isAssignableFrom(runnerClass);
    }
}
