package com.oracle.bedrock.runtime.virtual;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jk 2014.06.27
 */
public class HostAddressIterator implements Iterator<String>
{
    private int part1;

    private int part2;

    private int part3;

    private int part4;

    public HostAddressIterator(String startingAddress)
    {
        if (startingAddress == null)
        {
            throw new IllegalArgumentException("Address cannot be null");
        }

        Pattern pattern = Pattern.compile("^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                                          "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                                          "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                                          "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");

        Matcher matcher = pattern.matcher(startingAddress);
        if (!matcher.matches())
        {
            throw new IllegalArgumentException("Not a valid IP4 address (" + startingAddress + ")");
        }

        this.part1 = Integer.parseInt(matcher.group(1));
        this.part2 = Integer.parseInt(matcher.group(2));
        this.part3 = Integer.parseInt(matcher.group(3));
        this.part4 = Integer.parseInt(matcher.group(4));
    }

    @Override
    public synchronized boolean hasNext()
    {
        return part4 <= 255;
    }

    @Override
    public synchronized String next()
    {
        return String.format("%d.%d.%d.%d", part1, part2, part3, part4++);
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }


}
