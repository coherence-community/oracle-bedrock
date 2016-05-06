/*
 * File: JProfilerTest.java
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

import com.jprofiler.api.agent.ProbeObjectType;
import com.jprofiler.api.agent.ProbeValueType;
import com.oracle.bedrock.runtime.concurrent.RemoteCallable;
import com.oracle.bedrock.runtime.concurrent.callable.RemoteCallableStaticMethod;
import org.junit.Test;

import java.awt.*;
import java.io.File;
import java.lang.reflect.Field;

import static org.mockito.Mockito.verify;

/**
 * @author Jonathan Knight
 */
public class JProfilerTest
{
    @Test
    public void shouldAddBookmark() throws Exception
    {
        RemoteCallable<Void> callable = JProfiler.addBookmark("foo");

        changeClass(callable);

        callable.call();

        verify(ControllerStub.methodStub).method("addBookmark", "foo");
    }


    @Test
    public void shouldAddBookmarkWithColor() throws Exception
    {
        RemoteCallable<Void> callable = JProfiler.addBookmark("foo", Color.RED, true);

        changeClass(callable);

        callable.call();

        verify(ControllerStub.methodStub).method("addBookmark", "foo", Color.RED, true);
    }


    @Test
    public void shouldEnableTriggerGroup() throws Exception
    {
        RemoteCallable<Void> callable = JProfiler.enableTriggerGroup("foo");

        changeClass(callable);

        callable.call();

        verify(ControllerStub.methodStub).method("enableTriggerGroup", true, "foo");
    }


    @Test
    public void shouldDisableTriggerGroup() throws Exception
    {
        RemoteCallable<Void> callable = JProfiler.disableTriggerGroup("foo");

        changeClass(callable);

        callable.call();

        verify(ControllerStub.methodStub).method("enableTriggerGroup", false, "foo");
    }


    @Test
    public void shouldEnableTriggers() throws Exception
    {
        RemoteCallable<Void> callable = JProfiler.enableTriggers();

        changeClass(callable);

        callable.call();

        verify(ControllerStub.methodStub).method("enableTriggers", true);
    }


    @Test
    public void shouldDisableTriggers() throws Exception
    {
        RemoteCallable<Void> callable = JProfiler.disableTriggers();

        changeClass(callable);

        callable.call();

        verify(ControllerStub.methodStub).method("enableTriggers", false);
    }


    @Test
    public void shouldSaveSnapshot() throws Exception
    {
        File                 file     = new File("/foo");
        RemoteCallable<Void> callable = JProfiler.saveSnapshot(file);

        changeClass(callable);

        callable.call();

        verify(ControllerStub.methodStub).method("saveSnapshot", file);
    }


    @Test
    public void shouldSaveSnapshotOnExit() throws Exception
    {
        File                 file     = new File("/foo");
        RemoteCallable<Void> callable = JProfiler.saveSnapshotOnExit(file);

        changeClass(callable);

        callable.call();

        verify(ControllerStub.methodStub).method("saveSnapshotOnExit", file);
    }


    @Test
    public void shouldStartAllocRecordingWithReset() throws Exception
    {
        RemoteCallable<Void> callable = JProfiler.startAllocRecording(true);

        changeClass(callable);

        callable.call();

        verify(ControllerStub.methodStub).method("startAllocRecording", true);
    }


    @Test
    public void shouldStartAllocRecordingWithoutReset() throws Exception
    {
        RemoteCallable<Void> callable = JProfiler.startAllocRecording(false);

        changeClass(callable);

        callable.call();

        verify(ControllerStub.methodStub).method("startAllocRecording", false);
    }


    @Test
    public void shouldStopAllocRecording() throws Exception
    {
        RemoteCallable<Void> callable = JProfiler.stopAllocRecording();

        changeClass(callable);

        callable.call();

        verify(ControllerStub.methodStub).method("stopAllocRecording");
    }


    @Test
    public void shouldStartCallTracer() throws Exception
    {
        RemoteCallable<Void> callable = JProfiler.startCallTracer(19, true, false);

        changeClass(callable);

        callable.call();

        verify(ControllerStub.methodStub).method("startCallTracer", 19, true, false);
    }


    @Test
    public void shouldStopCallTracer() throws Exception
    {
        RemoteCallable<Void> callable = JProfiler.stopCallTracer();

        changeClass(callable);

        callable.call();

        verify(ControllerStub.methodStub).method("stopCallTracer");
    }


    @Test
    public void shouldStartCPURecordingWithReset() throws Exception
    {
        RemoteCallable<Void> callable = JProfiler.startCPURecording(true);

        changeClass(callable);

        callable.call();

        verify(ControllerStub.methodStub).method("startCPURecording", true);
    }


    @Test
    public void shouldStartCPURecordingWithoutReset() throws Exception
    {
        RemoteCallable<Void> callable = JProfiler.startCPURecording(false);

        changeClass(callable);

        callable.call();

        verify(ControllerStub.methodStub).method("startCPURecording", false);
    }


    @Test
    public void shouldStopCPURecording() throws Exception
    {
        RemoteCallable<Void> callable = JProfiler.stopCPURecording();

        changeClass(callable);

        callable.call();

        verify(ControllerStub.methodStub).method("stopCPURecording");
    }


