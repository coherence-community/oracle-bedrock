package com.oracle.tools.runtime.java;

import com.oracle.tools.Option;

/**
 * An {@link Option} to define modifications that can be
 * applied to a Java class path String.
 *
 * @author jk 2015.01.15
 */
public class ClassPathModifier
    implements Option
{
    /**
     * Apply any required modifications to the original class path String.
     *
     * @param classPath  the class path String to modify
     *
     * @return the modified class path String.
     */
    public String modify(String classPath)
    {
        return classPath;
    }


    /**
     * Obtain a ClassPathModifier that performs no modification
     * on a class path String.
     *
     * @return a ClassPathModifier that performs no modification
     *         on a class path String
     */
    public static ClassPathModifier none()
    {
        return new ClassPathModifier();
    }

    /**
     * Obtain a ClassPathModifier that performs modifications
     * to the class path required by Cygwin platforms.
     *
     * @return a ClassPathModifier that performs modifications
     *         to the class path required by Cygwin platforms
     */
    public static ClassPathModifier forCygwin()
    {
        return new CygwinModifier();
    }

    /**
     * An implementation of {@link ClassPathModifier} that
     * applies modifications for the Cygwin platform.
     */
    private static class CygwinModifier
        extends ClassPathModifier
    {
        @Override
        public String modify(String classPath)
        {
            return String.format("$(cygpath -wp %s)", classPath);
        }
    }
}
