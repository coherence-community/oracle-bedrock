/*
 * File: SoapFaultException.java
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

package com.oracle.bedrock.runtime.remote.windows.winrm;

import com.microsoft.wsman.fault.ProviderFaultType;
import com.microsoft.wsman.fault.WSManFaultType;

import org.w3c.soap.envelope.Fault;
import org.w3c.soap.envelope.Faultreason;
import org.w3c.soap.envelope.Reasontext;

import javax.xml.bind.JAXBElement;
import java.util.List;

/**
 * An exception caused by a SOAP {@link Fault}.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class SoapFaultException extends Exception
{
    private Fault fault;


    /**
     * Create a {@link SoapFaultException} wrapping the specified
	 * {@link Fault}.
     *
     * @param fault the {@link Fault} causing the exception
     */
    public SoapFaultException(Fault fault)
    {
        this.fault = fault;
    }


    @Override
    public String getMessage()
    {
        StringBuilder sb = new StringBuilder();

        if (fault.isSetDetail() && fault.getDetail().isSetAny())
        {

            for (Object obj : fault.getDetail().getAny())
            {
                if (obj instanceof JAXBElement)
                {
                    obj = ((JAXBElement) obj).getValue();
                }

                if (obj instanceof WSManFaultType)
                {
                    sb.append(faultTypeToString((WSManFaultType) obj));
                }
                else if (obj != null)
                {
                    sb.append(obj.toString());
                }
            }
        }
        else
        {
            sb.append("Unknown SOAP Fault");
        }

        Faultreason reason = fault.getReason();
        if (reason != null)
        {
            List<Reasontext> textList = reason.getText();
            if (textList != null)
            {
                for (int i=0; i<textList.size(); i++)
                {
                    if (i == 0)
                    {
                        sb.append(' ');
                    }
                    else
                    {
                        sb.append('\n');
                    }

                    sb.append(textList.get(i).getValue());
                }
            }
        }

        return sb.toString();
    }


    protected String faultTypeToString(WSManFaultType type)
    {
        StringBuilder sb = new StringBuilder();

        if (type.isSetMessage() && type.getMessage().isSetContent())
        {
            boolean first = true;

            for (Object obj : type.getMessage().getContent())
            {
                if (obj instanceof JAXBElement)
                {
                    obj = ((JAXBElement) obj).getValue();
                }

                if (obj instanceof ProviderFaultType)
                {
                    ProviderFaultType pft = (ProviderFaultType) obj;

                    if (pft.isSetProviderId())
                    {
                        sb.append("Provider: ").append(pft.getProviderId());
                    }

                    if (pft.isSetContent())
                    {
                        for (Object content : pft.getContent())
                        {
                            if (content instanceof JAXBElement)
                            {
                                content = ((JAXBElement) content).getValue();
                            }

                            if (content instanceof WSManFaultType)
                            {
                                obj = faultTypeToString((WSManFaultType) content);
                            }
                        }
                    }
                }

                if (obj != null)
                {
                    if (!first)
                    {
                        sb.append(' ');
                    }
                    sb.append(obj.toString().trim());
                    first = false;
                }
            }
        }

        if (type.isSetCode())
        {
            String code = "Code: " + type.getCode();

            if (sb.length() == 0)
            {
                sb.append(code);
            }
            else if (sb.indexOf(code) == -1)
            {
                sb.append(" [").append(code).append("]");
            }
        }

        return sb.toString();
    }
}
