/*
 * File: AbstractFeature.java
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

package com.oracle.bedrock.extensible;

/**
 *
 * An abstract implementation of a {@link Feature}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public abstract class AbstractFeature implements Feature
{
    /**
     * The {@link Extensible} to which this {@link Feature} has been added.
     * (<code>null</code> when not associated with an {@link Extensible})
     */
    private Extensible extensible;


    /**
     * Constructs an {@link AbstractFeature}.
     */
    public AbstractFeature()
    {
        this.extensible = null;
    }


    /**
     * Obtains the {@link Extensible} to which this {@link Feature} has been added.
     *
     * @return  the {@link Extensible}
     *
     * @throws IllegalStateException  if attempting to access the {@link Extensible} when
     *                                the {@link Feature} is not currently added to one
     */
    public Extensible getExtensible()
    {
        return extensible;
    }


    /**
     * Determines if a {@link Feature} as been added to an {@link Extensible}.
     *
     * @return  <code>true</code> if a {@link Feature} is associated with an {@link Extensible},
     *          <code>false</code> otherwise
     */
    public boolean isExtending()
    {
        return extensible != null;
    }


    @Override
    public void onAddingTo(Extensible extensible)
    {
        this.extensible = extensible;
    }


    @Override
    public void onRemovingFrom(Extensible extensible)
    {
        this.extensible = null;
    }
}
