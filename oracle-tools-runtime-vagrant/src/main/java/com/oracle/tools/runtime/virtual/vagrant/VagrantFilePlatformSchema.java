/*
 * File: VagrantFilePlatformSchema.java
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;

/**
 * An implementation of {@link com.oracle.tools.runtime.virtual.VirtualPlatformSchema} for defining instances
 * of {@link VagrantPlatform}s using a specific Vagrant configuration file.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class VagrantFilePlatformSchema extends AbstractVagrantPlatformSchema<VagrantFilePlatformSchema>
{
    /** The URL of the template vagrantFile that will be copied to the working directory */
    private final URL vagrantFileURL;

    /** The name of the public host name for the VM */
    private String publicHostName;


    /**
     * Create a new VagrantFilePlatformSchema
     *
     * @param name                 the name of this schema
     * @param workingDirectory     the location to write the vagrantFile to
     * @param vagrantFileLocation  the URL of the template vagrantFile
     */
    public VagrantFilePlatformSchema(String name,
                                     File   workingDirectory,
                                     URL    vagrantFileLocation)
    {
        super(name, false, workingDirectory);

        if (workingDirectory == null)
        {
            throw new IllegalArgumentException("Working directory cannot be null");
        }

        if (workingDirectory.exists() &&!workingDirectory.isDirectory())
        {
            throw new IllegalStateException("Working Directory exists but is not a directory: " + workingDirectory);
        }

        if (!workingDirectory.exists() && vagrantFileLocation == null)
        {
            throw new IllegalStateException("Working Directory does not exist and no VagrantFile URL specified: "
                                            + workingDirectory);
        }

        vagrantFileURL = vagrantFileLocation;
    }


    /**
     * Obtain the {@link URL} of the template vagrantFile
     * to copy to the working directory
     *
     * @return the {@link URL} of the template vagrantFile
     */
    public URL getVagrantFileURL()
    {
        return vagrantFileURL;
    }


    /**
     * Set the host name of the public NIC for the VM.
     *
     * @param name  the host name of the public NIC for the VM
     *
     * @return this AbstractVagrantPlatformSchema for chaining
     */
    @SuppressWarnings("unchecked")
    public VagrantFilePlatformSchema setPublicHostName(String name)
    {
        publicHostName = name;

        return this;
    }


    /**
     * Obtain the host name for the public NIC of the VM.
     *
     * @return the host name of the public NIC for the VM
     */
    public String getPublicHostName()
    {
        return publicHostName;
    }


    /**
     * Write the vagrantFile configuration using the Vagrant VM
     * definition in this schema to the working directory and instantiate
     * a {@link VagrantPlatform} based on the configuration.
     *
     * @param name         the name to assign to the {@link VagrantPlatform}
     * @param vagrantFile  the {@link java.io.File} to write the Vagrant configuration to
     */
    protected VagrantPlatform realize(String name, File vagrantFile) throws IOException
    {
        if (vagrantFileURL != null)
        {
            PrintWriter writer = null;

            try
            {
                writer = new PrintWriter(vagrantFile);

                BufferedReader reader = new BufferedReader(new InputStreamReader(vagrantFileURL.openStream()));
                String         line   = reader.readLine();

                while (line != null)
                {
                    writer.println(line);
                    line = reader.readLine();
                }

                writer.flush();
            }
            finally
            {
                try
                {
                    if (writer != null)
                    {
                        writer.close();
                    }
                }
                catch (Exception e)
                {
                    // ignored
                }
            }
        }

        return instantiatePlatform(name, getCloseAction(), vagrantFile.getParentFile(), getPublicHostName());
    }
}
