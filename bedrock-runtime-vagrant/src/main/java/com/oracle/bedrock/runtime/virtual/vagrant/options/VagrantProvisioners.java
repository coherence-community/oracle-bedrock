/*
 * File: VagrantProvisioners.java
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

package com.oracle.bedrock.runtime.virtual.vagrant.options;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;

/**
 * A {@link Collector} of {@link VagrantProvisioner}s.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class VagrantProvisioners implements Option.Collector<VagrantProvisioner, VagrantProvisioners>
{
    /**
     * The {@link VagrantProvisioner}s.
     */
    private ArrayList<VagrantProvisioner> provisioners;


    /**
     * Constructs an empty {@link VagrantProvisioners}.
     */
    public VagrantProvisioners()
    {
        this.provisioners = new ArrayList<>();
    }


    /**
     * Constructs a {@link VagrantProvisioners} based on another {@link VagrantProvisioners}.
     *
     * @param other  the other {@link VagrantProvisioners}
     */
    public VagrantProvisioners(VagrantProvisioners other)
    {
        this.provisioners = new ArrayList<>(other.provisioners);
    }


    /**
     * Write {@link VagrantProvisioner}s to a {@link PrintWriter}.
     *
     * @param writer   the {@link PrintWriter}
     * @param prefix   the prefix to write before each configuration
     * @param padding  the padding to write before each line
     */
    public void write(PrintWriter writer,
                      String      prefix,
                      String      padding)
    {
        provisioners.forEach(provisioner -> provisioner.write(writer, prefix, padding));

        writer.println();
    }


    /**
     * Constructs an empty {@link VagrantProvisioners}.
     *
     * @return an empty {@link VagrantProvisioners}
     */
    @OptionsByType.Default
    public static VagrantProvisioners none()
    {
        return new VagrantProvisioners();
    }


    @Override
    public VagrantProvisioners with(VagrantProvisioner provisioner)
    {
        VagrantProvisioners vagrantProvisioners = new VagrantProvisioners(this);

        vagrantProvisioners.provisioners.add(provisioner);

        return vagrantProvisioners;
    }


    @Override
    public VagrantProvisioners without(VagrantProvisioner provisioner)
    {
        VagrantProvisioners vagrantProvisioners = new VagrantProvisioners(this);

        vagrantProvisioners.provisioners.remove(provisioner);

        return vagrantProvisioners;
    }


    @Override
    public <O> Iterable<O> getInstancesOf(Class<O> requiredClass)
    {
        return provisioners.stream().filter(provisioner -> requiredClass.isInstance(provisioner))
        .map(provisioner -> (O) provisioner).collect(Collectors.toList());
    }


    @Override
    public Iterator<VagrantProvisioner> iterator()
    {
        return provisioners.iterator();
    }
}
