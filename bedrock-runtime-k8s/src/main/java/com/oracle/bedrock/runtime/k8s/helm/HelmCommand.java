/*
 * File: HelmCommand.java
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

package com.oracle.bedrock.runtime.k8s.helm;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.SimpleApplication;
import com.oracle.bedrock.runtime.options.Arguments;
import com.oracle.bedrock.runtime.options.EnvironmentVariables;
import com.oracle.bedrock.runtime.options.Executable;

import java.io.File;
import java.net.URL;
import java.util.Objects;

/**
 * An immutable representation of a Helm command that can be executed
 * by a Bedrock {@link Platform}.
 * <p>
 * Copyright (c) 2018. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @param <C>  the type of the command
 *
 * @author Jonathan Knight
 */
public abstract class HelmCommand<C extends HelmCommand>
        implements CLI<C>
{
    /**
     * The default location of the Helm executable.
     */
    public static final String DEFAULT_HELM = System.getProperty("bedrock.helm", "/usr/local/bin/helm");

    /**
     * The Helm executable to run when executing this command.
     */
    private final String helmExecutable;

    /**
     * The Helm command to execute.
     */
    private final String[] commands;

    /**
     * The arguments to this command.
     */
    private final Arguments arguments;

    /**
     * The flags to this command.
     */
    private final Arguments flags;

    /**
     * The environment variables to this command.
     */
    private final EnvironmentVariables environmentVariables;

    /**
     * {@code true} if flags come before arguments in the command line for this command.
     */
    private final boolean flagsFirst;

    /**
     * Create a Helm command.
     *
     * @param helmExecutable  the Helm executable to invoke
     * @param arguments       the command arguments
     * @param flags           the command flags
     * @param env             the environment variables to apply to the command execution
     * @param flagsFirst      {@code true} to indicate that flags come before arguments
     * @param commands        the Helm command to execute
     */
    HelmCommand(String               helmExecutable,
                Arguments            arguments,
                Arguments            flags,
                EnvironmentVariables env,
                boolean              flagsFirst,
                String... commands)
    {
        this.helmExecutable       = helmExecutable == null || helmExecutable.trim().isEmpty() ? DEFAULT_HELM : helmExecutable;
        this.arguments            = Objects.requireNonNull(arguments);
        this.flags                = Objects.requireNonNull(flags);
        this.environmentVariables = Objects.requireNonNull(env);
        this.flagsFirst           = flagsFirst;
        this.commands             = Objects.requireNonNull(commands);
    }


    /**
     * Obtain the Helm executable to run.
     *
     * @return  the Helm executable to run
     */
    public String getHelmLocation()
    {
        return helmExecutable == null ? DEFAULT_HELM : helmExecutable;
    }

    /**
     * Determine whether the Helm executable that this command
     * would use exists.
     *
     * @return  {@code true} if the Helm executable exists
     */
    public boolean helmExists()
    {
        File file = new File(helmExecutable);

        return file.exists() && file.canExecute();
    }

    /**
     * Obtain the Helm command that will be executed.
     *
     * @return  the Helm command that will be executed
     */
    public String[] getCommands()
    {
        return commands;
    }

    /**
     * Obtain the arguments to this command.
     *
     * @return  the arguments to this command
     */
    public Arguments getArguments()
    {
        return arguments;
    }

    /**
     * Obtain the environment variables for this command.
     *
     * @return  the environment variables for this command
     */
    public EnvironmentVariables getEnvironment()
    {
        return environmentVariables;
    }

    /**
     * Obtain the flags to this command.
     *
     * @return  the flags to this command
     */
    public Arguments getFlags()
    {
        return flags;
    }

    /**
     * Determine whether the flags appear at the start or end of the command line.
     *
     * @return  {@code true} if the flags appear at the start or end of
     *          the command line
     */
    public boolean isFlagsFirst()
    {
        return flagsFirst;
    }

    /**
     * Execute this Helm command on the {@link LocalPlatform}.
     *
     * @param options  any options to apply to the process
     *
     * @return  the {@link Application} representing the Helm command execution
     */
    public Application execute(Option... options)
    {
        return LocalPlatform.get().launch(this, options);
    }

    /**
     * Execute this Helm command on the {@link LocalPlatform} and wait for the command to complete.
     *
     * @param options  any options to apply to the process
     *
     * @return  the return code from the completed command.
     */
    public int executeAndWait(Option... options)
    {
        try (Application app = LocalPlatform.get().launch(this, options))
        {
            return app.waitFor(options);
        }
    }

    @Override
    public Class<? extends Application> getImplementationClass(Platform platform, OptionsByType options)
    {
        return SimpleApplication.class;
    }

    @Override
    public void onLaunching(Platform platform, OptionsByType optionsByType)
    {
        optionsByType.add(Executable.named(getHelmLocation()));

        Arguments args = optionsByType.get(Arguments.class);

        for (String cmd : commands)
        {
            args = args.with(cmd);
        }

        if (flagsFirst)
        {
            args = args.with(flags)
                       .with(arguments);
        }
        else
        {
            args = args.with(arguments)
                       .with(flags);
        }

        optionsByType.add(args);


        if (environmentVariables != null)
        {
            EnvironmentVariables env = optionsByType.get(EnvironmentVariables.class);
            optionsByType.add(env.with(environmentVariables));
        }
    }

    @Override
    public void onLaunch(Platform platform, OptionsByType optionsByType)
    {
        // there is nothing to do here
    }

    @Override
    public void onLaunched(Platform platform, Application application, OptionsByType optionsByType)
    {
        // there is nothing to do here
    }


    /**
     * A generic Helm command.
     */
    public static class Template
            extends HelmCommand<Template>
    {
        /**
         * Create a generic Helm command.
         *
         * @param flags  the flags to apply
         */
        Template(Arguments flags)
        {
            super(null, Arguments.empty(), flags, EnvironmentVariables.custom(), false);
        }

        Template(String helm, Arguments arguments, Arguments flags, EnvironmentVariables env)
        {
            super(helm, arguments, flags, env, false);
        }

        @Override
        public Template newInstance(String helm, Arguments arguments, Arguments flags, EnvironmentVariables env)
        {
            return new Template(getHelmLocation(), arguments, flags, env);
        }


        /**
         * Create a new chart with the given name.
         *
         * @param name  the name of the chart to create
         */
        public HelmCreate create(String name)
        {
            return new HelmCreate(name)
                    .withHelmAt(getHelmLocation())
                    .withArguments(getArguments())
                    .withFlags(getFlags())
                    .withEnvironment(getEnvironment());
        }

        /**
         * Given a release name, delete the release from Kubernetes.
         *
         * @param name  the release to delete
         */
        public HelmDelete delete(String name)
        {
            return new HelmDelete(name)
                    .withHelmAt(getHelmLocation())
                    .withArguments(getArguments())
                    .withFlags(getFlags())
                    .withEnvironment(getEnvironment());
        }

        /**
         * Manage a chart's dependencies - rebuild the charts/ directory
         * based on the requirements.lock file.
         *
         * @param chart  the chart name
         */
        public HelmDependencyBuild dependencyBuild(String chart)
        {
            return new HelmDependencyBuild(chart)
                        .withHelmAt(getHelmLocation())
                        .withArguments(getArguments())
                        .withFlags(getFlags())
                        .withEnvironment(getEnvironment());
        }

        /**
         * Manage a chart's dependencies - list the dependencies for the given chart.
         *
         * @param chart  the chart name
         */
        public HelmDependencyList dependencyList(String chart)
        {
            return new HelmDependencyList(chart)
                    .withHelmAt(getHelmLocation())
                    .withArguments(getArguments())
                    .withFlags(getFlags())
                    .withEnvironment(getEnvironment());
        }

        /**
         * Manage a chart's dependencies - update charts/ based on the contents
         * of requirements.yaml.
         *
         * @param chart  the chart name
         */
        public HelmDependencyUpdate dependencyUpdate(String chart)
        {
            return new HelmDependencyUpdate(chart)
                    .withHelmAt(getHelmLocation())
                    .withArguments(getArguments())
                    .withFlags(getFlags())
                    .withEnvironment(getEnvironment());
        }

        /**
         * Download a chart from a repository and (optionally) unpack it in local directory.
         *
         * @param charts  the charts to download
         */
        public HelmFetch fetch(String... charts)
        {
            return new HelmFetch(charts)
                    .withHelmAt(getHelmLocation())
                    .withArguments(getArguments())
                    .withFlags(getFlags())
                    .withEnvironment(getEnvironment());
        }

        /**
         * Download a named release.
         */
        public HelmGet get(String releaseName)
        {
            return new HelmGet(releaseName)
                    .withHelmAt(getHelmLocation())
                    .withArguments(getArguments())
                    .withFlags(getFlags())
                    .withEnvironment(getEnvironment());
        }

        /**
         * Download all hooks for a named release.
         *
         * @param releaseName  the name of the release
         */
        public HelmGetHooks getHooks(String releaseName)
        {
            return new HelmGetHooks(releaseName)
                    .withHelmAt(getHelmLocation())
                    .withArguments(getArguments())
                    .withFlags(getFlags())
                    .withEnvironment(getEnvironment());
        }

        /**
         * Download the manifest for a named release.
         *
         * @param releaseName  the name of the release
         */
        public HelmGetManifest getManifest(String releaseName)
        {
            return new HelmGetManifest(releaseName)
                    .withHelmAt(getHelmLocation())
                    .withArguments(getArguments())
                    .withFlags(getFlags())
                    .withEnvironment(getEnvironment());
        }

        /**
         * Download all values file for a named release.
         *
         * @param releaseName  the name of the release
         */
        public HelmGetValues getValues(String releaseName)
        {
            return new HelmGetValues(releaseName)
                    .withHelmAt(getHelmLocation())
                    .withArguments(getArguments())
                    .withFlags(getFlags())
                    .withEnvironment(getEnvironment());
        }

        /**
         * Fetch release history.
         *
         * @param releaseName  the name of the release
         */
        public HelmHistory history(String releaseName)
        {
            return new HelmHistory(releaseName)
                    .withHelmAt(getHelmLocation())
                    .withArguments(getArguments())
                    .withFlags(getFlags())
                    .withEnvironment(getEnvironment());
        }

        /**
         * Initialize Helm on both client and server.
         */
        public HelmInit init()
        {
            return new HelmInit(getHelmLocation(), getArguments(), getFlags(), getEnvironment());
        }

        /**
         * Create a Helm inspect command.
         *
         * @param chart  the chart to inspect
         */
        public HelmInspect inspect(String chart)
        {
            return new HelmInspect(getHelmLocation(), Arguments.of(chart).with(getArguments()), getFlags(), getEnvironment());
        }

        /**
         * Create a Helm inspect command.
         *
         * @param chart  the chart to inspect
         */
        public HelmInspect inspect(File chart)
        {
            return new HelmInspect(getHelmLocation(), Arguments.of(chart).with(getArguments()), getFlags(), getEnvironment());
        }

        /**
         * Create a Helm inspect command.
         *
         * @param chart  the chart to inspect
         */
        public HelmInspect inspect(URL chart)
        {
            return new HelmInspect(getHelmLocation(), Arguments.of(chart).with(getArguments()), getFlags(), getEnvironment());
        }

        /**
         * Install a chart archive.
         *
         * @param chart  the name of the chart
         */
        public HelmInstall install(String chart)
        {
            return new HelmInstall(getHelmLocation(), Arguments.of(chart).with(getArguments()), getFlags(), getEnvironment());
        }

        /**
         * Install a chart archive.
         *
         * @param chart  the name of the chart
         */
        public HelmInstall install(File chart)
        {
            return new HelmInstall(getHelmLocation(), Arguments.of(chart).with(getArguments()), getFlags(), getEnvironment());
        }

        /**
         * Install a chart archive.
         *
         * @param directory  the directory containing the chart directory
         * @param chart      the name of the chart
         */
        public HelmInstall install(File directory, String chart)
        {
            File file = new File(directory, chart);

            return new HelmInstall(getHelmLocation(), Arguments.of(file).with(getArguments()), getFlags(), getEnvironment());
        }

        /**
         * Examines a chart for possible issues.
         *
         * @param chart  the name of the chart
         */
        public HelmLint lint(String chart)
        {
            return new HelmLint(getHelmLocation(), Arguments.of(chart).with(getArguments()), getFlags(), getEnvironment());
        }

        /**
         * Examines a chart for possible issues.
         *
         * @param chart  the name of the chart
         */
        public HelmLint lint(File chart)
        {
            return new HelmLint(getHelmLocation(), Arguments.of(chart).with(getArguments()), getFlags(), getEnvironment());
        }

        /**
         * Examines a chart for possible issues.
         *
         * @param directory  the directory containing the chart directory
         * @param chart      the name of the chart
         */
        public HelmLint lint(File directory, String chart)
        {
            File file = new File(directory, chart);

            return new HelmLint(getHelmLocation(), Arguments.of(file).with(getArguments()), getFlags(), getEnvironment());
        }

        /**
         * List releases.
         */
        public HelmList list()
        {
            return new HelmList();
        }

        /**
         * List releases with a filter.
         *
         * @param filter  the Helm list command's filter argument
         */
        public HelmList list(String filter)
        {
            return new HelmList(filter);
        }

        /**
         * Roll back a release to a previous revision.
         *
         * @param release   the name of the release
         * @param revision  the revision to rollback to
         */
        public HelmRollback rollback(String release, String revision)
        {
            return new HelmRollback(getHelmLocation(), Arguments.of(release, revision).with(getArguments()), getFlags(), getEnvironment());
        }

        /**
         * Displays the status of the named release.
         *
         * @param release  the name of the release to test
         */
        public HelmStatus status(String release)
        {
            return new HelmStatus(getHelmLocation(), Arguments.of(release).with(getArguments()), getFlags(), getEnvironment());
        }

        /**
         * Test a release.
         *
         * @param release  the name of the release to test
         */
        public HelmTest test(String release)
        {
            return new HelmTest(getHelmLocation(), Arguments.of(release).with(getArguments()), getFlags(), getEnvironment());
        }

        /**
         * Upgrade a release.
         *
         * @param release  the name of the release to be upgraded
         * @param chart    the name of the chart to use to upgrade the release
         */
        public HelmUpgrade upgrade(String release, String chart)
        {
            return new HelmUpgrade(getHelmLocation(), Arguments.of(release, chart).with(getArguments()), getFlags(), getEnvironment());
        }

        /**
         * Upgrade a release.
         *
         * @param release  the name of the release to be upgraded
         * @param chart    the name of the chart to use to upgrade the release
         */
        public HelmUpgrade upgrade(String release, File chart)
        {
            return new HelmUpgrade(getHelmLocation(), Arguments.of(release, chart).with(getArguments()), getFlags(), getEnvironment());
        }
    }

}
