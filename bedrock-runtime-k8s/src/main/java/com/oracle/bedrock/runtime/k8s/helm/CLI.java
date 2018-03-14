/*
 * File: CLI.java
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

import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.MetaClass;
import com.oracle.bedrock.runtime.options.Argument;
import com.oracle.bedrock.runtime.options.Arguments;
import com.oracle.bedrock.runtime.options.EnvironmentVariable;
import com.oracle.bedrock.runtime.options.EnvironmentVariables;

import java.util.Arrays;

/**
 * A Helm CLI command.
 * <p>
 * Copyright (c) 2018. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public interface CLI<C extends CLI>
        extends MetaClass<Application>
{
    /**
     * Obtain the Helm command that will be executed.
     *
     * @return  the Helm command that will be executed
     */
    String[] getCommands();

    /**
     * Obtain the arguments to this command.
     *
     * @return  the arguments to this command
     */
    Arguments getArguments();

    /**
     * Obtain the environment variables for this command.
     *
     * @return  the environment variables for this command
     */
    EnvironmentVariables getEnvironment();

    /**
     * Obtain the flags to this command.
     *
     * @return  the flags to this command
     */
    Arguments getFlags();

    /**
     * Determine whether the flags appear at the start or end of the command line.
     *
     * @return  {@code true} if the flags appear at the start or end of
     *          the command line
     */
    boolean isFlagsFirst();

    /**
     * Obtain a copy of this Helm command with debug enabled.
     *
     * @return  a copy of this Helm command with debug enabled
     */
    default C debug()
    {
        return withFlags(Argument.of("--debug"));
    }

    /**
     * Obtain a copy of this Helm command with the specified Helm home,
     * overrides $HELM_HOME (default {@code $HOME/.helm}).
     *
     * @param helmHome  the location of Helm home
     *
     * @return  a copy of this Helm command with the specified helm home
     */
    default C home(String helmHome)
    {
        return withFlags(Argument.of("--home", helmHome));
    }

    /**
     * Obtain a copy of this Helm command with the specified Tiller address.
     *
     * @param tillerHost  the Tiller host
     *
     * @return  a copy of this Helm command with the specified Tiller host
     */
    default C host(String tillerHost)
    {
        return withFlags(Argument.of("--host", tillerHost));
    }

    /**
     * Obtain a copy of this Helm command with the kubeconfig context to use.
     *
     * @param contextName  the kubeconfig context to use
     *
     * @return  a copy of this Helm command with the kubeconfig context to use
     */
    default C kubeContext(String contextName)
    {
        return withFlags(Argument.of("--kube-context", contextName));
    }

    /**
     * Obtain a copy of this Helm command with the alternative kubeconfig to use.
     *
     * @param config  the kubeconfig to use
     *
     * @return  a copy of this Helm command with the alternative kubeconfig to use
     */
    default C kubeConfig(String config)
    {
        return withEnvironment(EnvironmentVariable.of("KUBECONFIG", config));
    }

    /**
     * Obtain a copy of this Helm command with the specified Tiller namespace
     *
     * @param namespace  the Tiller namespace
     *
     * @return  a copy of this Helm command with the specified Tiller namespace
     */
    default C tillerNamespace(String namespace)
    {
        return withFlags(Argument.of("--tiller-namespace", namespace));
    }

    /**
     * Create a new instance of this Helm command.
     *
     *
     * @param helm
     * @param arguments  the command arguments
     * @param flags      the command flags
     * @param env        the environment variables
     *
     * @return  a new instance of this command
     */
    C newInstance(String helm, Arguments arguments, Arguments flags, EnvironmentVariables env);

    /**
     * Obtain a copy of this command with the addition of the
     * specified Helm executable location.
     *
     * @param helm  the location of the Helm executable
     *
     * @return  a copy of this command with the addition of
     *          specified Helm executable location.
     */
    default C withHelmAt(String helm)
    {
        return newInstance(helm, getArguments(), getFlags(), getEnvironment());
    }

    /**
     * Obtain the location of the Helm executable.
     *
     * @return  the location of the Helm executable
     */
    String getHelmLocation();

    /**
     * Obtain a copy of this command with the addition of the
     * specified arguments.
     *
     * @param args  the arguments to add
     *
     * @return  a copy of this command with the addition of
     *          the specified arguments
     */
    default C withArguments(Object... args)
    {
        return newInstance(getHelmLocation(), getArguments().with(args), getFlags(), getEnvironment());
    }

    /**
     * Obtain a copy of this command with the addition of the
     * specified arguments.
     *
     * @param args  the arguments to add
     *
     * @return  a copy of this command with the addition of
     *          the specified arguments
     */
    default C withArguments(Argument... args)
    {
        return newInstance(getHelmLocation(), getArguments().with(args), getFlags(), getEnvironment());
    }

    /**
     * Obtain a copy of this command with the addition of the
     * specified arguments.
     *
     * @param args  the arguments to add
     *
     * @return  a copy of this command with the addition of
     *          the specified arguments
     */
    default C withArguments(Arguments args)
    {
        return newInstance(getHelmLocation(), getArguments().with(args), getFlags(), getEnvironment());
    }

    /**
     * Obtain a copy of this command with the addition of the
     * specified flags.
     *
     * @param flags  the flags to add
     *
     * @return  a copy of this command with the addition of
     *          the specified flags
     */
    default C withFlags(Object... flags)
    {
        return newInstance(getHelmLocation(), getArguments(), getFlags().with(flags), getEnvironment());
    }

    /**
     * Obtain a copy of this command with the addition of the
     * specified flags.
     *
     * @param flags  the flags to add
     *
     * @return  a copy of this command with the addition of
     *          the specified flags
     */
    default C withFlags(Argument... flags)
    {
        return newInstance(getHelmLocation(), getArguments(), getFlags().with(flags), getEnvironment());
    }

    /**
     * Obtain a copy of this command with the addition of the
     * specified flags.
     *
     * @param flags  the flags to add
     *
     * @return  a copy of this command with the addition of
     *          the specified flags
     */
    default C withFlags(Arguments flags)
    {
        return newInstance(getHelmLocation(), getArguments(), getFlags().with(flags), getEnvironment());
    }

    /**
     * Obtain a copy of this command with the addition of the
     * specified environment variables.
     *
     * @param envVariable  the environment variable to add
     *
     * @return  a copy of this command with the addition of
     *          the specified environment variables
     */
    default C withEnvironment(String envVariable)
    {
        return newInstance(getHelmLocation(), getArguments(), getFlags(), getEnvironment().with(EnvironmentVariable.of(envVariable)));
    }

    /**
     * Obtain a copy of this command with the addition of the
     * specified environment variables.
     *
     * @param name   the environment variable name
     * @param value  the environment variable value
     *
     * @return  a copy of this command with the addition of
     *          the specified environment variables
     */
    default C withEnvironment(String name, Object value)
    {
        return newInstance(getHelmLocation(), getArguments(), getFlags(), getEnvironment().with(EnvironmentVariable.of(name, value)));
    }

    /**
     * Obtain a copy of this command with the addition of the
     * specified environment variables.
     *
     * @param env  the environment variables to add
     *
     * @return  a copy of this command with the addition of
     *          the specified environment variables
     */
    default C withEnvironment(EnvironmentVariable... env)
    {
        return newInstance(getHelmLocation(), getArguments(), getFlags(), getEnvironment().with(Arrays.asList(env)));
    }

    /**
     * Obtain a copy of this command with the addition of the
     * specified environment variables.
     *
     * @param env  the environment variables to add
     *
     * @return  a copy of this command with the addition of
     *          the specified environment variables
     */
    default C withEnvironment(EnvironmentVariables env)
    {
        return newInstance(getHelmLocation(), getArguments(), getFlags(), getEnvironment().with(env));
    }

    /**
     * A {@link CLI} command with TLS flags.
     *
     * @param <C>  the type of the command
     */
    interface WithTLS<C extends WithTLS> extends CLI<C>
    {
        /**
         * Enable TLS.
         *
         * @return  a copy of this command with the {@code --tls} flag appended
         */
        default C tls()
        {
            return withFlags(Argument.of("--tls"));
        }

        /**
         * Set the path to TLS CA certificate file (default "$HELM_HOME/ca.pem").
         *
         * @return  a copy of this command with the {@code --tls} flag appended
         */
        default C tlsCaCert(String path)
        {
            return withFlags(Argument.of("--tls-ca-cert", path));
        }

        /**
         * Set the path to TLS certificate file (default "$HELM_HOME/cert.pem").
         *
         * @return  a copy of this command with the {@code --tls} flag appended
         */
        default C tlsCert(String path)
        {
            return withFlags(Argument.of("--tls-cert", path));
        }

        /**
         * Set the path to TLS key file (default "$HELM_HOME/key.pem").
         *
         * @return  a copy of this command with the {@code --tls} flag appended
         */
        default C tlsKey(String path)
        {
            return withFlags(Argument.of("--tls-key", path));
        }

        /**
         * Enable TLS for request and verify remote.
         *
         * @return  a copy of this command with the {@code --tls-verify} flag appended
         */
        default C tlsVerify()
        {
            return withFlags(Argument.of("--tls-verify"));
        }
    }

    /**
     * A {@link CLI} command with certificate flags.
     *
     * @param <C>  the type of the command
     */
    interface WithCerts<C extends WithCerts> extends CLI<C>
    {
        /**
         * Verify certificates of HTTPS-enabled servers using this CA bundle.
         *
         * @param caBundle  the CA bundle file
         *
         * @return  a copy of this command with the {@code --ca-file} flag appended
         */
        default C caFile(String caBundle)
        {
            return withFlags(Argument.of("--ca-file", caBundle));
        }

        /**
         * Identify HTTPS client using this SSL certificate file.
         *
         * @param file  SSL certificate file
         *
         * @return  a copy of this command with the {@code --cert-file} flag appended
         */
        default C certFile(String file)
        {
            return withFlags(Argument.of("--cert-file", file));
        }

        /**
         * Identify HTTPS client using this SSL key file.
         *
         * @param file  SSL key file
         *
         * @return  a copy of this command with the {@code --key-file} flag appended
         */
        default C keyFile(String file)
        {
            return withFlags(Argument.of("--key-file", file));
        }

        /**
         * Keyring containing public keys (default ~/.gnupg/pubring.gpg).
         *
         * @param file  keyring file
         *
         * @return  a copy of this command with the {@code --keyring} flag appended
         */
        default C keyRing(String file)
        {
            return withFlags(Argument.of("--keyring", file));
        }
    }

    /**
     * A {@link CLI} command with repo flags.
     *
     * @param <C>  the type of the command
     */
    interface WithRepo<C extends WithRepo> extends CLI<C>
    {
        /**
         * Chart repository url where to locate the requested chart.
         *
         * @param repoName  the repository url
         *
         * @return  a copy of this command with the {@code --repo} flag appended
         */
        default C repo(String repoName)
        {
            return withFlags(Argument.of("--repo", repoName));
        }

        /**
         * Verify the package against its signature.
         *
         * @return  a copy of this command with the {@code --verify} flag appended
         */
        default C verify()
        {
            return withFlags(Argument.of("--verify"));
        }

        /**
         * Fetch the specific version of a chart. Without this, the latest version is fetched
         *
         * @return  a copy of this command with the {@code --verify} flag appended
         */
        default C version(String version)
        {
            return withFlags(Argument.of("--version", version));
        }
    }
}
