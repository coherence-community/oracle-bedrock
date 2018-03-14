/*
 * File: HelmInit.java
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
import com.oracle.bedrock.runtime.options.EnvironmentVariables;

/**
 * A representation of the Helm init command.
 * <p>
 * Copyright (c) 2018. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class HelmInit
        extends HelmCommand<HelmInit>
{
    HelmInit()
    {
    this(DEFAULT_HELM, Arguments.empty(), Arguments.empty(), EnvironmentVariables.custom());
    }

    HelmInit(String helm, Arguments arguments, Arguments flags, EnvironmentVariables env)
    {
        super(helm, arguments, flags, env, false, "init");
    }

    @Override
    public HelmInit newInstance(String helm, Arguments arguments, Arguments flags, EnvironmentVariables env)
    {
        return new HelmInit(helm, arguments, flags, env);
    }

    /**
     * Obtain a copy of this Helm create command that also applies
     * the {@code --canary-image} flag.
     *
     * @return  a copy of this Helm create command that also
     *          applies the {@code --canary-image} flag
     */
    public HelmInit canaryImage()
    {
        return withFlags(Arguments.of("--canary-image"));
    }

    /**
     * Obtain a copy of this Helm create command that also applies
     * the {@code --client-only} flag.
     *
     * @return  a copy of this Helm create command that also
     *          applies the {@code --client-only} flag
     */
    public HelmInit clientOnly()
    {
        return withFlags(Arguments.of("--client-only"));
    }

    /**
     * Obtain a copy of this Helm create command that also applies
     * the {@code --dry-run} flag.
     *
     * @return  a copy of this Helm create command that also
     *          applies the {@code --dry-run} flag
     */
    public HelmInit dryRun()
    {
        return withFlags(Arguments.of("--dry-run"));
    }

    /**
     * Obtain a copy of this Helm create command that also applies
     * the {@code --local-repo-url} flag.
     *
     * @param url  URL for local repository (default "http://127.0.0.1:8879/charts")
     *
     * @return  a copy of this Helm create command that also
     *          applies the {@code --local-repo-url} flag
     */
    public HelmInit localRepoURL(String url)
    {
        return withFlags(Arguments.of("--local-repo-url", url));
    }

    /**
     * Obtain a copy of this Helm create command that also applies
     * the {@code --net-host} flag.
     *
     * @return  a copy of this Helm create command that also
     *          applies the {@code --net-host} flag
     */
    public HelmInit netHost()
    {
        return withFlags(Arguments.of("--net-host"));
    }

    /**
     * Obtain a copy of this Helm create command that also applies
     * the {@code --service-account} flag.
     *
     * @param name  name of service account
     *
     * @return  a copy of this Helm create command that also
     *          applies the {@code --service-account} flag
     */
    public HelmInit serviceAccount(String name)
    {
        return withFlags(Arguments.of("--service-account", name));
    }

    /**
     * Obtain a copy of this Helm create command that also applies
     * the {@code --skip-refresh} flag.
     *
     * @return  a copy of this Helm create command that also
     *          applies the {@code --skip-refresh} flag
     */
    public HelmInit skipRefresh()
    {
        return withFlags(Arguments.of("--skip-refresh"));
    }

    /**
     * Obtain a copy of this Helm create command that also applies
     * the {@code --stable-repo-url} flag.
     *
     * @param url  URL for stable repository
     *             (default "https://kubernetes-charts.storage.googleapis.com")
     *
     * @return  a copy of this Helm create command that also
     *          applies the {@code --stable-repo-url} flag
     */
    public HelmInit stableRepoURL(String url)
    {
        return withFlags(Arguments.of("--stable-repo-url", url));
    }

    /**
     * Obtain a copy of this Helm create command that also applies
     * the {@code --tiller-image} flag.
     *
     * @param image  the name of the Tiller image to use
     *
     * @return  a copy of this Helm create command that also
     *          applies the {@code --tiller-image} flag
     */
    public HelmInit tillerImage(String image)
    {
        return withFlags(Arguments.of("--tiller-image", image));
    }

    /**
     * Obtain a copy of this Helm create command that also applies
     * the {@code --tiller-tls} flag.
     *
     * @return  a copy of this Helm create command that also
     *          applies the {@code --tiller-tls} flag
     */
    public HelmInit tillerTLS()
    {
        return withFlags(Arguments.of("--tiller-tls"));
    }

    /**
     * Obtain a copy of this Helm create command that also applies
     * the {@code --tiller-tls-cert} flag.
     *
     * @param cert  path to TLS certificate file to install with Tiller
     *
     * @return  a copy of this Helm create command that also
     *          applies the {@code --tiller-tls-cert} flag
     */
    public HelmInit tillerTLSCert(String cert)
    {
        return withFlags(Arguments.of("--tiller-tls-cert", cert));
    }

    /**
     * Obtain a copy of this Helm create command that also applies
     * the {@code --tiller-tls-key} flag.
     *
     * @param key  path to TLS key file to install with Tiller
     *
     * @return  a copy of this Helm create command that also
     *          applies the {@code --tiller-tls-key} flag
     */
    public HelmInit tillerTLSKey(String key)
    {
        return withFlags(Arguments.of("--tiller-tls-key", key));
    }

    /**
     * Obtain a copy of this Helm create command that also applies
     * the {@code --tiller-tls-verify} flag.
     *
     * @return  a copy of this Helm create command that also
     *          applies the {@code --tiller-tls-verify} flag
     */
    public HelmInit tillerTLSVerify()
    {
        return withFlags(Arguments.of("--tiller-tls-verify"));
    }

    /**
     * Obtain a copy of this Helm create command that also applies
     * the {@code --tiller-ca-cert} flag.
     *
     * @param cert  path to CA root certificate
     *
     * @return  a copy of this Helm create command that also
     *          applies the {@code --tiller-ca-cert} flag
     */
    public HelmInit tlsCACert(String cert)
    {
        return withFlags(Arguments.of("--tls-ca-cert", cert));
    }

    /**
     * Obtain a copy of this Helm create command that also applies
     * the {@code --upgrade} flag.
     *
     * @return  a copy of this Helm create command that also
     *          applies the {@code --upgrade} flag
     */
    public HelmInit upgrade()
    {
        return withFlags(Arguments.of("--upgrade"));
    }
}
