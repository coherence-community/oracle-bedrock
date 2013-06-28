/*
 * File: Deferred.java
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

package com.oracle.tools.deferred;

import java.util.concurrent.Future;

/**
 * A {@link Deferred} object represents a reference some well-known object, that
 * of which may yet not be available.
 * <p>
 * A program may use a {@link Deferred} to represent an object that is not
 * available/required immediately but instead available/required at some point
 * in the future.  Alternatively a {@link Deferred} may be used to represent an
 * object that is not available at the time of declaration.
 * <p>
 * {@link Deferred}s, as they are more commonly known, are neither builders or
 * factories.  Rather each {@link Deferred} is designed to encapsulate the
 * implementation to acquire a single well-known object, where as builders and
 * factories are typically used to construct, realize, build or allocate any
 * number of objects. {@link Deferred}s may use builders and factories internally
 * as part of their implementation, but they themselves are not considered to be
 * builders or factories.
 * <p>
 * For example, a {@link Deferred} may be used to represent a connection to a
 * specific server, that of which has yet to start, where as a Connection
 * builder/factory would instead be used to create connections to any number of
 * servers.
 * <p>
 * Once resolved, the referenced value of a {@link Deferred} will not change.
 * <p>
 * While similar to Java {@link Future}s, {@link Deferred}s are not Java
 * {@link Future}s.  In most circumstances, {@link Future}s are used to represent
 * the result of a computation that is occurring asynchronously and thus will be
 * produced in the future.  Calling either the {@link Future#get()}
 * or {@link Future#get(long, java.util.concurrent.TimeUnit)} blocks the calling
 * thread, at least for some period of time, to wait for a result.  More
 * specifically, how a thread waits for a result {@link Future} is in fact
 * encapsulated by the {@link Future} itself, with little option for a
 * developer to control the semantics.  However calling {@link Deferred#get()}
 * never blocks (with exception to attempt to acquire the object), unless of
 * course the {@link Deferred} implementation provides this facility
 * (see {@link Ensured} as an example).  ie: {@link Future}s essentially force
 * calling {@link Thread}s to block, {@link Deferred}s don't.  This difference
 * is significant as it allows {@link Deferred}s to present numerous types of
 * lazily evaluation (eg: deferred method invocation on deferred objects), that
 * of which is not easily possible with {@link Future}s.  Lastly {@link Future}
 * provides mechanisms to both {@link Future#cancel(boolean)} and determine
 * cancellation state.  This is because a {@link Future} represents some
 * background operation, where as a {@link Deferred} is simply a reference to
 * an object that may be available at a later point in time.
 * <p>
 * As there are many types of {@link Deferred}s, each with their own strategies
 * for dealing with object acquisition, recovering from acquisition failure and
 * handling certainly types of objects, careful consideration should be made as
 * to the choice of {@link Deferred} types to ensure that correct semantics are
 * achieved.
 * <p>
 * Copyright (c) 2012. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public interface Deferred<T>
{
    /**
     * Attempts to obtain the underlying object.
     *
     * @return the object of type T or <code>null</code> if it's not ready to
     *         be acquired (but may become available at some time in the future)
     *
     * @throws ObjectNotAvailableException  when the {@link Deferred}
     *         implementation can't produce the requested resource.  When this
     *         exception is thrown a program may safely assume that the
     *         required object <strong>will never</strong> be become available
     *
     * @throws RuntimeException  when some other problem occurred attempting
     *         to acquire the object.  When this exception is thrown a
     *         program may safely assume that the object <strong>will
     *         eventually</strong> become available, and thus re-issuing
     *         a call to {@link #get()}, after some reasonable delay, is ok.
     *         Throwing a {@link RuntimeException} is semantically equivalent
     *         to returning <code>null</code> from this method
     */
    public T get() throws ObjectNotAvailableException;


    /**
     * Obtains the {@link Class} of the {@link Deferred} reference. ie: the
     * {@link Class} that a call to {@link #get()}.getClass() should return
     * if/when the {@link Deferred} object becomes available.
     * <p>
     * This method is important as it allows applications to determine
     * an expected type of returned object, even when the actual object
     * may not, or ever become available.
     *
     * @return the {@link Class} of the {@link Deferred} object
     */
    public Class<T> getDeferredClass();
}
