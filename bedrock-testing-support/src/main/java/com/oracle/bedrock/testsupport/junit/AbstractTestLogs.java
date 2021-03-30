package com.oracle.bedrock.testsupport.junit;

import com.oracle.bedrock.runtime.ApplicationConsole;
import com.oracle.bedrock.runtime.ApplicationConsoleBuilder;
import com.oracle.bedrock.runtime.console.EventsApplicationConsole;
import com.oracle.bedrock.runtime.console.FileWriterApplicationConsole;
import com.oracle.bedrock.testsupport.MavenProjectFileUtils;
import com.oracle.bedrock.util.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A baseclass that can be used as a JUnit rule or extension in a test class to easily create test
 * output folders and {@link ApplicationConsole}s.
 *
 * The {@link ApplicationConsoleBuilder} created also allows the capturing of events based on log line
 * output which can then be asserted in tests.
 *
 * @author jk  2021.03.29
 */
public abstract class AbstractTestLogs
        implements ApplicationConsoleBuilder
{
    public static final Logger LOGGER = Logger.getLogger(AbstractTestLogs.class.getName());

    /**
     * The current test class.
     */
    protected Class<?> testClass;

    /**
     * The current test method.
     */
    protected String methodName;

    /**
     * The root folder to put logs.
     */
    protected File outputDirectory;

    /**
     * Initialise this {@link AbstractTestLogs}.
     *
     * @param clsTest      the test class
     * @param sMethodName  the test method name
     */
    public void init(Class<?> clsTest, String sMethodName)
    {
        testClass = clsTest;
        methodName = sMethodName;
        outputDirectory = MavenProjectFileUtils.ensureTestOutputBaseFolder(testClass);

        outputDirectory.mkdirs();
    }

    /**
     * Obtain the current test class.
     *
     * @return the current test class.
     */
    public Class<?> getTestClass()
    {
        return testClass;
    }

    /**
     * Obtain the logs folder.
     *
     * @return the logs folder
     */
    public File getOutputFolder()
    {
        return outputDirectory;
    }

    /**
     * Obtain an {@link ConsoleBuilder} that will write the process output to a file.
     *
     * @return an {@link ConsoleBuilder} that will write the process output to a file
     */
    public ConsoleBuilder builder()
    {
        return new ConsoleBuilder(this);
    }

    @Override
    public ApplicationConsole build(String name)
    {
        File dir = new File(outputDirectory, testClass.getSimpleName());

        dir.mkdir();

        if (methodName != null && methodName.trim().length() > 0)
        {
            dir = new File(dir, methodName);

            dir.mkdir();
        }

        File logFile = new File(dir, name + ".log");
        int count = 1;
        while(logFile.exists())
        {
            logFile = new File(dir, name + " (" + count++ + ").log");
        }

        try {
            LOGGER.info("Logging output from '" + name + "' to " + logFile.getCanonicalPath());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to write log file location", e);
        }

        return new Console(logFile);
    }


    /**
     * An {@link ApplicationConsoleBuilder} that uses {@link AbstractTestLogs} to build a .{@link
     * FileWriterApplicationConsole}.
     */
    public static class ConsoleBuilder
            implements ApplicationConsoleBuilder
    {
        // ----- constructors ---------------------------------------------------

        public ConsoleBuilder(AbstractTestLogs testLogs)
        {
            m_testLogs = testLogs;
        }

        // ----- ConsoleBuilder methods -------------------------------------

        /**
         * Build an {@link ApplicationConsole}.
         *
         * @param name the name of the application
         * @return an {@link ApplicationConsole}
         */
        public ApplicationConsole build(String name)
        {
            Console console = (Console) m_testLogs.build(name);

            stdOutListeners.stream()
                    .filter(pair -> pair.getX() == null)
                    .map(Pair::getY)
                    .forEach(console::withStdOutListener);

            stdErrListeners.stream()
                    .filter(pair -> pair.getX() == null)
                    .map(Pair::getY)
                    .forEach(console::withStdErrListener);

            stdOutListeners.stream()
                    .filter(pair -> pair.getX() != null)
                    .forEach(pair -> console.withStdOutListener(pair.getX(), pair.getY()));

            stdErrListeners.stream()
                    .filter(pair -> pair.getX() != null)
                    .forEach(pair -> console.withStdErrListener(pair.getX(), pair.getY()));

            return console;
        }

        /**
         * Remove all of the output listeners.
         */
        public void clearListeners()
        {
            stdErrListeners.clear();
            stdOutListeners.clear();
        }

        /**
         * Add a listener to receive stdout console lines as events.
         *
         * @param listener the {@link EventsApplicationConsole.Listener}
         * @return this {@link ConsoleBuilder}
         */
        public ConsoleBuilder addStdOutListener(EventsApplicationConsole.Listener listener)
        {
            stdOutListeners.add(new Pair<>(null, listener));

            return this;
        }

        /**
         * Add a listener to receive stdout console lines as events that match the specified {@link Predicate}.
         *
         * @param predicate the {@link Predicate} to use to match console output lines
         * @param listener  the {@link EventsApplicationConsole.Listener}
         * @return this {@link ConsoleBuilder}
         */
        public ConsoleBuilder addStdOutListener(Predicate<String> predicate, EventsApplicationConsole.Listener listener)
        {
            stdOutListeners.add(new Pair<>(predicate, listener));

            return this;
        }

        /**
         * Add a listener to receive stderr console lines as events.
         *
         * @param listener the {@link EventsApplicationConsole.Listener}
         * @return this {@link ConsoleBuilder}
         */
        public ConsoleBuilder addStdErrListener(EventsApplicationConsole.Listener listener)
        {
            stdErrListeners.add(new Pair<>(null, listener));

            return this;
        }

        /**
         * Add a listener to receive stderr console lines as events that match the specified {@link Predicate}.
         *
         * @param predicate the {@link Predicate} to use to match console output lines
         * @param listener  the {@link EventsApplicationConsole.Listener}
         * @return this {@link ConsoleBuilder}
         */
        public ConsoleBuilder addStdErrListener(Predicate<String> predicate, EventsApplicationConsole.Listener listener)
        {
            stdErrListeners.add(new Pair<>(predicate, listener));

            return this;
        }

        // ----- data members -----------------------------------------------

        /**
         * The {@link AbstractTestLogs} to use to build {@link ApplicationConsole}s.
         */
        private AbstractTestLogs m_testLogs;

        /**
         * The listeners to receive lines written to stdout.
         */
        private List<Pair<Predicate<String>, EventsApplicationConsole.Listener>> stdOutListeners = new ArrayList<>();

        /**
         * The listeners to receive lines written to stderr.
         */
        private List<Pair<Predicate<String>, EventsApplicationConsole.Listener>> stdErrListeners = new ArrayList<>();
    }


    // ----- inner class: Console -------------------------------------------

    /**
     * A custom {@link ApplicationConsole}.
     */
    public static class Console
            extends EventsApplicationConsole
    {
        /**
         * The {@link File} to write log lines to.
         */
        private final File file;

        /**
         * The {@link FileWriter} to write log lines to.
         */
        private final FileWriter writer;

        /**
         * Create a {@link Console}.
         *
         * @param file  the {@link File} to write log lines to
         */
        private Console(File file)
        {
            try
            {
                this.file = file;
                this.writer = new FileWriter(file);
                withStdOutListener(this::write);
                withStdErrListener(this::write);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        /**
         * Write the log line.
         *
         * @param line  the line to write
         */
        private void write(String line)
        {
            try
            {
                writer.write(line);
                writer.write('\n');
                writer.flush();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public String toString() {
            return "Console(file:" + file.getName() + ")";
        }
    }
}
