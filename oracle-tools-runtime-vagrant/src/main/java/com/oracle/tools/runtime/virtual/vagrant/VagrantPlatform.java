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
import com.oracle.tools.options.Timeout;
import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.console.Console;
import com.oracle.tools.runtime.console.PipedApplicationConsole;
import com.oracle.tools.runtime.options.Argument;
import com.oracle.tools.runtime.options.Arguments;
import com.oracle.tools.runtime.options.DisplayName;
import com.oracle.tools.runtime.options.Executable;
import com.oracle.tools.runtime.options.WorkingDirectory;
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
import java.util.concurrent.TimeUnit;

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
    private File   vagrantFile;
    private String publicHostName;


    /**
     * Construct a new {@link VagrantPlatform}.
     *
     * @param name            the name of this {@link VagrantPlatform}
     * @param vagrantFile     the location of this {@link VagrantPlatform}'s VagrantFile
     * @param publicHostName  the host name of the public interface of the {@link VagrantPlatform}
     * @param port            the remote port that will be used to SSH into
     *                        this {@link VirtualPlatform}
     * @param options         the {@link Option}s for the {@link VirtualPlatform}
     */
    public VagrantPlatform(String    name,
                           File      vagrantFile,
                           String    publicHostName,
                           int       port,
                           Option... options)
    {
        super(name, null, 0, null, null, options);

        this.vagrantFile    = vagrantFile;
        this.publicHostName = publicHostName;
        this.port           = port;
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
        close(new Option[0]);
    }


    @Override
    public void close(Option... closeOptions) throws IOException
    {
        Options options = new Options(getOptions()).addAll(getDefaultOptions());

        options.addAll(closeOptions);

        CloseAction action = options.getOrDefault(CloseAction.class, CloseAction.Destroy);

        switch (action)
        {
        case None :
            return;

        case Destroy :
        case PowerButton :
            options.add(Arguments.of("destroy", "--force"));
            break;

        case Shutdown :
            options.add(Argument.of("halt"));
            break;

        case SaveState :
            options.add(Argument.of("suspend"));
            break;

        default :
            throw new IllegalArgumentException("Unsupported CloseAction " + action);
        }

        execute(options);
    }


    /**
     * Start this {@link VagrantPlatform}.
     * When this method returns the virtual machine this {@link VagrantPlatform}
     * represents will be in a running state.
     */
    public void start()
    {
        Options options = getDefaultOptions().add(Argument.of("up"));

        execute(options);

        Properties sshProperties = detectSSH();

        try
        {
            // If no public host name has been specified then use the
            // settings configured by Vagrant
            if (publicHostName == null || publicHostName.isEmpty())
            {
                // Important:  At this point all we know is that we can connect to the local loopback
                // NAT'd address so we can SSH into the Vagrant Box.   We don't know what the
                // address of the Vagrant Box is.  It may not even have an address we can access.
                this.address        = InetAddress.getLoopbackAddress();
                this.port           = Integer.parseInt(sshProperties.getProperty("Port"));
                this.userName       = sshProperties.getProperty("User");
                this.authentication = SecureKeys.fromPrivateKeyFile(sshProperties.getProperty("IdentityFile"));
            }
            else
            {
                this.address        = InetAddress.getByName(publicHostName);
                this.userName       = sshProperties.getProperty("User");
                this.authentication = SecureKeys.fromPrivateKeyFile(sshProperties.getProperty("IdentityFile"));
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
     *
     * @return the SSH properties configured by Vagrant
     */
    protected Properties detectSSH()
    {
        Options       options  = getDefaultOptions().add(Argument.of("ssh-config"));

        LocalPlatform platform = LocalPlatform.get();

        try (PipedApplicationConsole console = new PipedApplicationConsole();
            Application application = platform.launch(Application.class, options.add(Console.of(console)).asArray()))
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

                    if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"')
                    {
                        value = value.substring(1, value.length() - 1);
                    }

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

            return sshProperties;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error attempting to detect VM's SSH settings", e);
        }

    }


    @Override
    public <A extends Application> A launch(Class<A>  applicationClass,
                                            Option... options)
    {
        // TODO: we need to use "default platform options" here to automatically turn off strict-host-checking
        Options launchOptions = new Options(options);

        launchOptions.addIfAbsent(StrictHostChecking.disabled());

        return super.launch(applicationClass, launchOptions.asArray());
    }


    /**
     * Execute the application defined by the specified {@link Options}.
     *
     * @param options  the {@link Options}
     */
    protected void execute(Options options)
    {
        LocalPlatform platform = LocalPlatform.get();
        Timeout       timeout  = options.getOrDefault(Timeout.class, Timeout.after(5, TimeUnit.MINUTES));

        try (Application application = platform.launch(Application.class, options.asArray()))
        {
            application.waitFor(timeout);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error executing Vagrant command", e);
        }
    }


    /**
     * Options the default {@link Options} to use when launching Vagrant.
     *
     * @return the default {@link Options}
     */
    protected Options getDefaultOptions()
    {
        return new Options(Executable.named(vagrantCommand),
                           WorkingDirectory.at(vagrantFile),
                           Timeout.after(5, TimeUnit.MINUTES),
                           DisplayName.of("Vagrant"));
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
