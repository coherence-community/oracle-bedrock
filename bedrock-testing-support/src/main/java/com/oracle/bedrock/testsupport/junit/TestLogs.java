package com.oracle.bedrock.testsupport.junit;

import com.oracle.bedrock.runtime.ApplicationConsole;
import com.oracle.bedrock.runtime.ApplicationConsoleBuilder;
import com.oracle.bedrock.runtime.console.EventsApplicationConsole;
import com.oracle.bedrock.runtime.console.FileWriterApplicationConsole;
import com.oracle.bedrock.testsupport.MavenProjectFileUtils;
import com.oracle.bedrock.util.Pair;
import org.junit.rules.TestName;
import org.junit.runner.Description;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * An extension of the JUnit {@link TestName} class that can be used as
 * a JUnit {@link org.junit.Rule} in a test class to easily create test
 * output folders and {@link ApplicationConsole}s.
 *
 * The {@link ApplicationConsoleBuilder} created also allows the capturing
 * of events based on log line output which can then be asserted in tests.
 *
 * @author jk  2018.10.17
 */
public class TestLogs
        extends TestName
{
    /**
     * The current test class.
     */
    private Class<?> m_clsTest;

    /**
     * The current test method.
     */
    private String m_sMethodName;

    /**
     * The root folder to put logs.
     */
    private File m_fileOutDir;


    /**
     * Create a {@link TestLogs}.
     */
    public TestLogs()
    {
        this(null);
    }

    /**
     * Create a {@link TestLogs}.
     *
     * @param clsTest the test class
     */
    public TestLogs(Class<?> clsTest)
    {
        m_clsTest = clsTest;
    }

    // ----- TestWatcher methods --------------------------------------------

    @Override
    public void starting(Description description)
    {
        super.starting(description);

        Class<?> cls = description.getTestClass();

        if (cls == null)
        {
            try
            {
                String sName = description.getClassName();
                cls = Class.forName(sName);
            }
            catch (ClassNotFoundException e)
            {
                // ignored
            }
        }

        init(cls, description.getMethodName());
    }

    // Overridden to make public
    @Override
    public void finished(Description description)
    {
        super.finished(description);
    }

    // ----- helper methods -------------------------------------------------

    /**
     * Initialise this {@link TestLogs}.
     *
     * @param clsTest      the test class
     * @param sMethodName  the test method name
     */
    public void init(Class<?> clsTest, String sMethodName)
    {
        m_clsTest     = clsTest;
        m_sMethodName = sMethodName;
        m_fileOutDir  = MavenProjectFileUtils.ensureTestOutputBaseFolder(m_clsTest);

        m_fileOutDir.mkdirs();
    }

    /**
     * Obtain the current test class.
     *
     * @return the current test class.
     */
    public Class<?> getTestClass()
    {
        return m_clsTest;
    }

    /**
     * Obtain the logs folder.
     *
     * @return the logs folder
     */
    public File getOutputFolder()
    {
        return m_fileOutDir;
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

    private Console buildConsole(String name) throws IOException
    {
        File dir = new File(m_fileOutDir, m_clsTest.getSimpleName());

        dir.mkdir();

        if (m_sMethodName != null && m_sMethodName.trim().length() > 0)
        {
            dir = new File(dir, m_sMethodName);

            dir.mkdir();
        }

        System.err.println("Logging output from application '" + name + "' to " + dir + File.separator + name + ".log");

        return new Console(new FileWriter(new File(dir, name + ".log")));
    }


    /**
     * An {@link ApplicationConsoleBuilder} that uses {@link TestLogs} to build a .{@link
     * FileWriterApplicationConsole}.
     */
    public static class ConsoleBuilder
            implements ApplicationConsoleBuilder
    {
        // ----- constructors ---------------------------------------------------

        public ConsoleBuilder(TestLogs testLogs)
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
            try
            {
                Console console = m_testLogs.buildConsole(name);

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
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
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
         * The {@link TestLogs} to use to build {@link ApplicationConsole}s.
         */
        private TestLogs m_testLogs;

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
         * The {@link FileWriter} to write log lines to.
         */
        private final FileWriter writer;

        /**
         * Create a {@link Console}.
         *
         * @param writer  the {@link FileWriter} to write log lines to
         */
        private Console(FileWriter writer)
        {
            this.writer = writer;
            withStdOutListener(this::write);
            withStdErrListener(this::write);
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
    }
}
