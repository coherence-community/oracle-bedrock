package com.oracle.bedrock.runtime.java;

import com.oracle.bedrock.OptionsByType;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A builder for {@link Process} implementations.
 */
public interface LocalProcessBuilder {
    /**
     * Sets this process builder's operating system program and
     * arguments.  This is a convenience method that sets the command
     * to a string list containing the same strings as the
     * {@code command} array, in the same order.  It is not
     * checked whether {@code command} corresponds to a valid
     * operating system command.
     *
     * @param  command a string array containing the program and its arguments
     * @return this process builder
     */
    LocalProcessBuilder command(String... command);

    /**
     * Returns this process builder's operating system program and
     * arguments.  The returned list is <i>not</i> a copy.  Subsequent
     * updates to the list will be reflected in the state of this
     * process builder.
     *
     * @return this process builder's program and its arguments
     */
    List<String> command();

    /**
     * Sets this process builder's working directory.
     *
     * Subprocesses subsequently started by this object's {@link
     * #start(OptionsByType)} method will use this as their working directory.
     * The argument may be {@code null} -- this means to use the
     * working directory of the current Java process, usually the
     * directory named by the system property {@code user.dir},
     * as the working directory of the child process.
     *
     * @param  directory the new working directory
     * @return this process builder
     */
    LocalProcessBuilder directory(File directory);


    /**
     * Returns this process builder's working directory.
     *
     * Subprocesses subsequently started by this object's {@link
     * #start(OptionsByType)} method will use this as their working directory.
     * The returned value may be {@code null} -- this means to use
     * the working directory of the current Java process, usually the
     * directory named by the system property {@code user.dir},
     * as the working directory of the child process.
     *
     * @return this process builder's working directory
     */
    File directory();

    /**
     * Returns a string map view of this process builder's environment.
     *
     * Whenever a process builder is created, the environment is
     * initialized to a copy of the current process environment (see
     * {@link System#getenv()}).  Subprocesses subsequently started by
     * this object's {@link #start(OptionsByType)} method will use this map as
     * their environment.
     *
     * <p>The returned object may be modified using ordinary {@link
     * java.util.Map Map} operations.  These modifications will be
     * visible to subprocesses started via the {@link #start(OptionsByType)}
     * method.  Two {@code ProcessBuilder} instances always
     * contain independent process environments, so changes to the
     * returned map will never be reflected in any other
     * {@code ProcessBuilder} instance or the values returned by
     * {@link System#getenv System.getenv}.
     *
     * <p>If the system does not support environment variables, an
     * empty map is returned.
     *
     * <p>The returned map does not permit null keys or values.
     * Attempting to insert or query the presence of a null key or
     * value will throw a {@link NullPointerException}.
     * Attempting to query the presence of a key or value which is not
     * of type {@link String} will throw a {@link ClassCastException}.
     *
     * <p>The behavior of the returned map is system-dependent.  A
     * system may not allow modifications to environment variables or
     * may forbid certain variable names or values.  For this reason,
     * attempts to modify the map may fail with
     * {@link UnsupportedOperationException} or
     * {@link IllegalArgumentException}
     * if the modification is not permitted by the operating system.
     *
     * <p>Since the external format of environment variable names and
     * values is system-dependent, there may not be a one-to-one
     * mapping between them and Java's Unicode strings.  Nevertheless,
     * the map is implemented in such a way that environment variables
     * which are not modified by Java code will have an unmodified
     * native representation in the subprocess.
     *
     * <p>The returned map and its collection views may not obey the
     * general contract of the {@link Object#equals} and
     * {@link Object#hashCode} methods.
     *
     * <p>The returned map is typically case-sensitive on all platforms.
     *
     * <p>If a security manager exists, its
     * {@link SecurityManager#checkPermission checkPermission} method
     * is called with a
     * {@link RuntimePermission}{@code ("getenv.*")} permission.
     * This may result in a {@link SecurityException} being thrown.
     *
     * <p>When passing information to a Java subprocess,
     * <a href=System.html#EnvironmentVSSystemProperties>system properties</a>
     * are generally preferred over environment variables.
     *
     * @return this process builder's environment
     *
     * @throws SecurityException
     *         if a security manager exists and its
     *         {@link SecurityManager#checkPermission checkPermission}
     *         method doesn't allow access to the process environment
     *
     * @see    Runtime#exec(String[],String[],java.io.File)
     * @see    System#getenv()
     */
    Map<String,String> environment();


