/*
 * File: WindowsSession.java
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

package com.oracle.tools.runtime.remote.winrm;

import com.microsoft.wsman.shell.*;

import com.oracle.tools.Option;
import com.oracle.tools.Options;

import com.oracle.tools.lang.StringHelper;

import com.oracle.tools.runtime.remote.Authentication;

import org.dmtf.wsman.AttributableDuration;
import org.dmtf.wsman.AttributableURI;
import org.dmtf.wsman.MaxEnvelopeSizeType;
import org.dmtf.wsman.OptionSet;
import org.dmtf.wsman.OptionType;
import org.dmtf.wsman.SelectorSetType;
import org.dmtf.wsman.SelectorType;

import org.w3c.soap.envelope.Body;
import org.w3c.soap.envelope.Envelope;
import org.w3c.soap.envelope.Header;

import org.xmlsoap.ws.addressing.AttributedURI;
import org.xmlsoap.ws.addressing.EndpointReferenceType;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/**
 * This class represents a reference to a WinRS remote
 * shell.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class WindowsSession implements Closeable
{
    /**
     * The action name for the WS-Man Create action
     */
    public static final String ACTION_CREATE = "http://schemas.xmlsoap.org/ws/2004/09/transfer/Create";

    /**
     * The action name for the WinRM Command action
     */
    public static final String ACTION_COMMAND = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Command";

    /**
     * The action name for the WinRM Receive action
     */
    public static final String ACTION_RECEIVE = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Receive";

    /**
     * The action name for the WinRM Signal action
     */
    public static final String ACTION_SIGNAL = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Signal";

    /**
     * The action name for the WinRM Delete action
     */
    public static final String ACTION_DELETE = "http://schemas.xmlsoap.org/ws/2004/09/transfer/Delete";

    /**
     * The action name for the WinRM Send action
     */
    public static final String ACTION_SEND = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Send";

    /**
     * The action name for the WinRM terminate signal
     */
    public static final String SIGNAL_TERM = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/signal/terminate";

    /**
     * The WinRM resource URI used in the SOAP envelope
     */
    public static final String URI_WINRM_RESOURCE = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/cmd";

    /**
     * The reply-to URI used in the SOAP envelope
     */
    public static final String URI_REPLY_TO = "http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous";

    /**
     * The default port that WinRM listens on
     */
    public static final int DEFAULT_WINRM_PORT = 5985;

    /**
     * The default WinRM url suffix
     */
    public static final String WSMAN_PATH = "/wsman";

    /**
     * The set of {@link Options} to use to control the session
     */
    private Options options;

    /**
     * The {@link SoapConnection} instance to use to send SOAP messages
     */
    private SoapConnection connection;

    /**
     * The reference ID for the remote WinRM shell
     */
    private String shellReferenceId;

    /**
     * The ID of the current command
     */
    private String commandId;

    /**
     * The {@link OutputStreamConnector} for the currently executing command
     */
    private OutputStreamConnector outputStreamConnector;

    /**
     * The {@link InputStreamConnector} for the currently executing command
     */
    private InputStreamConnector inputStreamConnector;


    /**
     * Create a new {@link WindowsSession} that connects to the
     * WinRM service on the specified host.
     *
     * @param connection      the {@link SoapConnection} to use to send SOAP messages
     * @param options         the {@link com.oracle.tools.Option}s controlling the session
     */
    public WindowsSession(SoapConnection connection,
                          Option...      options)
    {
        this.connection = connection;
        this.options    = new Options(options);
    }


    /**
     * Create a new {@link WindowsSession} that connects to the
     * WinRM service on the specified host.
     *
     * @param hostName        the host name of the host running the WinRM service
     * @param port            the port that the WinRM service is listening on
     * @param userName        the name of the user to use to connect to the WinRM service
     * @param authentication  the authentication to use to connect to the WinRM
     * @param options         the {@link Option}s controlling the session
     */
    public WindowsSession(String         hostName,
                          int            port,
                          String         userName,
                          Authentication authentication,
                          Option...      options)
    {
        this(new SoapConnection(hostName, port, WSMAN_PATH, userName, authentication, options), options);
    }


    /**
     * Obtain the {@link SoapConnection} being used by this
     * {@link WindowsSession} to send SOAP messages.
     *
     * @return the {@link SoapConnection} being used by this
     *         {@link WindowsSession} to send SOAP messages
     */
    public SoapConnection getSoapConnection()
    {
        return connection;
    }

    /**
     * Obtain the ID of the WinRM shell being used by this session.
     *
     * @return the ID of the WinRM shell being used by this session
     */
    public String getShellReferenceId()
    {
        return shellReferenceId;
    }


    /**
     * Set the reference ID of the currently connected WinRM Shell.
     *
     * @param id the reference ID of the currently connected WinRM Shell
     */
    protected void setShellReferenceId(String id)
    {
        this.shellReferenceId = id;
    }


    /**
     * Obtain the ID of the currently executing command.
     *
     * @return the ID of the currently executing command
     */
    public String getCommandId()
    {
        return commandId;
    }


    /**
     * Set the ID of the currently executing command.
     *
     * @param commandId the ID of the currently
     *                  executing command
     */
    protected void setCommandId(String commandId)
    {
        this.commandId = commandId;
    }


    /**
     * Obtain the {@link InputStreamConnector} that connects the
     * current command stdin stream.
     *
     * @return the {@link InputStreamConnector} that connects the
     *         current command stdin stream
     */
    protected InputStreamConnector getInputStreamConnector()
    {
        return inputStreamConnector;
    }


    /**
     * Set the {@link InputStreamConnector} that connects the
     * current command stdin stream.
     *
     * @param inputStreamConnector the {@link InputStreamConnector}
     *                             that connects the current command
     *                             stdin stream
     */
    protected void setInputStreamConnector(InputStreamConnector inputStreamConnector)
    {
        this.inputStreamConnector = inputStreamConnector;
    }


    protected OutputStreamConnector getOutputStreamConnector()
    {
        return outputStreamConnector;
    }


    protected void setOutputStreamConnector(OutputStreamConnector outputStreamConnector)
    {
        this.outputStreamConnector = outputStreamConnector;
    }


    /**
     * Close this {@link WindowsSession}.
     */
    @Override
    public void close()
    {
        if (shellReferenceId == null)
        {
            return;
        }

        try
        {
            // Terminate the current command if one is running
            try
            {
                terminateCommand();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            // Delete the current Shell
            try
            {
                connection.send(createEnvelope(ACTION_DELETE));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        finally
        {
            commandId        = null;
            shellReferenceId = null;
        }
    }


    /**
     * Connect this {@link WindowsSession} to the remote Windows
     * platform. The working directory will be the users home
     * directory.
     *
     * @throws Exception if the connection fails
     */
    protected void connect() throws Exception
    {
        connect(null);
    }


    /**
     * Connect this {@link WindowsSession} to the remote Windows
     * platform. The working directory will be the specified
     * directory.
     *
     * @param workingDirectory the working directory for this session
     *
     * @throws Exception if the connection fails
     */
    protected void connect(String workingDirectory) throws Exception
    {
        connect(workingDirectory, null);
    }


    /**
     * Connect this {@link WindowsSession} to the remote Windows
     * platform. The working directory will be the specified
     * directory.
     *
     * @param workingDirectory the working directory for this session
     * @param environment      the environment variables to set for the
     *                         session
     *
     * @throws IOException           if the connection fails
     * @throws IllegalStateException if this session is already connected
     */
    protected void connect(String     workingDirectory,
                           Properties environment) throws IOException
    {
        if (shellReferenceId != null)
        {
            throw new IllegalStateException("Already connected to shell " + shellReferenceId);
        }

        WindowsShellOptions shellOptions = options.get(WindowsShellOptions.class, WindowsShellOptions.basic());
        ShellType           shellType    = ObjectFactories.SHELL.createShellType();

        if (workingDirectory == null || workingDirectory.isEmpty())
        {
            shellType.setWorkingDirectory("%USERPROFILE%");
        }
        else
        {
            shellType.setWorkingDirectory(workingDirectory);
        }

        shellType.setLifetime(shellOptions.getShellLifetime());
        shellType.getInputStreams().add("stdin");
        shellType.getOutputStreams().add("stdout");
        shellType.getOutputStreams().add("stderr");

        if (environment != null && environment.size() > 0)
        {
            EnvironmentVariableList   environmentVariableList = ObjectFactories.SHELL.createEnvironmentVariableList();
            List<EnvironmentVariable> variables               = environmentVariableList.getVariable();

            for (String name : environment.stringPropertyNames())
            {
                EnvironmentVariable variable = ObjectFactories.SHELL.createEnvironmentVariable();

                variable.setName(name);
                variable.setValue(StringHelper.doubleQuoteIfNecessary(environment.getProperty(name)));
                variables.add(variable);
            }

            shellType.setEnvironment(environmentVariableList);
        }

        AttributedURI toURI = ObjectFactories.ADDRESSING.createAttributedURI();

        toURI.setValue(connection.getUrl().toExternalForm());

        Properties       properties  = shellOptions.getBuilder().realize();
        OptionSet        optionSet   = ObjectFactories.WSMAN.createOptionSet();
        List<OptionType> optionsList = optionSet.getOption();

        for (String name : properties.stringPropertyNames())
        {
            OptionType optionType = ObjectFactories.WSMAN.createOptionType();

            optionType.setName(name);
            optionType.setValue(properties.getProperty(name));
            optionsList.add(optionType);
        }

        Envelope     envelope       = createEnvelope(ACTION_CREATE);
        Header       header         = envelope.getHeader();
        List<Object> headerElements = header.getAny();

        headerElements.add(optionSet);

        envelope.getBody().getAny().add(ObjectFactories.SHELL.createShell(shellType));

        List<Object> results = connection.send(envelope);

        if (results != null)
        {
            ShellType resultShell = findFirst(results, ShellType.class);

            shellReferenceId = resultShell.getShellId();
        }
    }


    /**
     * Execute the specified command in the current Shell.
     *
     * @param command the command to execute
     * @param args    the command line arguments for the command
     * @param stdIn   the {@link InputStream} to use as the commands stdin
     * @param stdOut  the {@link OutputStream} to use as the commands stdout
     * @param stdErr  the {@link OutputStream} to use as the commands stderr
     *
     * @throws IOException if an error occurs executing the command
     */
    public void execute(String       command,
                        List<String> args,
                        InputStream  stdIn,
                        OutputStream stdOut,
                        OutputStream stdErr) throws IOException
    {
        Envelope     envelope           = createEnvelope(ACTION_COMMAND);
        Header       header             = envelope.getHeader();
        List<Object> headerElements     = header.getAny();

        OptionType   optionWinRSProfile = ObjectFactories.WSMAN.createOptionType();

        optionWinRSProfile.setName("WINRS_CONSOLEMODE_STDIN");
        optionWinRSProfile.setValue("TRUE");

        OptionSet        optionSet = ObjectFactories.WSMAN.createOptionSet();
        List<OptionType> options   = optionSet.getOption();

        options.add(optionWinRSProfile);

        headerElements.add(optionSet);

        CommandLine  commandLine = ObjectFactories.SHELL.createCommandLine();
        List<String> arguments   = commandLine.getArguments();

        commandLine.setCommand(command);
        arguments.addAll(args);

        envelope.getBody().getAny().add(ObjectFactories.SHELL.createCommandLine(commandLine));

        List<?>         results  = connection.send(envelope);
        CommandResponse response = findFirst(results, CommandResponse.class);

        commandId             = response.getCommandId();
        outputStreamConnector = createOutputStreamConnector(stdOut, stdErr);
        inputStreamConnector  = createInputStreamConnector(stdIn);

        outputStreamConnector.start();
        inputStreamConnector.start();
    }


    protected OutputStreamConnector createOutputStreamConnector(OutputStream stdOut, OutputStream stdErr)
    {
        return new OutputStreamConnector(this, stdOut, stdErr);
    }


    protected InputStreamConnector createInputStreamConnector(InputStream  stdIn)
    {
        return new InputStreamConnector(this, stdIn);
    }


    protected void terminateCommand() throws IOException
    {
        if (commandId == null || commandId.isEmpty())
        {
            return;
        }

        try
        {
            Signal signal = ObjectFactories.SHELL.createSignal();

            signal.setCommandId(commandId);
            signal.setCode(SIGNAL_TERM);

            Envelope envelope = createEnvelope(ACTION_SIGNAL);

            envelope.getBody().getAny().add(ObjectFactories.SHELL.createSignal(signal));

            connection.send(envelope);

            if (outputStreamConnector != null)
            {
                outputStreamConnector.close();
            }

            try
            {
                if (inputStreamConnector != null)
                {
                    inputStreamConnector.close();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        finally
        {
            commandId = null;
        }
    }


    /**
     * Read the output from the current commands stdout and stderr streams.
     *
     * @return the {@link ReceiveResponse} containing the stdout
     *         and stderr streams
     *
     * @throws IOException if an error occurs
     */
    protected ReceiveResponse readOutputStreams() throws IOException
    {
        Envelope     envelope       = createEnvelope(ACTION_RECEIVE);
        Header       header         = envelope.getHeader();
        List<Object> headerElements = header.getAny();

        OptionType   optionType     = ObjectFactories.WSMAN.createOptionType();

        optionType.setName("WSMAN_CMDSHELL_OPTION_KEEPALIVE");
        optionType.setValue("TRUE");

        OptionSet        optionSet = ObjectFactories.WSMAN.createOptionSet();
        List<OptionType> options   = optionSet.getOption();

        options.add(optionType);

        headerElements.add(optionSet);

        Receive           receive    = ObjectFactories.SHELL.createReceive();
        DesiredStreamType streamType = ObjectFactories.SHELL.createDesiredStreamType();

        streamType.setCommandId(commandId);
        streamType.getValue().add("stdout");
        streamType.getValue().add("stderr");

        receive.setDesiredStream(streamType);

        envelope.getBody().getAny().add(ObjectFactories.SHELL.createReceive(receive));

        List<?> results = connection.send(envelope);

        return findFirst(results, ReceiveResponse.class);
    }


    protected void writeToInputStream(String message) throws IOException
    {
        if (commandId == null)
        {
            throw new IllegalStateException("Cannot pipe to StdIn if not executing a command");
        }

        Envelope   envelope   = createEnvelope(ACTION_SEND);
        Send       send       = ObjectFactories.SHELL.createSend();
        StreamType streamType = ObjectFactories.SHELL.createStreamType();

        streamType.setCommandId(commandId);
        streamType.setName("stdin");
        streamType.unsetEnd();
        streamType.setValue(message.getBytes());

        send.getStream().add(streamType);

        envelope.getBody().getAny().add(ObjectFactories.SHELL.createSend(send));

        connection.send(envelope);
    }


    protected Envelope createEnvelope(String action) throws IOException
    {
        if (action == null || action.isEmpty())
        {
            throw new IllegalArgumentException("Actions string cannot be null or empty string");
        }

        Envelope envelope = ObjectFactories.SOAP.createEnvelope();
        Header   header   = ObjectFactories.SOAP.createHeader();
        Body     body     = ObjectFactories.SOAP.createBody();

        envelope.setHeader(header);
        envelope.setBody(body);

        List<Object>  headerElements = header.getAny();

        AttributedURI toURI          = ObjectFactories.ADDRESSING.createAttributedURI();

        toURI.setValue(connection.getUrl().toExternalForm());

        AttributedURI replyToURI = ObjectFactories.ADDRESSING.createAttributedURI();

        replyToURI.setValue(URI_REPLY_TO);

        EndpointReferenceType endpoint = ObjectFactories.ADDRESSING.createEndpointReferenceType();

        endpoint.setAddress(replyToURI);

        AttributedURI messageId = ObjectFactories.ADDRESSING.createAttributedURI();

        messageId.setValue("uuid:" + UUID.randomUUID().toString().toUpperCase());

        AttributableURI resourceURI = ObjectFactories.WSMAN.createAttributableURI();

        resourceURI.setValue(URI_WINRM_RESOURCE);

        WindowsSoapOptions  soapOptions     = options.get(WindowsSoapOptions.class, WindowsSoapOptions.basic());

        MaxEnvelopeSizeType maxEnvelopeSize = ObjectFactories.WSMAN.createMaxEnvelopeSizeType();

        maxEnvelopeSize.setValue(soapOptions.getMaxEnvelopeSize());

        AttributableDuration timeout = ObjectFactories.WSMAN.createAttributableDuration();

        timeout.setValue(soapOptions.getTimeout());

        headerElements.add(ObjectFactories.ADDRESSING.createTo(toURI));
        headerElements.add(ObjectFactories.ADDRESSING.createReplyTo(endpoint));
        headerElements.add(ObjectFactories.ADDRESSING.createMessageID(messageId));
        headerElements.add(ObjectFactories.WSMAN.createResourceURI(resourceURI));
        headerElements.add(ObjectFactories.WSMAN.createMaxEnvelopeSize(maxEnvelopeSize));
        headerElements.add(ObjectFactories.WSMAN.createOperationTimeout(timeout));

        AttributedURI actionURI = ObjectFactories.ADDRESSING.createAttributedURI();

        actionURI.setValue(action);
        headerElements.add(ObjectFactories.ADDRESSING.createAction(actionURI));

        if (shellReferenceId != null)
        {
            SelectorSetType    selectorSet   = ObjectFactories.WSMAN.createSelectorSetType();
            List<SelectorType> selectors     = selectorSet.getSelector();

            SelectorType       shellSelector = ObjectFactories.WSMAN.createSelectorType();

            shellSelector.setName("ShellId");
            shellSelector.getContent().add(shellReferenceId);

            selectors.add(shellSelector);

            headerElements.add(ObjectFactories.WSMAN.createSelectorSet(selectorSet));
        }

        return envelope;
    }


    @SuppressWarnings("unchecked")
    protected <T> T findFirst(Collection<?> collection,
                              Class<T>      type)
    {
        for (Object o : collection)
        {
            if (type.isAssignableFrom(o.getClass()))
            {
                return (T) o;
            }
        }

        return null;
    }


    /**
     * Obtain the exit code from the last executed command.
     *
     * @return the exit code from the last executed command
     *         or -1 if no command has been executed
     */
    public int exitValue()
    {
        return outputStreamConnector != null ? outputStreamConnector.getExitCode() : -1;
    }


    /**
     * Wait for the currently executing command to terminate and return
     * its exit value.
     *
     * @return the exit value from the currently executing command
     */
    public int waitFor()
    {
        return outputStreamConnector.waitFor();
    }
}
