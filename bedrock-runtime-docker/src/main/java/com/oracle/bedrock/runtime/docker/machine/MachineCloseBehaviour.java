/*
 * File: MachineCloseBehaviour.java
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

package com.oracle.bedrock.runtime.docker.machine;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.docker.DockerContainer;
import com.oracle.bedrock.runtime.docker.options.ImageCloseBehaviour;

import java.util.function.Consumer;

/**
 * An {@link Enum} and {@link Option} representing the types of behaviour
 * to occur when closing a {@link DockerContainer}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class MachineCloseBehaviour implements Option, Consumer<DockerMachinePlatform>
{
    /**
     * The {link {@link Consumer} to call when this {@link MachineCloseBehaviour}
     * is applied to a {@link DockerContainer}
     */
    private final Consumer<DockerMachinePlatform> action;


    /**
     * Create an {@link MachineCloseBehaviour} that will perform
     * the specified action on a {@link DockerContainer}.
     *
     * @param action  the action to perform
     */
    private MachineCloseBehaviour(Consumer<DockerMachinePlatform> action)
    {
        this.action = action;
    }


    /**
     * Perform this {@link ImageCloseBehaviour}'s action on the
     * specified {@link DockerMachinePlatform}.
     *
     * @param platform  the {@link DockerMachinePlatform} to have the
     *                  action performed on
     */
    public void accept(DockerMachinePlatform platform)
    {
        action.accept(platform);
    }


    private static void stopOrKill(DockerMachinePlatform platform)
    {
        DockerMachine machine = platform.getDockerMachine();
        String        name    = platform.getName();
        int           exit;

        try
        {
            exit = machine.stop(name);
        }
        catch (Throwable _ignored)
        {
            exit = -1;
        }

        if (exit != 0)
        {
            machine.kill(name);
        }
    }


    private static void remove(DockerMachinePlatform platform)
    {
        DockerMachine machine = platform.getDockerMachine();
        String        name    = platform.getName();

        stopOrKill(platform);
        machine.remove(true, name);
    }


    /**
     * Do nothing when a {@link DockerMachinePlatform} is closed.
     *
     * @return a {@link MachineCloseBehaviour}
     */
    public static MachineCloseBehaviour none()
    {
        return new MachineCloseBehaviour((platform) -> {}
        );
    }


    /**
     * Stop the {@link DockerMachinePlatform} when a container application is closed.
     *
     * @return a {@link MachineCloseBehaviour}
     */
    public static MachineCloseBehaviour stop()
    {
        return new MachineCloseBehaviour(MachineCloseBehaviour::stopOrKill);
    }


    /**
     * Stop and remove the {@link DockerMachinePlatform} when closed.
     *
     * @return a {@link MachineCloseBehaviour}
     */
    @OptionsByType.Default
    public static MachineCloseBehaviour remove()
    {
        return new MachineCloseBehaviour(MachineCloseBehaviour::remove);
    }
}
