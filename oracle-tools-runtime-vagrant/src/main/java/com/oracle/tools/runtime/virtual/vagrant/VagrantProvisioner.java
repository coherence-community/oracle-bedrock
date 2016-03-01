/*
 * File: VagrantProvision.java
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

package com.oracle.tools.runtime.virtual.vagrant;

import java.io.PrintWriter;

/**
 * A representation of a Vagrant provisioner configuration.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class VagrantProvisioner
{
    /**
     * The type of this {@link VagrantProvisioner}.
     */
    private final String type;

    /**
     * The configuration string for the {@link VagrantProvisioner}.
     */
    private final String configuration;

    /**
     * Whether this {@link VagrantProvisioner} is always run
     */
    protected boolean runAlways = false;


    /**
     * Create a new {@link VagrantProvisioner} of the specified type.
     *
     * @param type           the type of this {@link VagrantProvisioner}
     * @param configuration  the configuration of the {@link VagrantProvisioner}
     */
    protected VagrantProvisioner(String type, String configuration)
    {
        this.type          = type;
        this.configuration = configuration;
    }


    /**
     * Determine whether this {@link VagrantProvisioner} should always run
     * when a {@link VagrantPlatform} is started.
     *
     * @return  true if the provisioner always runs or false if it only runs
     *          on machine creation.
     */
    public boolean isRunAlways()
    {
        return runAlways;
    }


    /**
     * Run this provisoner every time the Vagrant box is started.
     *
     * @return this {@link VagrantProvisioner} for fluent method chaining
     */
    public VagrantProvisioner runAlways()
    {
        runAlways = true;

        return this;
    }


    /**
     * Only run this provisoner when the Vagrant box is first started.
     *
     * @return this {@link VagrantProvisioner} for fluent method chaining
     */
    public VagrantProvisioner runOnce()
    {
        runAlways = false;

        return this;
    }


    /**
     * Write the configuration of this {@link VagrantProvisioner} to
     * a Vagranfile.
     *
     * @param writer   the {@link PrintWriter} that will write the Vagrantfile
     * @param prefix   the prefix to write before each configuration
     * @param padding  the padding to write before each line
     */
    public void write(PrintWriter writer, String prefix, String padding)
    {
        String sRunAlways = runAlways ? ", run: \"always\"" : "";

        writer.printf("%s    %s.vm.provision \"%s\"%s, ", padding, prefix, type, sRunAlways);
        writer.printf(configuration);
        writer.println();
    }


    /**
     * Create an in-line shell {@link VagrantProvisioner}.
     *
     * @param cmd  the shell command to execute
     *
     * @return  an in-line shell {@link VagrantProvisioner}
     */
    public static VagrantProvisioner inlineShell(String cmd)
    {
        return new VagrantProvisioner("shell", String.format("inline: \"%s\"", cmd));
    }


    /**
     * Create a file copy {@link VagrantProvisioner}.
     *
     * @param source  the source file to copy
     * @param source  the destination to copy the file to
     *
     * @return  an in-line shell {@link VagrantProvisioner}
     */
    public static VagrantProvisioner file(String source, String destination)
    {
        return new VagrantProvisioner("file",
                String.format("source: \"%s\", destination: \"%s\"", source, destination));
    }


    /**
     * Create an custom {@link VagrantProvisioner}.
     *
     * @param type          the Vagrant provisioner type
     * @param configuraton  the configuration to write for the provisioner
     *
     * @return  an in-line shell {@link VagrantProvisioner}
     */
    public static VagrantProvisioner custom(String type, String configuraton)
    {
        return new VagrantProvisioner(type, configuraton);
    }
}
