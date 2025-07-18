package com.oracle.bedrock.runtime.concurrent;

import com.oracle.bedrock.Option;

/**
 * A custom serializer to use for remote channels.
 */
public interface RemoteChannelSerializer
        extends Option
    {
    /**
     * Serialize a value.
     *
     * @param o  the value to serialize
     *
     * @return  the serialized value
     */
    byte[] serialize(Object o);

    /**
     * Deserialize a value.
     *
     * @param bytes  the serialized value
     * @param <T>    the expected type
     *
     * @return the deserialized value
     */
    <T> T deserialize(byte[] bytes);
    }
