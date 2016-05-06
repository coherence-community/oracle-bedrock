/*
 * File: JProfiler.java
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

package com.oracle.bedrock.jprofiler;

import com.jprofiler.api.agent.Controller;
import com.jprofiler.api.agent.HeapDumpOptions;
import com.jprofiler.api.agent.ProbeObjectType;
import com.jprofiler.api.agent.ProbeRecordingOptions;
import com.jprofiler.api.agent.ProbeValueType;
import com.jprofiler.api.agent.TrackingOptions;
import com.oracle.bedrock.runtime.concurrent.RemoteCallable;
import com.oracle.bedrock.runtime.concurrent.callable.RemoteCallableStaticMethod;

import java.awt.*;
import java.io.File;

/**
 * A factory for {@link RemoteCallable} to execute JProfiler functions
 * in a JVM running with JProfiler enabled.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class JProfiler
{
    /**
     * The name of the JProfiler controller class.
     */
    private static final String CONTROLLER_CLASS = Controller.class.getCanonicalName();


    /**
     * Create a {@link JProfiler} that adds a bookmark at the current time.
     * The bookmark will be displayed in all JProfiler graphs with a time axis.
     * The description will be displayed in the tooltip for the bookmark.
     *
     * @param description  the name of the bookmark, may also be null
     *
     * @return a {@link JProfiler} that adds a bookmark at the current time
     */
    public static RemoteCallable<Void> addBookmark(String description)
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS, "addBookmark", description);
    }


    /**
     * Create a {@link JProfiler} that adds a bookmark at the current time.
     * The bookmark will be displayed in all JProfiler graphs with a time axis.
     * The description will be displayed in the tooltip for the bookmark.
     *
     * @param description  the name of the bookmark, may also be null
     * @param color        the color to be used for drawing the bookmark. If null the default
     *                     color will be used.
     * @param dashed       if the line for drawing the bookmark should be dashed or not
     *
     * @return a {@link JProfiler} that adds a bookmark at the current time
     */
    public static RemoteCallable<Void> addBookmark(String  description,
                                                   Color   color,
                                                   boolean dashed)
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS, "addBookmark", description, color, dashed);
    }


    /**
     * Create a {@link JProfiler} that enables all triggers.
     *
     * @return a {@link JProfiler} that enables all triggers
     */
    public static RemoteCallable<Void> enableTriggers()
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS, "enableTriggers", true);
    }


    /**
     * Create a {@link JProfiler} that disables all triggers.
     * The enabled/disabled state of the single triggers will not be lost, disabling
     * all triggers with this method overrides the enabled state of the single triggers
     *
     * @return a {@link JProfiler} that enables all triggers
     */
    public static RemoteCallable<Void> disableTriggers()
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS, "enableTriggers", false);
    }


    /**
     * Create a {@link JProfiler} that enables all triggers with a specified group ID.
     * The group ID can be entered in the "Group ID" step of the trigger configuration wizard
     * in the JProfiler GUI.
     *
     * @param groupId  the group ID
     *
     * @return a {@link JProfiler} that enables all triggers with a specified group ID
     */
    public static RemoteCallable<Void> enableTriggerGroup(String groupId) throws IllegalArgumentException
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS, "enableTriggerGroup", true, groupId);
    }


    /**
     * Create a {@link JProfiler} that disables all triggers with a specified group ID.
     * The group ID can be entered in the "Group ID" step of the trigger configuration wizard
     * in the JProfiler GUI.
     *
     * @param groupId  the group ID
     *
     * @return a {@link JProfiler} that disables all triggers with a specified group ID
     */
    public static RemoteCallable<Void> disableTriggerGroup(String groupId) throws IllegalArgumentException
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS, "enableTriggerGroup", false, groupId);
    }


    /**
     * Create a {@link JProfiler} that saves a snapshot of all profiling data to disk.
     * This is especially important for offline profiling. You should choose the standard extension .jps
     * for the file parameter, since JProfiler's GUI frontend filters the corresponding file choosers for
     * that extension. If you want to save several snapshots during one profiling run, please take care
     * to provide unique file parameters since snapshot files will be overwritten otherwise.
     * <p>
     * <strong>ATTENTION:</strong> Saving a snapshot takes a long time (on the order of seconds).
     * If you call this method to often, your application might become unusable or take an excessively
     * long time to finish, and your hard disk might run out of space.
     *
     * @param file  the file to which the snapshot should be saved
     *
     * @return a {@link JProfiler} that saves a snapshot of all profiling data to disk
     */
    public static RemoteCallable<Void> saveSnapshot(File file)
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS, "saveSnapshot", file);
    }


    /**
     * Create a {@link JProfiler} that saves a snapshot of all profiling data to disk when the
     * VM shuts down. This is especially important for offline profiling. You should choose the standard
     * extension .jps for the file parameter, since JProfiler's GUI frontend filters the corresponding
     * file choosers for that extension.
     * <p>
     * <strong>ATTENTION:</strong> Saving a snapshot can take quite some time (on the order of seconds).
     * When the VM is shut down during a user logout or a system shutdown, the OS may terminate the VM
     * before saving is completed.
     *
     * @param file  the file to which the snapshot should be saved.
     *
     * @return a {@link JProfiler} that saves a snapshot of all profiling data to disk when
     *         the VM shuts down
     */
    public static RemoteCallable<Void> saveSnapshotOnExit(File file)
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS, "saveSnapshotOnExit", file);
    }


    /**
     * Create a {@link JProfiler} that starts recording of memory allocations.
     * This method can be called repeatedly and alternately with {@link #stopAllocRecording()}.
     * With these methods you can restrict memory allocation profiling to certain regions of your code.
     * This is especially useful for profiling an application running within an application server.
     *
     * @param reset   if true, any previously recorded profiling data will be discarded. If false,
     *                allocations within all pairs of invocations of this method and
     *                {@link #stopAllocRecording()} will be recorded.
     *
     * @return a {@link JProfiler} that starts recording of memory allocations
     */
    public static RemoteCallable<Void> startAllocRecording(boolean reset)
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS, "startAllocRecording", reset);
    }


    /**
     * Create a {@link JProfiler} that starts recording of memory allocations.
     * This method can be called repeatedly and alternately with {@link #stopAllocRecording()}.
     * With these methods you can restrict memory allocation profiling to certain regions of your code.
     * This is especially useful for profiling an application running within an application server.
     *
     * @param reset    if true, any previously recorded profiling data will be discarded. If false,
     *                 allocations within all pairs of invocations of this method and
     *                 {@link #stopAllocRecording()} will be recorded.
     * @param options  the request tracking options
     *
     * @return a {@link JProfiler} that starts recording of memory allocations
     */
    public static RemoteCallable<Void> startAllocRecording(boolean         reset,
                                                           TrackingOptions options)
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS, "startAllocRecording", reset, options);
    }


    /**
     * Create a {@link JProfiler} that stops recording of memory allocations.
     * This method can be called after or {@link #startAllocRecording(boolean)}has been
     * called. However, you do not have to call it since memory profiling can run until the JVM exits.
     *
     * @return a {@link JProfiler} that stops recording of memory allocations
     */
    public static RemoteCallable<Void> stopAllocRecording()
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS, "stopAllocRecording");
    }


    /**
     * Create a {@link JProfiler} that starts the call tracer.
     * This method can be called repeatedly and alternately with {@link #stopCallTracer()}
     *
     * @param cap             the maximum number of events to be recorded. A good default is 100000
     * @param recordFiltered  if true, calls into filtered classes will be recorded, too
     * @param reset           if true previously recorded calls will be cleared
     *
     * @return a {@link JProfiler} that starts the call tracer
     */
    public static RemoteCallable<Void> startCallTracer(int     cap,
                                                       boolean recordFiltered,
                                                       boolean reset)
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS, "startCallTracer", cap, recordFiltered, reset);
    }


    /**
     * Create a {@link JProfiler} that stops the call tracer.
     * This method can be called after {@link #startCallTracer(int, boolean, boolean)} has been called.
     * However, you do not have to call it since the call tracer will stop automatically after the cap
     * has been reached. The data will be delivered to the frontend afterwards if used in online mode.
     *
     * @return a {@link JProfiler} that stops the call tracer
     */
    public static RemoteCallable<Void> stopCallTracer()
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS, "stopCallTracer");
    }


    /**
     * Create a {@link JProfiler} that starts recording CPU data.
     * This method can be called repeatedly and alternately with stopCPURecording().
     * With these methods you can restrict CPU profiling to certain regions of your code.
     *
     * @param reset if true, any previously accumulated CPU profiling data will be discarded. If false,
     *              CPU data will be accumulated across pairs of invocations of startCPURecording()
     *              and stopCPURecording()
     *
     * @return a {@link JProfiler} that starts recording CPU data
     */
    public static RemoteCallable<Void> startCPURecording(boolean reset)
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS, "startCPURecording", reset);
    }


    /**
     * Create a {@link JProfiler} that starts recording CPU data.
     * This method can be called repeatedly and alternately with stopCPURecording().
     * With these methods you can restrict CPU profiling to certain regions of your code.
     *
     * @param reset    if true, any previously accumulated CPU profiling data will be discarded. If false,
     *                 CPU data will be accumulated across pairs of invocations of startCPURecording()
     *                 and stopCPURecording()
     * @param options  the request tracking options
     *
     * @return a {@link JProfiler} that starts recording CPU data
     */
    public static RemoteCallable<Void> startCPURecording(boolean         reset,
                                                         TrackingOptions options)
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS, "startCPURecording", reset, options);
    }


    /**
     * Create a {@link JProfiler} that stops CPU recording.
     * This method can be called after startCPURecording() has been called.
     * However, you do not have to call it since CPU profiling can run until the JVM exits.
     *
     * @return a {@link JProfiler} that stops CPU recording
     */
    public static RemoteCallable<Void> stopCPURecording()
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS, "stopCPURecording");

    }


    /**
     * Create a {@link JProfiler} that starts method statistics recording.
     * This will reset previously recorded method statistics.
     * This method can be called repeatedly and alternately with {@link #stopMethodStatsRecording()}
     *
     * @return a {@link JProfiler} that starts method statistics recording
     */
    public static RemoteCallable<Void> startMethodStatsRecording()
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS, "startMethodStatsRecording");
    }


    /**
     * Create a {@link JProfiler} that stops method statistics recording.
     *
     * @return a {@link JProfiler} stops method statistics recording
     */
    public static RemoteCallable<Void> stopMethodStatsRecording()
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS, "stopMethodStatsRecording");
    }


    /**
     * Create a {@link JProfiler} that starts recording of monitor usage with default
     * thresholds of 100 microseconds for blocking events and 100 ms for waiting events.
     * This method can be called repeatedly and alternately with {@link #stopMonitorRecording()}.
     * Monitor profiling is switched off by default.
     *
     * @return a {@link JProfiler} that starts recording of monitor usage
     */
    public static RemoteCallable<Void> startMonitorRecording()
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS, "startMonitorRecording");
    }


    /**
     * Create a {@link JProfiler} that starts recording of monitor usage.
     * This method can be called repeatedly and alternately with {@link #stopMonitorRecording()}.
     * Monitor profiling is switched off by default.
     *
     * @param blockingThreshold  the recording threshold for blocking events in microseconds
     * @param waitingThreshold   the recording threshold for waiting events in microseconds
     *
     * @return a {@link JProfiler}
     */
    public static RemoteCallable<Void> startMonitorRecording(int blockingThreshold,
                                                             int waitingThreshold)
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS,
                                                "startMonitorRecording",
                                                blockingThreshold,
                                                waitingThreshold);
    }


    /**
     * Create a {@link JProfiler} that Stop recording of monitor usage.
     * This method can be called repeatedly and alternately with {@link #startMonitorRecording()}
     * or {@link #startMonitorRecording(int, int)}. However, you do not have to call it since monitor
     * profiling can run until the JVM exits.
     *
     * @return a {@link JProfiler}
     */
    public static RemoteCallable<Void> stopMonitorRecording()
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS, "stopMonitorRecording");
    }


    /**
     * Create a {@link JProfiler} that starts recording data for a
     * selected probe.
     *
     * @param probeName  the name of the probe. For built-in probes,
     *                   see the PROBE_NAME constants in the com.jprofiler.api.agent.Controller
     *                   class. For custom probes, this name is custom.n where n is the one-based
     *                   index of the custom probe
     * @param events     whether single events should be recorded for the "Events" view of the probe
     *
     * @return a {@link JProfiler} that starts recording data for a selected probe
     */
    public static RemoteCallable<Void> startProbeRecording(String  probeName,
                                                           boolean events)
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS, "startProbeRecording", probeName, events);
    }


    /**
     * Create a {@link JProfiler} that starts recording data for a
     * selected probe.
     *
     * @param probeName  the name of the probe. For built-in probes,
     *                   see the PROBE_NAME constants in the com.jprofiler.api.agent.Controller
     *                   class. For custom probes, this name is custom.n where n is the one-based
     *                   index of the custom probe
     * @param options    the request tracking options
     *
     * @return a {@link JProfiler} that starts recording data for a selected probe
     */
    public static RemoteCallable<Void> startProbeRecording(String                probeName,
                                                           ProbeRecordingOptions options)
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS, "startProbeRecording", probeName, options);
    }


    /**
     * Create a {@link JProfiler} that stops recording data for a selected probe.
     *
     * @param probeName  the name of the probe. For built-in probes, see the PROBE_NAME constants
     *                   in the com.jprofiler.api.agent.Controller class. For custom probes, this name
     *                   is custom.n where n is the one-based index of the custom probe
     *
     * @return a {@link JProfiler} stops recording data for a selected probe
     */
    public static RemoteCallable<Void> stopProbeRecording(String probeName)
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS, "stopProbeRecording", probeName);

    }


    /**
     * Create a {@link JProfiler} that starts tracking selected elements for a selected
     * probe. Probe recording must be switched on for the selected probe otherwise this method
     * does not have any effect.
     *
     * @param probeName     the name of the probe. For built-in probes, see the PROBE_NAME constants
     *                      in the com.jprofiler.api.agent.Controller class. For custom probes, this name
     *                      is custom.n where n is the one-based index of the custom probe
     * @param descriptions  the descriptions of the tracked elements. These are the strings that you see
     *                      in the probe view in the JProfiler GUI. The sum of the selected elements will
     *                      be tracked in a single graph. To get separate graphs, call this method multiple times.
     * @param objectType    the type of the elements to be tracked
     * @param valueType     the type of the values to be tracked
     *
     * @return a {@link JProfiler} that starts tracking selected elements for a selected probe
     */
    public static RemoteCallable<Void> startProbeTracking(String          probeName,
                                                          String[]        descriptions,
                                                          ProbeObjectType objectType,
                                                          ProbeValueType  valueType)
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS,
                                                "startProbeTracking",
                                                probeName,
                                                descriptions,
                                                objectType,
                                                valueType);
    }


    /**
     * Create a {@link JProfiler} that stops tracking selected elements for a selected probe.
     * This only has an effect if {@link #startProbeTracking(String, String[], ProbeObjectType, ProbeValueType)}
     * has been called before.
     *
     * @param probeName     the name of the probe. For built-in probes, see the PROBE_NAME constants
     *                      in the com.jprofiler.api.agent.Controller class. For custom probes, this name
     *                      is custom.n where n is the one-based index of the custom probe
     * @param descriptions  the descriptions of the tracked elements. These are the strings that you see
     *                      in the probe view in the JProfiler GUI. The sum of the selected elements will
     *                      be tracked in a single graph. To get separate graphs, call this method multiple times.
     * @param objectType    the type of the elements to be tracked
     * @param valueType     the type of the values to be tracked
     *
     * @return a {@link JProfiler} that stops tracking selected elements for a selected probe
     */
    public static RemoteCallable<Void> stopProbeTracking(String          probeName,
                                                         String[]        descriptions,
                                                         ProbeObjectType objectType,
                                                         ProbeValueType  valueType)
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS,
                                                "stopProbeTracking",
                                                probeName,
                                                descriptions,
                                                objectType,
                                                valueType);
    }


    /**
     * Create a {@link JProfiler} that starts recording of thread states and monitor usage.
     * This method can be called repeatedly and alternately with {@link #stopThreadProfiling()}.
     * For an offline session, thread profiling is switched on by default.
     *
     * @return a {@link JProfiler} that starts recording of thread states and monitor usage
     */
    public static RemoteCallable<Void> startThreadProfiling()
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS, "startThreadProfiling");
    }


    /**
     * Create a {@link JProfiler} that stops recording of thread states and monitor usage.
     * This method can be called repeatedly and alternately with {@link #startThreadProfiling()}.
     * However, you do not have to call it since thread profiling can run until the JVM exits.
     *
     * @return a {@link JProfiler} that stops recording of thread states and monitor usage
     */
    public static RemoteCallable<Void> stopThreadProfiling()
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS, "stopThreadProfiling");
    }


    /**
     * Create a {@link JProfiler} that starts recording of VM telemetry data.
     * This method can be called repeatedly and alternately with {@link #stopVMTelemetryRecording()}.
     * For an offline session, VM telemetry recording is switched on by default.
     *
     * @return a {@link JProfiler} that starts recording of VM telemetry data
     */
    public static RemoteCallable<Void> startVMTelemetryRecording()
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS, "startVMTelemetryRecording");
    }


    /**
     * Create a {@link JProfiler} that stops recording of VM telemetry data.
     * This method can be called repeatedly and alternately with {@link #startVMTelemetryRecording()}.
     * However, you do not have to call it since VM telemetry recording can run until the JVM exits.
     *
     * @return a {@link JProfiler} that stops recording of VM telemetry data
     */
    public static RemoteCallable<Void> stopVMTelemetryRecording()
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS, "stopVMTelemetryRecording");
    }


    /**
     * Create a {@link JProfiler} that triggers a heap dump.
     * If you want to analyze a heap dump with the heap walker in a snapshot saved with the
     * {@link #saveSnapshot(File)} method, you should call this method from your source code
     * at an appropriate time.
     *
     * @return a {@link JProfiler} that triggers a heap dump
     */
    public static RemoteCallable<Void> triggerHeapDump()
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS, "triggerHeapDump");
    }


    /**
     * Create a {@link JProfiler} that triggers a heap dump.
     * If you want to analyze a heap dump with the heap walker in a snapshot saved with the
     * {@link #saveSnapshot(File)} method, you should call this method from your source code
     * at an appropriate time.
     *
     * @param options  the options controlling the heap dump
     *
     * @return a {@link JProfiler} that triggers a heap dump
     */
    public static RemoteCallable<Void> triggerHeapDump(HeapDumpOptions options)
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS, "triggerHeapDump", options);
    }


    /**
     * Create a {@link JProfiler} that triggers a thread dump.
     *
     * @return a {@link JProfiler} that triggers a thread dump
     */
    public static RemoteCallable<Void> triggerThreadDump()
    {
        return new RemoteCallableStaticMethod<>(CONTROLLER_CLASS, "triggerThreadDump");
    }
}
