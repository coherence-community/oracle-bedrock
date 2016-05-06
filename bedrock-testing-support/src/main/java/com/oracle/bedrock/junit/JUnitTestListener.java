/*
 * File: JUnitTestListener.java
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

package com.oracle.bedrock.junit;

import com.oracle.bedrock.runtime.concurrent.RemoteEvent;

import java.util.Properties;

/**
 * A listener that receives events during the progress of a JUnit test run.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public interface JUnitTestListener
{
    /**
     * Indicates the start of a JUnit application
     *
     * @param event the event
     */
    void junitStarted(Event event);


    /**
     * Indicates the end of a JUnit application
     *
     * @param event the event
     */
    void junitCompleted(Event event);


    /**
     * Indicates the start of a given test-set
     *
     * @param event the report entry describing the test suite
     */
    void testRunStarted(Event event);


    /**
     * Indicates end of a given test-set
     *
     * @param event the report entry describing the test suitet
     */
    void testRunFinished(Event event);


    /**
     * Indicates the start of a given test-set
     *
     * @param event the report entry describing the test suite
     */
    void testClassStarted(Event event);


    /**
     * Indicates end of a given test-set
     *
     * @param event the report entry describing the test suite
     */
    void testClassFinished(Event event);


    /**
     * Event fired when a test is about to start
     *
     * @param event The report entry to log for
     */
    void testStarted(Event event);


    /**
     * Event fired when a test ended successfully
     *
     * @param event The report entry to log for
     */
    void testSucceeded(Event event);


    /**
     * Event fired when a test assumption failure was encountered.
     * An assumption failure indicates that the test is not relevant
     *
     * @param event The report entry to log for
     */
    void testAssumptionFailure(Event event);


    /**
     * Event fired when a test ended with an error (non anticipated problem)
     *
     * @param event The report entry to log for
     */
    void testError(Event event);


    /**
     * Event fired when a test ended with a failure (anticipated problem)
     *
     * @param event The report entry to log for
     */
    void testFailed(Event event);


    /**
     * Event fired when a test is skipped
     *
     * @param event The report entry to log for
     */
    void testIgnored(Event event);


    class Event implements RemoteEvent
    {
        private final Type                type;
        private final String              className;
        private final String              name;
        private final StackTraceElement[] stackTrace;
        private final long                time;
        private final String              exception;
        private final String              message;
        private final Properties          properties;


        /**
         * Enum description
         */
        public enum Type
        {
            JUnitStarted,
            JUnitCompleted,
            testRunStarted,
            testRunFinished,
            testClassStarted,
            testClassFinished,
            testStarted,
            testSuccess,
            testIgnored,
            testAssumptionFailure,
            testFailure,
            testError,
        }


        private Event(Type                type,
                      String              name,
                      String              className,
                      long                time,
                      String              exception,
                      String              message,
                      StackTraceElement[] stackTrace,
                      Properties          properties)
        {
            this.type       = type;
            this.name       = name;
            this.className  = className;
            this.time       = time;
            this.exception  = exception;
            this.message    = message;
            this.stackTrace = stackTrace;
            this.properties = properties;
        }


        public Type getType()
        {
            return type;
        }


        public String getName()
        {
            return name;
        }


        public String getClassName()
        {
            return className;
        }


        public long getTime()
        {
            return time;
        }


        public String getException()
        {
            return exception;
        }


        public String getMessage()
        {
            return message;
        }


        public StackTraceElement[] getStackTrace()
        {
            return stackTrace;
        }


        public Properties getProperties()
        {
            return properties;
        }


        public boolean isSkipped()
        {
            return type == Type.testAssumptionFailure || type == Type.testIgnored;
        }


        public boolean isFailure()
        {
            return type == Type.testFailure;
        }


        public boolean isError()
        {
            return type == Type.testError;
        }


        @Override
        public String toString()
        {
            return "JUnitTestListener.Event(" + "type=" + type + ", name='" + name + '\'' + ", className='" + className
                   + '\'' + ", time=" + time + ", message='" + message + '\'' + ')';
        }


        public static Event junitStarted()
        {
            return new Event(Type.JUnitStarted, "Start", null, 0, null, null, null, null);
        }


        public static Event junitCompleted(long time)
        {
            return new Event(Type.JUnitCompleted, "End", null, time, null, null, null, null);
        }


        public static Event testRunStarted(String     name,
                                           Properties properties)
        {
            return new Event(Type.testRunStarted, name, null, 0, null, null, null, properties);
        }


        public static Event testRunFinsihed(String name,
                                            long   time)
        {
            return new Event(Type.testRunFinished, name, null, time, null, null, null, null);
        }


        public static Event testClassStarted(String className)
        {
            return new Event(Type.testClassStarted, null, className, 0, null, null, null, null);
        }


        public static Event testClassFinished(String className,
                                              long   time)
        {
            return new Event(Type.testClassFinished, null, className, time, null, null, null, null);
        }


        public static Event testStarted(String name,
                                        String className)
        {
            return new Event(Type.testStarted, name, className, 0, null, null, null, null);
        }


        public static Event testSucceded(String name,
                                         String className,
                                         long   time)
        {
            return new Event(Type.testSuccess, name, className, time, null, null, null, null);
        }


        public static Event ignored(String name,
                                    String className,
                                    String message)
        {
            return new Event(Type.testIgnored, name, className, 0, null, message, null, null);
        }


        public static Event failure(String              name,
                                    String              className,
                                    long                time,
                                    String              exception,
                                    String              message,
                                    StackTraceElement[] stackTrace)
        {
            return new Event(Type.testFailure, name, className, time, exception, message, stackTrace, null);
        }


        public static Event error(String              name,
                                  String              className,
                                  long                time,
                                  String              exception,
                                  String              message,
                                  StackTraceElement[] stackTrace)
        {
            return new Event(Type.testError, name, className, time, exception, message, stackTrace, null);
        }


        public static Event assumptionFailure(String              name,
                                              String              className,
                                              long                time,
                                              String              message,
                                              StackTraceElement[] stackTrace)
        {
            return new Event(Type.testAssumptionFailure, name, className, time, null, message, stackTrace, null);
        }
    }
}
