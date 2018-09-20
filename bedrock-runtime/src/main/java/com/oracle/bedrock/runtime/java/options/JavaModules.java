/*
 * File: UseModules.java
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

package com.oracle.bedrock.runtime.java.options;

import com.oracle.bedrock.ComposableOption;
import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.java.ClassPath;
import com.oracle.bedrock.runtime.java.JavaApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An {@link Option} to specify that a {@link JavaApplication} should run using Java 9 modules.
 * <p>
 * Copyright (c) 2017. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author JK
 */
public class JavaModules implements ComposableOption<JavaModules>, JvmOption
{
    /**
     * Is modular mode enabled?
     */
    private final boolean enabled;

    /**
     * The modules to add.
     */
    private final Set<String> modules;

    /**
     * The modules to export.
     */
    private final Set<String> exports;

    /**
     * The modules to patch.
     */
    private final Set<String> patches;

    /**
     * The values to use in the --add-reads parameter.
     */
    private final Set<String> reading;

    /**
     * The values to use in the --add-opens parameter.
     */
    private final Set<String> opens;

    /**
     * The optional class path to use.
     */
    private final ClassPath classPath;

    /**
     * Constructs a {@link JavaModules} for the specified value.
     *
     * @param enabled if modular mode is enabled
     * @param modules the set of modules to add and export to bedrock
     */
    private JavaModules(boolean     enabled,
                        Set<String> modules,
                        Set<String> exports,
                        Set<String> patches,
                        Set<String> reading,
                        Set<String> opens,
                        ClassPath   classPath)
    {
        this.enabled   = enabled;
        this.modules   = modules;
        this.exports   = exports;
        this.patches   = patches;
        this.reading   = reading;
        this.opens     = opens;
        this.classPath = classPath;
    }


    /**
     * Obtains a {@link JavaModules} option that enables
     * running an application as a post Java 9 modular process.
     *
     * @return a {@link JavaModules} option to enable
     *         a modular JVM process
     */
    public static JavaModules enabled()
    {
        return new JavaModules(true, Collections.emptySet(), Collections.emptySet(),
                               Collections.emptySet(), Collections.emptySet(),
                               Collections.emptySet(), new ClassPath());
    }


    /**
     * Obtains a {@link JavaModules} option that enables
     * running an application as a Java process using class
     * path instead of modules.
     *
     * @return a {@link JavaModules} option to disable
     *         a modular JVM process
     */
    @OptionsByType.Default
    public static JavaModules disabled()
    {
        return new JavaModules(false, Collections.emptySet(), Collections.emptySet(),
                               Collections.emptySet(), Collections.emptySet(),
                               Collections.emptySet(), new ClassPath());
    }


    /**
     * Obtain a copy of this {@link JavaModules} option
     * with the specified modules in the {@code --add-modules}
     * option.
     *
     * @param modules  the modules to list in the {@code --add-modules}
     *                 JVM option
     *
     * @return  a copy of this {@link JavaModules} option
     *          with the specified modules in the
     *          {@code --add-modules} option.
     */
    public JavaModules adding(String... modules)
    {
        if (modules.length == 0)
        {
            return this;
        }

        return new JavaModules(true, toSet(this.modules, modules), this.exports, this.patches,
                               this.reading, this.opens, this.classPath);
    }


    /**
     * Obtain a copy of this {@link JavaModules} option
     * with the specified modules in the {@code --add-exports}
     * option.
     *
     * @param exports  the module export statement to list
     *                 in the {@code --add-exports} JVM option
     *
     * @return  a copy of this {@link JavaModules} option
     *          with the specified modules in the
     *          {@code --add-exports} option.
     */
    public JavaModules exporting(String... exports)
    {
        return new JavaModules(true, this.modules, toSet(this.exports, exports),
                               this.patches, this.reading, this.opens, this.classPath);
    }