    /**
     * Starts a new process using the attributes of this process builder.
     *
     * <p>The new process will
     * invoke the command and arguments given by {@link #command()},
     * in a working directory as given by {@link #directory()},
     * with a process environment as given by {@link #environment()}.
     *
     * <p>This method checks that the command is a valid operating
     * system command.  Which commands are valid is system-dependent,
     * but at the very least the command must be a non-empty list of
     * non-null strings.
     *
     * <p>A minimal set of system dependent environment variables may
     * be required to start a process on some operating systems.
     * As a result, the subprocess may inherit additional environment variable
     * settings beyond those in the process builder's {@link #environment()}.
     *
     * <p>If there is a security manager, its
     * {@link SecurityManager#checkExec checkExec}
     * method is called with the first component of this object's
     * {@code command} array as its argument. This may result in
     * a {@link SecurityException} being thrown.
     *
     * <p>Starting an operating system process is highly system-dependent.
     * Among the many things that can go wrong are:
     * <ul>
     * <li>The operating system program file was not found.
     * <li>Access to the program file was denied.
     * <li>The working directory does not exist.
     * <li>Invalid character in command argument, such as NUL.
     * </ul>
     *
     * <p>In such cases an exception will be thrown.  The exact nature
     * of the exception is system-dependent, but it will always be a
     * subclass of {@link IOException}.
     *
     * <p>If the operating system does not support the creation of
     * processes, an {@link UnsupportedOperationException} will be thrown.
     *
     * <p>Subsequent modifications to this process builder will not
     * affect the returned {@link Process}.
     *
     * @return a new {@link Process} object for managing the subprocess
     *
     * @throws NullPointerException
     *         if an element of the command list is null
     *
     * @throws IndexOutOfBoundsException
     *         if the command is an empty list (has size {@code 0})
     *
     * @throws SecurityException
     *         if a security manager exists and
     *         <ul>
     *
     *         <li>its
     *         {@link SecurityManager#checkExec checkExec}
     *         method doesn't allow creation of the subprocess, or
     *
     *         <li>the standard input to the subprocess was
     *         redirected from a file and the security manager's
     *         {@link SecurityManager#checkRead(String) checkRead} method
     *         denies read access to the file, or
     *
     *         <li>the standard output or standard error of the
     *         subprocess was redirected to a file and the security manager's
     *         {@link SecurityManager#checkWrite(String) checkWrite} method
     *         denies write access to the file
     *
     *         </ul>
     *
     * @throws  UnsupportedOperationException
     *          If the operating system does not support the creation of processes.
     *
     * @throws IOException if an I/O error occurs
     *
     * @see Runtime#exec(String[], String[], java.io.File)
     */
    Process start(OptionsByType options) throws IOException;

    /**
     * Sets this process builder's {@code redirectErrorStream} property.
     *
     * <p>If this property is {@code true}, then any error output
     * generated by subprocesses subsequently started by this object's
     * start() method will be merged with the standard
     * output, so that both can be read using the
     * {@link Process#getInputStream()} method.  This makes it easier
     * to correlate error messages with the corresponding output.
     * The initial value is {@code false}.
     *
     * @param  redirectErrorStream the new property value
     * @return this process builder
     */
    LocalProcessBuilder redirectErrorStream(boolean redirectErrorStream);
}
