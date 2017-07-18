/*
 * File: VersionTest.java
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

package com.oracle.bedrock.util;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.Is.is;

/**
 * Unit tests for {@link Version}.
 * <p>
 * Copyright (c) 2017. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class VersionTest
{
    /**
     * Ensure that we can parse numerous types of {@link Version} representations.
     */
    @Test
    public void shouldParseVersion()
    {
        assertThat(Version.of(""), is(Version.of("0")));

        assertThat(Version.of("1"), is(Version.of("1")));
        assertThat(Version.of("12"), is(Version.of("12")));
        assertThat(Version.of("1.2"), is(Version.of("1", "2")));
        assertThat(Version.of("12.34"), is(Version.of("12", "34")));

        assertThat(Version.of("A"), is(Version.of("a")));
        assertThat(Version.of("A"), is(Version.of("alpha")));
        assertThat(Version.of("AB"), is(Version.of("ab")));
        assertThat(Version.of("A.B"), is(Version.of("a", "b")));
        assertThat(Version.of("A.B"), is(Version.of("alpha", "beta")));
        assertThat(Version.of("AB.CD"), is(Version.of("ab", "cd")));

        assertThat(Version.of("1A"), is(Version.of("1", "a")));
        assertThat(Version.of("1A2B"), is(Version.of("1", "a", "2", "b")));

        assertThat(Version.of(".1"), is(Version.of("0", "1")));
        assertThat(Version.of(".A"), is(Version.of("0", "a")));
        assertThat(Version.of("-1"), is(Version.of("0", "1")));
        assertThat(Version.of("-A"), is(Version.of("0", "a")));

        assertThat(Version.of("1."), is(Version.of("1")));
        assertThat(Version.of("A."), is(Version.of("a")));
        assertThat(Version.of("1-"), is(Version.of("1")));
        assertThat(Version.of("A-"), is(Version.of("a")));

        assertThat(Version.of("1.A"), is(Version.of("1", "a")));
        assertThat(Version.of("1-A"), is(Version.of("1", "a")));

        assertThat(Version.of("1.2.3-A"), is(Version.of("1", "2", "3", "a")));
        assertThat(Version.of("1.2.3-A.B"), is(Version.of("1", "2", "3", "a", "b")));

        assertThat(Version.of("1.0.0-SNAPSHOT"), is(Version.of("1", "0", "0", "SNAPSHOT")));
    }


    /**
     * Ensure that we can compare numerous types of {@link Version}s.
     */
    @Test
    public void shouldCompareVersions()
    {
        assertThat(Version.of(""), is(lessThan(Version.of("1"))));

        assertThat(Version.of("1"), is(lessThan(Version.of("2"))));
        assertThat(Version.of("12"), is(lessThan(Version.of("12.1"))));
        assertThat(Version.of("12.1"), is(lessThan(Version.of("12.2"))));
        assertThat(Version.of("12-A"), is(lessThan(Version.of("12"))));
        assertThat(Version.of("12-A"), is(lessThan(Version.of("12.1"))));

        assertThat(Version.of("1.2"), is(lessThan(Version.of("1.2.1"))));
        assertThat(Version.of("1.2.1"), is(lessThan(Version.of("1.2.2"))));
        assertThat(Version.of("1.2.1"), is(lessThan(Version.of("1.2.2-sp2"))));
        assertThat(Version.of("1.2-sp2"), is(lessThan(Version.of("1.2.1"))));
        assertThat(Version.of("1.2-SNAPSHOT"), is(lessThan(Version.of("1.2"))));
        assertThat(Version.of("1.2-SNAPSHOT"), is(lessThan(Version.of("1.2.1-SNAPSHOT"))));
    }


    /**
     * Ensure that we output {@link Version}s in a standardized format.
     */
    @Test
    public void shouldOptionVersions()
    {
        assertThat(Version.of("").toString(), is("0"));
        assertThat(Version.of("1").toString(), is("1"));
        assertThat(Version.of("12").toString(), is("12"));
        assertThat(Version.of("12.1").toString(), is("12.1"));

        assertThat(Version.of("12-A").toString(), is("12.alpha"));
        assertThat(Version.of("12-A.3").toString(), is("12.alpha.3"));
        assertThat(Version.of("12-B3").toString(), is("12.beta.3"));

        assertThat(Version.of("1.2.1").toString(), is("1.2.1"));
        assertThat(Version.of("1.0rc3").toString(), is("1.0.release candidate.3"));
        assertThat(Version.of("1.2-sp2").toString(), is("1.2.service pack.2"));
    }
}
