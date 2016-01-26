/*
 * File: ExpressionEvaluator.java
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

package com.oracle.tools.lang;

import com.oracle.tools.Options;

import com.oracle.tools.options.Variable;
import com.oracle.tools.options.Variables;

import java.util.HashMap;

import java.util.regex.Pattern;

import javax.el.ELProcessor;
import javax.el.StandardELContext;

/**
 * Evaluates Java Expression Language expressions.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @see Variable
 * @see Variables
 */
public class ExpressionEvaluator
{
    /**
     * The {@link ELProcessor} to use for evaluating expressions.
     */
    private ELProcessor processor;


    /**
     * Constructs a default {@link ExpressionEvaluator} without any established
     * {@link Variable}s.
     */
    public ExpressionEvaluator()
    {
        this(new Variables());
    }


    /**
     * Constructs an {@link ExpressionEvaluator} based on the specified {@link Options}.
     *
     * @param options  the {@link Options}
     */
    public ExpressionEvaluator(Options options)
    {
        this(options.get(Variables.class));
    }


    /**
     * Constructs an {@link ExpressionEvaluator} with the specified {@link Variable}s.
     *
     * @param variables  the {@link Variable}s
     */
    public ExpressionEvaluator(Variable... variables)
    {
        this(new Variables(variables));
    }


    /**
     * Constructs an {@link ExpressionEvaluator} with the specified {@link Variable}s.
     *
     * @param variables  the {@link Variables}
     */
    public ExpressionEvaluator(Variables variables)
    {
        // create an ELProcessor that we'll use to perform the evaluation
        processor = new ELProcessor();

        // add all of the variables to the processor (to create a context)
        for (Variable variable : variables)
        {
            defineVariable(variable);
        }
    }


    /**
     * Defines a new {@link Variable} for the {@link ExpressionEvaluator}.
     *
     * @param name  the {@link Variable} to define
     * @param value the value for the {@link Variable}
     */
    public void defineVariable(String name,
                               Object value)
    {
        defineVariable(Variable.of(name, value));
    }


    /**
     * Defines a new {@link Variable} for the {@link ExpressionEvaluator}.
     *
     * @param variable  the {@link Variable} to define
     */
    public void defineVariable(Variable variable)
    {
        // when a variable name contains periods, we automatically create maps to represent the chain of variable names
        // as maps of maps
        if (variable.getName().contains("."))
        {
            String[]                partNames = variable.getName().split(Pattern.quote("."));

            HashMap<String, Object> lastMap   = null;

            for (int i = 0; i < partNames.length; i++)
            {
                String  partName       = partNames[i];
                boolean isLastPartName = i == partNames.length - 1;

                if (lastMap == null)
                {
                    StandardELContext context = processor.getELManager().getELContext();

                    Object            part    = context.getELResolver().getValue(context, null, partName);

                    if (part == null)
                    {
                        lastMap = new HashMap<>();

                        processor.defineBean(partName, lastMap);
                    }
                    else
                    {
                        lastMap = (HashMap) part;
                    }
                }
                else
                {
                    Object part = lastMap.get(partName);

                    if (part == null)
                    {
                        if (isLastPartName)
                        {
                            lastMap.put(partName, variable.getValue());
                        }
                        else
                        {
                            HashMap<String, Object> nextMap = new HashMap<>();

                            lastMap.put(partName, nextMap);

                            lastMap = nextMap;
                        }
                    }
                    else
                    {
                        lastMap = (HashMap) part;
                    }
                }
            }
        }
        else
        {
            processor.defineBean(variable.getName(), variable.getValue());
        }
    }


    /**
     * Evaluate the specified expression, resolving any Java Expression Language expressions where appropriate
     * using the {@link Variable}s defined by the {@link ExpressionEvaluator}, returning the result cast/coerced
     * into the specified type.
     *
     * @param expression  the expression to evaluate
     * @param asClass     the desired return type of expression
     *
     * @param <T>         the desired return type of expression
     *
     * @return  the evaluated expression
     */
    public <T> T evaluate(String   expression,
                          Class<T> asClass)
    {
        // there are three styles of usage for expressions.
        // i).   the expression is a simple value, in which case it is returned (as a String).
        //
        // ii).  the expression contains a ${expression} (and nothing else), in which case the expression is evaluated
        // and returned.
        //
        // iii). the expression contains zero or more ${expression}s (ie: a composite), in which case we have to
        // resolve each ${expression} and replace them in the string, after which we return the resulting string.

        Object result = null;

        // is the expression a composite string
        boolean isCompositeExpression = false;

        // we may need to build a string containing the result, before we can produce a result
        StringBuilder builder = new StringBuilder();

        for (int index = 0; index < expression.length(); index++)
        {
            if (expression.startsWith("\\$\\{", index))
            {
                builder.append("${");
                index++;
                isCompositeExpression = true;
            }
            else if (expression.startsWith("\\}", index))
            {
                builder.append("}");
                index++;
                isCompositeExpression = true;
            }
            else if (expression.startsWith("${", index))
            {
                String subExpression;

                int    indexEndSubExpression = expression.indexOf("}", index + 1);

                if (indexEndSubExpression > index + 2)
                {
                    subExpression = expression.substring(index + 2, indexEndSubExpression).trim();
                    index         = indexEndSubExpression;
                }
                else
                {
                    throw new IllegalArgumentException(String.format("Invalid expression definition in [%s].  "
                                                                     + "Missing closing brace '}'.",
                                                                     expression));
                }

                result = processor.eval(subExpression);

                // when there's more characters after the expression, assume we're composite
                if (index < expression.length() - 1)
                {
                    isCompositeExpression = true;
                }

                if (isCompositeExpression || result instanceof String)
                {
                    try
                    {
                        builder.append(result.toString());
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }
            else
            {
                builder.append(expression.charAt(index));
                isCompositeExpression = true;
            }
        }

        // resolve the composite expression (if necessary)
        result = isCompositeExpression ? builder.toString() : result;

        // coerce the result into the required type
        if (asClass.equals(String.class))
        {
            return (T) (result == null ? "null" : result.toString());
        }
        else
        {
            return asClass.cast(result);
        }
    }
}
