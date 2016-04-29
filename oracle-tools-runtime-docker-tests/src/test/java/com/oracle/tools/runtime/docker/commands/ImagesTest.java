/*
 * File: ImagesTest.java
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

package com.oracle.tools.runtime.docker.commands;

import com.oracle.tools.runtime.options.Argument;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link Images}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class ImagesTest extends AbstractCommandTest
{
    @Test
    public void shouldCreateListImagesCommand() throws Exception
    {
        Images       images = Images.list();
        List<String> args   = resolveArguments(images);

        assertThat(args, contains("images"));
    }


    @Test
    public void shouldCreateRepoImagesCommand() throws Exception
    {
        Images       images = Images.forRepo("foo:1.0");
        List<String> args   = resolveArguments(images);

        assertThat(args, contains("images", "foo:1.0"));
    }


    @Test
    public void shouldImmutablySetAll() throws Exception
    {
        Images       images1 = Images.list();
        List<String> before  = resolveArguments(images1);
        Images       images2 = images1.all();

        assertThat(images1, is (not(sameInstance(images2))));

        List<String> arguments1 = resolveArguments(images1);
        List<String> arguments2 = resolveArguments(images2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--all"));
    }


    @Test
    public void shouldImmutablySetDigests() throws Exception
    {
        Images       images1  = Images.list();
        List<String> before = resolveArguments(images1);
        Images       images2  = images1.digests();

        assertThat(images1, is (not(sameInstance(images2))));

        List<String> arguments1 = resolveArguments(images1);
        List<String> arguments2 = resolveArguments(images2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--digests"));
    }


    @Test
    public void shouldImmutablySetFilter() throws Exception
    {
        Images       images1  = Images.list();
        List<String> before = resolveArguments(images1);
        Images       images2  = images1.filter("a=b", "c=d");

        assertThat(images1, is (not(sameInstance(images2))));

        List<String> arguments1 = resolveArguments(images1);
        List<String> arguments2 = resolveArguments(images2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--filter=a=b", "--filter=c=d"));
    }


    @Test
    public void shouldImmutablySetFormat() throws Exception
    {
        Images       images1 = Images.list();
        List<String> before  = resolveArguments(images1);
        Images       images2 = images1.format("a", "b");

        assertThat(images1, is (not(sameInstance(images2))));

        List<String> arguments1 = resolveArguments(images1);
        List<String> arguments2 = resolveArguments(images2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--format=a", "--format=b"));
    }

    @Test
    public void shouldImmutablySetNoTruncate() throws Exception
    {
        Images       images1 = Images.list();
        List<String> before  = resolveArguments(images1);
        Images       images2 = images1.noTruncate();

        assertThat(images1, is (not(sameInstance(images2))));

        List<String> arguments1 = resolveArguments(images1);
        List<String> arguments2 = resolveArguments(images2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--no-trunc"));
    }


    @Test
    public void shouldImmutablySetCustomArguments() throws Exception
    {
        Images       images1 = Images.list();
        List<String> before  = resolveArguments(images1);
        Images       images2 = images1.withCommandArguments(Argument.of("--test1"), Argument.of("--test2"));

        assertThat(images1, is (not(sameInstance(images2))));

        List<String> arguments1 = resolveArguments(images1);
        List<String> arguments2 = resolveArguments(images2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--test1", "--test2"));
    }


    @Test
    public void shouldImmutablyRemoveArguments() throws Exception
    {
        Images       images1 = Images.list().withCommandArguments(Argument.of("--test1"), Argument.of("--test2"));
        List<String> before  = resolveArguments(images1);
        Images       images2 = images1.withoutCommandArguments(Argument.of("--test1"), Argument.of("--test2"));

        assertThat(images1, is (not(sameInstance(images2))));

        List<String> arguments1 = resolveArguments(images1);
        List<String> arguments2 = resolveArguments(images2);

        assertThat(arguments1, is(before));
        assertThat(arguments1.contains("--test1"), is(true));
        assertThat(arguments1.contains("--test2"), is(true));
        assertThat(arguments2.contains("--test1"), is(false));
        assertThat(arguments2.contains("--test2"), is(false));
    }
}
