/*
 * File: OptionsBasedVagrantFileBuilder.java
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

package com.oracle.bedrock.runtime.virtual.vagrant;

import com.oracle.bedrock.runtime.virtual.vagrant.options.Networks;
import com.oracle.bedrock.Option;
import com.oracle.bedrock.Options;
import com.oracle.bedrock.runtime.options.OperatingSystem;
import com.oracle.bedrock.runtime.remote.options.HostName;
import com.oracle.bedrock.runtime.virtual.vagrant.options.BoxName;
import com.oracle.bedrock.runtime.virtual.vagrant.options.UpdateBox;
import com.oracle.bedrock.runtime.virtual.vagrant.options.VagrantConfigurations;
import com.oracle.bedrock.runtime.virtual.vagrant.options.VagrantProperties;
import com.oracle.bedrock.runtime.virtual.vagrant.options.VagrantProperty;
import com.oracle.bedrock.runtime.virtual.vagrant.options.VagrantProvisioners;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

/**
 * A {@link VagrantFileBuilder} that uses a set of {@link Option}s to dynamically
 * create a VagrantFile.
 * <p>
 * See the com.oracle.bedrock.runtime.virtaul.vagrant.options for available options.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @see com.oracle.bedrock.runtime.virtual.vagrant.options
 */
class OptionsBasedVagrantFileBuilder implements VagrantFileBuilder
{
    /**
     * The default extension for box.
     */
    public static final String CONFIG_VM_BOX = ".vm.box";

    /**
     * The property to check for box updating.
     */
    public static final String CONFIG_VM_BOX_CHECK_UPDATE = ".vm.box_check_update";

    /**
     * The {@link Options} to be used for creating the {@link VagrantFileBuilder}.
     */
    private Options options;


    /**
     * Constructs an {@link OptionsBasedVagrantFileBuilder}.
     *
     * @param options  the {@link Option}s
     *
     * @see com.oracle.bedrock.runtime.virtual.vagrant.options
     */
    OptionsBasedVagrantFileBuilder(Option... options)
    {
        this.options = new Options(options);

        BoxName   boxName   = this.options.get(BoxName.class);
        UpdateBox updateBox = this.options.get(UpdateBox.class);

        // TODO: assert a boxname has been provided?

        // configure some default properties based on options
        this.options.add(VagrantProperty.of(CONFIG_VM_BOX, boxName.get()));
        this.options.add(VagrantProperty.of(CONFIG_VM_BOX_CHECK_UPDATE, updateBox.isEnabled()));
    }


    @Override
    public Optional<HostName> create(File file, Options createOptions) throws IOException
    {
        try (PrintWriter writer = new PrintWriter(file))
        {
            Options vagrantOptions = new Options(this.options).addAll(createOptions);

            // ----- output the header -----
            writer.println("# -*- mode: ruby -*-");
            writer.println("# vi: set ft=ruby :");
            writer.println("");
            writer.println("# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!");
            writer.println("VAGRANTFILE_API_VERSION = \"2\"");
            writer.println("");
            writer.println("Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|");

            String prefix  = "config";
            String padding = "";

            // ----- output the operating system -----

            OperatingSystem operatingSystem = vagrantOptions.getOrDefault(OperatingSystem.class, null);

            if (operatingSystem != null)
            {
                writer.println();
                writer.println(padding + operatingSystem.getName());
            }

            // ----- output the provisioners -----

            VagrantProvisioners provisioners = vagrantOptions.get(VagrantProvisioners.class);

            provisioners.write(writer, prefix, padding);

            // ----- write the configurations -----

            VagrantConfigurations configurations = vagrantOptions.get(VagrantConfigurations.class);

            configurations.write(writer, padding);

            // ----- output the VagrantProperties -----

            VagrantProperties properties = vagrantOptions.get(VagrantProperties.class);

            properties.write(writer, prefix, padding);

            // ----- output the Networks -----

            Networks networks = vagrantOptions.get(Networks.class);

            Optional<HostName> hostName = networks.write(writer, prefix, padding);

            writer.println("end");
            writer.flush();

            return hostName;
        }
    }
}
