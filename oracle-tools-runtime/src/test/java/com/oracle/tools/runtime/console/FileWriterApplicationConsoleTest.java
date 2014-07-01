/*
 * File: FileWriterApplicationConsoleTest.java
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

package com.oracle.tools.runtime.console;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Jonathan Knight
 */
public class FileWriterApplicationConsoleTest
{
    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void shouldNotBePlainModeByDefault() throws Exception
    {
        File                         file     = temporaryFolder.newFile();
        FileWriterApplicationConsole console1 = new FileWriterApplicationConsole(new FileWriter(file));
        FileWriterApplicationConsole console2 = new FileWriterApplicationConsole(file.getCanonicalPath());

        assertThat(console1.isPlainMode(), is(false));
        assertThat(console2.isPlainMode(), is(false));
    }

    @Test
    public void shouldNotBePlainMode() throws Exception
    {
        File                         file    = temporaryFolder.newFile();
        FileWriterApplicationConsole console = new FileWriterApplicationConsole(new FileWriter(file), false);

        assertThat(console.isPlainMode(), is(false));
    }

    @Test
    public void shouldBePlainMode() throws Exception
    {
        File                         file    = temporaryFolder.newFile();
        FileWriterApplicationConsole console = new FileWriterApplicationConsole(new FileWriter(file), true);

        assertThat(console.isPlainMode(), is(true));
    }
}
