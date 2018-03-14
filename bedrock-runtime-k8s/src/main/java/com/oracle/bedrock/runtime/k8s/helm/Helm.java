/*
 * File: Helm.java
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

import com.oracle.bedrock.runtime.options.Arguments;

import java.io.File;
import java.net.URL;

/**
 * A factory of Helm commands.
 * <p>
 * Copyright (c) 2018. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public abstract class Helm
{
    /**
     * Cannot create utility class.
     */
    private Helm()
    {
    }

    /**
     * Create a {@link HelmCommand.Template} to use as a template
     * of common flags to then use to create other commands.
     */
    public static HelmCommand.Template template()
    {
        return new HelmCommand.Template(Arguments.empty());
    }

    /**
     * Create a new chart with the given name.
     *
     * @param name  the name of the chart to create
     */
    public static HelmCreate create(String name)
    {
        return new HelmCreate(name);
    }

    /**
     * Given a release name, delete the release from Kubernetes.
     *
     * @param name  the release to delete
     */
    public static HelmDelete delete(String name)
    {
        return new HelmDelete(name);
    }

    /**
     * Manage a chart's dependencies - rebuild the charts/ directory
     * based on the requirements.lock file.
     *
     * @param chart  the chart name
     */
    public static HelmDependencyBuild dependencyBuild(String chart)
    {
        return new HelmDependencyBuild(chart);
    }

    /**
     * Manage a chart's dependencies - list the dependencies for the given chart.
     *
     * @param chart  the chart name
     */
    public static HelmDependencyList dependencyList(String chart)
    {
        return new HelmDependencyList(chart);
    }

    /**
     * Manage a chart's dependencies - update charts/ based on the contents
     * of requirements.yaml.
     *
     * @param chart  the chart name
     */
    public static HelmDependencyUpdate dependencyUpdate(String chart)
    {
        return new HelmDependencyUpdate(chart);
    }

    /**
     * Download a chart from a repository and (optionally) unpack it in local directory.
     *
     * @param charts  the charts to download
     */
    public static HelmFetch fetch(String... charts)
    {
        return new HelmFetch(charts);
    }

    /**
     * Download a named release.
     *
     * @param release  the name of the release
     */
    public static HelmGet get(String release)
    {
        return new HelmGet(release);
    }

    /**
     * Download all hooks for a named release.
     *
     * @param release  the name of the release
     */
    public static HelmGetHooks getHooks(String release)
    {
        return new HelmGetHooks(release);
    }

    /**
     * Download the manifest for a named release.
     *
     * @param release  the name of the release
     */
    public static HelmGetManifest getManifest(String release)
    {
        return new HelmGetManifest(release);
    }

    /**
     * Download the values file for a named release.
     *
     * @param release  the name of the release
     */
    public static HelmGetValues getValues(String release)
    {
        return new HelmGetValues(release);
    }

    /**
     * Fetch a release history.
     *
     * @param release  the name of the release
     */
    public static HelmHistory history(String release)
    {
        return new HelmHistory(release);
    }

    /**
     * Initialize Helm on both client and server.
     */
    public static HelmInit init()
    {
        return new HelmInit();
    }

    /**
     * Create a Helm inspect command.
     *
     * @param chart  the name of the chart
     */
    public static HelmInspect inspect(String chart)
    {
        return new HelmInspect(chart);
    }

    /**
     * Create a Helm inspect chart command.
     *
     * @param chart  the name of the chart
     */
    public static HelmInspect inspectChart(String chart)
    {
        return HelmInspect.chart(chart);
    }

    /**
     * Create a Helm inspect chart command.
     *
     * @param chart  the name of the chart
     */
    public static HelmInspect inspectChart(File chart)
    {
        return HelmInspect.chart(chart);
    }

    /**
     * Create a Helm inspect chart command.
     *
     * @param chart  the name of the chart
     */
    public static HelmInspect inspectChart(URL chart)
    {
        return HelmInspect.chart(chart);
    }

    /**
     * Create a Helm inspect values command.
     *
     * @param chart  the name of the chart
     */
    public static HelmInspect inspectValues(String chart)
    {
        return HelmInspect.values(chart);
    }

    /**
     * Install a chart archive.
     *
     * @param chart  the location of the chart to install
     */
    public static HelmInstall install(String chart)
    {
        return new HelmInstall(chart);
    }

    /**
     * Install a chart archive.
     *
     * @param chart  the location of the chart to install
     */
    public static HelmInstall install(File chart)
    {
        return new HelmInstall(chart);
    }

    /**
     * Install a chart archive.
     *
     * @param parentDir  the directory containing the charts
     * @param chartName  the name of the chart to install
     */
    public static HelmInstall install(File parentDir, String chartName)
    {
        return install(new File(parentDir, chartName));
    }

    /**
     * Examines a chart for possible issues.
     *
     * @param chart  the location of the chart to install
     */
    public static HelmLint lint(String chart)
    {
        return new HelmLint(chart);
    }

    /**
     * Examines a chart for possible issues.
     *
     * @param chart  the location of the chart to install
     */
    public static HelmLint lint(File chart)
    {
        return new HelmLint(chart);
    }

    /**
     * Examines a chart for possible issues.
     *
     * @param parentDir  the directory containing the charts
     * @param chartName  the name of the chart to install
     */
    public static HelmLint lint(File parentDir, String chartName)
    {
        return new HelmLint(new File(parentDir, chartName));
    }

    /**
     * List releases.
     */
    public static HelmList list()
    {
        return new HelmList();
    }

    /**
     * List releases.
     *
     * @param filter  the Helm list command's filter argument
     */
    public static HelmList list(String filter)
    {
        return new HelmList(filter);
    }

    /**
     * Roll back a release to a previous revision.
     *
     * @param release   the name of the release
     * @param revision  the revision to rollback to
     */
    public static HelmRollback rollback(String release, String revision)
    {
        return new HelmRollback(release, revision);
    }

    /**
     * Displays the status of the named release.
     *
     * @param release  the name of the release
     */
    public static HelmStatus status(String release)
    {
        return new HelmStatus(release);
    }

    /**
     * Test a release.
     *
     * @param release  the name of the release
     */
    public static HelmTest test(String release)
    {
        return new HelmTest(release);
    }

    /**
     * Upgrade a release.
     *
     * @param release  the name of the release to be upgraded
     * @param chart    the name of the chart to use to upgrade the release
     */
    public static HelmUpgrade upgrade(String release, String chart)
    {
        return new HelmUpgrade(release, chart);
    }

    /**
     * Upgrade a release.
     *
     * @param release  the name of the release to be upgraded
     * @param chart    the name of the chart to use to upgrade the release
     */
    public static HelmUpgrade upgrade(String release, File chart)
    {
        return new HelmUpgrade(release, chart);
    }
}
