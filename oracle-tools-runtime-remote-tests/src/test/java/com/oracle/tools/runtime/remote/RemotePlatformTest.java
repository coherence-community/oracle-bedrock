/*
 * File: RemotePlatformTest.java
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

package com.oracle.tools.runtime.remote;

import com.oracle.tools.runtime.ApplicationBuilder;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.SimpleApplication;
import com.oracle.tools.runtime.java.JavaApplication;
import com.oracle.tools.runtime.remote.java.RemoteJavaApplicationBuilder;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

/**
 * Unit tests for {@link RemotePlatform}s.
 *
 * @author Jonathan Knight
 */
public class RemotePlatformTest
{
    @Test
    public void shouldReturnHostName() throws Exception
    {
        Platform platform = new RemotePlatform("test-host", 1234, "jk", new Password("foo"));

        assertThat(platform.getHostname(), is("test-host"));
    }

    @Test
    public void shouldReturnJavaApplicationBuilder()
    {
        Authentication     auth     = new Password("foo");
        Platform           platform = new RemotePlatform("test-host", 1234, "jk", auth);

        ApplicationBuilder builder  = platform.getApplicationBuilder(JavaApplication.class);

        assertThat(builder, instanceOf(RemoteJavaApplicationBuilder.class));
        assertThat(((RemoteJavaApplicationBuilder) builder).hostName, is("test-host"));
        assertThat(((RemoteJavaApplicationBuilder) builder).port, is(1234));
        assertThat(((RemoteJavaApplicationBuilder) builder).userName, is("jk"));
        assertThat(((RemoteJavaApplicationBuilder) builder).authentication, is(sameInstance(auth)));
    }


    @Test
    public void shouldReturnLocalApplicationBuilder()
    {
        Authentication     auth     = new Password("foo");
        Platform           platform = new RemotePlatform("test-host", 1234, "jk", auth);

        ApplicationBuilder builder  = platform.getApplicationBuilder(SimpleApplication.class);

        assertThat(builder, instanceOf(SimpleRemoteApplicationBuilder.class));
        assertThat(((SimpleRemoteApplicationBuilder) builder).hostName, is("test-host"));
        assertThat(((SimpleRemoteApplicationBuilder) builder).port, is(1234));
        assertThat(((SimpleRemoteApplicationBuilder) builder).userName, is("jk"));
        assertThat(((SimpleRemoteApplicationBuilder) builder).authentication, is(sameInstance(auth)));
    }
}
