/*
 * File: Primes.java
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

import java.util.Arrays;

/**
 * A utility class for working with prime numbers between 1 and 1000;
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Primes
{
    /**
     * The first 1000 prime numbers.
     */
    private static final int[] PRIMES = new int[] {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61,
                                                   67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 127, 131, 137,
                                                   139, 149, 151, 157, 163, 167, 173, 179, 181, 191, 193, 197, 199, 211,
                                                   223, 227, 229, 233, 239, 241, 251, 257, 263, 269, 271, 277, 281, 283,
                                                   293, 307, 311, 313, 317, 331, 337, 347, 349, 353, 359, 367, 373, 379,
                                                   383, 389, 397, 401, 409, 419, 421, 431, 433, 439, 443, 449, 457, 461,
                                                   463, 467, 479, 487, 491, 499, 503, 509, 521, 523, 541, 547, 557, 563,
                                                   569, 571, 577, 587, 593, 599, 601, 607, 613, 617, 619, 631, 641, 643,
                                                   647, 653, 659, 661, 673, 677, 683, 691, 701, 709, 719, 727, 733, 739,
                                                   743, 751, 757, 761, 769, 773, 787, 797, 809, 811, 821, 823, 827, 829,
                                                   839, 853, 857, 859, 863, 877, 881, 883, 887, 907, 911, 919, 929, 937,
                                                   941, 947, 953, 967, 971, 977, 983, 991, 997};


    /**
     * Obtains the ith prime number, the first being 2.
     * <p>
     * When the specified index is less than 1, the value 1 will be returned.
     * When the specified index it out of the range of primes knowm by {@link Primes}, the
     * {@link #largestPrime()} prime will be returned.
     *
     * @param i  the prime number
     *
     * @return the ith prime number
     */
    public static int nthPrime(int i)
    {
        if (i <= 0)
        {
            return 1;
        }
        else if (i > PRIMES.length)
        {
            return largestPrime();
        }
        else
        {
            return PRIMES[i - 1];
        }

    }


    /**
     * Obtains the largest prime known to {@link Primes}.
     *
     * @return  the largest prime
     */
    public static int largestPrime()
    {
        return PRIMES[PRIMES.length - 1];
    }


    /**
     * Obtains the closest prime number to the specified value.
     * <p>
     * When the specified value is a prime number, the specified value is simply returned.
     * <p>
     * When the specified value is less than or equal to 1, the value 1 is returned.  When the specified value is out
     * of the range of prime numbers maintained by {@link Primes}, the {@link #largestPrime()} prime is returned.
     * <p>
     * When the specified value falls precisely between two prime numbers, the larger prime is returned.
     *
     * @param n  the value
     *
     * @return the closest prime number
     */
    public static int closestPrimeTo(int n)
    {
        if (n <= 1)
        {
            return 1;
        }
        else
        {
            // binary search for the prime number
            int i = Arrays.binarySearch(PRIMES, n);

            if (i < 0)
            {
                // when a prime wasn't found, we find the closest
                i = -i - 1;

                if (i >= PRIMES.length)
                {
                    // to big!
                    return largestPrime();
                }
                else
                {
                    // determine the closest
                    int nextPrime     = PRIMES[i];
                    int previousPrime = i > 0 ? PRIMES[i - 1] : 1;

                    return nextPrime - n > n - previousPrime ? previousPrime : nextPrime;
                }
            }
            else
            {
                // an actual prime was found!
                return PRIMES[i];
            }
        }
    }
}
