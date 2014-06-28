/*
 * File: VagrantPlatformSchema.java
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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An implementation of {@link com.oracle.tools.runtime.virtual.VirtualPlatformSchema} for defining instances
 * of {@link VagrantPlatform}s where the Vagrant configuration file will be produced
 * at runtime using the configuration from this schema.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class VagrantPlatformSchema extends AbstractVagrantPlatformSchema<VagrantPlatformSchema>
{
    /** 
     * the box name configuration property
     */
    public static final String CONFIG_VM_BOX = ".vm.box";

    /** 
     * the box update check configuration property
     */
    public static final String                CONFIG_VM_BOX_CHECK_UPDATE = ".vm.box_check_update";

    /** The name of the Vagrant Box to use for the VM */
    private final String                      boxName;

    /** The various Vagrant properties that define the VM */
    private final Map<String, Object>         properties;

    /** The network interfaces to add to the VM */
    private Map<String, VagrantNetworkSchema> networks;


    /**
     * Constructs a new VagrantPlatformSchema
     *
     * @param name              the name of the Vagrant VM
     * @param workingDirectory  the directory that will contain the Vagrant configuration
     * @param boxName           the nae of the Vagrant Box to use for the VM
     */
    public VagrantPlatformSchema(String name,
                                 File   workingDirectory,
                                 String boxName)
    {
        super(name, true, workingDirectory);

        this.boxName = boxName;
        properties   = new LinkedHashMap<String, Object>();
        networks     = new HashMap<String, VagrantNetworkSchema>();

        properties.put(CONFIG_VM_BOX, boxName);
        properties.put(CONFIG_VM_BOX_CHECK_UPDATE, false);
    }


    /**
     * Obtain the name of the Vagrant Box to use for the VM
     *
     * @return the name of the Vagrant Box to use for the VM
     */
    public String getBoxName()
    {
        return boxName;
    }


    /**
     * Obtain the flag indicating whether Vagrant should perform a check
     * for box updates prior to starting the VM.
     *
     * @return true if Vagrant should perform an update check on the box
     *         prior to starting the VM
     */
    public boolean shouldBoxCheckUpdate()
    {
        return (Boolean) properties.get(CONFIG_VM_BOX_CHECK_UPDATE);
    }


    /**
     * Set the flag indicating whether Vagrant should perform a check
     * for box updates prior to starting the VM.
     *
     * @param boxCheckUpdate  true if Vagrant should perform an update check on the box
     *                        prior to starting the VM
     *
     * @return this {@link VagrantPlatformSchema} for method chaining
     */
    public VagrantPlatformSchema setBoxCheckUpdate(boolean boxCheckUpdate)
    {
        return setVagrantProperty(CONFIG_VM_BOX_CHECK_UPDATE, boxCheckUpdate);
    }


    /**
     * Add the specified network interface definition to the platform definition
     *
     * @param schema  the {@link VagrantNetworkSchema} defining the network interface to add
     *
     * @return this {@link VagrantPlatformSchema} for method chaining
     */
    public VagrantPlatformSchema addNetworkAdapter(VagrantNetworkSchema schema)
    {
        networks.put(schema.getId(), schema);

        return this;
    }


    /**
     * Set a Vagrant property to add to the Vargant configuration for the VM
     *
     * @param propertyName  the name of the property to add
     * @param value         the value of the property to add
     *
     * @return this {@link VagrantPlatformSchema} for method chaining
     */
    public VagrantPlatformSchema setVagrantProperty(String propertyName,
                                                    Object value)
    {
        properties.put(propertyName, value);

        return this;
    }


    /**
     * Set a Vagrant property to add to the Vargant configuration for the VM
     *
     * @param propertyName  the name of the property to add
     * @param iterator      an {@link Iterator} providing values for the
     *                      Vagrant property
     *
     * @return this {@link VagrantPlatformSchema} for method chaining
     */
    public VagrantPlatformSchema setVagrantProperty(String      propertyName,
                                                    Iterator<?> iterator)
    {
        properties.put(propertyName, iterator);

        return this;
    }


    /**
     * Obtain the Vagrant property with the specified name
     *
     * @param propertyName  the name of the Vagrant property
     *
     * @param <T>  the type of the Vagrant property
     *
     * @return the Vagrant property with the specified name or null
     *         if no property with the specified name exists
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String propertyName)
    {
        return (T) properties.get(propertyName);
    }


    /**
     * Write the vagrantFile configuration using the Vagrant VM
     * definition in this schema to the specified file.
     *
     * @param name         the name to assign to the{@link VagrantPlatform}
     * @param vagrantFile  the {@link java.io.File} to wrote the Vagrant configuration to
     */
    protected VagrantPlatform realize(String name, File vagrantFile) throws IOException
    {
        PrintWriter writer = null;

        try
        {
            writer = new PrintWriter(vagrantFile);

            writer.println("# -*- mode: ruby -*-");
            writer.println("# vi: set ft=ruby :");
            writer.println("");
            writer.println("# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!");
            writer.println("VAGRANTFILE_API_VERSION = \"2\"");
            writer.println("");
            writer.println("Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|");

            writeVagrantProperties(writer, "config", "");

            String publicHostName = realizeNetworks(writer, "config", "");

            writer.println("end");
            writer.flush();

            return instantiatePlatform(name, getCloseAction(), vagrantFile.getParentFile(), publicHostName);
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


    protected void writeVagrantProperties(PrintWriter writer,
                                          String      prefix,
                                          String      padding)
    {
        for (Map.Entry<String, Object> entry : properties.entrySet())
        {
            writeProperty(writer, entry.getKey(), entry.getValue(), prefix, padding);
        }
    }

    /**
     * Realize the network interfaces added to this {@link VagrantPlatformSchema}.
     * This method will return the public hst name to use for the {@link VagrantPlatform},
     * this will be either the public host name set in this schema, or if that is not set then
     * the host name from the first public network interface being added.
     *
     * @param writer   the {@link PrintWriter} to write the network configuration to
     * @param prefix   the prefix name to add to each configuration parameter
     * @param padding  the padding to add to the front of the string written to the configuration file
     *
     * @return the public host name for the {@link VagrantPlatform}
     */
    protected String realizeNetworks(PrintWriter writer,
                                   String        prefix,
                                   String        padding)
    {
        String publicHostName = null;

        for (VagrantNetworkSchema schema : networks.values())
        {
            String hostName = schema.realize(writer, prefix, padding);
            if (schema.isPublic() && publicHostName == null)
            {
                publicHostName = hostName;
            }
        }

        return publicHostName;
    }

    protected void writeProperty(PrintWriter writer,
                                 String      sPropertyName,
                                 Object      oValue,
                                 String      sPrefix,
                                 String      sPad)
    {
        if (oValue instanceof Iterator)
        {
            Iterator iterator = (Iterator) oValue;

            if (!iterator.hasNext())
            {
                throw new IndexOutOfBoundsException(String.format("No more values available for the property [%s]",
                                                                  sPropertyName));
            }

            writeProperty(writer, sPropertyName, iterator.next(), sPrefix, sPad);

            return;
        }

        String sValue;

        if (oValue instanceof String)
        {
            sValue = "'" + String.valueOf(oValue) + "'";
        }
        else if (oValue != null)
        {
            sValue = String.valueOf(oValue);
        }
        else
        {
            sValue = "";
        }

        writer.printf("%s    %s%s = %s\n", sPad, sPrefix, sPropertyName, sValue);

    }
}
