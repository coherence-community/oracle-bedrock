/*
 * File: CheckJDK.java
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

package com.oracle.bedrock.testsupport.junit;

import org.junit.Assume;

import static org.hamcrest.CoreMatchers.is;

/**
 * A utility to assert information about the test JDK.
 *
 * @author Jonathan Knight  2020.07.22
 */
public class CheckJDK
{
    public static final String JDK_VENDOR_ORACLE = "Oracle Corporation";

    public static void assumeOracleJDK()
    {
        String sVendor = System.getProperty("java.vm.vendor");
        Assume.assumeThat("Skipping test - JVM vendor is not Oracle", sVendor, is(JDK_VENDOR_ORACLE));
    }
}
