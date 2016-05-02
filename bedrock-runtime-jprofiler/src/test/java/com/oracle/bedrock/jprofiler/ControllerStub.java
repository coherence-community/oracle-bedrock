/*
 * File: ControllerStub.java
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

import com.jprofiler.api.agent.HeapDumpOptions;
import com.jprofiler.api.agent.ProbeObjectType;
import com.jprofiler.api.agent.ProbeRecordingOptions;
import com.jprofiler.api.agent.ProbeValueType;
import com.jprofiler.api.agent.TrackingOptions;
import org.mockito.Answers;

import java.awt.*;
import java.io.File;

import static org.mockito.Mockito.mock;

/**
 * A stub class to use in place of the JProfiler Controller class.
 *
 * @author Jonathan Knight
 */
public class ControllerStub
{
    public static final MethodStub methodStub = mock(MethodStub.class, Answers.RETURNS_SMART_NULLS.get());

    public static void startCPURecording(boolean reset)
    {
        methodStub.method("startCPURecording", reset);
    }

    public static void startCPURecording(boolean reset, TrackingOptions trackingOptions)
    {
        methodStub.method("startCPURecording", reset, trackingOptions);

    }

    public static void startProbeRecording(String probeName, boolean events)
    {
        methodStub.method("startProbeRecording", probeName, events);
    }

    public static void startProbeRecording(String probeName, ProbeRecordingOptions recordingOptions)
    {
        methodStub.method("startProbeRecording", probeName, recordingOptions);
    }

    public static void stopProbeRecording(String probeName)
    {
        methodStub.method("stopProbeRecording", probeName);

    }

    public static void startProbeTracking(String probeName, String[] descriptions, ProbeObjectType objectType, ProbeValueType valueType)
    {
        methodStub.method("startProbeTracking", probeName, descriptions, objectType, valueType);

    }

    public static void stopProbeTracking(String probeName, String[] description, ProbeObjectType objectType, ProbeValueType valueType)
    {
        methodStub.method("stopProbeTracking", probeName, description, objectType, valueType);

    }

    public static void stopCPURecording()
    {
        methodStub.method("stopCPURecording");

    }

    public static void startCallTracer(int cap, boolean recordFiltered, boolean reset)
    {
        methodStub.method("startCallTracer", cap, recordFiltered, reset);
    }

    public static void stopCallTracer()
    {
        methodStub.method("stopCallTracer");
    }

    public static void startMethodStatsRecording()
    {
        methodStub.method("startMethodStatsRecording");
    }

    public static void stopMethodStatsRecording()
    {
        methodStub.method("stopMethodStatsRecording");
    }

    public static void startMonitorRecording()
    {
        methodStub.method("startMonitorRecording");
    }

    public static void startMonitorRecording(int blockingThreshold, int waitingThreshold)
    {
        methodStub.method("startMonitorRecording", blockingThreshold, waitingThreshold);
    }

    public static void stopMonitorRecording()
    {
        methodStub.method("stopMonitorRecording");
    }

    public static void startAllocRecording(boolean reset)
    {
        methodStub.method("startAllocRecording", reset);
    }

    public static void startAllocRecording(boolean reset, TrackingOptions trackingOptions)
    {
        methodStub.method("startAllocRecording", reset, trackingOptions);
    }

    public static void stopAllocRecording()
    {
        methodStub.method("stopAllocRecording");
    }

    public static void addBookmark(String description)
    {
        methodStub.method("addBookmark", description);
    }

    public static void addBookmark(String description, Color color, boolean dashed)
    {
        methodStub.method("addBookmark", description, color, dashed);
    }

    public static void triggerThreadDump()
    {
        methodStub.method("triggerThreadDump");

    }

    public static void triggerHeapDump()
    {
        methodStub.method("triggerHeapDump");
    }

    public static void triggerHeapDump(HeapDumpOptions heapDumpOptions)
    {
        methodStub.method("triggerHeapDump", heapDumpOptions);
    }

    public static void saveSnapshot(File file)
    {
        methodStub.method("saveSnapshot", file);

    }

    public static void startThreadProfiling()
    {
        methodStub.method("startThreadProfiling");
    }

    public static void stopThreadProfiling()
    {
        methodStub.method("stopThreadProfiling");
    }

    public static void startVMTelemetryRecording()
    {
        methodStub.method("startVMTelemetryRecording");
    }

    public static void stopVMTelemetryRecording()
    {
        methodStub.method("stopVMTelemetryRecording");

    }

    public static void saveSnapshotOnExit(File file)
    {
        methodStub.method("saveSnapshotOnExit", file);
    }

    public static void enableTriggerGroup(boolean enabled, String groupId) throws IllegalArgumentException
    {
        methodStub.method("enableTriggerGroup", enabled, groupId);
    }

    public static void enableTriggers(boolean enabled)
    {
        methodStub.method("enableTriggers", enabled);
    }


    public static interface MethodStub
    {
        public void method(Object... args);
    }
}
