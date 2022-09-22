open module com.oracle.bedrock.maven {
    exports com.oracle.bedrock.maven;

    requires transitive com.oracle.bedrock.runtime;

    requires java.logging;

    requires aether.api;
    requires aether.connector.basic;
    requires aether.impl;
    requires aether.spi;
    requires aether.transport.file;
    requires aether.transport.http;
    requires aether.util;

    requires maven.aether.provider;
    requires maven.settings;
    requires maven.settings.builder;
}