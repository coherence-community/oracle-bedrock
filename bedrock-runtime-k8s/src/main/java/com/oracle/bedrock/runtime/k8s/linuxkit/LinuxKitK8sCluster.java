/*
 * File: LinuxKitK8sCluster.java
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

package com.oracle.bedrock.runtime.k8s.linuxkit;

import com.oracle.bedrock.io.FileHelper;

import com.oracle.bedrock.options.LaunchLogging;
import com.oracle.bedrock.options.Timeout;

import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.ApplicationConsoleBuilder;
import com.oracle.bedrock.runtime.LocalPlatform;

import com.oracle.bedrock.runtime.SimpleApplication;
import com.oracle.bedrock.runtime.console.CapturingApplicationConsole;
import com.oracle.bedrock.runtime.console.FileWriterApplicationConsole;
import com.oracle.bedrock.runtime.console.SystemApplicationConsole;

import com.oracle.bedrock.runtime.k8s.K8sCluster;

import com.oracle.bedrock.runtime.network.AvailablePortIterator;

import com.oracle.bedrock.runtime.options.Argument;
import com.oracle.bedrock.runtime.options.Arguments;
import com.oracle.bedrock.runtime.options.Console;
import com.oracle.bedrock.runtime.options.DisplayName;
import com.oracle.bedrock.runtime.options.Executable;
import com.oracle.bedrock.runtime.options.WorkingDirectory;

import com.oracle.bedrock.runtime.remote.RemotePlatform;
import com.oracle.bedrock.runtime.remote.SecureKeys;
import com.oracle.bedrock.runtime.remote.options.Deployer;
import com.oracle.bedrock.runtime.remote.options.StrictHostChecking;
import com.oracle.bedrock.runtime.remote.options.UserKnownHostsFile;

import com.oracle.bedrock.testsupport.deferred.Eventually;

import com.oracle.bedrock.util.Capture;
import com.oracle.bedrock.util.Pair;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import java.net.InetAddress;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import java.util.logging.Logger;

import java.util.stream.Collectors;

import static com.oracle.bedrock.deferred.DeferredHelper.invoking;
import static org.hamcrest.CoreMatchers.is;

/**
 * A Kubernetes cluster running on a LinuxKit cluster.
 * <p>
 * Copyright (c) 2018. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class LinuxKitK8sCluster
        extends K8sCluster<LinuxKitK8sCluster>
        implements Closeable
{
    /**
     * The {@link Logger} for this class.
     */
    private static Logger LOGGER = Logger.getLogger(LinuxKitK8sCluster.class.getName());

    /**
     * The location of the linuxkit VM's kubectl config file.
     */
    private static final String K8S_ADMIN_CONF = "/etc/kubernetes/admin.conf";

    /**
     * The location of the linux kit binary.
     */
    public static final String LINUXKIT_CMD;

    /**
     * The System property to use to set the location of the LinuxKit K8s source code.
     */
    public static final String BEDROCK_LINUXKIT_K8S_HOME = "bedrock.linuxkit.k8s.home";

    /**
     * The location of LinuxKit.
     */
    private String linuxKit;

    /**
     * The location of the LinuxKit K8s source. See https://github.com/linuxkit/kubernetes
     */
    private String linuxKitK8sHome;

    /**
     * The {@link ApplicationConsoleBuilder} to use to create consoles for the linux kit VMs.
     */
    private ApplicationConsoleBuilder consoleBuilder = SystemApplicationConsole.builder();

    /**
     * Temporary folder for this cluster.
     */
    private File tempFolder;

    /**
     * The K8s Master VM {@link Application}.
     */
    private Application appMaster;

    /**
     * The {@link RemotePlatform} linked to the master node VM.
     */
    private RemotePlatform platformMaster;

    /**
     * The master node nat'ed ssh port.
     */
    private Capture<Integer> sshPort;

    /**
     * The master node nat'ed K8s port.
     */
    private Capture<Integer> k8sPort;

    /**
     * The location to write VM logs to.
     */
    private File logDir;

    /**
     * The number of worker nodes to start.
     */
    private int workerNodeCount = 2;

    /**
     * The master VM memory in MB.
     */
    private int masterMemory = 1024;

    /**
     * The master VM memory in MB.
     */
    private int nodeMemory = 2048;

    /**
     * The master VM disc size in GB.
     */
    private int masterDiscSize = 4;

    /**
     * The master VM disc size in GB.
     */
    private int nodeDiscSize = 4;

    /**
     * Flag indicating whether to clear the LinuxKit k8s VM state before starting.
     */
    private boolean clearState = true;

    /**
     * Flag indicating whether the .iso file used by the VM should be copied
     * into the VM state directory due to some environments locking the file
     * and causing issues if multiple VMs use the same .iso file.
     */
    private boolean copyISO = true;

    /**
     * The list of worker node VM {@link Application}s
     */
    private final List<Pair<Application, RemotePlatform>> workerNodes = new ArrayList<>();

    /**
     * Create a {@link LinuxKitK8sCluster}.
     */
    public LinuxKitK8sCluster()
    {
        this(LINUXKIT_CMD, null);
    }

    /**
     * Create a {@link LinuxKitK8sCluster}.
     *
     * @param linuxKitK8sHome  the location of the LinuxKit K8s source
     */
    public LinuxKitK8sCluster(String linuxKitK8sHome)
    {
        this(LINUXKIT_CMD, linuxKitK8sHome);
    }

    /**
     * Create a {@link LinuxKitK8sCluster}.
     *
     * @param linuxKit         the location of LinuxKit
     * @param linuxKitK8sHome  the location of the LinuxKit K8s source
     */
    public LinuxKitK8sCluster(String linuxKit, String linuxKitK8sHome)
    {
        this.linuxKit        = linuxKit;
        this.linuxKitK8sHome = linuxKitK8sHome;

        if (this.linuxKitK8sHome == null || this.linuxKit.isEmpty())
        {
            this.linuxKitK8sHome = System.getProperty(BEDROCK_LINUXKIT_K8S_HOME);
        }

        if (this.linuxKitK8sHome == null || this.linuxKitK8sHome.isEmpty())
        {
            throw new IllegalArgumentException("LinuxKit K8s Home not provided either as a parameter of via the "
                                               + BEDROCK_LINUXKIT_K8S_HOME + " System property");
        }

        try
        {
            tempFolder = FileHelper.createTemporaryFolder("bedrock-linuxkit");
        }
        catch (IOException e)
        {
            throw ensureRuntimeException(e);
        }
    }

    /**
     * Start the K8s cluster.
     */
    @Override
    public void start()
    {
        LOGGER.info("Starting linuxkit k8s cluster in " + linuxKitK8sHome);

        try
        {
            LocalPlatform             platform    = LocalPlatform.get();
            AvailablePortIterator     ports       = platform.getAvailablePorts();
            File                      k8sHome     = new File(linuxKitK8sHome);
            String                    master      = "kube-master-state";
            File                      masterDir   = new File(k8sHome, master);
            String                    os          = System.getProperty("os.name");
            String                    masterDisc  = String.format("size=%dG", masterDiscSize);
            boolean                   efi         = false;
            String                    sshKey      = System.getProperty("user.home") + "/.ssh/id_rsa";
            ApplicationConsoleBuilder console     = consoleBuilder;
            String                    kubeEfi;
            String                    masterImage;


            if (logDir != null)
            {
                logDir.mkdirs();

                console = FileWriterApplicationConsole.builder(logDir.getCanonicalPath(), "", ".log");
            }

            sshPort = new Capture<>(ports);
            k8sPort = new Capture<>(ports);


            if (clearState)
            {
                LOGGER.info("Deleting previous k8s master state in " + masterDir);
                FileHelper.recursiveDelete(masterDir);
            }

            masterDir.mkdirs();

            ensureMasterMetaData(masterDir);

            if ("Mac OS X".equals(os))
            {
                efi = true;
            }

            if (efi)
            {
                masterImage = "kube-master-efi.iso";
                kubeEfi     = "--uefi";
            }
            else
            {
                masterImage = "kube-master.iso";
                kubeEfi     = "";
            }

            LOGGER.info("Starting linuxkit k8s master");

            // start the linuxkit k8s master VM
            appMaster = platform.launch(linuxKit,
                                        Argument.of("run"),
                                        Argument.of("-publish", sshPort.get() + ":22"),
                                        Argument.of("-publish", k8sPort.get() + ":8443"),
                                        Argument.of("-networking", "default"),
                                        Argument.of("-cpus", "2"),
                                        Argument.of("-mem", masterMemory),
                                        Argument.of("-disk", masterDisc),
                                        Argument.of("-state", masterDir.getName()),
                                        Argument.of("-data-file", master + "/metadata.json"),
                                        Argument.of(kubeEfi),
                                        Argument.of(masterImage),
                                        WorkingDirectory.at(linuxKitK8sHome),
                                        DisplayName.of("k8s-master"),
                                        console);

            // create a Bedrock RemotePlatform that can be used to execute commands directly on the master VM
            platformMaster = new RemotePlatform("master",
                                                InetAddress.getByName("localhost"),
                                                sshPort.get(),
                                                "root",
                                                SecureKeys.fromPrivateKeyFile(sshKey),
                                                WorkingDirectory.at("/root"),
                                                Deployer.NULL,
                                                ContainerdCommandInterceptor.instance(),
                                                StrictHostChecking.disabled(),
                                                UserKnownHostsFile.at("/dev/null"));

            LOGGER.info("Waiting to connect to k8s master...");

            // we are now basically waiting for the master VM to start by seeing if we can ssh into it
            Eventually.assertThat(invoking(this).canConnectTo(platformMaster),
                                  is(true),
                                  Timeout.after(2, TimeUnit.MINUTES));

            LOGGER.info("Connected to k8s master");

            // copy the kubectl config from the master VM to a local folder so that we
            // can use kubectl from the local host
            writeKubectlConfig();

            LOGGER.info("Waiting for k8s master status to be Ready...");

            // The linuxkit VMs take a while to actually start and initialize K8s
            // so we'll pause here to give the master node time to get going
            Thread.sleep(60000);

            // Now wait for the K8s master status to be ready
            Eventually.assertThat(invoking(this).isMasterReady(), is(true), Timeout.after(2, TimeUnit.MINUTES));

            LOGGER.info("k8s master status is Ready");

            LOGGER.info("Starting " + workerNodeCount + " k8s worker nodes...");


            for (int i = 0; i < workerNodeCount; i++)
            {
                int nodeId = addWorkerNode();

                // The linuxkit VMs take a while to actually start and initialize K8s
                // so we'll pause here to give the worker nodes time to get going
                Thread.sleep(30000);

                // Now wait for the K8s worker node status to be ready
                LOGGER.info("Waiting for all " + (1 + nodeId) + " k8s nodes to be ready...");
                Eventually.assertThat(invoking(this).areAllNodesReady(1 + nodeId), is(true),
                                      Timeout.after(2, TimeUnit.MINUTES));
            }

            LOGGER.info("Started " + workerNodeCount + " k8s worker nodes");


            LOGGER.info("K8s Cluster is Ready");
        }
        catch (Exception e)
        {
            throw ensureRuntimeException(e);
        }
    }

    @Override
    public void close()
    {
        for (Pair<Application, RemotePlatform> pair : workerNodes)
        {
            // send the poweroff command to the worker node VM
            close(pair.getX(), pair.getY());
        }

        workerNodes.clear();

        if (appMaster != null)
        {
            // send the poweroff command to the master VM
            close(appMaster, platformMaster);
        }

    }

    private void close(Application application, RemotePlatform platform)
    {
        try
        {
            platform.launch("poweroff -f");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            application.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Add a worker node to the cluster.
     *
     * @return the id of the new node
     *
     * @throws Exception  if there is an error
     */
    public synchronized int addWorkerNode() throws Exception
    {
        LocalPlatform             platform    = LocalPlatform.get();
        AvailablePortIterator     ports       = platform.getAvailablePorts();
        File                      k8sHome     = new File(linuxKitK8sHome);
        String                    os          = System.getProperty("os.name");
        boolean                   efi         = false;
        ApplicationConsoleBuilder console     = consoleBuilder;
        String                    nodeDisc    = String.format("size=%dG", nodeDiscSize);
        String                    sshKey      = System.getProperty("user.home") + "/.ssh/id_rsa";
        String                    nodeImage;
        String                    kubeEfi;

        if (logDir != null)
        {
            logDir.mkdirs();

            console = FileWriterApplicationConsole.builder(logDir.getCanonicalPath(), "", ".log");
        }

        if ("Mac OS X".equals(os))
        {
            efi = true;
        }

        if (efi)
        {
            nodeImage   = "kube-node-efi.iso";
            kubeEfi     = "--uefi";
        }
        else
        {
            nodeImage   = "kube-node.iso";
            kubeEfi     = "";
        }

        String           masterAddress = getMasterAddress();
        String           joinToken     = getJoinToken();
        int              nodeId        = 1 + workerNodes.size();
        String           node          = "kube-node-" + nodeId + "-state";
        File             nodeDir       = new File(k8sHome, node);
        Capture<Integer> port          = new Capture<>(ports);

        if (clearState)
        {
            LOGGER.info("Deleting previous k8s master state in " + nodeDir);
            FileHelper.recursiveDelete(nodeDir);
        }

        nodeDir.mkdirs();

        File fileNodeISO = new File(nodeDir, nodeImage);

        if (!fileNodeISO.exists())
        {
            Files.copy(new File(k8sHome, nodeImage).toPath(), fileNodeISO.toPath());
        }
        
        ensureNodeMetaData(nodeDir, joinToken, masterAddress);

        LOGGER.info("Starting k8s worker node " + nodeId + "...");

        Application appNode = platform.launch(linuxKit,
                                            Argument.of("run"),
                                            Argument.of("-publish", port.get() + ":22"),
                                            Argument.of("-networking", "default"),
                                            Argument.of("-cpus", "2"),
                                            Argument.of("-mem", nodeMemory),
                                            Argument.of("-disk", nodeDisc),
                                            Argument.of("-state", nodeDir.getName()),
                                            Argument.of("-data-file", node + "/metadata.json"),
                                            Argument.of(kubeEfi),
                                            Argument.of(nodeDir + "/" + nodeImage),
                                            WorkingDirectory.at(linuxKitK8sHome),
                                            DisplayName.of("k8s-node-" + nodeId),
                                            console);

        RemotePlatform platformNode = new RemotePlatform("node-" + nodeId,
                                                        InetAddress.getByName("localhost"),
                                                        port.get(),
                                                        "root",
                                                        SecureKeys.fromPrivateKeyFile(sshKey),
                                                        WorkingDirectory.at("/root"),
                                                        Deployer.NULL,
                                                        ContainerdCommandInterceptor.instance(),
                                                        StrictHostChecking.disabled(),
                                                        UserKnownHostsFile.at("/dev/null"));

        // we are now basically waiting for the node VM to start by seeing if we can ssh into it
        LOGGER.info("Waiting for k8s worker node " + nodeId + " VM to start...");
        Eventually.assertThat(invoking(this).canConnectTo(platformNode),
                              is(true),
                              Timeout.after(2, TimeUnit.MINUTES));

        LOGGER.info("K8s worker node " + nodeId + " VM started");

        workerNodes.add(new Pair<>(appNode, platformNode));

        return nodeId;
    }

    @Override
    public boolean isMasterReady()
    {
        CapturingApplicationConsole console = new CapturingApplicationConsole();
        String                      command = "kubectl";

        try (Application application = platformMaster.launch(command,
                                                             Argument.of("--kubeconfig"),
                                                             Argument.of(K8S_ADMIN_CONF),
                                                             Arguments.of("get", "nodes"),
                                                             Console.of(console),
                                                             LaunchLogging.disabled()))
        {
            int exitCode = application.waitFor();

            if (exitCode == 0)
            {
                String line = console.getCapturedOutputLines().stream()
                              .filter(this::isMasterNodeLine)
                              .findFirst()
                              .orElse("");

                LOGGER.info("Master status check: line=" + line);

                return "Ready".equalsIgnoreCase(getNodeStatus(line));
            } 
            else
            {
                String lines = console.getCapturedOutputLines().stream().collect(Collectors.joining("\n"))
                             + console.getCapturedErrorLines().stream().collect(Collectors.joining("\n"));

                LOGGER.info("Master status check: return code=" + exitCode + " console=\n" + lines);
            }
        }
        catch (Exception e)
        {
            // ignored
        }

        return false;
    }

    public boolean areAllNodesReady(int nodeCount)
    {
        CapturingApplicationConsole console = new CapturingApplicationConsole();
        String                      command = "kubectl";

        try (Application application = platformMaster.launch(command,
                                                             Argument.of("--kubeconfig"),
                                                             Argument.of(K8S_ADMIN_CONF),
                                                             Arguments.of("get", "nodes"),
                                                             Console.of(console),
                                                             LaunchLogging.disabled()))
        {
            int exitCode = application.waitFor();

            LOGGER.info("Node status check: return code=" + exitCode + " console=\n"
                        + console.getCapturedOutputLines().stream().collect(Collectors.joining("\n"))
                        + console.getCapturedErrorLines().stream().collect(Collectors.joining("\n")));

            if (exitCode == 0)
            {

                Queue<String> lines = console.getCapturedOutputLines();
                
                // pull off the header
                lines.poll();

                // count the ready lines
                long readyCount = lines.stream()
                                       .filter(s -> !("(terminated)".equals(s))) // ignore the terminator line
                                       .filter(s -> "ready".equalsIgnoreCase(getNodeStatus(s)))
                                       .count();

                return readyCount == nodeCount;
            }
        }
        catch (Exception e)
        {
            // ignored
        }

        return false;
    }

    /**
     * Determine whether the master node VM is running.
     *
     * @return  {@code true} if the master node VM is running
     */
    public boolean isMasterVmRunning()
    {
        return appMaster != null && appMaster.isOperational();
    }

    /**
     * Obtain the {@link RemotePlatform} to use to execute
     * processes on the master node VM.
     *
     * @return  the {@link RemotePlatform} to use to execute
     *          processes on the master node VM
     */
    public RemotePlatform getMasterPlatform()
    {
        if (isMasterVmRunning() && platformMaster != null)
        {
            throw new IllegalStateException("Master node is not running");
        }

        return platformMaster;
    }

    /**
     * Set the {@link ApplicationConsoleBuilder} to use to build consoles for
     * the Linux Kit VMs.
     *
     * @param builder  the {@link ApplicationConsoleBuilder} to use
     *
     * @return  this {@link LinuxKitK8sCluster}
     */
    public LinuxKitK8sCluster withConsoleBuilder(ApplicationConsoleBuilder builder)
    {
        this.consoleBuilder = builder;

        return this;
    }

    /**
     * Set the location for the VM logs.
     *
     * @param logDir  the location for the VM logs
     *
     * @return  this {@link LinuxKitK8sCluster}
     */
    public LinuxKitK8sCluster withLogsAt(File logDir)
    {
        this.logDir = logDir;

        return this;
    }

    /**
     * Set the number of worker nodes to create.
     *
     * @param count  the number of worker nodes
     *
     * @return  this {@link LinuxKitK8sCluster}
     */
    public LinuxKitK8sCluster withWorkerCount(int count)
    {
        workerNodeCount = count;
        
        return this;
    }

    /**
     * Set the number of worker nodes to create.
     *
     * @param clear  {@code true} to clear the k8s LinuxKit VM state prior to starting VM instances
     *
     * @return  this {@link LinuxKitK8sCluster}
     */
    public LinuxKitK8sCluster withClearedState(boolean clear)
    {
        clearState = clear;

        return this;
    }

    /**
     * Set the flag indicating whether to copy the iso file used by the VM.
     *
     * @param copy  {@code true} to make a copy of the .iso file used by the VM
     *
     * @return  this {@link LinuxKitK8sCluster}
     */
    public LinuxKitK8sCluster withIsoCopy(boolean copy)
    {
        copyISO = copy;

        return this;
    }

    /**
     * Determine whether a connection can be made to the
     * specified node {@link RemotePlatform}.
     *
     * @param platform  the node to attempt to connect to
     *
     * @return  {@code true} if a connection can be made to
     *          the node {@link RemotePlatform}
     */
    // must be public - used in Eventually.assertThat
    public boolean canConnectTo(RemotePlatform platform)
    {
        if (isMasterVmRunning())
        {
            try (Application application = platform.launch("echo connection test", LaunchLogging.disabled()))
            {
                int exitCode = application.waitFor();

                return exitCode == 0;
            }
            catch (Throwable t)
            {
                // ignored
            }
        }

        return false;
    }

    /**
     * Write the linuxkit metadata file for the master VM.
     *
     * @param masterDir  the folder to write the metadata file to
     *
     * @throws IOException  if an error occurs writing the metadata
     */
    private void ensureMasterMetaData(File masterDir) throws IOException
    {
        File file = new File(masterDir, "metadata.json");

        if (!file.exists())
        {
            try (PrintWriter writer = new PrintWriter(file))
            {
                writer.print("{ \"kubeadm\": { \"entries\": { \"init\": { \"content\": \"\" } } } }");
            }
        }
    }

    /**
     * Write the linuxkit metadata file for the master VM.
     *
     * @param nodeDir        the folder to write the metadata file to
     * @param joinToken      the k8s join token to use
     * @param masterAddress  the IP address of the master node
     *
     * @throws IOException  if an error occurs writing the metadata
     */
    private void ensureNodeMetaData(File nodeDir, String joinToken, String masterAddress) throws IOException
    {
        File file = new File(nodeDir, "metadata.json");

        if (!file.exists())
        {
            String nodeMetaData = String.format("{ \"kubeadm\": { \"entries\": { \"join\": { \"content\": " +
                                                "\"--token %s %s:6443 --discovery-token-unsafe-skip-ca-verification\" }}}}",
                                                joinToken, masterAddress);


            try (PrintWriter writer = new PrintWriter(file))
            {
                writer.print(nodeMetaData);
            }
        }
    }

    /**
     * Write out the kubectl configuration file to use.
     *
     * @throws IOException  if an error occurs
     */
    private void writeKubectlConfig() throws IOException
    {
        LOGGER.info("Obtaining kubectl configuration from master...");

        Eventually.assertThat(invoking(this).masterKubectlConfigExists(), is(true), Timeout.after(2, TimeUnit.MINUTES));
        
        CapturingApplicationConsole console = new CapturingApplicationConsole();
        String                      catCmd  = "cat /etc/kubernetes/admin.conf";

        try (Application application = platformMaster.launch(catCmd, Console.of(console)))
        {
            application.waitFor();
        }

        File kubectlConfigFile = new File(tempFolder, "admin.conf");

        try (PrintWriter writer = new PrintWriter(kubectlConfigFile))
        {
            console.getCapturedOutputLines()
                   .stream()
                   .filter(line -> !line.contains("(terminated)"))
                   .map(this::convertLine)
                   .forEach(writer::println);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(kubectlConfigFile)))
        {
            LOGGER.info(() -> "Saved kubectl configuration to " + kubectlConfigFile + "\n"
                              + reader.lines().collect(Collectors.joining("\n")));
        }

        withKubectlConfig(kubectlConfigFile);
    }

    /**
     * Determine whether the kubectl config file exists on the master VM.
     *
     * @return  {@code true} if the kubectl config file exists on the master VM
     */
    // must be public to be used in Eventually.assertThat
    public boolean masterKubectlConfigExists()
    {
        try
        {
            String cmdTest = "test -f /etc/kubernetes/admin.conf";

            try (Application application = platformMaster.launch(cmdTest,
                                                                 DisplayName.of("test"),
                                                                 LaunchLogging.disabled(),
                                                                 SystemApplicationConsole.builder()))
            {
                return application.waitFor() == 0;
            }
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Obtain the internal IP address of the K8s master node.
     *
     * @return  the internal IP address of the K8s master node
     */
    protected String getMasterAddress()
    {
        CapturingApplicationConsole console = new CapturingApplicationConsole();
        String                      command = "ip -f inet -o addr show eth0";

        try (Application kubectl = platformMaster.launch(command,
                                                         Console.of(console),
                                                         LaunchLogging.disabled()))
        {
            if (kubectl.waitFor() == 0)
            {
                String   line  = console.getCapturedOutputLines().poll();
                String[] parts = line.split("\\s+");
                String   ip    = parts[3];

                return ip.split("/")[0];
            }
        }

        return null;
    }

    /**
     * Obtain the join token from the k8s master node that can be used
     * by worker nodes to join the cluster.
     *
     * @return  the join token from the k8s master node
     */
    protected String getJoinToken()
    {
        CapturingApplicationConsole console = new CapturingApplicationConsole();

        try (Application kubeadm = platformMaster.launch(SimpleApplication.class,
                                                         Executable.named("kubeadm"),
                                                         Arguments.of("token", "list"),
                                                         Console.of(console),
                                                         DisplayName.of("kubeadm")))
        {
            if (kubeadm.waitFor() == 0)
            {
                Queue<String> lines = console.getCapturedOutputLines();

                if (lines.size() > 1)
                {
                    // skip the header line
                    lines.poll();

                    // read the first token line
                    String line = lines.poll();

                    // return the token part
                    return line.split("\\s+")[0];
                }
            }

        }

        return null;
    }

    /**
     * Convert lines of the kubectl configuration file.
     *
     * @param line  the line to convert
     *
     * @return  the converted line
     */
    private String convertLine(String line)
    {
        if (line.startsWith("    server: https://"))
        {
            // this is the server line so convert to local host and the nat'ed port
            return "    server: https://127.0.0.1:" + k8sPort.get();
        }

        return line;
    }

    // initialise the linuxkit location
    static
    {
        String linux = System.getProperty("bedrock.linuxkit");

        if (linux == null || linux.isEmpty())
        {
            linux = "/usr/local/bin/linuxkit";
        }

        LINUXKIT_CMD = linux;
    }
}