    /**
     * Obtain a copy of this {@link JavaModules} option
     * with the specified modules in the {@code --add-exports}
     * option.
     *
     * @param toModule the name of the module to export to
     * @param modules  the modules to list in the
     *                 {@code --add-exports} JVM option
     *
     * @return  a copy of this {@link JavaModules} option
     *          with the specified modules in the
     *          {@code --add-exports} option
     */
    public JavaModules exportingTo(String toModule, String... modules)
    {
        if (modules.length == 0)
        {
            return this;
        }

        Set<String> exports = new LinkedHashSet<>(this.exports);

        Arrays.stream(modules)
              .map(m -> m + "/" + m + "=" + toModule)
              .forEach(exports::add);

        return new JavaModules(true, this.modules, exports, this.patches,
                               this.reading, this.opens, this.classPath);
    }


    /**
     * Obtain a copy of this {@link JavaModules} option
     * with the specified modules exported to
     * {@code com.oracle.bedrock.runtime} in the
     * {@code --add-exports} option.
     *
     * @param modules  the modules to export to Bedrock in the
     *                 {@code --add-exports} JVM option
     *
     * @return  a copy of this {@link JavaModules} option
     *          with the specified modules exported to
     *          {@code com.oracle.bedrock.runtime} in the
     *           {@code --add-exports} option
     */
    public JavaModules exportingToBedrock(String... modules)
    {
        return exportingTo("com.oracle.bedrock.runtime", modules);
    }


    /**
     * Obtain a copy of this {@link JavaModules} option
     * with the specified modules in the {@code --add-opens}
     * option.
     *
     * @param module   the module name to open
     * @param _package the name of the package within the provided
     *                 <code>module</code>
     * @param targetModule the name of the module the code within
     *                     the specified <code>module</code> and
     *                     <code>package</code> should be opened to
     *
     * @return  a copy of this {@link JavaModules} option
     *          with the specified modules/packages exported opened via
     *          the {@code --add-opens} option
     */
    public JavaModules opens(String module, String _package, String targetModule)
    {
        if (module == null || _package == null || targetModule == null)
        {
            return this;
        }

        Set<String> opens = new LinkedHashSet<>(this.opens);
        opens.add(module + '/' + _package + '=' + targetModule);

        return new JavaModules(true, this.modules, this.exports, this.patches,
                               this.reading, opens, this.classPath);
    }


    /**
     * Obtain a copy of this {@link JavaModules} option
     * with the specified patch module statements in
     * the {@code --patch-module} JVM option.
     *
     * @param patches  the patch module statements to list
     *                 in the {@code --patch-module} JVM option
     *
     * @return  a copy of this {@link JavaModules} option
     *          with the specified module patch statements in
     *          the {@code --patch-module} JVM option.
     */
    public JavaModules patching(String... patches)
    {
        if (patches.length == 0)
        {
            return this;
        }

        return new JavaModules(true, this.modules, this.exports, toSet(this.patches, patches),
                               this.reading, this.opens, this.classPath);
    }


    /**
     * Obtain a copy of this {@link JavaModules} option
     * with the specified reads module statements in
     * the {@code --add-reads} JVM option.
     *
     * @param reads  the reads module statements to list
     *               in the {@code --add-reads} JVM option
     *
     * @return  a copy of this {@link JavaModules} option
     *          with the specified reads module statements in
     *          the {@code --add-reads} JVM option.
     */
    public JavaModules reading(String... reads)
    {
        if (reads.length == 0)
        {
            return this;
        }

        return new JavaModules(true, this.modules, this.exports, this.patches,
                               toSet(this.reading, reads), this.opens, this.classPath);
    }


    /**
     * Set the {@link ClassPath} to use as well as a module path.
     *
     * @param classPath  the {@link ClassPath} to use as well as a module path
     *
     * @return  a copy of this {@link JavaModules} option
     *          with the specified classpath.
     */
    public JavaModules withClassPath(ClassPath classPath)
    {
        if (classPath == null || classPath.isEmpty())
        {
            return this;
        }

        return new JavaModules(true, this.modules, this.exports, this.patches,
                               this.reading, this.opens, classPath);

    }


