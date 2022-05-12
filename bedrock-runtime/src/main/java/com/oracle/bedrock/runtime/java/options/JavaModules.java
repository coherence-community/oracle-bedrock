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

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
     * The modules to exclude.
     */
    private final Set<String> excludes;

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
     * The system property to check to determine whether modules should be used.
     */
    public static final String PROP_USE_MODULES = "com.oracle.bedrock.modules.enabled";

    /**
     * Constructs a {@link JavaModules} for the specified value.
     *
     * @param enabled if modular mode is enabled
     * @param modules the set of modules to add and export to bedrock
     */
    private JavaModules(boolean     enabled,
                        Set<String> modules,
                        Set<String> excludes,
                        Set<String> exports,
                        Set<String> patches,
                        Set<String> reading,
                        Set<String> opens,
                        ClassPath   classPath)
    {
        this.enabled   = enabled;
        this.modules   = modules;
        this.excludes  = excludes;
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
        return new JavaModules(true, Collections.emptySet(), Collections.emptySet(), Collections.emptySet(),
                               Collections.emptySet(), Collections.emptySet(),
                               Collections.emptySet(), ClassPath.ofSystem());
    }


    /**
     * Obtains a {@link JavaModules} option that enables
     * running an application as a Java process using class
     * path instead of modules.
     *
     * @return a {@link JavaModules} option to disable
     *         a modular JVM process
     */
    public static JavaModules disabled()
    {
        return new JavaModules(false, Collections.emptySet(), Collections.emptySet(), Collections.emptySet(),
                               Collections.emptySet(), Collections.emptySet(),
                               Collections.emptySet(), ClassPath.ofSystem());
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
    public static JavaModules automatic()
    {
        RuntimeMXBean bean      = ManagementFactory.getRuntimeMXBean();
        List<String>  arguments = bean.getInputArguments();
        return automatic(useModules(), arguments);
    }


    static JavaModules automatic(boolean useModules, List<String>  arguments)
    {
        Set<String> patches = new HashSet<>();
        Set<String> modules = new HashSet<>();
        Set<String> reads   = new HashSet<>();
        Set<String> opens   = new HashSet<>();
        Set<String> exports = new HashSet<>();
        int         end     = arguments.size() - 1;

        for (int i = 0; i <= end; i++)
        {
            String arg = arguments.get(i);

            if (arg.startsWith("--patch-module="))
            {
                patches.add(arg.substring(15));
            }
            else if (arg.equals("--patch-module") && i < end)
            {
                patches.add(arguments.get(++i));
            }
            else if (arg.startsWith("--add-modules="))
            {
                modules.add(arg.substring(14));
            }
            else if (arg.equals("--add-modules") && i < end)
            {
                modules.add(arguments.get(++i));
            }
            else if (arg.startsWith("--add-reads="))
            {
                reads.add(arg.substring(12));
            }
            else if (arg.equals("--add-reads") && i < end)
            {
                reads.add(arguments.get(++i));
            }
            else if (arg.startsWith("--add-opens="))
            {
                opens.add(arg.substring(12));
            }
            else if (arg.equals("--add-opens") && i < end)
            {
                opens.add(arguments.get(++i));
            }
            else if (arg.startsWith("--add-exports="))
            {
                exports.add(arg.substring(14));
            }
            else  if (arg.equals("--add-exports") && i < end)
            {
                exports.add(arguments.get(++i));
            }
        }

        return new JavaModules(useModules, modules, Collections.emptySet(), exports,
                               patches, reads, opens, ClassPath.ofSystem());
    }

    /**
     * Returns {@code true} if modules should be used, based on the
     * value of the {@link #PROP_USE_MODULES} system property.
     *
     * @return {@code true} if modules should be used
     */
    public static boolean useModules()
    {
        String  useModulesProperty = System.getProperty(PROP_USE_MODULES);
        if (useModulesProperty == null || useModulesProperty.isBlank())
        {
            String  modulePath = System.getProperty("jdk.module.path");
            return modulePath != null && !modulePath.isBlank();
        }
        return Boolean.parseBoolean(useModulesProperty);
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

        return new JavaModules(true, toSet(this.modules, modules), this.excludes, this.exports, this.patches,
                               this.reading, this.opens, this.classPath);
    }


    /**
     * Obtain a copy of this {@link JavaModules} option
     * excluding the specified modules from the {@code --add-modules}
     * option.
     *
     * @param modules  the modules to exclude from the {@code --add-modules}
     *                 JVM option
     *
     * @return  a copy of this {@link JavaModules} option
     *          with the specified modules excluded from the
     *          {@code --add-modules} option.
     */
    public JavaModules excluding(String... modules)
    {
        if (modules.length == 0)
        {
            return this;
        }

        return new JavaModules(true, this.modules, toSet(this.excludes, modules), this.exports, this.patches,
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
        return new JavaModules(true, this.modules, this.excludes, toSet(this.exports, exports),
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

        return new JavaModules(true, this.modules, this.excludes, exports, this.patches,
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

        return new JavaModules(true, this.modules, this.excludes, this.exports, this.patches,
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

        return new JavaModules(true, this.modules, this.excludes, this.exports, toSet(this.patches, patches),
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

        return new JavaModules(true, this.modules, this.excludes, this.exports, this.patches,
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
        if (classPath == null)
        {
            classPath = ClassPath.ofSystem();
        }
        return new JavaModules(true, this.modules, this.excludes, this.exports, this.patches,
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

        return new JavaModules(true, this.modules, this.excludes, this.exports, this.patches,
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
        Set<String> setAdd     = new LinkedHashSet<>(this.modules);
        Set<String> setExclude = new LinkedHashSet<>(this.excludes);
        Set<String> setExport  = new LinkedHashSet<>(this.exports);
        Set<String> setPatch   = new LinkedHashSet<>(this.patches);
        Set<String> setReads   = new LinkedHashSet<>(this.reading);
        Set<String> setOpens   = new LinkedHashSet<>(this.opens);

        setAdd.addAll(other.modules);
        setExclude.addAll(other.excludes);
        setExport.addAll(other.exports);
        setPatch.addAll(other.patches);
        setReads.addAll(other.reading);
        setOpens.addAll(other.opens);

        boolean   isEnabled = this.enabled && other.enabled;
        ClassPath classPath = new ClassPath(this.classPath, other.classPath);

        return new JavaModules(isEnabled, setAdd, setExclude, setExport, setPatch, setReads, setOpens, classPath);
    }


    @Override
    public Iterable<String> resolve(OptionsByType optionsByType)
    {
        List<String> opts = new ArrayList<>();

        if (enabled)
        {
            if (!modules.isEmpty())
            {
                // syntax: --add-modules module[,module...]
                opts.add("--add-modules");
                if (excludes.isEmpty())
                {
                    opts.add(String.join(",", modules));
                }
                else
                {
                    LinkedHashSet<String> set = new LinkedHashSet<>(modules);
                    set.removeAll(excludes);
                    opts.add(String.join(",", set));
                }
            }

            if (!exports.isEmpty())
            {
                // syntax: --add-exports module/package=target-module(,target-module)*
                //         may be used more than once
                exports.forEach(export -> {
                    opts.add("--add-exports");
                    opts.add(export);
                });
            }

            if (!opens.isEmpty())
            {
                // syntax: --add-opens module/package=target-module(,target-module)*
                //         may be used more than once
                opens.forEach(open -> {
                    opts.add("--add-opens");
                    opts.add(open);
                });
            }

            if (!patches.isEmpty())
            {
                // syntax: --patch-module module=file(;file)*
                //         may be used more than once
                patches.forEach(patch -> {
                    opts.add("--patch-module");
                    opts.add(patch);
                });
            }

            if (!reading.isEmpty())
            {
                // syntax: --add-reads module=target-module(,target-module)*
                //         may be used more than once
                reading.forEach(read -> {
                    opts.add("--add-reads");
                    opts.add(read);
                });
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
        if (excludes != null ? !excludes.equals(modules1.excludes) : modules1.excludes != null)
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
        if (opens != null ? !opens.equals(modules1.opens) : modules1.opens != null)
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
        result = 31 * result + (excludes != null ? excludes.hashCode() : 0);
        result = 31 * result + (exports != null ? exports.hashCode() : 0);
        result = 31 * result + (patches != null ? patches.hashCode() : 0);
        result = 31 * result + (opens != null ? opens.hashCode() : 0);
        result = 31 * result + (reading != null ? reading.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "JavaModules(" + String.join(" ", resolve(OptionsByType.empty())) + ")";
    }
}
