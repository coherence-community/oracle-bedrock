package com.oracle.bedrock.runtime.java;

import com.oracle.bedrock.OptionsByType;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple {@link LocalProcessBuilder} implementation that
 * wraps a Java {@link ProcessBuilder}.
 */
public class SimpleLocalProcessBuilder
        implements LocalProcessBuilder
{
    /**
     * The {@link java.util.logging.Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(SimpleLocalProcessBuilder.class.getName());

    private final ProcessBuilder processBuilder;

    public SimpleLocalProcessBuilder(String executable) {
        processBuilder = new ProcessBuilder(executable);
    }

    @Override
    public LocalProcessBuilder command(String... command) {
        processBuilder.command(command);
        return this;
    }

    @Override
    public List<String> command() {
        return processBuilder.command();
    }

    @Override
    public LocalProcessBuilder directory(File directory) {
        processBuilder.directory(directory);
        return this;
    }

    @Override
    public File directory() {
        return processBuilder.directory();
    }

    @Override
    public Map<String, String> environment() {
        return processBuilder.environment();
    }

    @Override
    public Process start(OptionsByType options) throws IOException {
        String cmd = String.join(" ", processBuilder.command());
        LOGGER.log(Level.INFO, "ProcessBuilder: command line: " + cmd);

        return processBuilder.start();
    }

    @Override
    public LocalProcessBuilder redirectErrorStream(boolean redirectErrorStream) {
        processBuilder.redirectErrorStream(redirectErrorStream);
        return this;
    }
}
