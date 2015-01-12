/*
 * File: VagrantPlatform.java
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

import com.oracle.tools.Option;
import com.oracle.tools.Options;

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationBuilder;
import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.ApplicationSchema;
import com.oracle.tools.runtime.SimpleApplication;
import com.oracle.tools.runtime.SimpleApplicationBuilder;
import com.oracle.tools.runtime.SimpleApplicationSchema;

import com.oracle.tools.runtime.console.PipedApplicationConsole;
import com.oracle.tools.runtime.console.SystemApplicationConsole;

import com.oracle.tools.runtime.remote.AbstractRemoteApplicationBuilder;
import com.oracle.tools.runtime.remote.SecureKeys;
import com.oracle.tools.runtime.remote.options.StrictHostChecking;

import com.oracle.tools.runtime.virtual.CloseAction;
import com.oracle.tools.runtime.virtual.VirtualPlatform;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.Properties;

/**
 * A {@link com.oracle.tools.runtime.Platform} implementation that represents
 * an O/S running in a virtual machine managed by the Vagrant.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class VagrantPlatform extends VirtualPlatform
{
    /** The default command used to run the Vagrant command line interface. */
    public static final String DEFAULT_VAGRANT_COMMAND = "vagrant";

    /** The command to use to run the Vagrant command line interface. */
    private String vagrantCommand = getDefaultVagrantCommand();

    /**
     * The location of this {@link VagrantPlatform}'s VagrantFile.
     */
    private File vagrantFile;

    /**
     * The {@link ApplicationBuilder} to use to run Vagrant commands
     */
    private ApplicationBuilder<SimpleApplication> applicationBuilder;
    private String                                publicHostName;


    /**
     * Construct a new {@link VagrantPlatform}.
     *
     * @param name            the name of this {@link VagrantPlatform}
     * @param vagrantFile     the location of this {@link VagrantPlatform}'s VagrantFile
     * @param publicHostName  the host name of the public interface of the {@link VagrantPlatform}
     * @param options         the {@link Option}s for the {@link VirtualPlatform}
     */
    public VagrantPlatform(String    name,
                           File      vagrantFile,
                           String    publicHostName,
                           Option... options)
    {
        super(name, null, 0, null, null, options);

        this.vagrantFile        = vagrantFile;
        this.applicationBuilder = new SimpleApplicationBuilder();
        this.publicHostName     = publicHostName;
    }


    /**
     * Obtain the command used to run the Vagrant command line interface.
     *
     * @return the command used to run the Vagrant command line interface
     */
    public String getVagrantCommand()
    {
        return vagrantCommand;
    }


    /**
     * Set the command used to run the Vagrant command line interface.
     *
     * @param vagrantCommand  the command used to run the Vagrant command
     *                        line interface
     */
    public void setVagrantCommand(String vagrantCommand)
    {
        this.vagrantCommand = vagrantCommand;
    }


    /**
     * Obtain the location of this {@link VagrantPlatform}'s VagrantFile.
     *
     * @return the location of this {@link VagrantPlatform}'s VagrantFile
     */
    public File getVagrantFile()
    {
        return vagrantFile;
    }


    /**
     * Obtain the host name of the public network interface on the VM
     *
     * @return the host name of the public network interface on the VM
     */
    public String getPublicHostName()
    {
        return publicHostName;
    }


    @Override
    public void close() throws IOException
    {
        SimpleApplicationSchema schema = instantiateSchema();

        CloseAction             action = getOptions().get(CloseAction.class, CloseAction.Shutdown);

        switch (action)
        {
        case None :
            return;

        case PowerButton :
            schema.addArgument("destroy").addArgument("--force");
            break;

        case Shutdown :
            schema.addArgument("halt");
            break;

        case SaveState :
            schema.addArgument("suspend");
            break;

        default :
            throw new IllegalArgumentException("Unsupported CloseAction " + action);
        }

        execute(schema);
    }


    /**
     * Start this {@link VagrantPlatform}.
     * When this method returns the virtual machine this {@link VagrantPlatform}
     * represents will be in a running state.
     */
    public void start()
    {
        SimpleApplicationSchema schemaUp = instantiateSchema().addArgument("up");

        execute(schemaUp);
        detectSSH();

        try
        {
            if (publicHostName != null &&!publicHostName.isEmpty())
            {
                this.address = InetAddress.getByName(publicHostName);
            }
            else
            {
                // TODO: is this correct?  Should we assume the loopback address?
                // perhaps it's possible to ask the RemoteApplication it's address?
                this.address = InetAddress.getLoopbackAddress();
            }
        }
        catch (UnknownHostException e)
        {
            throw new RuntimeException("Error setting public InetAddress", e);
        }
    }


    /**
     * Detect the SSH settings for the NAT port forwarding that Vagrant
     * has configured on the VM and set them into this {@link VagrantPlatform}.
     */
    protected void detectSSH()
    {
        SimpleApplicationSchema  schema  = instantiateSchema().addArgument("ssh-config");
        SimpleApplicationBuilder builder = new SimpleApplicationBuilder();

        try (PipedApplicationConsole console = new PipedApplicationConsole();
            Application application = builder.realize(schema, "Vagrant", console);)
        {
            application.waitFor();
            application.close();

            Properties     sshProperties = new Properties();
            BufferedReader reader        = console.getOutputReader();
            String         line          = reader.readLine();

            while (line != null)
            {
                line = line.trim();

                int index = line.indexOf(']');

                index = line.indexOf(':', index);
                line  = line.substring(index + 1).trim();
                index = line.indexOf(' ');

                if (index > 0)
                {
                    String key   = line.substring(0, index).trim();
                    String value = line.substring(index + 1).trim();

                    sshProperties.setProperty(key, value);
                }

                try
                {
                    line = reader.readLine();
                }
                catch (IOException e)
                {
                    line = null;
                }
            }

            // Important:  At this point all we know is that we can connect to the local loopback
            // NAT'd address so we can SSH into the Vagrant Box.   We don't know what the
            // address of the Vagrant Box is.  It may not even have an address we can access.
            this.address        = InetAddress.getLoopbackAddress();
            this.port           = Integer.parseInt(sshProperties.getProperty("Port"));
            this.userName       = sshProperties.getProperty("User");
            this.authentication = SecureKeys.fromPrivateKeyFile(sshProperties.getProperty("IdentityFile"));
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error attempting to detect VM's SSH settings", e);
        }

    }


    @Override
    public <A extends Application, S extends ApplicationSchema<A>> A realize(String             applicationName,
                                                                             S                  applicationSchema,
                                                                             ApplicationConsole console,
                                                                             Option...          options)
    {
        // TODO: we need to use "default platform options" here to automatically turn off strict-host-checking
        Options managedOptions = new Options(options);

        managedOptions.addIfAbsent(StrictHostChecking.disabled());

        return super.realize(applicationName, applicationSchema, console, managedOptions.asArray());
    }


    @SuppressWarnings("unchecked")
    @Override
    public <A extends Application, B extends ApplicationBuilder<A>> B getApplicationBuilder(Class<A> applicationClass)
    {
        AbstractRemoteApplicationBuilder builder = super.getApplicationBuilder(applicationClass);

        return (B) builder;
    }


    /**
     * Execute the application defined by the specified {@link SimpleApplicationSchema}.
     *
     * @param schema  the {@link SimpleApplicationSchema} defining the application to execute
     */
    protected void execute(SimpleApplicationSchema schema)
    {
        try
        {
            Application application = applicationBuilder.realize(schema, "Vagrant", new SystemApplicationConsole());

            application.waitFor();
            application.close();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error executing Vagrant command", e);
        }
    }


    /**
     * Create a {@link SimpleApplicationSchema} to execute the Vagrant
     * command line.
     *
     * @return a {@link SimpleApplicationSchema} to execute the Vagrant
     *         command line
     */
    protected SimpleApplicationSchema instantiateSchema()
    {
        return new SimpleApplicationSchema(vagrantCommand).setWorkingDirectory(vagrantFile);
    }


    /**
     * Get the default Vagrant command to use to execute the Vagrant CLI commands.
     *
     * @return the default Vagrant command
     */
    public static String getDefaultVagrantCommand()
    {
        return System.getProperty("vagrant.command", DEFAULT_VAGRANT_COMMAND);
    }
}