    @Test
    public void shouldStartMethodStatsRecording() throws Exception
    {
        RemoteCallable<Void> callable = JProfiler.startMethodStatsRecording();

        changeClass(callable);

        callable.call();

        verify(ControllerStub.methodStub).method("startMethodStatsRecording");
    }


    @Test
    public void shouldStopMethodStatsRecording() throws Exception
    {
        RemoteCallable<Void> callable = JProfiler.stopMethodStatsRecording();

        changeClass(callable);

        callable.call();

        verify(ControllerStub.methodStub).method("stopMethodStatsRecording");
    }


    @Test
    public void shouldStartMonitorRecording() throws Exception
    {
        RemoteCallable<Void> callable = JProfiler.startMonitorRecording();

        changeClass(callable);

        callable.call();

        verify(ControllerStub.methodStub).method("startMonitorRecording");
    }


    @Test
    public void shouldStartMonitorRecordingWithThresholds() throws Exception
    {
        RemoteCallable<Void> callable = JProfiler.startMonitorRecording(123, 987);

        changeClass(callable);

        callable.call();

        verify(ControllerStub.methodStub).method("startMonitorRecording", 123, 987);
    }


    @Test
    public void shouldStopMonitorRecording() throws Exception
    {
        RemoteCallable<Void> callable = JProfiler.stopMonitorRecording();

        changeClass(callable);

        callable.call();

        verify(ControllerStub.methodStub).method("stopMonitorRecording");
    }


    @Test
    public void shouldStartProbeRecordingWithEvents() throws Exception
    {
        RemoteCallable<Void> callable = JProfiler.startProbeRecording("foo", true);

        changeClass(callable);

        callable.call();

        verify(ControllerStub.methodStub).method("startProbeRecording", "foo", true);
    }


    @Test
    public void shouldStopProbeRecording() throws Exception
    {
        RemoteCallable<Void> callable = JProfiler.stopProbeRecording("foo");

        changeClass(callable);

        callable.call();

        verify(ControllerStub.methodStub).method("stopProbeRecording", "foo");
    }


    @Test
    public void shouldStartProbeTracking() throws Exception
    {
        String[]             desctiptions = {"foo", "bar"};
        ProbeObjectType      type         = ProbeObjectType.HOTSPOT;
        ProbeValueType       values       = ProbeValueType.TIMES;
        RemoteCallable<Void> callable     = JProfiler.startProbeTracking("foo", desctiptions, type, values);

        changeClass(callable);

        callable.call();

        verify(ControllerStub.methodStub).method("startProbeTracking",
                                                 "foo",
                                                 desctiptions,
                                                 ProbeObjectType.HOTSPOT,
                                                 ProbeValueType.TIMES);
    }


    @Test
    public void shouldStopProbeTracking() throws Exception
    {
        String[]             desctiptions = {"foo", "bar"};
        ProbeObjectType      type         = ProbeObjectType.CONTROL_OBJECT_FROM_ID;
        ProbeValueType       values       = ProbeValueType.COUNT;
        RemoteCallable<Void> callable     = JProfiler.stopProbeTracking("foo", desctiptions, type, values);

        changeClass(callable);

        callable.call();

        verify(ControllerStub.methodStub).method("stopProbeTracking",
                                                 "foo",
                                                 desctiptions,
                                                 ProbeObjectType.CONTROL_OBJECT_FROM_ID,
                                                 ProbeValueType.COUNT);
    }


    @Test
    public void shouldStartThreadProfiling() throws Exception
    {
        RemoteCallable<Void> callable = JProfiler.startThreadProfiling();

        changeClass(callable);

        callable.call();

        verify(ControllerStub.methodStub).method("startThreadProfiling");
    }


    @Test
    public void shouldStopThreadProfiling() throws Exception
    {
        RemoteCallable<Void> callable = JProfiler.stopThreadProfiling();

        changeClass(callable);

        callable.call();

        verify(ControllerStub.methodStub).method("stopThreadProfiling");
    }


    @Test
    public void shouldStartVMTelemetryRecording() throws Exception
    {
        RemoteCallable<Void> callable = JProfiler.startVMTelemetryRecording();

        changeClass(callable);

        callable.call();

        verify(ControllerStub.methodStub).method("startVMTelemetryRecording");
    }


    @Test
    public void shouldStopVMTelemetryRecording() throws Exception
    {
        RemoteCallable<Void> callable = JProfiler.stopVMTelemetryRecording();

        changeClass(callable);

        callable.call();

        verify(ControllerStub.methodStub).method("stopVMTelemetryRecording");
    }


    @Test
    public void shouldTriggerHeapDump() throws Exception
    {
        RemoteCallable<Void> callable = JProfiler.triggerHeapDump();

        changeClass(callable);

        callable.call();

        verify(ControllerStub.methodStub).method("triggerHeapDump");
    }


    @Test
    public void shouldTriggerThreadDump() throws Exception
    {
        RemoteCallable<Void> callable = JProfiler.triggerThreadDump();

        changeClass(callable);

        callable.call();

        verify(ControllerStub.methodStub).method("triggerThreadDump");
    }


    private void changeClass(RemoteCallable<Void> callable) throws Exception
    {
        Field field = RemoteCallableStaticMethod.class.getDeclaredField("className");

        field.setAccessible(true);
        field.set(callable, ControllerStub.class.getCanonicalName());
    }
}