    /**
     * Add a {@link ClassPath} to use as well as a module path, appending the
     * {@link ClassPath} to any other classpath that this {@link JavaModules}
     * may already have.
     *
     * @param classPath  a {@link ClassPath} to use as well as a module path
     *
     * @return  a copy of this {@link JavaModules} option
     *          with the specified addition of the classpath.
     */
    public JavaModules appendingToClassPath(ClassPath classPath)
    {
        if (classPath == null || classPath.isEmpty())
        {
            return this;
        }

        return new JavaModules(true, this.modules, this.exports, this.patches,
                               this.reading, this.opens, new ClassPath(this.classPath, classPath));

    }

    /**
     * Obtain the {@link ClassPath} to use as well as a module path.
     *
     * @return  the {@link ClassPath} to use as well as a module path
     */
    public ClassPath getClassPath()
    {
        return classPath;
    }

    /**
     * Determine whether a JVM application should be run with Modules.
     *
     * @return  {@code true} if a JVM application should be run with Modules
     */
    public boolean isEnabled()
    {
        return enabled;
    }


    private static Set<String> toSet(Set<String> set, String[] modules)
    {
        Set<String> moduleSet;

        if (modules == null || modules.length == 0)
        {
            moduleSet = set;
        }
        else
        {
            moduleSet = new LinkedHashSet<>(set);

            Arrays.stream(modules)
                    .filter(Objects::nonNull)
                    .forEach(moduleSet::add);

        }
        return moduleSet;
    }


    @Override
    public JavaModules compose(JavaModules other)
    {
        Set<String> setAdd    = new LinkedHashSet<>(this.modules);
        Set<String> setExport = new LinkedHashSet<>(this.exports);
        Set<String> setPatch  = new LinkedHashSet<>(this.patches);
        Set<String> setReads  = new LinkedHashSet<>(this.reading);
        Set<String> setOpens  = new LinkedHashSet<>(this.opens);

        setAdd.addAll(other.modules);
        setExport.addAll(other.exports);
        setPatch.addAll(other.patches);
        setReads.addAll(other.reading);
        setOpens.addAll(other.opens);

        boolean   isEnabled = this.enabled && other.enabled;
        ClassPath classPath = new ClassPath(this.classPath, other.classPath);

        return new JavaModules(isEnabled, setAdd, setExport, setPatch, setReads, setOpens, classPath);
    }


    @Override
    public Iterable<String> resolve(OptionsByType optionsByType)
    {
        List<String> opts = new ArrayList<>();

        if (enabled)
        {
            if (modules.size() > 0)
            {
                opts.add("--add-modules");
                opts.add(modules.stream().collect(Collectors.joining(",")));
            }

            if (exports.size() > 0)
            {
                opts.add("--add-exports");
                opts.add(exports.stream().collect(Collectors.joining(",")));
            }

            if (!opens.isEmpty())
            {
                opts.add("--add-opens");
                opts.add(opens.stream().collect(Collectors.joining(",")));
            }

            for (String patch : patches)
            {
                opts.add("--patch-module");
                opts.add(patch);
            }

            if (reading.size() > 0)
            {
                opts.add("--add-reads");
                opts.add(reading.stream().collect(Collectors.joining(",")));
            }
        }

        return opts;
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

        JavaModules modules1 = (JavaModules) o;

        if (enabled != modules1.enabled)
        {
            return false;
        }
        if (modules != null ? !modules.equals(modules1.modules) : modules1.modules != null)
        {
            return false;
        }
        if (exports != null ? !exports.equals(modules1.exports) : modules1.exports != null)
        {
            return false;
        }
        if (patches != null ? !patches.equals(modules1.patches) : modules1.patches != null)
        {
            return false;
        }
        return reading != null ? reading.equals(modules1.reading) : modules1.reading == null;
    }

    @Override
    public int hashCode()
    {
        int result = (enabled ? 1 : 0);
        result = 31 * result + (modules != null ? modules.hashCode() : 0);
        result = 31 * result + (exports != null ? exports.hashCode() : 0);
        result = 31 * result + (patches != null ? patches.hashCode() : 0);
        result = 31 * result + (reading != null ? reading.hashCode() : 0);
        return result;
    }
}
